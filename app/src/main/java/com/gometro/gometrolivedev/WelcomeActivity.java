package com.gometro.gometrolivedev;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class WelcomeActivity extends AppCompatActivity
{

    //TAGS
    private final String TAG = "WelcomeActivity";
    private final String FRAG_TAG_WELCOME = "fragWelcome";
    private final String FRAG_TAG_LOGIN = "fargLogin";

    private FragmentManager fragMang;
    private FrameLayout fLayContent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Get ref to views
        fLayContent = (FrameLayout) findViewById(R.id.fLayWAContent);

        init();

    }

    private void init()
    {
        fragMang = getSupportFragmentManager();

        //Init initial fragment
        android.support.v4.app.FragmentTransaction fragTrans = fragMang.beginTransaction();
        fragTrans.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.abc_fade_out);
        fragTrans.add(R.id.fLayWAContent, new FragmentWelcome(), FRAG_TAG_WELCOME);
        fragTrans.commit();

    }

    public void displayLoginFrag()
    {
        FragmentTransaction fragTrans = fragMang.beginTransaction();
        fragTrans.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.abc_fade_out);
        fragTrans.replace(R.id.fLayWAContent, new FragmentLogin(), FRAG_TAG_LOGIN);
        fragTrans.addToBackStack(FRAG_TAG_LOGIN);
        fragTrans.commit();
    }

    public void displayMainMenu()
    {
        Intent intentMainMenu = new Intent(WelcomeActivity.this, MainActivity.class);
        //TODO: Send username with in intent extras
        intentMainMenu.putExtra("userName", "Test User");
        startActivity(intentMainMenu);

        finish();
    }
}
