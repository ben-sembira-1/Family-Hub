package com.example.sembi.logingui;

public class OldConectionsModel {
    String dad;
    String mother;
    String partner;

    public OldConectionsModel(String dad, String mother, String partner) {
        this.dad = dad;
        this.mother = mother;
        this.partner = partner;
    }

    public OldConectionsModel() {
        dad = null;
        mother = null;
        partner = null;
    }

    public String getDad() {
        return dad;
    }

    public void setDad(String dad) {
        this.dad = dad;
    }

    public String getMother() {
        return mother;
    }

    public void setMother(String mother) {
        this.mother = mother;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }
}
