package caltrack.blub.com.caltrack.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.regex.Pattern;

import caltrack.blub.com.caltrack.Utilities.Constants;
import caltrack.blub.com.caltrack.PageAdapter.FragmentPageAdapter;
import caltrack.blub.com.caltrack.Utilities.CustomProgressDialog;
import caltrack.blub.com.caltrack.Utilities.FileUtils;
import caltrack.blub.com.caltrack.Utilities.HttpRequests;
import caltrack.blub.com.caltrack.R;

public class RegisterFragment extends Fragment implements View.OnClickListener
{
    private TextInputEditText username, password, confirmpass, email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        CardView registerButton;

        //init views
        username = view.findViewById(R.id.usernameReg);
        password = view.findViewById(R.id.passwordReg);
        confirmpass = view.findViewById(R.id.confPasswordReg);
        email = view.findViewById(R.id.email);
        registerButton = view.findViewById(R.id.registerBtn);
        registerButton.setOnClickListener(this);

        return view;
    }

    public String getUsername()
    {
        return username.getText().toString();
    }

    public String getPassword()
    {
        return password.getText().toString();
    }

    public String getEncryptedPassword()
    {
        return FileUtils.hashWith256(getPassword());
    }

    public String getPasswordConfirm()
    {
        return confirmpass.getText().toString();
    }

    public String getEmail()
    {
        return email.getText().toString();
    }

    public void clearFields()
    {
        username.setText("");
        email.setText("");
        password.setText("");
        confirmpass.setText("");
    }

    @Override
    public void onClick(View v)
    {

        //check text conditions
        //valid email format, no empty textfields, etc
        //check if password and confirm password are the same
        if (!fieldsFilled()) return;
        if (!isValidFormat(getEmail()))
        {
            Toast.makeText(getActivity(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getPassword().length() < 6)
        {
            Toast.makeText(getActivity(), "Password must at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!getPassword().equals(getPasswordConfirm()))
        {
            Toast.makeText(getActivity(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        //send request using open Connection
        //use buffered writer to provide parameters for server api
        if (!isOnline())
        {
            Toast.makeText(getActivity(), "No connection", Toast.LENGTH_SHORT).show();
        } else
        {
            new RegisterTask().execute();
        }
    }

    private boolean fieldsFilled()
    {
        boolean check = true;

        if (TextUtils.isEmpty(getUsername()))
        {
            username.setError("This field cannot be empty");
            getView().findViewById(R.id.usernameContainer).setAnimation(new ShakeAnimation().shakeError());
            check = false;
        }
        if (TextUtils.isEmpty(getEmail()))
        {
            email.setError("This field cannot be empty");
            getView().findViewById(R.id.emailContainer).setAnimation(new ShakeAnimation().shakeError());
            check = false;
        }
        if (TextUtils.isEmpty(getPassword()))
        {
            password.setError("This field cannot be empty");
            getView().findViewById(R.id.passwordContainer).setAnimation(new ShakeAnimation().shakeError());
            check = false;
        }
        if (TextUtils.isEmpty(getPasswordConfirm()))
        {
            confirmpass.setError("This field cannot be empty");
            getView().findViewById(R.id.confPasswordContainer).setAnimation(new ShakeAnimation().shakeError());
            check = false;
        }

        return check;
    }

    //Check if email is in a valid format
    public static boolean isValidFormat(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public boolean isOnline()
    {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private class RegisterTask extends AsyncTask<Void, Void, String>
    {
        ProgressDialog progDailog;

        @Override
        protected String doInBackground(Void... voids)
        {

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("username", getUsername())
                    .appendQueryParameter("email", getEmail())
                    .appendQueryParameter("password", getEncryptedPassword())
                    .appendQueryParameter("passwordconfirm", getPasswordConfirm());
            String query = builder.build().getEncodedQuery();

            return HttpRequests.sendPost(Constants.REGISTER, query);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progDailog = new CustomProgressDialog(getActivity());
        }

        //What happens after request is sent and response received
        @Override
        protected void onPostExecute(String result)
        {
            progDailog.dismiss();
            if (result.contains("Successful"))
            {
                //switch back to login screen
                FragmentPageAdapter.changeFragment(Constants.LOGIN_PAGE);
                clearFields();
            }
            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
        }
    }
}
