package com.example.sembi.logingui;

public class EventUIModel {
    private String eventName,
            hostEmail, date, location,
            description, coming, key;

    public EventUIModel(String eventName, String hostEmail, String date, String location, String description, String coming, String key) {
        this.eventName = eventName;
        this.hostEmail = hostEmail;
        this.date = date;
        this.location = location;
        this.description = description;
        setComing(coming);
        this.key = key;
    }

    public EventUIModel() {
        this.eventName = "";
        this.hostEmail = "";
        this.date = "";
        this.location = "";
        this.description = "";
        setComing("");
        this.key = "";
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getHostEmail() {
        return hostEmail;
    }

    public void setHostEmail(String hostEmail) {
        this.hostEmail = hostEmail;
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
        if (coming.equals(StaticMethods.coming) || coming.equals(StaticMethods.notComing) || coming.equals(StaticMethods.thinking))
            this.coming = coming;
        else
            this.coming = StaticMethods.thinking;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
