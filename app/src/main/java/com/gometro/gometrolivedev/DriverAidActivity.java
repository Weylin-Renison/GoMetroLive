package com.gometro.gometrolivedev;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.gometro.gometrolive.StreamPacketProtos;
import com.gometro.gometrolive.StreamPacketProtos.StreamPacket.StreamData;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;

import java.io.ByteArrayInputStream;
import java.util.List;

public class DriverAidActivity extends AppCompatActivity implements MixedLocationUserClassInterface {

    //Constants
    private final String TAG = "DiverAidActivity";
    private static final int REQ_CODE_RESOLVE_GOOG_API_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";

    //Location Service Vars
    private Context context = this;
    private MixedLocationService locService;
    private boolean isBound = false;

    //Vars
    //TODO: Route data var here
    private List<Marker> stopsMarkerList;   //List of stop markers user has to stop at
    private Marker userLocMarker;   //User's location
    private boolean follow = true; //Used to det if user should be followed on the map or not
    private boolean liveStream = false; //Used to check if stream should be broadcast to companion apps

    //Fragment  vars
    private FragmentManager fragMang;
    private final String FRAG_TAG_TRIP_STATUS_BAR = "fragTripStatusBar";
    private final String FRAG_TAG_ACTION_BAR = "fragActionBar";
    private final String FRAG_TAG_START_TRIP = "fragStartTrip";

    private FragmentTripStatusBar fragTripStatusBar;

    //Views vars
    private FrameLayout fLayBtnDarwerContent;
    private FrameLayout fLayMainContent;
    private MapView mapView;
    private ImageButton btnLiveStream;

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.i(TAG, "onServiceConnected:");

            //Get ref to service from binder here
            MixedLocationService.MixedLocServBinder binder = (MixedLocationService.MixedLocServBinder) service;
            locService = binder.getService();
            isBound = true;

            locService.initService(context);
            locService.requestLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.i(TAG, "onServiceDisconnected:");
            locService.stopLocationUpdates();
            isBound = false;
        }
    };

    public void onDialogDismissed()
    {
        Log.i(TAG, "onDialogDismissed: ");

        //forward to service
        locService.onDialogDismissed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_aid);

        Log.i(TAG, "onCreate: ");

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Get ref to views
        mapView = (MapView) findViewById(R.id.mvDAMap);
        fLayMainContent = (FrameLayout) findViewById(R.id.fLayDAMainContent);
        fLayBtnDarwerContent = (FrameLayout) findViewById(R.id.fLayDAButtonDrawerContent);
        btnLiveStream = (ImageButton) findViewById(R.id.ibtnDAStreamLive);

        //Get ref to frag manager
        fragMang = getSupportFragmentManager();

        initMap();
        initActionBar();
        initStartTrip();
    }

    private void initMap()
    {
        Log.i(TAG, "init: ");
        //init map
//        mapView.setTileSource(new MapboxTileLayer("wprenison.30d139e5"));
        mapView.setCenter(new ILatLng()
        {
            @Override
            public double getLatitude()
            {
                return -33.9164;
            }

            @Override
            public double getLongitude()
            {
                return 18.4233;
            }

            @Override
            public double getAltitude()
            {
                return 10;
            }
        });
        mapView.setZoom(17);
    }


    private void initActionBar()
    {
        //Add own custom action bar

        FragmentTransaction fragTrans = fragMang.beginTransaction();
        fragTrans.add(R.id.fLayDAActionBar, new FragmentActionBar(), FRAG_TAG_ACTION_BAR);
        fragTrans.commit();
    }

    private void initStartTrip()
    {
        FragmentTransaction fragTrans = fragMang.beginTransaction();
        fragTrans.setCustomAnimations(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom, R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_bottom);
        fragTrans.add(R.id.fLayDAMainContent, new FragmentStartTrip(), FRAG_TAG_START_TRIP);
        fragTrans.commit();
    }

    private void initTripStatusBar()
    {
        //Add trip status bar fragment with animation

        //Prepare animation
        /*Animation animSlideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        animSlideIn.setDuration(2000);*/

        fLayBtnDarwerContent.setVisibility(View.VISIBLE);

        fragTripStatusBar = new FragmentTripStatusBar();
        FragmentTransaction fragTrans = fragMang.beginTransaction();
        fragTrans.setCustomAnimations(R.anim.slide_in_right, android.R.anim.slide_out_right);
        fragTrans.add(R.id.fLayDAButtonDrawerContent, fragTripStatusBar , FRAG_TAG_TRIP_STATUS_BAR);
        fragTrans.commit();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.i(TAG, "onStart: ");

        //Bind to service
        Intent bindIntent = new Intent(DriverAidActivity.this, MixedLocationService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Forward to service
        locService.onDialogResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(TAG, "onPause: ");
        //Unbind and stop service
        if(isBound)
        {
            locService.stopLocationUpdates();
            unbindService(serviceConnection);
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Log.i(TAG, "onStop: ");
    }

    public void onClickLiveStream(View view)
    {
        //Swap status
        liveStream = !liveStream;

        //Swap button images
        if(liveStream)
            btnLiveStream.setImageDrawable(getResources().getDrawable(R.drawable.btn_live_stream_active));
        else
            btnLiveStream.setImageDrawable(getResources().getDrawable(R.drawable.selector_btn_live_stream));
    }

    @Override
    public void onLocationUpdate(Location newLocation)
    {
        Log.i(TAG, "onLocationUpdate: ");

        if(newLocation != null)
        {
            //TODO: do what you would like to do with location update here
            /*Toast.makeText(this, "New Location: " + newLocation.getLatitude() + ", " + newLocation.getLongitude() + " Provider: " + newLocation.getProvider()
                           + " Bearing: " + newLocation.getBearing() + " Accuracy: " + newLocation.getAccuracy() + " Altitidue: " + newLocation.getAltitude()
                    + " Speed: " + newLocation.getSpeed(), Toast.LENGTH_LONG).show();*/

            addUserMarker(newLocation);

            //TODO: Move camera to new location if follow is on
            if(follow)
                animateCamera(newLocation);

            //TODO: package update
            StreamData upstreamDataPacket = constructUpstreamPacket(newLocation);

            //TODO: upload stream data to server
            uploadUpstreamData(upstreamDataPacket);
        }
        else
            Toast.makeText(this, "Attempting to locate you", Toast.LENGTH_LONG).show();
    }

    //Writes stream data to byte array and uploads to server
    private void uploadUpstreamData(StreamData upstreamDataPacket)
    {
        if(upstreamDataPacket != null && upstreamDataPacket.isInitialized())
        {
            //Write data to byte array for upload
            Log.i(TAG, "uploadUpstreamData: upstream data = " + upstreamDataPacket.toString());
            byte[] upstreamBytes = upstreamDataPacket.toByteArray();
            ByteArrayInputStream upstreamIS = new ByteArrayInputStream(upstreamBytes);

            //Upload data to server
            if (upstreamBytes != null)
            {
                //Send for upload to server
                RequestParams params = new RequestParams();
                //params.put("imei", "001Abc");
                params.put("upstreamByteData", upstreamIS);

                //Get url
                SharedPreferences prefMang = PreferenceManager.getDefaultSharedPreferences(this);
                String SERVER_ADDRESS = prefMang.getString("SERVER_ADDRESS", null);
                String SERVER_API_UPLOAD = prefMang.getString("SERVER_API_UPLOAD", null);
                Log.i(TAG, "URL: " + SERVER_ADDRESS + SERVER_API_UPLOAD);

                AsyncHttpClient client = new AsyncHttpClient();
                client.setTimeout(240 * 1000);
                client.setUserAgent("android");
                client.post("http://192.168.8.102:9000" + SERVER_API_UPLOAD, params,		//192.168.0.29 home URL_BASE + API_UPLOAD for server (54.68.55.70)
                            new AsyncHttpResponseHandler()
                            {
                                @Override
                                public void onSuccess(String response)
                                {
                                    Log.i(TAG, "uploadUpstreamData: Packet successfully sent");
                                }

                                public void onFailure(Throwable error, String content)
                                {
                                    Log.e(TAG, "uploadUpstreamData: upload failed :(  " + error + " " + content);
                                }
                            });
            }
        }
    }

    //Constructs upstream data packet this includes location data and status data
    private StreamData constructUpstreamPacket(Location location)
    {
        //Create proto builders
        StreamData.Builder upstreamBuilder = StreamData.newBuilder();
        StreamData.LocData.Builder locDataBuilder = StreamData.LocData.newBuilder();
        StreamData.StatusData.Builder statusDataBuilder = StreamData.StatusData.newBuilder();

        //Build location data TODO: add speed
        locDataBuilder.setLat((float) location.getLatitude());
        locDataBuilder.setLon((float) location.getLongitude());
        locDataBuilder.setBearing(location.getBearing());
        locDataBuilder.setUctTime(location.getTime());
        locDataBuilder.setAccuracy(location.getAccuracy());
        locDataBuilder.setSpeed(location.getSpeed());
        StreamData.LocData locData = locDataBuilder.build();

        //Build status data TODO: get status data from status bar buttons
        boolean[] statusValues = {false, false, false}; //Save guard if frag tip status bar has not been initialized for some reason
        if(fragTripStatusBar != null)
            statusValues = fragTripStatusBar.getStatus();

        statusDataBuilder.setBusFull(statusValues[0]);
        statusDataBuilder.setHeavyTraffic(statusValues[1]);
        statusDataBuilder.setBusEmpty(statusValues[2]);

        StreamData.StatusData statusData = statusDataBuilder.build();

        //Build upstream data packet
        //TODO: change vehicle id and driver id to dynamic
        upstreamBuilder.setVehicleId(1);
        upstreamBuilder.setDriverId(1);
        upstreamBuilder.setLiveStreaming(liveStream);
        upstreamBuilder.setLocData(locData);
        upstreamBuilder.setStatusData(statusData);
        StreamData upstreamDataPacket = upstreamBuilder.build();

        return upstreamDataPacket;
    }

    //Adds user location to map with direction
    private void addUserMarker(Location location)
    {

        //Add user location marker
        if (userLocMarker != null)  //Remove previous location marker if exists
            mapView.removeMarker(userLocMarker);

        userLocMarker = new Marker(mapView, "You", "Your last known location", new LatLng(location.getLatitude(), location.getLongitude()));

        //TODO: Rotate drawable according to barring
        //        BitmapDrawable userMarkerIcon = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_user_marker);
        Drawable rotatedDrawable = rotateToBearing(location.getBearing(), R.drawable.ic_user_marker);
        userLocMarker.setIcon(new Icon(rotatedDrawable));

        mapView.addMarker(userLocMarker);
    }

    //Animates camera to location
    private void animateCamera(Location location)
    {
        mapView.getController().animateTo(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    public void closeTripStartScreen()
    {

        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_bottom);
        slideOut.setDuration(500);

        fLayMainContent.startAnimation(slideOut);
        fLayMainContent.setVisibility(View.GONE);

        initTripStatusBar();
    }

    //Display chosen trip on map
    private void displayTrip()
    {
        /*//add test line
            PathOverlay line = new PathOverlay(Color.BLUE, 3);
            line.addPoint(-33.9035862,18.543525);
            line.addPoint(-33.9195113, 18.5819035);

            Paint linePaint = line.getPaint();

            float[] intervals = {4.0f, 4.0f, 10.0f};
            SafeDashPathEffect dashEffect = new SafeDashPathEffect(intervals, 5.0f, 4.5f);
            linePaint.setPathEffect(dashEffect);

            line.setPaint(linePaint);


            mapView.getOverlays().add(line);*/
    }

    private void addStopMarker(LatLng location)
    {

    }

    private Drawable rotateToBearing(float bearing, int drawableResource)
    {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), drawableResource);
        // Getting width & height of the given image.
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        // Setting post rotate to 90
        Matrix mtx = new Matrix();
        mtx.postRotate(bearing);
        // Rotating Bitmap
        Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
        BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);

        //Print bearing
//        Toast.makeText(this, "Bearing: " + bearing, Toast.LENGTH_LONG).show();

        return bmd;
    }

}
