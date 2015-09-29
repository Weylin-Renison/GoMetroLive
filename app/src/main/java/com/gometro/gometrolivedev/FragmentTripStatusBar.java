package com.gometro.gometrolivedev;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by wprenison on 2015/09/11.
 */
public class FragmentTripStatusBar extends Fragment implements View.OnClickListener
{

    //Const
    private final String TAG = "fragTripStatusBar";

    //Views
    private ImageButton btnBusFull;
    private ImageButton btnHeavyTraffic;
    private ImageButton btnBusEmpty;

    //Vars
    private boolean busFull = false;
    private boolean heavyTraffic = false;
    private boolean busEmpty = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.frag_trip_status_bar, container, false);

        //Get Ref to views
        btnBusFull = (ImageButton) constructedView.findViewById(R.id.ibtnFTSBBusFull);
        btnHeavyTraffic = (ImageButton) constructedView.findViewById(R.id.ibtnFTSBHeavyTraffic);
        btnBusEmpty = (ImageButton) constructedView.findViewById(R.id.ibtnFTSBBusEmpty);

        return constructedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        //Init frag
        initBtns();
    }

    private void initBtns()
    {
        btnBusFull.setOnClickListener(this);
        btnHeavyTraffic.setOnClickListener(this);
        btnBusEmpty.setOnClickListener(this);
    }

    //Returns all status parameters as a boolean array
    public boolean[] getStatus()
    {
        boolean[] statusArray = {busFull, heavyTraffic, busEmpty};
        return statusArray;
    }

    @Override
    public void onClick(View v)
    {
        if(v == btnBusFull)
        {
            //TODO: Report status bus full to server

            //Swap bool
            busFull = !busFull;

            //Swap button images
            if(busFull)
                btnBusFull.setImageDrawable(getResources().getDrawable(R.drawable.btn_bus_full_active));
            else
                btnBusFull.setImageDrawable(getResources().getDrawable(R.drawable.selector_btn_bus_full));

            //Check bus cannot be full and empty at same time so swap bus empty off
            if(busEmpty)
            {
                //Swap bool
                busEmpty = !busEmpty;

                //Swap button images
                if(busEmpty)
                    btnBusEmpty.setImageDrawable(getResources().getDrawable(R.drawable.btn_bus_empty_active));
                else
                    btnBusEmpty.setImageDrawable(getResources().getDrawable(R.drawable.selector_btn_bus_empty));
            }
        }
        else if(v == btnHeavyTraffic)
        {
            //TODO: Report status Heavy Traffic to server

            //Swap bool
            heavyTraffic = !heavyTraffic;

            //Swap button images
            if(heavyTraffic)
                btnHeavyTraffic.setImageDrawable(getResources().getDrawable(R.drawable.btn_heavy_traffic_active));
            else
                btnHeavyTraffic.setImageDrawable(getResources().getDrawable(R.drawable.selector_btn_heavy_traffic));
        }
        else if(v == btnBusEmpty)
        {
            //TODO: Report status Bus Empty to server

            //Swap bool
            busEmpty = !busEmpty;

            //Swap button images
            if(busEmpty)
                btnBusEmpty.setImageDrawable(getResources().getDrawable(R.drawable.btn_bus_empty_active));
            else
                btnBusEmpty.setImageDrawable(getResources().getDrawable(R.drawable.selector_btn_bus_empty));

            //Check bus cannot be full and empty at same time so swap bus full off
            if(busFull)
            {
                //Swap bool
                busFull = !busFull;

                //Swap button images
                if(busFull)
                    btnBusFull.setImageDrawable(getResources().getDrawable(R.drawable.btn_bus_full_active));
                else
                    btnBusFull.setImageDrawable(getResources().getDrawable(R.drawable.selector_btn_bus_full));
            }
        }
    }
}
