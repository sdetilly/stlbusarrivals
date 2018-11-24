package com.tilly.steven.stlbusarrivals.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.tilly.steven.stlbusarrivals.Model.Details;
import com.tilly.steven.stlbusarrivals.Model.TimeList;
import com.tilly.steven.stlbusarrivals.R;
import com.tilly.steven.stlbusarrivals.VolleySingleton;
import com.tilly.steven.stlbusarrivals.XmlParser;
import com.tilly.steven.stlbusarrivals.dao.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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
    TextView tv_routeName, tv_stopName, firstPrediction, secondPrediction, tvMessage;
    ArrayList<Details> detailsList;
    private Handler mHandler;



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
        mHandler = new Handler();
    }

    @Override
    public void onResume(){
        super.onResume();
        startRepeatingTask();
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mStatusChecker);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            sendRequest();
            int mInterval = 60000;
            mHandler.postDelayed(mStatusChecker, mInterval);
        }
    };


    private void sendRequest(){
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
                sendRequest();
            }
        });
        queue.add(request);
    }

    private void sendMessageRequest(){
        RequestQueue queue = VolleySingleton.getInstance(getActivity()).getRequestQueue();
        String url = "http://webservices.nextbus.com/service/publicXMLFeed?command=messages&a=stl&r=" + routeTag;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // we got the response, now our job is to handle it
                //parseXmlResponse(response);
                try{
                    xmlparser.readMessages(response);
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                sendMessageRequest();
            }
        });
        queue.add(request);
    }

    private ArrayList<Details> getAllOrderedDetails() {
        // Construct the data source
        // get our query builder from the DAO
        QueryBuilder<Details, Integer> queryBuilder = getHelper().getDetailsDao().queryBuilder();
        // the 'password' field must be equal to "qwerty"
        // prepare our sql statement
        PreparedQuery<Details> preparedQuery = null;
        try {
            preparedQuery = queryBuilder.prepare();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (ArrayList) getHelper().getDetailsDao().query(preparedQuery);
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
        tvMessage = (TextView) v.findViewById(R.id.tv_messages);
        tv_routeName.setText(routeName);
        tv_stopName.setText(stopName);

        detailsList = getAllOrderedDetails();

        final FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Details details = new Details(getActivity());
                details.setTag(routeTag);
                details.setStopId(stopId);
                details.setRouteName(routeName);
                details.setStopName(stopName);
                getHelper().getDetailsDao().create(details);
                Toast.makeText(getActivity(), getString(R.string.added_favorites), Toast.LENGTH_SHORT).show();
                fab.setVisibility(View.GONE);
            }
        });
        for(int i=0; i<detailsList.size();i++){
            if(detailsList.get(i).getStopId().equals(stopId)){
                fab.setVisibility(View.GONE);
            }
        }
        sendMessageRequest();
        return v;
    }

    @Override
     public void update(Observable observable, Object o) {
        if(getActivity() != null) {
            Log.d("detailsfrag update", "entered");
            if (o instanceof TimeList) {
                timeList = (TimeList) o;
                Calendar c = Calendar.getInstance();
                int currentHour = c.get(Calendar.HOUR_OF_DAY);
                int currentMinutes = c.get(Calendar.MINUTE);
                if (timeList.size() != 0) {
                    int predictedMinutes = currentMinutes + Integer.valueOf(timeList.get(0).getTime());
                    while (predictedMinutes >= 60) {
                        currentHour++;
                        predictedMinutes = predictedMinutes - 60;
                    }
                    if (predictedMinutes < 10) {
                        firstPrediction.setText(currentHour + ":0" + predictedMinutes + "   " + getString(R.string.next_bus, timeList.get(0).getTime()));
                    } else {
                        firstPrediction.setText(currentHour + ":" + predictedMinutes + "   " + getString(R.string.next_bus, timeList.get(0).getTime()));
                    }
                    if (timeList.size() > 1) {
                        int nextHour = c.get(Calendar.HOUR_OF_DAY);
                        int nextMinutes = c.get(Calendar.MINUTE);
                        int nextPredictedMinutes = nextMinutes + Integer.valueOf(timeList.get(1).getTime());
                        while (nextPredictedMinutes >= 60) {
                            nextHour++;
                            nextPredictedMinutes = nextPredictedMinutes - 60;
                        }
                        if (nextPredictedMinutes < 10) {
                            secondPrediction.setText(nextHour + ":0" + nextPredictedMinutes + "   " + getString(R.string.other_bus, timeList.get(1).getTime()));
                        } else {
                            secondPrediction.setText(nextHour + ":" + nextPredictedMinutes + "   " + getString(R.string.other_bus, timeList.get(1).getTime()));
                        }
                    }else secondPrediction.setVisibility(View.GONE);
                }
            } else if(o instanceof String){
                String text = o + "";
                tvMessage.setText(text);
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