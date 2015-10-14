package com.gometro.gometrolivedev;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wprenison on 2015/09/11.
 */
public class FragmentLogin extends Fragment implements View.OnClickListener
{

    //Const
    private final String TAG = "fragLogin";

    //Views
    private EditText etxtUsername;
    private EditText etxtPassword;
    private Button btnLogin;

    //Vars
    private WelcomeActivity activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.frag_login, container, false);

        //Get handle on views
        etxtUsername = (EditText) constructedView.findViewById(R.id.etxtFLUsername);
        etxtPassword = (EditText) constructedView.findViewById(R.id.etxtFLPassword);
        btnLogin = (Button) constructedView.findViewById(R.id.btnFLLogin);

        return constructedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        activity = (WelcomeActivity) getActivity();

        init();
    }

    private void init()
    {
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if(v == btnLogin)
        {
            //Check credentials
            String username = etxtUsername.getText().toString();
            String password = etxtPassword.getText().toString();

            //Validation
            boolean valid = true;

            if(username.isEmpty())
            {
                etxtUsername.setError("Required field");
                valid = false;
            }


            if(password.isEmpty())
            {
                etxtPassword.setError("Required field");
                valid = false;
            }


            if(valid)
            {
                ServerApiHelper apiHelp = new ServerApiHelper(activity);
                apiHelp.loginDriver(username, password, new OnJsonResponseListner()
                {
                    @Override
                    public void onJsonResponse(boolean successful, JSONArray jsonArrayResults)
                    {

                    }

                    @Override
                    public void onJsonResponse(boolean successful, JSONObject jsonResult)
                    {
                        if(jsonResult != null)
                        {
                            try
                            {
                                int status = jsonResult.getInt("status");

                                if(status == 200)
                                {
                                    activity.displayMainMenu();

                                    //Store driver & agency id for later use
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                                    prefs.edit()
                                            .putInt("driverId", jsonResult.getInt("driverId"))
                                            .putString("agencyId", jsonResult.getString("agencyId")).commit();

                                }
                                else    //Display error msg
                                    Toast.makeText(activity, jsonResult.getString("msg"), Toast.LENGTH_LONG).show();

                            }
                            catch (JSONException je)
                            {
                                je.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }
}
