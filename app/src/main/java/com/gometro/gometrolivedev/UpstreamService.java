package com.gometro.gometrolivedev;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gometro.gometrolive.StreamPacketProtos;
import com.gometro.gometrolive.StreamPacketProtos.StreamPacket.StreamData;
import com.gometro.gometrolive.StreamPacketProtos.StreamPacket.StreamData.LocData;
import com.gometro.gometrolive.StreamPacketProtos.StreamPacket.StreamData.StatusData;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayInputStream;

/**
 * Created by wprenison on 2015/10/05.
 */
public class UpstreamService extends Service
{
    private final String TAG = "UpstreamService";


    private final IBinder binder = new UpstreamServBinder();

    public class UpstreamServBinder extends Binder
    {
        UpstreamService getService(){return UpstreamService.this;}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        Log.i(TAG, "onBind: Service bound!");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.i(TAG, "unBind: Service unbound!");
        return super.onUnbind(intent);
    }

    public void streamContent(Location location, boolean [] statusValues, boolean liveStreaming, int streamId, int driverId, int vehicleId)
    {
        //Bundle data for async task
        Bundle streamInfoBundle = new Bundle();
        streamInfoBundle.putDouble("loc:lat", location.getLatitude());
        streamInfoBundle.putDouble("loc:lon", location.getLongitude());
        streamInfoBundle.putLong("loc:time", location.getTime());
        streamInfoBundle.putFloat("loc:speed", location.getSpeed());
        streamInfoBundle.putFloat("loc:bearing", location.getBearing());
        streamInfoBundle.putFloat("loc:accuracy", location.getAccuracy());
        streamInfoBundle.putBooleanArray("status:values", statusValues);
        streamInfoBundle.putBoolean("liveStreaming", liveStreaming);
        streamInfoBundle.putInt("streamId", streamId);
        streamInfoBundle.putInt("vehicleId", vehicleId);
        streamInfoBundle.putInt("driverId", driverId);

        new AsyncTask<Bundle, Integer, Void>()
        {
            @Override
            protected Void doInBackground(Bundle... params)
            {
                StreamData packetData = constructPacket(params[0]);

                uploadUpstreamData(packetData);
                return null;
            }

        }.execute(streamInfoBundle);
    }

    public StreamData constructPacket(Bundle streamInfo)
    {
        //Create proto builders
        StreamData.Builder upstreamBuilder = StreamPacketProtos.StreamPacket.StreamData.newBuilder();
        LocData.Builder locDataBuilder = StreamPacketProtos.StreamPacket.StreamData.LocData.newBuilder();
        StatusData.Builder statusDataBuilder = StreamPacketProtos.StreamPacket.StreamData.StatusData.newBuilder();

        //Build location data
        locDataBuilder.setLat((float) streamInfo.getDouble("loc:lat"));
        locDataBuilder.setLon((float) streamInfo.getDouble("loc:lon"));
        locDataBuilder.setBearing(streamInfo.getFloat("loc:bearing"));
        locDataBuilder.setUctTime(streamInfo.getLong("loc:time"));
        locDataBuilder.setAccuracy(streamInfo.getFloat("loc:accuracy"));
        locDataBuilder.setSpeed(streamInfo.getFloat("loc:speed"));
        LocData locData = locDataBuilder.build();

        //Build status data
        boolean [] statusValues = streamInfo.getBooleanArray("status:values");
        statusDataBuilder.setBusFull(statusValues[0]);
        statusDataBuilder.setHeavyTraffic(statusValues[1]);
        statusDataBuilder.setBusEmpty(statusValues[2]);

        StatusData statusData = statusDataBuilder.build();

        //Build upstream data packet
        //TODO: change vehicle id and driver id to dynamic
        upstreamBuilder.setVehicleId(streamInfo.getInt("vehicleId"));
        upstreamBuilder.setDriverId(streamInfo.getInt("driverId"));
        upstreamBuilder.setLiveStreaming(streamInfo.getBoolean("liveStreaming"));
        upstreamBuilder.setLocData(locData);
        upstreamBuilder.setStatusData(statusData);
        StreamData upstreamDataPacket = upstreamBuilder.build();

        return upstreamDataPacket;
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
}
