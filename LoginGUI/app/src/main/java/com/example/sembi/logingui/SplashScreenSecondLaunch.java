package com.example.sembi.logingui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenSecondLaunch extends AppCompatActivity {

    final int SPLASH_SCREEN_TIME_LENGTH = 2500;

    TextView Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_second_launch);
        Text = findViewById(R.id.secondSplashHeaderTV);

        splashScreenToNextSplash();
    }

    private void splashScreenToNextSplash() {

        //TODO
        //RunAnimation();
        //TODO - animation with colors

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(SplashScreenSecondLaunch.this, LoginActivity.class);
//                SplashScreenSecondLaunch.this.startActivity(intent);
//            }
//        }, 2 * SPLASH_SCREEN_TIME_LENGTH / 5);

//        Text.animate().alpha(0.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH).withEndAction(new Runnable() {
//            @Override
//            public void run() {
//                findViewById(R.id.secondSplashHeaderTV3).animate().alpha(1.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH / 5).
//                        withEndAction(new Runnable() {
//                            @Override
//                            public void run() {
//                                Text.animate().alpha(1.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH / 5).
//                                        withEndAction(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                findViewById(R.id.secondSplashHeaderTV2).animate().alpha(1.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH / 5);
//                                            }
//                                        });
//                            }
//                        });
//            }
//        });

        Text.animate().alpha(1.0f).setDuration(4 * SPLASH_SCREEN_TIME_LENGTH / 5).
                withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        Text.animate().alpha(0.0f).setDuration(SPLASH_SCREEN_TIME_LENGTH / 5);
                    }
                });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SplashScreenSecondLaunch.this.startActivity(new Intent(SplashScreenSecondLaunch.this, LoginActivity.class));
            }
        }, SPLASH_SCREEN_TIME_LENGTH);


    }
}
