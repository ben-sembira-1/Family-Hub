package com.example.sembi.logingui;

import android.net.Uri;
import android.widget.ImageView;

import java.net.URI;

public class FamilyTreeNodeUIModel {
    private String name;
    private Uri IV;

    public FamilyTreeNodeUIModel(String name, Uri IV) {
        this.name = name;
        this.IV = IV;
    }

    public Uri getIV() {
        return IV;
    }

    public void setIV(Uri IV) {
        this.IV = IV;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
