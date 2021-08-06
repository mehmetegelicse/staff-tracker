package com.example.stafftracker.model;

public class Task {
    String id;
    String userId;
    String title;
    long created;
    String description;
    int status;
    double latitude, longitude;

    public String getStaffNote() {
        return staffNote;
    }

    public void setStaffNote(String staffNote) {
        this.staffNote = staffNote;
    }

    String staffNote;

    public Task(String id, String userId, String title, long created, String description, int status, double latitude, double longitude,String staffNote) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.created = created;
        this.description = description;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.staffNote = staffNote;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
