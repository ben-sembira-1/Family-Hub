package com.example.sembi.logingui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class newAcount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_acount);
    }

    public void signUp(View view){
        Intent intent2 = new Intent(this, SignUp.class);
        startActivity(intent2);
    }
}
