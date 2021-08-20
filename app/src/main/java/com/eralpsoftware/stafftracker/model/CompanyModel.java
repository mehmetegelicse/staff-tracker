package com.eralpsoftware.stafftracker.model;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class CompanyModel {
    String date;
    String description;
    String id;

    public HashMap<String, Double> getLocation() {
        return location;
    }

    public void setLocation(HashMap<String, Double> location) {
        this.location = location;
    }

    HashMap<String, Double> location;
    String meeting;
    String getMeetingResult;
    long millisTime;
    String name;
    int rating;
    String user;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getMeeting() {
        return meeting;
    }

    public void setMeeting(String meeting) {
        this.meeting = meeting;
    }

    public String getGetMeetingResult() {
        return getMeetingResult;
    }

    public void setGetMeetingResult(String getMeetingResult) {
        this.getMeetingResult = getMeetingResult;
    }

    public long getMillisTime() {
        return millisTime;
    }

    public void setMillisTime(long millisTime) {
        this.millisTime = millisTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
