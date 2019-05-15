package com.example.googlelocationapiexample;

public class Contato {

    private String name;
    private String email;
    private double lat;
    private double lon;

    Contato(String name, String email, double lat, double lon) {
        this.name = name;
        this.email = email;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
