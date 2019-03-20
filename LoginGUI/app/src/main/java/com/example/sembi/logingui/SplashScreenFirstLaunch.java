package com.example.sembi.logingui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenFirstLaunch extends AppCompatActivity {

    final int SPLASH_SCREEN_TIME_LENGTH = 5000;
    FrameLayout topContainer, bottomContainer, mainContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screan_first_launch);
        topContainer = findViewById(R.id.firstSplashTopGifContainer);
        bottomContainer = findViewById(R.id.firstSplashBottomGifContainer);
        mainContainer = findViewById(R.id.firstSplashMainGifContainer);


        splashScreenToNextSplash();
    }

    private void splashScreenToNextSplash() {

        //TODO
        //RunAnimation();
        //TODO - animation with colors

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                topContainer.animate().alpha(1.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH / 3);
                bottomContainer.animate().alpha(1.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH / 3);
                mainContainer.animate().alpha(0.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH / 3);
            }
        }, 2 * SPLASH_SCREEN_TIME_LENGTH / 3);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenFirstLaunch.this, SplashScreenSecondLaunch.class);
                SplashScreenFirstLaunch.this.overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
                SplashScreenFirstLaunch.this.startActivity(intent);
            }


        }, SPLASH_SCREEN_TIME_LENGTH);

    }
}
