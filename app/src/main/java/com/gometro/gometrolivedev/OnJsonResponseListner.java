package com.gometro.gometrolivedev;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by wprenison on 2015/10/06.
 */
public interface OnJsonResponseListner
{
    void onJsonResponse(boolean successful, JSONArray jsonArrayResults);

    void onJsonResponse(boolean successful, JSONObject jsonResult);
}
