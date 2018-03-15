package com.eriksanne.edinburghbus.EdinburghBus;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * A BusStop object
 * Created by Erik on 28/02/2018.
 */

public class BusStop {

    private String stopId;
    private String name;
    private String x; //Latitude
    private String y; //Longitude
    private int cap;
    private String orientation;
    private String latLang;
    private String distance;
    double endLatitude;
    double endLongitude;
    float[] distanceArray;

    public BusStop(String stopId, String name, String x, String y, int cap) {
        this.stopId = stopId;
        this.name = name;
        this.x = x;
        this.y = y;
        this.cap = cap;
        if(cap > 337.5 || cap < 22.5){
            this.orientation = "Northbound";
        } else if (cap > 22.5 && cap < 67.5) {
            this.orientation = "Northeastbound";
        } else if (cap > 67.5 && cap < 112.5) {
            this.orientation = "Eastbound";
        } else if (cap > 112.5 && cap < 157.5) {
            this.orientation = "Southeastbound";
        } else if (cap > 157.5 && cap < 202.5) {
            this.orientation = "Southbound";
        } else if (cap > 202.5 && cap < 247.5) {
            this.orientation = "Southwestbound";
        } else if (cap > 247.5 && cap < 292.5) {
            this.orientation = "Westbound";
        } else if (cap > 292.5 && cap < 337.5) {
            this.orientation = "Northwestbound";
        }

        endLatitude = Double.parseDouble(this.x);
        endLongitude = Double.parseDouble(this.x);

        this.latLang = this.x + "," + this.y;

    }

    public String getStopId() {
        return stopId;
    }

    public String getName() {
        return name;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public int getCap(){
        return cap;
    }

    public String getOrientation() {
        return orientation;
    }

    public String getLatLang() {
        return latLang;
    }

    public String getDistance(){
        return distance;
    }

    /**
     * Method to set the distance between current location of the user (endLocation) and the
     * Bus Stop's location.
     * @param endLocation
     */
    public void setDistance(Location endLocation){

        Location startLocation = new Location("");
        startLocation.setLatitude(Double.parseDouble(x));
        startLocation.setLongitude(Double.parseDouble(y));

        float distanceInMeters = startLocation.distanceTo(endLocation);
        int tempint = Math.round(distanceInMeters);
        distance = String.valueOf(tempint);


    }

    @Override
    public String toString() {
        String string = this.getName();
        return string;
    }

}
