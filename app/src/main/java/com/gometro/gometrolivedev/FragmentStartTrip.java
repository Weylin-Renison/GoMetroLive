package com.gometro.gometrolivedev;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wprenison on 2015/09/14.
 */
public class FragmentStartTrip extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
    private final String TAG = "fragStartTrip";

    private DriverAidActivity activity;
    private FragmentStartTrip fragStartTrip = this;
    //private ImageView imgvStaticScreen;
    private Spinner spnrVehicle;
    private Spinner spnrRoute;
    private Spinner spnrTrip;
    private Spinner spnrTripTime;
    private String selectedRouteId;
    private String selectedShapeId;
    private String selectedTripId;

    int vehicleItemSelectedCount = 0;
    int routeItemSelectedCount = 0;
    int tripItemSelectedCount = 0;
    int tripTimeItemSelectedCount = 0;
    private AdapterJsonArray vehicleAdapter;
    private AdapterJsonArray routeAdapter;
    private AdapterJsonArray tripAdapter;
    private AdapterJsonArray tripTimeAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.frag_start_trip_new, container, false);

        //Get handle on views
        //imgvStaticScreen = (ImageView) constructedView.findViewById(R.id.imgvFSTSScreen);

        spnrVehicle = (Spinner) constructedView.findViewById(R.id.spnrFSTVehicle);
        spnrRoute = (Spinner) constructedView.findViewById(R.id.spnrFSTRoute);
        spnrTrip = (Spinner) constructedView.findViewById(R.id.spnrFSTTrip);
        spnrTripTime = (Spinner) constructedView.findViewById(R.id.spnrFSTTripTime);

        return constructedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        activity = (DriverAidActivity) getActivity();

        //Init frag
        //imgvStaticScreen.setOnClickListener(this);
        initSpnrVehicle();
    }

    @Override
    public void onClick(View v)
    {
//        if(v == imgvStaticScreen)
//            activity.closeTripStartScreen();
    }

    private void initSpnrVehicle()
    {
        //Get & insert data
        //call data from server here
        ServerApiHelper apiHelp = new ServerApiHelper(activity);
        apiHelp.getAvailableVehicles(new OnJsonResponseListner()
        {
            @Override
            public void onJsonResponse(boolean successful, JSONArray jsonArrayResults)
            {
                activity.showHideLoadingCircle(false);
                if (successful)
                {
                    //set Spnner data source

                    //Add description text item to start of data source: vehicle
                    try
                    {
                        JSONObject descObj = new JSONObject().put("vehicleId", -1).put("registration", "Select Vehicle");
                        JSONArray finalJsonResponseArray = new JSONArray();
                        finalJsonResponseArray.put(descObj);

                        for (int i = 0; i < jsonArrayResults.length(); i++)
                            finalJsonResponseArray.put(jsonArrayResults.getJSONObject(i));

                        //Include option to add a new vehicle
                        JSONObject addVehicleObj = new JSONObject().put("vehicleId", -2).put("registration", "+ New");
                        finalJsonResponseArray.put(addVehicleObj);

                        //set new data source
                        vehicleAdapter = new AdapterJsonArray(activity, R.layout.spinner_centered_item, finalJsonResponseArray, "registration", spnrVehicle);
                        spnrVehicle.setAdapter(vehicleAdapter);
                        spnrVehicle.invalidate();

                        //Set listeners
                        spnrVehicle.setOnItemSelectedListener(fragStartTrip);

                        //animate entrance
                        Animation dropIn = AnimationUtils.loadAnimation(activity, R.anim.abc_slide_in_top);
                        dropIn.setDuration(500);
                        dropIn.setFillAfter(true);
                        dropIn.setFillEnabled(true);
                        spnrVehicle.startAnimation(dropIn);

                    } catch (JSONException je)
                    {
                        je.printStackTrace();
                        Log.e(TAG, "initSpnrVehicle: onJsonResponse: an JsonException has occured: " + je.getMessage());
                        Toast.makeText(activity, "An error occurred whilst loading available vehicles, please try again later", Toast.LENGTH_LONG).show();
                    }
                } else
                    Toast.makeText(activity, "An error occurred whilst loading available vehicles, please check your internet connection and try again", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onJsonResponse(boolean successful, JSONObject jsonResult)
            {

            }
        });
    }

    private void initSpnrRoute()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        String agencyId = pref.getString("agencyId", "UP");

        //Get route data set fom server
        ServerApiHelper apiHelp = new ServerApiHelper(activity);
        activity.showHideLoadingCircle(true);
        apiHelp.getAvailableRoutes(agencyId, new OnJsonResponseListner()    //TODO: Change agency id to dynamic
        {
            @Override
            public void onJsonResponse(boolean successful, JSONArray jsonArrayResults)
            {
                activity.showHideLoadingCircle(false);
                if (successful)
                {
                    try
                    {
                        //Add description for spinner at start of data set
                        JSONObject descObj = new JSONObject().put("routeId", -1).put("routeLongName", "Select Route");
                        JSONArray finalJsonResponseArray = new JSONArray();
                        finalJsonResponseArray.put(descObj);

                        for (int i = 0; i < jsonArrayResults.length(); i++)
                            finalJsonResponseArray.put(jsonArrayResults.getJSONObject(i));

                        //set new data source
                        routeAdapter = new AdapterJsonArray(activity, R.layout.spinner_centered_item, finalJsonResponseArray, "routeLongName", spnrRoute);
                        spnrRoute.setAdapter(routeAdapter);
                        spnrRoute.invalidate();

                        //Set listeners
                        spnrRoute.setOnItemSelectedListener(fragStartTrip);

                        //Unhide & animate
                        ScaleAnimation scaleArrowAnim = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleArrowAnim.setDuration(500);
                        scaleArrowAnim.setFillEnabled(true);
                        scaleArrowAnim.setFillAfter(true);
                        spnrRoute.startAnimation(scaleArrowAnim);

                    } catch (JSONException je)
                    {
                        je.printStackTrace();
                        Log.e(TAG, "initSpnrRoute: onJsonResponse: an JsonException occurred: " + je.getMessage());
                        Toast.makeText(activity, "An error occurred whilst loading available routes, please try again later", Toast.LENGTH_LONG).show();
                    }
                } else
                {
                    Toast.makeText(activity, "An error occurred whilst loading available routes, please check your internet connection and try again", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onJsonResponse(boolean successful, JSONObject jsonResult)
            {

            }
        });
    }

    private void initSpnrTrip(String routeId)
    {
        //Get trip data set from server
        ServerApiHelper apiHelp = new ServerApiHelper(activity);
        activity.showHideLoadingCircle(true);
        apiHelp.getAvailableTrips(routeId, new OnJsonResponseListner()
        {
            @Override
            public void onJsonResponse(boolean successful, JSONArray jsonArrayResults)
            {
                activity.showHideLoadingCircle(false);
                if (successful)
                {
                    try
                    {
                        //Add description for spinner at start of data set
                        JSONObject descObj = new JSONObject().put("tripName", "Select Trip").put("shapeId", "-1");
                        JSONArray finalJsonResponseArray = new JSONArray();
                        finalJsonResponseArray.put(descObj);

                        int charStartingValue = 65; //TODO: Changes this to class that logically calculates in order to go beyond Z in extreme cases
                        for (int i = 0; i < jsonArrayResults.length(); i++)
                        {
                            //Add a dynamically create trip name for trips ie Trip A, Trip B ect
                            JSONObject tempObj = jsonArrayResults.getJSONObject(i);
                            char letter = (char) charStartingValue;
                            charStartingValue++;
                            tempObj.put("tripName", "Trip " + String.valueOf(letter));
                            finalJsonResponseArray.put(tempObj);
                        }

                        //set new data source
                        tripAdapter = new AdapterJsonArray(activity, R.layout.spinner_centered_item, finalJsonResponseArray, "tripName", spnrTrip);
                        spnrTrip.setAdapter(tripAdapter);
                        spnrTrip.invalidate();

                        //Set listners
                        spnrTrip.setOnItemSelectedListener(fragStartTrip);

                        //Unhide & animate
                        ScaleAnimation scaleArrowAnim = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleArrowAnim.setDuration(500);
                        scaleArrowAnim.setFillEnabled(true);
                        scaleArrowAnim.setFillAfter(true);
                        spnrTrip.startAnimation(scaleArrowAnim);


                    } catch (JSONException je)
                    {
                        je.printStackTrace();
                        Log.e(TAG, "getAvailableTrips: onJsonResponse: " + je.getMessage());
                        Toast.makeText(activity, "An error occurred whilst loading available trips, please try again later", Toast.LENGTH_LONG).show();
                    }
                } else
                    Toast.makeText(activity, "An error occurred whilst loading available trips, please check your internet connection and try again", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onJsonResponse(boolean successful, JSONObject jsonResult)
            {

            }
        });
    }

    private void initSpnrTripTime(String routeId, String shapeId)
    {
        //Get trip data set from server
        ServerApiHelper apiHelp = new ServerApiHelper(activity);
        activity.showHideLoadingCircle(true);
        apiHelp.getTripTimes(routeId, shapeId, new OnJsonResponseListner()
        {
            @Override
            public void onJsonResponse(boolean successful, JSONArray jsonArrayResults)
            {
                activity.showHideLoadingCircle(false);
                if (successful)
                {
                    try
                    {
                        //Add description for spinner at start of data set
                        JSONObject descObj = new JSONObject().put("tripId", -1).put("startTime", "Select Time");
                        JSONArray finalJsonResponseArray = new JSONArray();
                        finalJsonResponseArray.put(descObj);

                        for (int i = 0; i < jsonArrayResults.length(); i++)
                            finalJsonResponseArray.put(jsonArrayResults.getJSONObject(i));

                        //set new data source
                        tripTimeAdapter = new AdapterJsonArray(activity, R.layout.spinner_centered_item, finalJsonResponseArray, "startTime", spnrTripTime);
                        spnrTripTime.setAdapter(tripTimeAdapter);
                        spnrTripTime.invalidate();

                        //Set listners
                        spnrTripTime.setOnItemSelectedListener(fragStartTrip);

                        //Unhide & animate
                        ScaleAnimation scaleArrowAnim = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleArrowAnim.setDuration(500);
                        scaleArrowAnim.setFillEnabled(true);
                        scaleArrowAnim.setFillAfter(true);
                        spnrTripTime.startAnimation(scaleArrowAnim);


                    } catch (JSONException je)
                    {
                        je.printStackTrace();
                        Log.e(TAG, "getTripTimes: onJsonResponse: " + je.getMessage());
                        Toast.makeText(activity, "An error occurred whilst loading available trip times, please try again later", Toast.LENGTH_LONG).show();
                    }
                } else
                    Toast.makeText(activity, "An error occurred whilst loading available trip times, please check your internet connection and try again", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onJsonResponse(boolean successful, JSONObject jsonResult)
            {

            }
        });
    }

    private void paintShape(String shapeId)
    {
        //Get shape file from server
        ServerApiHelper apiHelp = new ServerApiHelper(activity);
        apiHelp.getShape(shapeId, new OnJsonResponseListner()
        {
            @Override
            public void onJsonResponse(boolean successful, JSONArray jsonArrayResults)
            {
                if (successful)
                {
                    activity.paintShape(jsonArrayResults);
                } else
                    Toast.makeText(activity, "An error occurred whilst loading shape, please check your internet connection and try again", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onJsonResponse(boolean successful, JSONObject jsonResult)
            {

            }
        });
    }

    private void paintStops(String tripId)
    {
        //Get stops from server
        ServerApiHelper apiHelp = new ServerApiHelper(activity);
        apiHelp.getStops(tripId, new OnJsonResponseListner()
        {
            @Override
            public void onJsonResponse(boolean successful, JSONArray jsonArrayResults)
            {
                if (successful)
                {
                    activity.paintStops(jsonArrayResults);
                } else
                    Toast.makeText(activity, "An error occurred whilst loading stops, please check your internet connection and try again", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onJsonResponse(boolean successful, JSONObject jsonResult)
            {

            }
        });
    }

    public void disableInputs()
    {
        spnrVehicle.setEnabled(false);
        spnrRoute.setEnabled(false);
        spnrTrip.setEnabled(false);
        spnrTripTime.setEnabled(false);
    }

    public void enableInputs()
    {
        spnrVehicle.setEnabled(true);
        spnrRoute.setEnabled(true);
        spnrTrip.setEnabled(true);
        spnrTripTime.setEnabled(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        View parentView = (View) view.getParent();

       if (parentView == spnrVehicle)
       {
           vehicleItemSelectedCount++;
           //Toast.makeText(activity, "Vehicle was selected!", Toast.LENGTH_LONG).show();

           if (vehicleItemSelectedCount > 1)
           {
               int vehicleId = -99;

               try
               {
                   vehicleId = vehicleAdapter.dataSource.getJSONObject(position).getInt("vehicleId");
               }
               catch(JSONException je)
               {
                   je.printStackTrace();
                   Log.e(TAG, "onItemSelected: an JsonException occurred whilst checking vehicleId: " + je.getMessage());
               }


               if(vehicleId == -1)
               {
                    //If heading is reselected
               }
               else if(vehicleId == -2)
               {
                   //Add vehicle was selected display add vehicle dialog
                   final Dialog addVehicleDialog = new Dialog(activity);
                   addVehicleDialog.setContentView(R.layout.dialog_add_vehicle);
                   addVehicleDialog.setTitle(R.string.diagAV_txtv_heading);

                   //get ref to dialog views
                   final EditText etxtNumberPlate = (EditText) addVehicleDialog.findViewById(R.id.etxtDAVNumberPlate);
                   final Spinner spnrVehicleType = (Spinner) addVehicleDialog.findViewById(R.id.spnrDAVVehicleType);
                   spnrVehicleType.setAdapter(new ArrayAdapter<String>(activity, R.layout.spinner_centered_item, getResources().getStringArray(R.array.diagAV_array_vehicle_types)));
                   Button btnCancel = (Button) addVehicleDialog.findViewById(R.id.btnDAVCancel);
                   Button btnDone = (Button) addVehicleDialog.findViewById(R.id.btnDAVDone);

                   //Set click listeners
                   btnCancel.setOnClickListener(new View.OnClickListener()
                   {
                       @Override
                       public void onClick(View v)
                       {
                           addVehicleDialog.dismiss();
                       }
                   });

                   btnDone.setOnClickListener(new View.OnClickListener()
                   {
                       @Override
                       public void onClick(View v)
                       {
                           //TODO: Validate fields

                           String registration = etxtNumberPlate.getText().toString();
                           String vehicleType = ((TextView)spnrVehicleType.getSelectedView().findViewById(R.id.txtvItem)).getText().toString();

                           //Send of to server to add vehicle
                           //TODO: start progress loading circle
                           ServerApiHelper apiHelp = new ServerApiHelper(activity);
                           apiHelp.addVehicle(vehicleType, registration, new OnServerResponseListener()
                           {
                               @Override
                               public void onResponse(boolean successfull)
                               {
                                   //TODO: end loading circle
                                   addVehicleDialog.dismiss();

                                   if(successfull)
                                   {
                                       //Refresh start trip
                                       activity.restartTrip();
                                   }
                                   else
                                       Toast.makeText(activity, "An error occurred whilst trying to add a vehicle, check your internet connection and try again.", Toast.LENGTH_LONG).show();
                               }
                           });
                       }
                   });

                   addVehicleDialog.show();
               }
               else
               {
                   //A legitimate vehicle id proceed to initialise route spinner
                   initSpnrRoute();

                   //Set vehicle id for streaming packets
                   activity.vehicleId = vehicleId;
               }

           }

       }

       if (parentView == spnrRoute)
       {
           routeItemSelectedCount++;
           //Toast.makeText(activity, "Route was selected!", Toast.LENGTH_LONG).show();

           if(routeItemSelectedCount > 1)
           {
               try
               {
                   selectedRouteId = routeAdapter.dataSource.getJSONObject(position).getString("routeId");
                   initSpnrTrip(selectedRouteId);
               }
               catch(JSONException je)
               {
                   je.printStackTrace();
                   Log.e(TAG, "onItemSelected: route spinner threw an JsonException: " + je.getMessage());
               }

           }
       }

       if (parentView == spnrTrip)
       {
           tripItemSelectedCount++;
           //Toast.makeText(activity, "Trip was selected!", Toast.LENGTH_LONG).show();
           if(tripItemSelectedCount > 1)
           {
               try
               {
                   Log.d(TAG, "onItemSelected: getShape -> shapeId : " + tripAdapter.dataSource.getJSONObject(position).getString("shapeId"));

                   selectedShapeId = tripAdapter.dataSource.getJSONObject(position).getString("shapeId");
                   paintShape(selectedShapeId);
                   initSpnrTripTime(selectedRouteId, selectedShapeId);

               } catch (JSONException je)
               {
                   je.printStackTrace();
                   Log.e(TAG, "onItemSelected: trip spinner threw an JsonException: " + je.getMessage());
               }
           }
       }

        if(parentView == spnrTripTime)
        {
            tripTimeItemSelectedCount++;
            if(tripTimeItemSelectedCount > 1)
            {
                //Toast.makeText(activity, "Trip Time was selected!", Toast.LENGTH_LONG).show();
                try
                {
                    selectedTripId = tripTimeAdapter.dataSource.getJSONObject(position).getString("tripId");
                    //activity.showHideLiveStreamButton(true);
                    paintStops(selectedTripId);

                } catch (JSONException je)
                {
                    je.printStackTrace();
                    Log.e(TAG, "onItemSelected: trip spinner threw an JsonException: " + je.getMessage());
                }
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}
