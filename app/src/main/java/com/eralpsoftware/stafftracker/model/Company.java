package com.eralpsoftware.stafftracker.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;

public class Company {
    String name;
    LatLng location;
    String date;
    String user;
    String description;
    String id;
    String time;
    double rating;

    public Company(String name,  String date,LatLng location, String user, String description, double rating, String id, String time) {
        this.name = name;
        this.location = location;
        this.date = date;
        this.user = user;
        this.description = description;
        this.rating = rating;
        this.id = id;
        this.time = time;

    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getID() {
        return id;
    }

    public void setID(String description) {
        this.id = id;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Company(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return user;
    }

    public void setUserId(String id) {
        this.user = id;
    }
}
