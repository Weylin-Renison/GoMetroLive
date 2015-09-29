package com.gometro.gometrolivedev;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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
            //TODO: check login credentials

            activity.displayMainMenu();
        }
    }
}
