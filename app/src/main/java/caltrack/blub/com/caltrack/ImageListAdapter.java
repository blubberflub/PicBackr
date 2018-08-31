package caltrack.blub.com.caltrack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import caltrack.blub.com.caltrack.Utilities.Constants;
import caltrack.blub.com.caltrack.Utilities.CustomProgressDialog;
import caltrack.blub.com.caltrack.Utilities.GlideApp;
import caltrack.blub.com.caltrack.Utilities.HttpRequests;

//My adapter class, handles recycler view and images
public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder>
{
    private ArrayList<File> imageList;
    Context context;
    Activity activity;
    private boolean multiSelect = false;
    private ArrayList<File> selectedItems = new ArrayList<File>();
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            multiSelect = true;
            actionMode = mode;
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            //Delete item from cache
            for (File fileItem : selectedItems)
            {
                //delete item from database
                new DeleteTask().execute(fileItem.getName());

                fileItem.delete();
                imageList.remove(fileItem);
            }
            notifyDataSetChanged();
            mode.finish();

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            multiSelect = false;
            selectedItems.clear();
            notifyDataSetChanged();
        }
    };

    //constructor
    public ImageListAdapter(ArrayList<File> imageList, Context context, Activity activity)
    {
        this.imageList = imageList;
        this.context = context;
        this.activity = activity;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;

        public ViewHolder(View view)
        {
            super(view);
            imageView = view.findViewById(R.id.listImg);
        }
    }

    @NonNull
    @Override
    public ImageListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageListAdapter.ViewHolder holder, int position)
    {
        final File file = imageList.get(position);
        GlideApp.with(context).asBitmap().load(file).centerCrop().into(holder.imageView);

        //longclicking an item turns on multiselect mode
        if (multiSelect)
        {
            if (!selectedItems.contains(file))
                holder.imageView.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);

            holder.imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (selectedItems.contains(file))
                    {
                        holder.imageView.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);
                        selectedItems.remove(file);
                        actionMode.setTitle(selectedItems.size() + " Selected");
                    } else
                    {
                        holder.imageView.clearColorFilter();
                        selectedItems.add(file);
                        actionMode.setTitle(selectedItems.size() + " Selected");
                    }
                }
            });

            holder.imageView.setOnLongClickListener(null);
        } else
        {
            holder.imageView.clearColorFilter();

            //Click an image to open it full screen
            holder.imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //open new activity with image view
                    Intent intent = new Intent(context, FullscreenActivity.class);
                    intent.putExtra("picture", file);
                    activity.startActivity(intent);
                }
            });

            holder.imageView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    ((AppCompatActivity) view.getContext()).startSupportActionMode(actionModeCallbacks);
                    selectedItems.add(file);
                    actionMode.setTitle(selectedItems.size() + " Selected");
                    notifyDataSetChanged();
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return imageList.size();
    }

    public class DeleteTask extends AsyncTask<String, Integer, String>
    {
        CustomProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            progressDialog = new CustomProgressDialog(context);
            progressDialog.setMessage("Deleting image(s)...");
        }

        @Override
        protected String doInBackground(String... strings)
        {
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("username", DashboardActivity.currentUsername)
                    .appendQueryParameter("filename", strings[0]);
            String query = builder.build().getEncodedQuery();
            String response = HttpRequests.sendPost(Constants.DELETE_IMAGE, query);
            return response;
        }

        @Override
        protected void onPostExecute(String result)
        {
            progressDialog.dismiss();
        }
    }
}
