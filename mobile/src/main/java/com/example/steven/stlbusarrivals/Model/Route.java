package com.example.steven.stlbusarrivals.Model;

/**
 * Created by Steven on 2016-01-25.
 */
public class Route {
    String tag;

    String title;


    public Route(){    }

    public String getTag(){return tag;}

    public String getTitle(){return title;}

    public String getName(){return tag + " " + title.replace("Direction", "");}

    public void setTag(String tag){this.tag = tag;}

    public void setTitle(String title){this.title = title;}
}
