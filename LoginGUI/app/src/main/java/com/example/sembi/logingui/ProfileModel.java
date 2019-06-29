package com.example.sembi.logingui;

public class ProfileModel {


    private String name;
    private String phone;
    private String email;
    private String date;
    private String city;
    private String address;

    public ProfileModel(String name, String phone, String email, String date, String city, String address) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.date = date;
        this.city = city;
        this.address = address;
    }

    public ProfileModel(){
        this.name = "";
        this.phone = "";
        this.email = "";
        this.date = "";
        this.city = "";
        this.address = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}