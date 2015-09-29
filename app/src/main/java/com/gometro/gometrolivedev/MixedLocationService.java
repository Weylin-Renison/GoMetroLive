package com.gometro.gometrolivedev;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by wprenison on 2015/09/08.
 */
public class MixedLocationService extends Service implements MixedLocationInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener
{
    //Const
    private final String TAG = "MixedLocService";
    private int LOCATION_DISTANCE_INTERVAL = 0;   //Meters
    private int LOCATION_TIME_INTERVAL = 5000; //Milli Sec
    private int LOCATION_FASTEST_TIME_INTERVAL = 2000; //Milli Sec
    private int LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private SERVICE_PROVIDER LOCATION_PROVIDER = SERVICE_PROVIDER.MIXED; //enum check interface
    private String GPS_SWITCH_ON_MSG = "Please enable your gps to make use of this feature";

    //Main Globals
    private Context context;
    private DriverAidActivity activity; //Change this class type to the one using the service

    //Google Location Api
    private GoogleApiClient googApiClient;
    private boolean resolvingConError = false;
    private static final int REQ_CODE_RESOLVE_GOOG_API_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";
    private Location lastKnownLoc;
    private boolean locUpdatesRequestedButFailed = false;    //true if location updates have been requested but google api has not been connected yet,
                                                            // will cause location updates to be request as soon as api is connected

    private final IBinder binder = new MixedLocServBinder();

    public class MixedLocServBinder extends Binder
    {
        MixedLocationService getService()
        {
            return MixedLocationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        Log.i(TAG, "onBind: Service Bound");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.i(TAG, "onUnbind: Service Unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void initService(Context context)
    {
        this.context = context;
        this.activity = (DriverAidActivity) context;

        initLocationProvider();
    }

    @Override
    public void initService(Context context, SERVICE_PROVIDER locationProvider, int timeInterval, int distanceInterval)
    {
        this.context = context;
        this.activity = (DriverAidActivity) context;
        LOCATION_PROVIDER = locationProvider;
        LOCATION_TIME_INTERVAL = timeInterval;
        LOCATION_DISTANCE_INTERVAL = distanceInterval;

        initLocationProvider();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        //TODO: allow init vars to be passed using bundle extra as well
    }

    private void initLocationProvider()
    {
        switch (LOCATION_PROVIDER)
        {

            case MIXED: //Init mixed location providers ect
                //init google play location services
                GoogleApiClient.Builder googApiBuilder = new GoogleApiClient.Builder(this);
                googApiBuilder.addApi(LocationServices.API);
                googApiBuilder.addConnectionCallbacks(this);
                googApiBuilder.addOnConnectionFailedListener(this);

                googApiClient = googApiBuilder.build();
                googApiClient.connect();
                break;

            case GPS:   //Init gps location providers ect
                break;

            case NETWORK:   //Init network location providers ect
                break;

            case WIFI:  //Init wifi location providers ect
                break;

            case MIXED_ALL: //Init All location providers ect
                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        Log.i(TAG, "onConnected:");
        //Do location stuffs
        lastKnownLoc = LocationServices.FusedLocationApi.getLastLocation(googApiClient);
        activity.onLocationUpdate(lastKnownLoc);

        if(locUpdatesRequestedButFailed)
        {
            //Re request location updates
            LocationRequest locRequest = new LocationRequest();
            locRequest.setInterval(LOCATION_TIME_INTERVAL);
            locRequest.setFastestInterval(LOCATION_FASTEST_TIME_INTERVAL);
            locRequest.setPriority(LOCATION_PRIORITY);
            locRequest.setSmallestDisplacement(LOCATION_DISTANCE_INTERVAL);

            LocationServices.FusedLocationApi.requestLocationUpdates(googApiClient, locRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.i(TAG, "onConnectionSuspended:");
        Toast.makeText(context, "Awh your connection to location services have been suspended here is an int to make sense off: " + i, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.i(TAG, "onConnectionFailed: " + connectionResult.getErrorCode());
        if(resolvingConError)
        {
            //Already busy resolving an error
            return;
        }
        else if(connectionResult.hasResolution())
        {
            try
            {
                resolvingConError = true;
                connectionResult.startResolutionForResult(activity, REQ_CODE_RESOLVE_GOOG_API_ERROR);
            }
            catch(IntentSender.SendIntentException sie)
            {
                sie.printStackTrace();
                Log.e(TAG, "onConnectionFailed: "  + sie.getMessage());
            }
        }
        else
        {

            //Use show dialog for api error
            showLocationApiErrorDialog(connectionResult.getErrorCode());
            resolvingConError = true;
        }
    }

    @Override
    public void setServiceProvider(SERVICE_PROVIDER serviceProvider)
    {
        LOCATION_PROVIDER = serviceProvider;
    }

    @Override
    public void setTimeInterval(int timeInterval)
    {
        LOCATION_TIME_INTERVAL = timeInterval;
    }

    @Override
    public void setFastestTimeInterval(int timeInterval)
    {
        LOCATION_FASTEST_TIME_INTERVAL = timeInterval;
    }

    @Override
    public void setDistanceInterval(int distanceInterval)
    {
        LOCATION_DISTANCE_INTERVAL = distanceInterval;
    }

    @Override
    public void setLocationPriority(int locationPriority)
    {
        LOCATION_PRIORITY = locationPriority;
    }

    @Override
    public void setGpsSwitchOnMsg(String msg)
    {

    }

    @Override
    public boolean requestLocationUpdates()
    {
        Log.i(TAG, "requestLocationUpdates:");
        if(googApiClient.isConnected())
        {
            LocationRequest locRequest = new LocationRequest();
            locRequest.setInterval(LOCATION_TIME_INTERVAL);
            locRequest.setFastestInterval(LOCATION_FASTEST_TIME_INTERVAL);
            locRequest.setPriority(LOCATION_PRIORITY);
            locRequest.setSmallestDisplacement(LOCATION_DISTANCE_INTERVAL);

            LocationServices.FusedLocationApi.requestLocationUpdates(googApiClient, locRequest, this);
        }
        else
            locUpdatesRequestedButFailed = true;

        return true;
    }

    public void killService()
    {
        this.stopSelf();
    }

    //Location listener methods
    @Override
    public void onLocationChanged(Location location)
    {
        Log.i(TAG, "onLocationChanged:");
        //Send location back to bounded activity
        activity.onLocationUpdate(location); //TODO: re-enable
    }

    @Override
    public void stopLocationUpdates()
    {
        Log.i(TAG, "stopLocationUpdates:");
       if(googApiClient.isConnected())
           LocationServices.FusedLocationApi.removeLocationUpdates(googApiClient, this);
    }

    private void showLocationApiErrorDialog(int errorCode)
    {
        Log.i(TAG, "showLocationApiErrorDialog: " + errorCode);
        //Create frag for dialog
        ErrorDialogFragment fragDialog = new ErrorDialogFragment();

        //Compile bundle info like error that should be displayed
        Bundle bundleArguments = new Bundle();
        bundleArguments.putInt(DIALOG_ERROR, errorCode);

        fragDialog.setArguments(bundleArguments);
        fragDialog.show(activity.getSupportFragmentManager(), "ErrorDialog");
    }

    public void onDialogResult(int requestCode, int resultCode, Intent data)
    {
        Log.i(TAG, "onActivityResult: ");
        if(requestCode == REQ_CODE_RESOLVE_GOOG_API_ERROR)
        {
            resolvingConError = false;

            if(resultCode == Activity.RESULT_OK)
            {
                //Ensure the app is not already connected or busy connecting for some reason
                if(!googApiClient.isConnected() && !googApiClient.isConnecting())
                    googApiClient.connect();    //Try reconnect
            }
        }
    }

    @Override
    public void onDialogDismissed()
    {
        Log.i(TAG, "onDialogDismissed: ");
        resolvingConError = false;
    }

    public static class ErrorDialogFragment extends DialogFragment
    {
        private final String TAG_DIALOG = "ErrorDialogFragment:";

        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            Log.i(TAG_DIALOG, "onCreateDialog: ");
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(this.getActivity(), errorCode, REQ_CODE_RESOLVE_GOOG_API_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog)
        {
            Log.i(TAG_DIALOG, "onDismiss: ");
            ((DriverAidActivity) getActivity()).onDialogDismissed();
        }
    }
}
