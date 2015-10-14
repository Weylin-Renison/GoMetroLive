package com.gometro.gometrolivedev;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;


/**
 * Created by wprenison on 2015/10/06.
 */
public class AdapterJsonArray extends ArrayAdapter
{

    private Context context;
    private int layoutRes;
    public JSONArray dataSource;
    private Spinner spnrParent;
    private final String TAG = "AdapterJsonArray";
    private final String PROPERTY_NAME;

    public AdapterJsonArray(Context context, int layoutRes, JSONArray dataSource, String propertyName, Spinner spnrParent)
    {
        super(context, layoutRes);
        this.context = context;
        this.layoutRes = layoutRes;
        this.dataSource = dataSource;
        this.PROPERTY_NAME = propertyName;
        this.spnrParent = spnrParent;
    }

    @Override
    public int getCount()
    {
        return dataSource.length();
    }

    @Override
    public Object getItem(int position)
    {
        String item = null;

        try
        {
            item = dataSource.getJSONObject(position).getString(PROPERTY_NAME);
        }
        catch(JSONException je)
        {
            je.printStackTrace();
            Log.e(TAG, "getItem: an JsonException occured: " + je.getMessage());
        }

        return item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutRes, null);
        }

        TextView txtvItem = (TextView) convertView.findViewById(R.id.txtvItem);

        try
        {
            txtvItem.setText(dataSource.getJSONObject(position).getString(PROPERTY_NAME));
        }
        catch(JSONException je)
        {
            je.printStackTrace();
            Log.e(TAG, "getView: " + je.getMessage());
        }

        return convertView;
    }
}
