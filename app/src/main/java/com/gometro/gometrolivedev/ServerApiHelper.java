package com.gometro.gometrolivedev;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by wprenison on 2015/10/05.
 * Used to interact with basic server api functions in a more streamline manner
 */
public class ServerApiHelper
{
    private final String TAG = "ServerApiHelper";
    //Server vars retrieved from prefs

    private Context context;

    public ServerApiHelper(Context context){this.context = context;}

    public void loginDriver(String email, String password, final OnJsonResponseListner jsonCallback)
    {
        //Encrypt password
        String encryptedPassword = encrypt(password);

        if(encryptedPassword != null)
        {
            //Get Server vars
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            final int TIME_OUT = pref.getInt("SERVER_TIME_OUT", 8000);
            final String ADDRESS = pref.getString("SERVER_ADDRESS", "http://159.8.180.6:9000");
            final String API_LOGIN_DRIVER = pref.getString("SERVER_API_LOGIN_DRIVER", "/loginDriver");

            RequestParams params = new RequestParams();
            params.put("email", email);
            params.put("password", encryptedPassword);

            AsyncHttpClient httpClient = new AsyncHttpClient();
            httpClient.setTimeout(TIME_OUT);
            httpClient.setUserAgent("android");
            httpClient.post(ADDRESS + API_LOGIN_DRIVER, params, new AsyncHttpResponseHandler()
            {
                @Override
                public void onSuccess(int i, String s)
                {
                    super.onSuccess(i, s);

                    //Send response back trough call back
                    Log.i(TAG, "loginDriver: Success - " + s);

                    try
                    {
                        JSONObject jsonResponse = new JSONObject(s);
                        jsonCallback.onJsonResponse(true, jsonResponse);
                    }
                    catch(JSONException je)
                    {
                        je.printStackTrace();
                        Log.e(TAG, "loginDriver: a JsonException has occurred: " + je.getMessage());
                        JSONObject jsonReponse = null;
                        jsonCallback.onJsonResponse(false, jsonReponse);
                    }

                }

                @Override
                public void onFailure(Throwable throwable, String s)
                {
                    super.onFailure(throwable, s);
                    Log.e(TAG, "loginDriver: Failure - " + s);

                    try
                    {
                        JSONObject jsonResponse = new JSONObject(s);
                        jsonCallback.onJsonResponse(false, jsonResponse);
                    }
                    catch(JSONException je)
                    {
                        try
                        {
                            je.printStackTrace();
                            Log.e(TAG, "loginDriver: a JsonException has occurred: " + je.getMessage());
                            JSONObject jsonReponse = new JSONObject();
                            jsonReponse.put("status", 150);
                            jsonReponse.put("msg", "Network failure, check you internet connection and try again.");
                            jsonCallback.onJsonResponse(false, jsonReponse);
                        }
                        catch(JSONException ije)
                        {
                            ije.printStackTrace();
                            Log.e(TAG, "loginDriver: a JsonException has occurred: " + ije.getMessage());
                            JSONObject jsonReponse = null;
                            jsonCallback.onJsonResponse(false, jsonReponse);
                        }
                    }
                }
            });
        }
    }

    public void registerDriver(String firstName, String lastName, String dateOfBirth, String email, String password, String orgCode, final OnServerResponseListener responseCallback)
    {
        //Get Server vars
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final int TIME_OUT = pref.getInt("SERVER_TIME_OUT", 8000);
        final String ADDRESS = pref.getString("SERVER_ADDRESS", "http://159.8.180.6:9000");
        final String API_REGISTER_DRIVER = pref.getString("SERVER_API_REGISTER_DRIVER", "/registerDriver");

        RequestParams params = new RequestParams();
        params.put("firstName", firstName);
        params.put("lastName", lastName);
        params.put("dateOfBirth", dateOfBirth);
        params.put("email", email);
        params.put("password", encrypt(password));
        params.put("organizationCode", orgCode);

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(TIME_OUT);
        httpClient.setUserAgent("android");
        httpClient.post(ADDRESS + API_REGISTER_DRIVER, params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int i, String s)
            {
                super.onSuccess(i, s);

                //Send response back trough call back
                Log.i(TAG, "addVehicle: Success - " + s);

                responseCallback.onResponse(true);
            }

            @Override
            public void onFailure(Throwable throwable, String s)
            {
                super.onFailure(throwable, s);
                Log.e(TAG, "addVehicle: Failure - " + s);
                Toast.makeText(context, s, Toast.LENGTH_LONG).show();
                responseCallback.onResponse(false);
            }
        });
    }

    private String encrypt(String msg)
    {
        String retVal = null;
        //Use HMAC - SHA1 encryption with server secret as key
        //Encrypt password
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String key = pref.getString("SERVER_SECRET", "85afe0c0b4a338aa0b0f1e5b803693a1a542c82e");

        try
        {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(key.getBytes(), mac.getAlgorithm());
            mac.init(secret);
            byte[] digest = mac.doFinal(msg.getBytes("UTF-8"));

            retVal = bytesToHex(digest);    //TODO: include AES encryption here
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "An exception occurred whilst encrypting a message: " + e.getMessage());
        }

        return retVal;
    }

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void addVehicle(String vehicleType, String registration, final OnServerResponseListener responseCallback)
    {
        //Get Server vars
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final int TIME_OUT = pref.getInt("SERVER_TIME_OUT", 8000);
        final String ADDRESS = pref.getString("SERVER_ADDRESS", "http://159.8.180.6:9000");
        final String API_ADD_VEHICLE = pref.getString("SERVER_API_ADD_VEHICLE", "/addVehicle");

        RequestParams params = new RequestParams();
        params.put("vehicleType", vehicleType);
        params.put("registration", registration);
        params.put("streamId", "1");    //TODO: change stream id to dynamic value

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(TIME_OUT);
        httpClient.setUserAgent("android");
        httpClient.post(ADDRESS + API_ADD_VEHICLE, params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int i, String s)
            {
                super.onSuccess(i, s);

                //Send response back trough call back
                Log.i(TAG, "addVehicle: Success - " + s);

                responseCallback.onResponse(true);
            }

            @Override
            public void onFailure(Throwable throwable, String s)
            {
                super.onFailure(throwable, s);
                Log.e(TAG, "addVehicle: Failure - " + s);
                responseCallback.onResponse(false);
            }
        });
    }

    /**
     *  Get's available vehicles from server using stream id
     * @param jsonCallback callback / listener back method when a response is received from server
     */
    public void getAvailableVehicles(final OnJsonResponseListner jsonCallback)
    {
        //Get Server vars
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final int TIME_OUT = pref.getInt("SERVER_TIME_OUT", 4000);
        final String ADDRESS = pref.getString("SERVER_ADDRESS", "http://159.8.180.6:9000");
        final String API_GET_VEHICLES = pref.getString("SERVER_API_GET_VEHICLES", "/getAvailableVehicles");

        RequestParams params = new RequestParams();
        params.put("streamId", "1");    //TODO: change stream id to dynamic value

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(TIME_OUT);
        httpClient.setUserAgent("android");
        httpClient.post(ADDRESS + API_GET_VEHICLES, params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int i, String s)
            {
                super.onSuccess(i, s);

                //Send response back trough call back
                Log.i(TAG, "getAvailableVehicles: Success - " + s);

                try
                {
                    JSONArray jsonArrayVechiles = new JSONArray(s);
                    jsonCallback.onJsonResponse(true, jsonArrayVechiles);
                }
                catch(JSONException je)
                {
                    je.printStackTrace();
                    Log.e(TAG, "getAvailableVehicles: a JsonException has occured: " + je.getMessage());
                    JSONArray jsonResponseArray = null;
                    jsonCallback.onJsonResponse(false, jsonResponseArray);
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s)
            {
                super.onFailure(throwable, s);
                Log.e(TAG, "getAvailableVehicles: Failure - " + s);
                JSONArray jsonResponseArray = null;
                jsonCallback.onJsonResponse(false, jsonResponseArray);
            }
        });
    }

    /**
     * Gets all routes for agency
     * @param agencyId Agency to retrieve routes for
     * @param jsonCallback  callback / listener back method when a response is received from server
     */
    public void getAvailableRoutes(String agencyId, final OnJsonResponseListner jsonCallback)
    {
        //Get Server vars
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final int TIME_OUT = pref.getInt("SERVER_TIME_OUT", 4000) * 2;
        final String ADDRESS = pref.getString("SERVER_ADDRESS", "http://159.8.180.6:9000");
        final String API_GET_ROUTES = pref.getString("SERVER_API_GET_ROUTES", "/getAvailableRoutes");

        RequestParams params = new RequestParams();
        params.put("agencyId", agencyId);

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(TIME_OUT);
        httpClient.setUserAgent("android");
        httpClient.post(ADDRESS + API_GET_ROUTES, params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int i, String s)
            {
                super.onSuccess(i, s);

                //Send response back trough call back
                Log.i(TAG, "getAvailableRoutes: Success - " + s);

                try
                {
                    JSONArray jsonArrayRoutes = new JSONArray(s);
                    jsonCallback.onJsonResponse(true, jsonArrayRoutes);
                } catch (JSONException je)
                {
                    je.printStackTrace();
                    Log.e(TAG, "getAvailableRoutes: a JsonException has occurred: " + je.getMessage());
                    JSONArray jsonResponseArray = null;
                    jsonCallback.onJsonResponse(false, jsonResponseArray);
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s)
            {
                super.onFailure(throwable, s);
                Log.e(TAG, "getAvailableRoutes: Failure - " + s);
                JSONArray jsonResponseArray = null;
                jsonCallback.onJsonResponse(false, jsonResponseArray);
            }
        });
    }

    /**
     * Gets available trips based on a route (returns unique shape ids)
     * @param routeId   route id to be used to look up trips
     * @param jsonCallback  callback / listener back method when a response is received from server
     */
    public void getAvailableTrips(String routeId, final OnJsonResponseListner jsonCallback)
    {
        //Get Server vars
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final int TIME_OUT = pref.getInt("SERVER_TIME_OUT", 4000) * 2;
        final String ADDRESS = pref.getString("SERVER_ADDRESS", "http://159.8.180.6:9000");
        final String API_GET_TRIPS = pref.getString("SERVER_API_GET_TRIPS", "/getAvailableTrips");

        RequestParams params = new RequestParams();
        params.put("routeId", routeId);

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(TIME_OUT);
        httpClient.setUserAgent("android");
        httpClient.post(ADDRESS + API_GET_TRIPS, params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int i, String s)
            {
                super.onSuccess(i, s);

                //Send response back trough call back
                Log.i(TAG, "getAvailableTrips: Success - " + s);

                try
                {
                    JSONArray jsonArrayTrips = new JSONArray(s);
                    jsonCallback.onJsonResponse(true, jsonArrayTrips);

                } catch (JSONException je)
                {
                    je.printStackTrace();
                    Log.e(TAG, "getAvailableTrips: a JsonException has occurred: " + je.getMessage());
                    JSONArray jsonResponseArray = null;
                    jsonCallback.onJsonResponse(false, jsonResponseArray);
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s)
            {
                super.onFailure(throwable, s);
                Log.e(TAG, "getAvailableTrips: Failure - " + s);
                JSONArray jsonResponseArray = null;
                jsonCallback.onJsonResponse(false, jsonResponseArray);
            }
        });
    }

    //Gets the shape for a specific shape id
    public void getShape(String shapeId, final OnJsonResponseListner jsonCallback)
    {
        //Get Server vars
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final int TIME_OUT = pref.getInt("SERVER_TIME_OUT", 4000) * 2;
        final String ADDRESS = pref.getString("SERVER_ADDRESS", "http://159.8.180.6:9000");
        final String API_GET_SHAPE = pref.getString("SERVER_API_GET_Shape", "/getShape");

        RequestParams params = new RequestParams();
        params.put("shapeId", shapeId);

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(TIME_OUT);
        httpClient.setUserAgent("android");
        httpClient.post(ADDRESS + API_GET_SHAPE, params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int i, String s)
            {
                super.onSuccess(i, s);

                //Send response back trough call back
                Log.i(TAG, "getShape: Success - " + s);

                try
                {
                    JSONArray jsonArrayShape = new JSONArray(s);
                    jsonCallback.onJsonResponse(true, jsonArrayShape);

                } catch (JSONException je)
                {
                    je.printStackTrace();
                    Log.e(TAG, "getShape: a JsonException has occurred: " + je.getMessage());
                    JSONArray jsonResponseArray = null;
                    jsonCallback.onJsonResponse(false, jsonResponseArray);
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s)
            {
                super.onFailure(throwable, s);
                Log.e(TAG, "getShape: Failure - " + s);
                JSONArray jsonResponseArray = null;
                jsonCallback.onJsonResponse(false, jsonResponseArray);
            }
        });
    }

    //Get the available times for this trip and their stop ids
    public void getTripTimes(String routeId, String shapeId, final OnJsonResponseListner jsonCallback)
    {
        //Get Server vars
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final int TIME_OUT = pref.getInt("SERVER_TIME_OUT", 4000) * 2;
        final String ADDRESS = pref.getString("SERVER_ADDRESS", "http://159.8.180.6:9000");
        final String API_GET_SHAPE = pref.getString("SERVER_API_GET_TRIP_TIMES", "/getTripTimes");

        RequestParams params = new RequestParams();
        params.put("routeId", routeId);
        params.put("shapeId", shapeId);

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(TIME_OUT);
        httpClient.setUserAgent("android");
        httpClient.post(ADDRESS + API_GET_SHAPE, params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int i, String s)
            {
                super.onSuccess(i, s);

                //Send response back trough call back
                Log.i(TAG, "getTripTimes: Success - " + s);

                try
                {
                    JSONArray jsonArrayTripTimes = new JSONArray(s);
                    jsonCallback.onJsonResponse(true, jsonArrayTripTimes);

                } catch (JSONException je)
                {
                    je.printStackTrace();
                    Log.e(TAG, "getTripTimes: a JsonException has occurred: " + je.getMessage());
                    JSONArray jsonResponseArray = null;
                    jsonCallback.onJsonResponse(false, jsonResponseArray);
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s)
            {
                super.onFailure(throwable, s);
                Log.e(TAG, "getTripTimes: Failure - " + s);
                JSONArray jsonResponseArray = null;
                jsonCallback.onJsonResponse(false, jsonResponseArray);
            }
        });
    }

    public void getStops(String tripId, final OnJsonResponseListner jsonCallback)
    {
        //Get Server vars
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final int TIME_OUT = pref.getInt("SERVER_TIME_OUT", 4000) * 2;
        final String ADDRESS = pref.getString("SERVER_ADDRESS", "http://159.8.180.6:9000");
        final String API_GET_SHAPE = pref.getString("SERVER_API_GET_Stops", "/getStops");

        RequestParams params = new RequestParams();
        params.put("tripId", tripId);

        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setTimeout(TIME_OUT);
        httpClient.setUserAgent("android");
        httpClient.post(ADDRESS + API_GET_SHAPE, params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int i, String s)
            {
                super.onSuccess(i, s);

                //Send response back trough call back
                Log.i(TAG, "getStops: Success - " + s);

                try
                {
                    JSONArray jsonArrayStops = new JSONArray(s);
                    jsonCallback.onJsonResponse(true, jsonArrayStops);

                } catch (JSONException je)
                {
                    je.printStackTrace();
                    Log.e(TAG, "getStops: a JsonException has occurred: " + je.getMessage());
                    JSONArray jsonResponseArray = null;
                    jsonCallback.onJsonResponse(false, jsonResponseArray);
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s)
            {
                super.onFailure(throwable, s);
                Log.e(TAG, "getStops: Failure - " + s);
                JSONArray jsonResponseArray = null;
                jsonCallback.onJsonResponse(false, jsonResponseArray);
            }
        });
    }
}
