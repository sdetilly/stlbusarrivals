package com.example.steven.stlbusarrivals.UI.Activity;

import android.Manifest;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.steven.stlbusarrivals.Model.VehiculeList;
import com.example.steven.stlbusarrivals.R;
import com.example.steven.stlbusarrivals.UI.Fragments.DetailsFragment;
import com.example.steven.stlbusarrivals.UI.Fragments.FavoritesFragment;
import com.example.steven.stlbusarrivals.UI.Fragments.MapsFragment;
import com.example.steven.stlbusarrivals.UI.Fragments.RouteSearchFragment;
import com.example.steven.stlbusarrivals.VolleySingleton;
import com.example.steven.stlbusarrivals.XmlParser;

import java.util.Observable;
import java.util.Observer;

public class StopDetailsActivity extends AppCompatActivity{

    static String routeTag, stopId, stopName, routeName;
    FragmentPagerAdapter adapterViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_details);

        ViewPager vpPager = (ViewPager) findViewById(R.id.pager_details);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        Bundle extras = getIntent().getExtras();
        routeTag = extras.getString("routeTag");
        stopId = extras.getString("stopId");
        stopName = extras.getString("stopName");
        routeName = extras.getString("routeName");

        setTitle("Stop Details");
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        private String tabTitles[] = new String[] { "Details", "Map" };

        public MyPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FavoritesFragment
                    DetailsFragment detailsFragment = new DetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("stopId", stopId);
                    bundle.putString("routeTag", routeTag);
                    bundle.putString("stopName", stopName);
                    bundle.putString("routeName", routeName);
                    detailsFragment.setArguments(bundle);
                    return detailsFragment;
                case 1: // Fragment # 1 - This will show RouteSearchFragment
                    MapsFragment mapsFragment = new MapsFragment();
                    Bundle bundleMaps = new Bundle();
                    bundleMaps.putString("routeTag", routeTag);
                    mapsFragment.setArguments(bundleMaps);
                    return mapsFragment;
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }
}