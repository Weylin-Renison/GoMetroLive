package com.gometro.gometrolivedev;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by wprenison on 2015/09/14.
 */
public class FragmentActionBar extends Fragment implements View.OnClickListener
{

    private Activity activity;
    private ImageButton ibtnMainMenu;
    private ImageView imgvLogo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.frag_action_bar, container, false);

        //Get ref / handle on views here
        ibtnMainMenu = (ImageButton) constructedView.findViewById(R.id.ibtnFABMainMenu);
        imgvLogo = (ImageView) constructedView.findViewById(R.id.imgvFABLogo);

        return constructedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();

        //Init Frag
        ibtnMainMenu.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if(v == ibtnMainMenu)
            activity.finish();
    }
}
