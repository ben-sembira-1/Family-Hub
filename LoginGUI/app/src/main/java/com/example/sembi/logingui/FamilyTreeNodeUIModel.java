package com.example.sembi.logingui;

import com.google.firebase.storage.StorageReference;

public class FamilyTreeNodeUIModel {
    private String name;

    private StorageReference IV;
    private String email;

    public FamilyTreeNodeUIModel(String name, StorageReference IV, String email) {
        this.name = name;
        this.IV = IV;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public StorageReference getStorageIVRef() {
        return IV;
    }

    public void setIV(StorageReference IV) {
        this.IV = IV;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
