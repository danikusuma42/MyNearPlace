package com.example.mynearplace;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class PlaceMarker {
    private int id;
    private String name;
    private String address;
    private float lat;
    private float lon;
    @SerializedName("Distance")
    private float distance;

    public PlaceMarker(int id, String name, String address, float lat, float lon, float distance) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public static ArrayList<PlaceMarker> fromJsonArray(String s) {
        return new Gson().fromJson(s, new TypeToken<List<PlaceMarker>>(){}.getType());
    }
}
