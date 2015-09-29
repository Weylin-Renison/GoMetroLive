package com.gometro.gometrolivedev;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //Const
    private final String FRAG_TAG_ACTION_BAR = "fragActionBar";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initActionBar();
    }

    private void initActionBar()
    {
        FragmentManager fragMang = getSupportFragmentManager();
        FragmentTransaction fragTrans = fragMang.beginTransaction();
        fragTrans.add(R.id.fLayMAActionBar, new FragmentActionBar(), FRAG_TAG_ACTION_BAR);
        fragTrans.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onClickNewRoute(View view)
    {
        Intent driverAidScreenIntent = new Intent(MainActivity.this, DriverAidActivity.class);
        startActivity(driverAidScreenIntent);
    }

    public void onClickRouteHistory(View view)
    {

    }

    public void onClickMyScore(View view)
    {

    }

    public void onClickMyTraining(View view)
    {
        Intent intentMyTraining = new Intent(MainActivity.this, MyTrainingActivity.class);
        startActivity(intentMyTraining);
    }

    public void onClickMyProfile(View view)
    {
        Intent intentMyProfile = new Intent(MainActivity.this, MyProfileActivity.class);
        startActivity(intentMyProfile);
    }

    public void onClickLogout(View view)
    {
        Intent intentLogout = new Intent(MainActivity.this, WelcomeActivity.class);
        startActivity(intentLogout);
        finish();
    }

}
