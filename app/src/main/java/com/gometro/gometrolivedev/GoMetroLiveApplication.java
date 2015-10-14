package com.gometro.gometrolivedev;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by wprenison on 2015/09/30.
 */
public class GoMetroLiveApplication extends Application
{
    private final String SERVER_SECRET = "85afe0c0b4a338aa0b0f1e5b803693a1a542c82e";
    private final String SERVER_ADDRESS = "http://192.168.1.106:9000";//"http://159.8.180.6:9001";  //"http://192.168.0.29:9000";
    private final int SERVER_TIME_OUT = 8000;
    private final String SERVER_API_UPLOAD = "/uploadUpstreamData";
    private final String SERVER_API_ADD_VEHICLE = "/addVehicle";
    private final String SERVER_API_GET_VEHICLES = "/getAvailableVehicles";
    private final String SERVER_API_GET_ROUTES = "/getAvailableRoutes";
    private final String SERVER_API_GET_TRIPS = "/getAvailableTrips";
    private final String SERVER_API_GET_SHAPE = "/getShape";
    private final String SERVER_API_GET_TRIP_TIMES = "/getTripTimes";
    private final String SERVER_API_GET_STOPS =  "/getStops";
    private final String SERVER_API_REGISTER_DRIVER = "/registerDriver";
    private final String SERVER_API_LOGIN_DRIVER = "/loginDriver";

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
        sharedPrefs.edit()
                .putString("SERVER_SECRET", SERVER_SECRET)
                .putString("SERVER_ADDRESS", SERVER_ADDRESS)
                .putString("SERVER_API_UPLOAD", SERVER_API_UPLOAD)
                .putString("SERVER_API_GET_VEHICLES", SERVER_API_GET_VEHICLES)
                .putString("SERVER_API_GET_ROUTES", SERVER_API_GET_ROUTES)
                .putString("SERVER_API_GET_TRIPS", SERVER_API_GET_TRIPS)
                .putString("SERVER_API_GET_SHAPE", SERVER_API_GET_SHAPE)
                .putInt("SERVER_TIME_OUT", SERVER_TIME_OUT)
                .putString("SERVER_API_ADD_VEHICLE", SERVER_API_ADD_VEHICLE)
                .putString("SERVER_API_GET_TRIP_TIMES", SERVER_API_GET_TRIP_TIMES)
                .putString("SERVER_API_GET_STOPS", SERVER_API_GET_STOPS)
                .putString("SERVER_API_REGISTER_DRIVER", SERVER_API_REGISTER_DRIVER)
                .putString("SERVER_API_LOGIN_DRIVER", SERVER_API_LOGIN_DRIVER).commit();
    }
}
