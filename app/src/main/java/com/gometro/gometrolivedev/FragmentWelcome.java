package com.gometro.gometrolivedev;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by wprenison on 2015/09/03.
 */
public class FragmentWelcome extends Fragment implements View.OnClickListener
{
    private WelcomeActivity activity;
    private Button btnRegister;
    private Button btnLogin;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.frag_welcome, container, false);
        btnRegister = (Button) constructedView.findViewById(R.id.btnFWRegister);
        btnLogin = (Button) constructedView.findViewById(R.id.btnFWLogin);

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
        //init buttons
        btnRegister.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        //Animate enter
        //animEnter();
    }

    private void animEnter()
    {
        Animation animFadeIn = AnimationUtils.loadAnimation(activity, R.anim.abc_fade_in);
        animFadeIn.setDuration(2000);
        animFadeIn.setFillAfter(true);
        animFadeIn.setFillEnabled(true);

        btnRegister.startAnimation(animFadeIn);
        btnLogin.startAnimation(animFadeIn);

    }

    @Override
    public void onClick(View v)
    {
        if(v == btnRegister)
        {
            Toast.makeText(activity, "Register Screen", Toast.LENGTH_SHORT).show();

        }
        else if(v == btnLogin)
        {
            activity.displayLoginFrag();
        }
    }
}
