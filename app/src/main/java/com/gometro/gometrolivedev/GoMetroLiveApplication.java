package com.gometro.gometrolivedev;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by wprenison on 2015/09/30.
 */
public class GoMetroLiveApplication extends Application
{
    private final String SERVER_ADDRESS = "http://159.8.180.6:9000";
    private final String SERVER_API_UPLOAD = "/uploadUpstreamData";

    @Override
    public void onCreate()
    {
        super.onCreate();

        //Init any global settings ect here
        setupServerSettings();
    }

    private void setupServerSettings()
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.edit().putString("SERVER_ADDRESS", SERVER_ADDRESS).commit();
        sharedPrefs.edit().putString("SERVER_API_UPLOAD", SERVER_API_UPLOAD).commit();
    }
}
