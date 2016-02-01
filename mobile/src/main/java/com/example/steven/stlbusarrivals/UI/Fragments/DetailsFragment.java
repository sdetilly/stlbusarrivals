package com.example.steven.stlbusarrivals.UI.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.steven.stlbusarrivals.Dao.DatabaseHelper;
import com.example.steven.stlbusarrivals.Model.Details;
import com.example.steven.stlbusarrivals.Model.TimeList;
import com.example.steven.stlbusarrivals.R;
import com.example.steven.stlbusarrivals.VolleySingleton;
import com.example.steven.stlbusarrivals.XmlParser;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Steven on 2016-01-28.
 */
public class DetailsFragment extends Fragment implements Observer{

    private DatabaseHelper databaseHelper = null;
    private static TimeList timeList;
    private XmlParser xmlparser = new XmlParser();
    private String stopId, routeTag, stopName, routeName;
    TextView tv_routeName, tv_stopName, firstPrediction, secondPrediction;



    public DetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        xmlparser.addObserver(this);
        stopId = getArguments().getString("stopId");
        stopName = getArguments().getString("stopName");
        routeTag = getArguments().getString("routeTag");
        routeName = getArguments().getString("routeName");
    }

    @Override
    public void onResume(){
        super.onResume();
        RequestQueue queue = VolleySingleton.getInstance(getActivity()).getRequestQueue();
        String url = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=stl&stopId=" + stopId + "&routeTag=" + routeTag;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // we got the response, now our job is to handle it
                //parseXmlResponse(response);
                try{
                    xmlparser.readPrediction(response);
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(request);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.fragment_details, container, false);
        tv_routeName = (TextView)v.findViewById(R.id.tv_details_route_name);
        tv_stopName = (TextView)v.findViewById(R.id.tv_details_stop_name);
        firstPrediction = (TextView) v.findViewById(R.id.first_prediction);
        secondPrediction = (TextView) v.findViewById(R.id.second_prediction);
        tv_routeName.setText(routeName);
        tv_stopName.setText(stopName);

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Details details = new Details(getActivity());
                details.setTag(routeTag);
                details.setStopId(stopId);
                details.setRouteName(routeName);
                details.setStopName(stopName);
                getHelper().getDetailsDao().create(details);
                Toast.makeText(getActivity()," Stop added to favorites!",Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d("detailsfrag update", "entered");
        if(o instanceof TimeList){
            timeList = (TimeList) o;
            if(timeList.size() !=0) {
                firstPrediction.setText("Next bus is in " + timeList.get(0).getTime() + " minutes");
                if (timeList.size() > 1) {
                    secondPrediction.setText("Other bus is in " + timeList.get(1).getTime() + " minutes");
                }
            }
        }
    }

    //Needed so that databaseHelper can be initialised
    private DatabaseHelper getHelper(){
        if(databaseHelper == null){
            databaseHelper = OpenHelperManager.getHelper(getActivity(),DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(databaseHelper != null){
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }
}