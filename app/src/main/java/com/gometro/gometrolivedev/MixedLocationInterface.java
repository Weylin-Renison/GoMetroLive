package com.gometro.gometrolivedev;

import android.content.Context;
import android.content.Intent;

/**
 * Created by wprenison on 2015/09/08.
 */
public interface MixedLocationInterface
{
    enum SERVICE_PROVIDER {MIXED, GPS, NETWORK, WIFI, MIXED_ALL};

    /**
     *Initializes the location service with defaults of: MIXED location provider, 0 time interval, 0 distance interval
     * this will provide location updates from the mixed provider as often as possible
     * @param context The context of the activity using the service
     * @return Creates an instance of MixedLocationService
     */
    void initService(Context context);

    /**
     *Initializes the location service with the given parameters
     * @param context   The context of the activity using the service
     * @param locationProvider  The type of location provider / technology to use for location updates
     * @param timeInterval  The time interval before providing a new coordinate in milliseconds
     * @param distanceInterval The distance in meters before providing a new coordinate
     */
    void initService(Context context, SERVICE_PROVIDER locationProvider, int timeInterval, int distanceInterval);

    /**
     * @param serviceProvider The type of location provider to be used for location updates, in enum -> (MIXED, GPS, NETWORK, WIFI, MIXED_ALL)
     */
    void setServiceProvider(SERVICE_PROVIDER serviceProvider);

    /**
     *
     * @param timeInterval The time interval before providing a new coordinate in milliseconds     *
     */
    void setTimeInterval(int timeInterval);

    /**
     * Sets fastest time location updates should be provided
     * This is important depending on provider chosen and priority
     * @param timeInterval  The fastest time for a new coordinate to be provided
     */
    void setFastestTimeInterval(int timeInterval);

    /**
     *
     * @param distanceInterval  The distance in meters before providing a new coordinate
     */
    void setDistanceInterval(int distanceInterval);

    /**
     *
     * @param msg   The message used when prompting a user to enable their gps
     */
    void setGpsSwitchOnMsg(String msg);

    /**
     * Sets the priority used for mixed provider
     * @param locationPriority  The priority level to use for the Mixed provider values need to be from the enum LocationRequest from the google play location services api
     */
    void setLocationPriority(int locationPriority);

    /**
     * Provides location updates your class, you must implement an interface to your class with an onLocationUpdate method
     * @return Boolean that represents if location updates where started successfully
     */
    boolean requestLocationUpdates();

    /**
     * Stops providing location updates to you class
     */
    void stopLocationUpdates();

    /**
     * Used to change variables when error dialog is dismissed
     */
    void onDialogDismissed();

    /**
     * Used to forward response from error dialog to service to change variables ect
     */
    void onDialogResult(int requestCode, int resultCode, Intent Data);

}
