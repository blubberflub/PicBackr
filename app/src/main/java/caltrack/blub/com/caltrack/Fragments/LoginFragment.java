package caltrack.blub.com.caltrack.Fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import caltrack.blub.com.caltrack.Utilities.Constants;
import caltrack.blub.com.caltrack.DashboardActivity;
import caltrack.blub.com.caltrack.PageAdapter.FragmentPageAdapter;
import caltrack.blub.com.caltrack.Utilities.CustomProgressDialog;
import caltrack.blub.com.caltrack.Utilities.FileUtils;
import caltrack.blub.com.caltrack.Utilities.HttpRequests;
import caltrack.blub.com.caltrack.R;

public class LoginFragment extends Fragment implements View.OnClickListener
{

    private TextInputEditText editTextUser, editTextPass;
    private CardView loginBtn;
    private TextView registerBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        editTextUser = view.findViewById(R.id.usernameReg);
        editTextPass = view.findViewById(R.id.passwordReg);
        loginBtn = view.findViewById(R.id.loginbtn);
        registerBtn = view.findViewById(R.id.registerText);

        loginBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);

        final CheckBox checkBox = view.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    editTextPass.setTransformationMethod(null);
                    editTextPass.setSelection(editTextPass.length());
                } else
                {
                    editTextPass.setTransformationMethod(new PasswordTransformationMethod());
                    editTextPass.setSelection(editTextPass.length());
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    public String getUsername()
    {
        return editTextUser.getText().toString();
    }

    public String getPassword()
    {
        return editTextPass.getText().toString();
    }

    public String getEncryptedPassword()
    {
        return FileUtils.hashWith256(getPassword());
    }

    @Override
    public void onClick(View v)
    {
        if (v == loginBtn)
        {
            if (getUsername().equals("") || getPassword().equals(""))
            {
                Toast toast = Toast.makeText(getActivity(), "Please enter a username and password.", Toast.LENGTH_SHORT);
                toast.show();
            } else
            {
                new LoginTask().execute();
            }
        }

        if (v == registerBtn)
        {
            //switch to register fragment
            FragmentPageAdapter.changeFragment(Constants.REGISTER_PAGE);

            //clear fields
            editTextUser.setText("");
            editTextPass.setText("");
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, Integer>
    {
        CustomProgressDialog progressDialog;

        @Override
        protected Integer doInBackground(Void... voids)
        {

            return HttpRequests.loginRequest(getUsername(), getEncryptedPassword());
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(getActivity());
        }

        //What happens after request is sent and response received
        @Override
        protected void onPostExecute(Integer result)
        {
            progressDialog.dismiss();

            switch (result)
            {
                case 200:
                    //Change activities to dashboard
                    Intent myIntent = new Intent(getActivity(), DashboardActivity.class);
                    myIntent.putExtra("username", getUsername());
                    getActivity().startActivity(myIntent);
                    getActivity().finish();
                    break;
                case 401:
                    Toast.makeText(getActivity(), "Wrong username or password", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
