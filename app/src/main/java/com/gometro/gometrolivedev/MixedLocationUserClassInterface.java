package com.gometro.gometrolivedev;

import android.content.Intent;
import android.location.Location;

/**
 * Created by wprenison on 2015/09/08.
 */
public interface MixedLocationUserClassInterface
{
    /**
     *
     * @param newLocation Used by your gps service to provide new locations
     */
   void onLocationUpdate(Location newLocation);

    /**
     * Used to forward events to service when error dialog is shown
     */
   void onDialogDismissed();

    //protected void onActivityResult(int requestCode, int resultCode, Intent data) must be overided to provide results to service by forwarding
}
