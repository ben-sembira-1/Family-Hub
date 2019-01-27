package com.example.sembi.logingui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class newAcount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_acount);
    }

    public void signUp(View view){
        Intent intent2 = new Intent(this, signUp.class);
        startActivity(intent2);
    }
}
