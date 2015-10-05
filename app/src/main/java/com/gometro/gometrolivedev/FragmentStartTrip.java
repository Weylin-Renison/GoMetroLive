package com.gometro.gometrolivedev;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by wprenison on 2015/09/14.
 */
public class FragmentStartTrip extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener
{

    private DriverAidActivity activity;
    //private ImageView imgvStaticScreen;
    private Spinner spnrVehicle;
    private ImageView imgvArrowRoute;
    private Spinner spnrRoute;
    private ImageView imgvArrowTrip;
    private Spinner spnrTrip;

    int vehicleItemSelectedCount =0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.frag_start_trip_new, container, false);

        //Get handle on views
        //imgvStaticScreen = (ImageView) constructedView.findViewById(R.id.imgvFSTSScreen);

        spnrVehicle = (Spinner) constructedView.findViewById(R.id.spnrFSTVehicle);
        imgvArrowRoute = (ImageView) constructedView.findViewById(R.id.imgvFSTNextArrowRoute);
        spnrRoute = (Spinner) constructedView.findViewById(R.id.spnrFSTRoute);
        imgvArrowTrip = (ImageView) constructedView.findViewById(R.id.imgvFSTNextArrowTrip);
        spnrTrip = (Spinner) constructedView.findViewById(R.id.spnrFSTTrip);


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
        //Set listeners
        spnrVehicle.setOnItemSelectedListener(this);

        //Get & insert data
        //TODO: call data from server here
    }

    private void initSpnrRoute()
    {
        //Set listners
        spnrRoute.setOnItemSelectedListener(this);

        //Get & insert data


        //Unhide & animate
        ScaleAnimation scaleArrowAnim = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleArrowAnim.setDuration(500);
        scaleArrowAnim.setFillEnabled(true);
        scaleArrowAnim.setFillAfter(true);
        imgvArrowRoute.startAnimation(scaleArrowAnim);

        Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.abc_fade_in);
        fadeIn.setDuration(800);
        fadeIn.setFillEnabled(true);
        fadeIn.setFillAfter(true);
        spnrRoute.startAnimation(fadeIn);
    }

    private void initSpnrTrip()
    {
        //Set listners
        spnrTrip.setOnItemSelectedListener(this);

        //Get & insert data

        //Unhide & animate
        ScaleAnimation scaleArrowAnim = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleArrowAnim.setDuration(500);
        scaleArrowAnim.setFillEnabled(true);
        scaleArrowAnim.setFillAfter(true);
        imgvArrowTrip.startAnimation(scaleArrowAnim);

        Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.abc_fade_in);
        fadeIn.setDuration(500);
        fadeIn.setFillEnabled(true);
        fadeIn.setFillAfter(true);
        spnrTrip.startAnimation(fadeIn);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
            if(view.getParent() == spnrVehicle)
            {
                vehicleItemSelectedCount++;
                Toast.makeText(activity, "Vehicle was selected!", Toast.LENGTH_LONG).show();

                if(vehicleItemSelectedCount > 1)
                    initSpnrRoute();
            }
            else if(view.getParent() == spnrRoute)
            {
                Toast.makeText(activity, "Route was selected!", Toast.LENGTH_LONG).show();
                initSpnrTrip();
            }
            else if(view.getParent() == spnrTrip)
            {
                Toast.makeText(activity, "Trip was selected!", Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}
