package com.example.sembi.logingui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenFirstLaunch extends AppCompatActivity {

    final int SPLASH_SCREEN_TIME_LENGTH = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screan_first_launch);


        splashScreenToApp();
    }

    private void splashScreenToApp() {

        //TODO
        //RunAnimation();
        //TODO - animation with colors

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenFirstLaunch.this, LoginActivity.class);
                SplashScreenFirstLaunch.this.startActivity(intent);
            }


        }, SPLASH_SCREEN_TIME_LENGTH);

    }
}
