package com.example.sembi.logingui;

public class ProfileModel {


    private String name;
    private String phone;
    private String email;
    private String date;
    private String city;
    private String adress;

    public ProfileModel(String name, String phone, String email, String date, String city, String adress) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.date = date;
        this.city = city;
        this.adress = adress;
    }

    public ProfileModel(){
        this.name = "";
        this.phone = "";
        this.email = "";
        this.date = "";
        this.city = "";
        this.adress = "";
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

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

}
