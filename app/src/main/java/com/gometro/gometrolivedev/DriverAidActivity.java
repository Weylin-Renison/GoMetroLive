package com.gometro.gometrolivedev;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.SafeDashPathEffect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class DriverAidActivity extends AppCompatActivity implements MixedLocationUserClassInterface {

    //Constants
    private final String TAG = "DiverAidActivity";
    private static final int REQ_CODE_RESOLVE_GOOG_API_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";

    //Location Service Vars
    private Context context = this;
    private MixedLocationService locService;
    private boolean isMixedLocServiceBound = false;

    private UpstreamService upstreamService;
    private boolean isUpstreamServiceBound = false;

    //Vars
    //TODO: Route data var here
    private List<Marker> stopsMarkerList;   //List of stop markers user has to stop at
    private Marker userLocMarker;   //User's location
    private boolean follow = true; //Used to det if user should be followed on the map or not
    private boolean liveStream = false; //Used to check if stream should be broadcast to companion apps
    private PathOverlay tripShapeLine;  //The line overlay painted for the selected trip
    private int driverId = 1;
    public int vehicleId = 1;   //Is modified from frag start trip
    private int streamId = 1;

    //Fragment  vars
    private FragmentManager fragMang;
    private final String FRAG_TAG_TRIP_STATUS_BAR = "fragTripStatusBar";
    private final String FRAG_TAG_ACTION_BAR = "fragActionBar";
    private final String FRAG_TAG_START_TRIP = "fragStartTrip";

    private FragmentTripStatusBar fragTripStatusBar;
    private FragmentStartTrip fragStartTrip;

    //Views vars
    private FrameLayout fLayBtnDarwerContent;
    private FrameLayout fLayMainContent;
    private MapView mapView;
    private ImageButton btnLiveStream;
    private TextView txtvLiveStream;
    private ProgressBar progbLoading;

    private ServiceConnection serviceMixedLocConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.i(TAG, "onServiceConnected:");

            //Get ref to service from binder here
            MixedLocationService.MixedLocServBinder binder = (MixedLocationService.MixedLocServBinder) service;
            locService = binder.getService();
            isMixedLocServiceBound = true;

            locService.initService(context);
            locService.requestLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.i(TAG, "onServiceDisconnected:");
            locService.stopLocationUpdates();
            isMixedLocServiceBound = false;
        }
    };

    private ServiceConnection serviceUpstreamConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.i(TAG, "onServiceConnected:");

            //Get ref to service from binder here
            UpstreamService.UpstreamServBinder binder = (UpstreamService.UpstreamServBinder) service;
            upstreamService = binder.getService();
            isUpstreamServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.i(TAG, "onServiceDisconnected:");
            isUpstreamServiceBound = false;
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
        txtvLiveStream = (TextView) findViewById(R.id.txtvDAStreamLiveDesc);
        progbLoading = (ProgressBar) findViewById(R.id.progbLoading);

        //Init vars
        stopsMarkerList = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        driverId = prefs.getInt("driverId", 1);

        //Get ref to frag manager
        fragMang = getSupportFragmentManager();

        initMap();
        initActionBar();
        initTripStatusBar();
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
        fragTrans.setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top, R.anim.abc_slide_in_top, R.anim.abc_slide_out_top);
        fragStartTrip = new FragmentStartTrip();
        fragTrans.add(R.id.fLayDAMainContent, fragStartTrip, FRAG_TAG_START_TRIP);
        fragTrans.commit();
    }

    public void restartTrip()
    {
        FragmentTransaction fragTrans = fragMang.beginTransaction();
        fragTrans.remove(fragStartTrip);
        fragStartTrip = new FragmentStartTrip();
        fragTrans.add(R.id.fLayDAMainContent, fragStartTrip, FRAG_TAG_START_TRIP);
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
        fragTrans.add(R.id.fLayDAButtonDrawerContent, fragTripStatusBar, FRAG_TAG_TRIP_STATUS_BAR);
        fragTrans.commit();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Log.i(TAG, "onStart: ");

        //Bind to service
        Intent bindMixedLocServIntent = new Intent(DriverAidActivity.this, MixedLocationService.class);
        bindService(bindMixedLocServIntent, serviceMixedLocConnection, Context.BIND_AUTO_CREATE);

        Intent bindUpstreamServiceIntent = new Intent(DriverAidActivity.this, UpstreamService.class);
        bindService(bindUpstreamServiceIntent, serviceUpstreamConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, "onResume: ");

        //Bind to service
        Intent bindMixedLocServIntent = new Intent(DriverAidActivity.this, MixedLocationService.class);
        bindService(bindMixedLocServIntent, serviceMixedLocConnection, Context.BIND_AUTO_CREATE);

        Intent bindUpstreamServiceIntent = new Intent(DriverAidActivity.this, UpstreamService.class);
        bindService(bindUpstreamServiceIntent, serviceUpstreamConnection, Context.BIND_AUTO_CREATE);
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
        Log.i(TAG, "onPause: ");
        //Unbind and stop service
        if(isMixedLocServiceBound)
        {
            locService.stopLocationUpdates();
            unbindService(serviceMixedLocConnection);
        }

        if(isUpstreamServiceBound)
        {
            unbindService(serviceUpstreamConnection);
        }

        super.onPause();
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
        follow = true;

        //Swap button images
        if(liveStream)
        {
            btnLiveStream.setImageDrawable(getResources().getDrawable(R.drawable.btn_live_stream_active));
            txtvLiveStream.setText("Finish Trip");

            //Also deactivate trip start bar
            fragStartTrip.disableInputs();
        }
        else
        {
            btnLiveStream.setImageDrawable(getResources().getDrawable(R.drawable.selector_btn_live_stream));
            txtvLiveStream.setText("Start Trip");

            //Also re enable inputs for starting a trip
            fragStartTrip.enableInputs();
        }
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

            //Send data to service for upstream
            if(isUpstreamServiceBound)
            {
                boolean[] statusValues = {false, false, false}; //Save guard if frag tip status bar has not been initialized for some reason
                if(fragTripStatusBar != null)
                    statusValues = fragTripStatusBar.getStatus();

                upstreamService.streamContent(newLocation, statusValues, liveStream, streamId, driverId, vehicleId);
            }

            /*//TODO: package update
            StreamData upstreamDataPacket = constructUpstreamPacket(newLocation);

            //TODO: upload stream data to server
            uploadUpstreamData(upstreamDataPacket);*/
        }
        else
            Toast.makeText(this, "Attempting to locate you", Toast.LENGTH_LONG).show();
    }

    public void paintShape(JSONArray jsonShapeArray)
    {
        //Turn follow off
        follow = false;

        //Remove shape if there was a pervious one painted on map
        if(tripShapeLine != null)
            mapView.removeOverlay(tripShapeLine);

        //paint shape line
        tripShapeLine = new PathOverlay(Color.BLUE, 3);

        //Add points to line
        for( int i = 0; i < jsonShapeArray.length(); i++)
        {
            try
            {
                JSONObject tempObj = jsonShapeArray.getJSONObject(i);
                tripShapeLine.addPoint(tempObj.getDouble("shapePtLat"), tempObj.getDouble("shapePtLon"));
            }
            catch(JSONException je)
            {
                je.printStackTrace();
                Log.e(TAG, "paintShape: an JsonException occurred whilst painting shape" + je.getMessage());
            }
        }

        Paint linePaint = tripShapeLine.getPaint();

        float[] intervals = {4.0f, 4.0f, 10.0f};
        SafeDashPathEffect dashEffect = new SafeDashPathEffect(intervals, 5.0f, 4.5f);
        linePaint.setPathEffect(dashEffect);

        tripShapeLine.setPaint(linePaint);


        mapView.getOverlays().add(tripShapeLine);

        try
        {
            JSONObject startingPoint = jsonShapeArray.getJSONObject(0);
            animateCamera(new LatLng(startingPoint.getDouble("shapePtLat"), startingPoint.getDouble("shapePtLon")));
        }
        catch(JSONException je)
        {
            je.printStackTrace();
            Log.e(TAG, "paintShape: an JsonException occurred whilst trying to find starting point for camera animation: " + je.getMessage());
        }
    }

    public void paintStops(JSONArray jsonStopsArray)
    {
        //Remove shape if there was a pervious one painted on map
        if(stopsMarkerList != null)
        {
            mapView.removeMarkers(stopsMarkerList);
            stopsMarkerList.clear();
        }

       for(int i = 0; i < jsonStopsArray.length(); i++)
       {
           try
           {
               JSONObject stopTime = jsonStopsArray.getJSONObject(i);
               JSONObject stop = stopTime.getJSONObject("stopId");

               //Log.d(TAG, "Stop: " + stop.getString("stopName") + ", " + stop.getString("stopDesc") + ", " + stop.getDouble("stopLat") + ", " + stop.getDouble("stopLon"));
               Marker stopMarker = new Marker(stop.getString("stopName"), stop.getString("stopDesc"), new LatLng(stop.getDouble("stopLat"), stop.getDouble("stopLon")));
               stopMarker.setIcon(new Icon(getResources().getDrawable(R.drawable.ic_stop_marker)));

               stopsMarkerList.add(stopMarker);
               mapView.addMarker(stopMarker);
           }
           catch(JSONException je)
           {
                je.printStackTrace();
               Log.e(TAG, "An JsonException occured whilst painting stops: " + je.getMessage());
           }
       }
    }

    public void showHideLoadingCircle(boolean visibilityAction)
    {
        if(visibilityAction)
        {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
            fadeIn.setDuration(500);
            fadeIn.setFillEnabled(true);
            fadeIn.setFillAfter(true);

            progbLoading.startAnimation(fadeIn);
        }
        else
        {
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
            fadeOut.setDuration(500);

            progbLoading.startAnimation(fadeOut);
            progbLoading.setVisibility(View.GONE);
        }
    }

    public void showHideLiveStreamButton(boolean visibilityAction)
    {
      /*  if(visibilityAction)
        {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom);
            slideIn.setDuration(500);
            slideIn.setFillEnabled(true);
            slideIn.setFillAfter(true);

            btnLiveStream.startAnimation(slideIn);
            txtvLiveStream.startAnimation(slideIn);
        }*/
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
                client.post(SERVER_ADDRESS + SERVER_API_UPLOAD, params,		//"http://192.168.8.102:9000" 192.168.0.29 home URL_BASE + API_UPLOAD for server (54.68.55.70)
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

        //Get dynamic drawable based on status
        int ic_dynamic_vehicle = R.drawable.ic_bus_big;

        if(fragTripStatusBar != null)
            ic_dynamic_vehicle = getStatusDrawableResource(fragTripStatusBar.getStatus()[0], fragTripStatusBar.getStatus()[1], fragTripStatusBar.getStatus()[2]);

        //Rotate drawable according to barring
        Drawable rotatedDrawable = rotateToBearing(location.getBearing(), ic_dynamic_vehicle);
        userLocMarker.setIcon(new Icon(rotatedDrawable));

        mapView.addMarker(userLocMarker);
    }

    private int getStatusDrawableResource(boolean busFull, boolean heavyTraffic, boolean busEmpty)
    {
        int drawableRes = R.drawable.ic_bus_big;

        if(busEmpty && !heavyTraffic)
            drawableRes = R.drawable.ic_bus_g;
        else if(busFull && !heavyTraffic)
            drawableRes = R.drawable.ic_bus_r;
        else if(heavyTraffic && !busEmpty && !busFull)
            drawableRes = R.drawable.ic_bus_y;
        else if(busEmpty && heavyTraffic)
            drawableRes = R.drawable.ic_bus_g_y;
        else if(busFull && heavyTraffic)
            drawableRes = R.drawable.ic_bus_r_y;

        return drawableRes;
    }

    //Animates camera to location
    private void animateCamera(Location location)
    {
        mapView.getController().animateTo(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    //Animates camera to location
    private void animateCamera(LatLng location)
    {
        mapView.getController().setZoomAnimated(17.0f, location, true, false);
        //mapView.getController().animateTo(location);
    }

    public void closeTripStartScreen()
    {

        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_bottom);
        slideOut.setDuration(500);

        fLayMainContent.startAnimation(slideOut);
        fLayMainContent.setVisibility(View.GONE);
    }

    private void addStopMarker(LatLng location)
    {

    }

    private Drawable rotateToBearing(float bearing, int drawableResource)
    {
        if(bearing < 10 || bearing > 350)
            bearing = 0;

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), drawableResource);
        // Getting width & height of the given image.
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        // Setting post rotate to bearing
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
