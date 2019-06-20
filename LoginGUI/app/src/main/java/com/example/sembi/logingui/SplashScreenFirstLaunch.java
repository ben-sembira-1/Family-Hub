package com.example.sembi.logingui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenFirstLaunch extends AppCompatActivity {

    final int SPLASH_SCREEN_TIME_LENGTH = 5000;
    FrameLayout mainContainer;
    TextView headerTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screan_first_launch);
        mainContainer = findViewById(R.id.firstSplashMainGifContainer);
        headerTV = findViewById(R.id.SplashHeaderTV);


        splashScreenToNextSplash();
    }

    private void splashScreenToNextSplash() {

        //TODO
        //RunAnimation();
        //TODO - animation with colors


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mainContainer.animate().alpha(0.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH / 4);
                headerTV.animate().alpha(1.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH / 4);
            }
        }, 2 * SPLASH_SCREEN_TIME_LENGTH / 4);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                headerTV.animate().alpha(0.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH / 4);
            }
        }, 3 * SPLASH_SCREEN_TIME_LENGTH / 4);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenFirstLaunch.this, LoginActivity.class);
                SplashScreenFirstLaunch.this.startActivity(intent);
            }


        }, SPLASH_SCREEN_TIME_LENGTH);

    }
}
