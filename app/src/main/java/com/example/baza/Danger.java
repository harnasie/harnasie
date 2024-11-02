package com.example.baza;

import com.google.android.gms.maps.model.LatLng;

public class Danger {
    private String id;
    private String type;
    private LatLng location;
    private String description;
    private String createdAt;
    private boolean accepted;
    //private int user;

    public Danger() {}

    public Danger(boolean accepted, String createdAt, String description, LatLng location, String type, String id) {
        this.accepted = accepted;
        this.createdAt = createdAt;
        this.description = description;
        this.location = location;
        this.type = type;
        this.id = id;
    }
}
