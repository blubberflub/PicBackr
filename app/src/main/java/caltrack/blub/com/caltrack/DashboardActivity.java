package caltrack.blub.com.caltrack;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.apache.commons.collections4.ListUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import caltrack.blub.com.caltrack.PageAdapter.FragmentPageAdapter;
import caltrack.blub.com.caltrack.Utilities.Constants;
import caltrack.blub.com.caltrack.Utilities.CustomProgressDialog;
import caltrack.blub.com.caltrack.Utilities.FileUtils;
import caltrack.blub.com.caltrack.Utilities.HttpRequests;
import caltrack.blub.com.caltrack.Utilities.MultipartUtility;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

import static caltrack.blub.com.caltrack.Utilities.Constants.CAMERA_PIC_REQUEST;

public class DashboardActivity extends AppCompatActivity
{
    final static int LOAD_IMAGE = 0;
    final static int UPLOAD_IMAGE = 1;
    private static final int IMAGE_LIST_INFO = 2;
    File userCacheDir;
    ArrayList<File> imageList;
    ArrayList<String> cachedFileNames, dbFileNames;
    JSONArray dbImageArray;
    RecyclerView.Adapter photoAdapter;
    static String currentUsername, orientation, filepath, fileName, byteArrayString;
    Context context;
    Activity activity;
    byte[] byteArray;
    Uri capturedImageUri = null;
    File file;
    File photoFile = null;
    int imageCount;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //initialize everything
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //grab username from intent bundle (login)
        Bundle extras = getIntent().getExtras();
        assert extras != null;

        //get user's username
        currentUsername = extras.getString("username");

        //enable this if testing just dashboard activity
//        currentUsername = "test";
        context = this;
        activity = this;

        //setup actionbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FabSpeedDial fab = findViewById(R.id.speedDial);

        //Sets click action for FAB
        fab.setMenuListener(new SimpleMenuListenerAdapter()
        {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem)
            {
                if (menuItem.getItemId() == R.id.action_upload)
                {
                    checkPermission(Constants.FILE_CHOOSER);
                } else if (menuItem.getItemId() == R.id.action_camera)
                {
                    //add camera feature
                    checkPermission(Constants.CAMERA_PIC_REQUEST);
                }
                return false;
            }
        });

        //initialize list of images for recycler view
        imageList = new ArrayList<>();
        cachedFileNames = new ArrayList<>();
        dbFileNames = new ArrayList<>();

        //create cache folder if first time
        userCacheDir = new File(getCacheDir() + "/" + currentUsername);
        userCacheDir.mkdir();

        //get cached images if available
        populateCachedImages(userCacheDir);

        //call image list api for needed information
        new HttpTasks((IMAGE_LIST_INFO)).execute();

        //Recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        photoAdapter = new ImageListAdapter(imageList, this, this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(photoAdapter);
    }

    //Grab all the cached images from cache directory
    public void populateCachedImages(File dir)
    {
        if (dir.exists())
        {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                File file = files[i];
                if (!file.isDirectory())
                {
                    //Record all filenames from the cache so we know which photos to retrieve from database
                    cachedFileNames.add(file.getName());
                    imageList.add(file);
                }
            }
        }
    }

    //Check if permissions are granted to access file manager
    private void checkPermission(int requestCode)
    {
        int permissionCheck = 0;

        if (requestCode == Constants.FILE_CHOOSER)
        {
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.FILE_CHOOSER);
            } else
            {
                pickImage();
            }
        } else
        {
            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.CAMERA}, CAMERA_PIC_REQUEST);
            } else
            {
                openCamera();
            }
        }
    }

    private File createImageFile() throws IOException
    {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyymmddHHmmss").format(new Date());
        String imageFileName = timeStamp;
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                userCacheDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case Constants.FILE_CHOOSER:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    pickImage();
                }
                break;
            case CAMERA_PIC_REQUEST:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    openCamera();
                }
                break;
            default:
                break;
        }
    }

    //open file chooser
    public void pickImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, Constants.FILE_CHOOSER);
    }

    //open camera
    public void openCamera()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            // Create the File where the photo should go
            try
            {
                photoFile = createImageFile();

                // Continue only if the File was successfully created
                if (photoFile != null)
                {
                    capturedImageUri = FileProvider.getUriForFile(context, "com.example.android.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                    startActivityForResult(takePictureIntent, Constants.CAMERA_PIC_REQUEST);
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    //after image is chosen
    //data = the image file
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        if (requestCode == Constants.CAMERA_PIC_REQUEST && resultCode == RESULT_OK)
        {
            try
            {
                //setup variables to send to server
                //bytearraystring, filename, orientation
                file = photoFile;
                ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
                orientation = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);

                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), capturedImageUri);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byteArray = byteArrayOutputStream.toByteArray();
                byteArrayString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                fileName = file.getName() + ".jpg";

                //upload data
                new HttpTasks(UPLOAD_IMAGE).execute();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if (requestCode == Constants.FILE_CHOOSER && resultCode == RESULT_OK)
        {
            try
            {
                //get exif orientation from original file
                ExifInterface exifInterface = new ExifInterface(FileUtils.getRealPathFromURI(this, data.getData()));
                orientation = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);

                //filename needed to upload
                filepath = data.getData().getPath();
                File actualFile = new File(filepath);
                fileName = actualFile.getName() + ".jpg";

                //create cached file container
                file = new File(userCacheDir + "/" + fileName);
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);

//                upload byte array to online db
                //convert bitmap to a byte array
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byteArray = byteArrayOutputStream.toByteArray();
                byteArrayString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                //write bytearray to file container
                out.write(byteArray);
                ExifInterface newFileExif = new ExifInterface(file.getAbsolutePath());
                newFileExif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
                newFileExif.saveAttributes();
                byteArrayOutputStream.close();

                out.close();
                //upload data
                new HttpTasks(UPLOAD_IMAGE).execute();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logoutBtn)
        {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {

                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            Intent myIntent = new Intent(activity, FragmentPageAdapter.class);
                            activity.startActivity(myIntent);
                            activity.finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public class HttpTasks extends AsyncTask<String, Integer, String>
    {
        CustomProgressDialog progressDialog;
        private int id, maxProgress;
        String response;

        private HttpTasks(int id)
        {
            this.id = id;
        }

        @Override
        protected String doInBackground(String... strings)
        {
            switch (id)
            {
                case IMAGE_LIST_INFO:
                {
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("username", currentUsername);
                    String query = builder.build().getEncodedQuery();

                    String response = HttpRequests.sendGet(Constants.IMAGE_COUNT + "?" + query);

                    try
                    {
                        JSONObject object = new JSONObject(response);

                        imageCount = object.getInt("imagecount");
                        dbImageArray = object.getJSONArray("filenames");

                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                    break;
                }
                case LOAD_IMAGE:
                {
                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("username", currentUsername)
                            .appendQueryParameter("filename", strings[0]);
                    String query = builder.build().getEncodedQuery();
                    String image = HttpRequests.sendGet(Constants.GET_IMAGES + "?" + query);
                    String[] split = image.split("xxxxxxx");
                    String imageData = split[0];
                    String dbOrientation = split[1];
                    try
                    {
                        file = new File(userCacheDir + "/" + strings[0]);
                        file.createNewFile();
                        FileOutputStream out = new FileOutputStream(file);
                        byteArray = Base64.decode(imageData, Base64.DEFAULT);
                        out.write(byteArray);
                        ExifInterface newFileExif = new ExifInterface(file.getAbsolutePath());
                        newFileExif.setAttribute(ExifInterface.TAG_ORIENTATION, dbOrientation);
                        newFileExif.saveAttributes();

                        imageList.add(file);

                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    break;
                }
                case UPLOAD_IMAGE:
                {
                    //multipartUtility
                    try
                    {
                        MultipartUtility multipart = new MultipartUtility(Constants.IMAGE_POST, "UTF-8", "XXXXXX");
                        multipart.addFormField("filename", fileName);
                        multipart.addFormField("username", currentUsername);
                        multipart.addFormField("orientation", orientation);

                        multipart.addFormField("base64bin", byteArrayString);

                        response = multipart.execute();

                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            if (progressDialog == null) progressDialog = new CustomProgressDialog(context);

            switch (id)
            {
                case LOAD_IMAGE:
                {
                    progressDialog.setMessage("Loading images...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    break;
                }
                case UPLOAD_IMAGE:
                {
                    progressDialog.setMessage("Uploading image...");
                    break;
                }
            }

        }

        //What happens after request is sent and response received
        @Override
        protected void onPostExecute(String result)
        {
            progressDialog.dismiss();
            switch (id)
            {
                case IMAGE_LIST_INFO:
                {
                    //get the amount of images on users database
                    //store it in dbImageArray
                    for (int i = 0; i < dbImageArray.length(); i++)
                    {
                        try
                        {
                            dbFileNames.add(dbImageArray.getString(i));
                        } catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    //create a list of remainingImgs that stores only the photos
                    //the user does NOT have in cache
                    List<String> remainingImgs = ListUtils.subtract(dbFileNames, cachedFileNames);

                    //download remaining images from server
                    for (int i = 0; i < remainingImgs.size(); i++)
                    {
                        new HttpTasks(LOAD_IMAGE).execute(remainingImgs.get(i));
                    }
                    break;
                }
                case LOAD_IMAGE:
                {
                    photoAdapter.notifyDataSetChanged();
                    break;
                }
                case UPLOAD_IMAGE:
                {
                    if (response != null)
                    {
                        Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                    } else
                    {
                        imageList.add(file);
                        photoAdapter.notifyDataSetChanged();
                    }
                    break;
                }
            }
        }
    }
}
