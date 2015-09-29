package com.gometro.gometrolivedev;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by wprenison on 2015/09/14.
 */
public class FragmentStartTrip extends Fragment implements View.OnClickListener
{

    private DriverAidActivity activity;
    private ImageView imgvStaticScreen;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.frag_start_trip_static, container, false);

        //Get handle on views
        imgvStaticScreen = (ImageView) constructedView.findViewById(R.id.imgvFSTSScreen);

        return constructedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        activity = (DriverAidActivity) getActivity();

        //Init frag
        imgvStaticScreen.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if(v == imgvStaticScreen)
            activity.closeTripStartScreen();
    }
}
