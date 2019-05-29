package com.example.sembi.logingui;

public class EventUIModel {
    private String name,
            host, date, location,
            description, coming;

    public EventUIModel(String name, String host, String date, String location, String description, String coming) {
        this.name = name;
        this.host = host;
        this.date = date;
        this.location = location;
        this.description = description;
        this.coming = coming;
    }

    public EventUIModel() {
        this.name = "";
        this.host = "";
        this.date = "";
        this.location = "";
        this.description = "";
        this.coming = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComing() {
        return coming;
    }

    public void setComing(String coming) {
        this.coming = coming;
    }
}
