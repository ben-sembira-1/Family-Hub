package com.example.sembi.logingui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    Button back, next;
    int curr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        curr = 0;
        back = findViewById(R.id.help_backBtn);
        next = findViewById(R.id.help_nextBtn);

        final ViewSwitcher viewSwitcher = findViewById(R.id.switcher);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curr == 0) {
                    Toast.makeText(HelpActivity.this, "This is the first image..", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (curr == 1) {
                    ((ImageView) findViewById(R.id.firstImageSwitch)).setImageDrawable(getDrawable(R.drawable.first_help_home_screen));
                    viewSwitcher.showPrevious();
                }
                if (curr == 2) {
                    ((ImageView) findViewById(R.id.secondImageSwitch)).setImageDrawable(getDrawable(R.drawable.second_help_family_tree));
                    viewSwitcher.showNext();
                }
                if (curr == 3) {
                    ((ImageView) findViewById(R.id.firstImageSwitch)).setImageDrawable(getDrawable(R.drawable.third_help_profile));
                    next.setText("next");
                    viewSwitcher.showPrevious();
                }
                curr--;
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curr == 0) {
                    ((ImageView) findViewById(R.id.secondImageSwitch)).setImageDrawable(getDrawable(R.drawable.second_help_family_tree));
                    viewSwitcher.showNext();
                }
                if (curr == 1) {
                    ((ImageView) findViewById(R.id.firstImageSwitch)).setImageDrawable(getDrawable(R.drawable.third_help_profile));
                    viewSwitcher.showPrevious();
                }
                if (curr == 2) {
                    next.setText("Go Home");
                    ((ImageView) findViewById(R.id.secondImageSwitch)).setImageDrawable(getDrawable(R.drawable.fourth_help_family_tree));
                    viewSwitcher.showNext();
                }
                if (curr == 3) {
                    startActivity(new Intent(HelpActivity.this, HomeScreen.class));
                    return;
                }
                curr++;
            }
        });
    }
}
