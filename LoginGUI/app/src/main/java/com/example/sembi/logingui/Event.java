package com.example.sembi.logingui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import static java.lang.System.arraycopy;

public class Event extends AppCompatActivity {

    private Button[] choiseButtons;
    private ImageView stateImageView;


    private EventUIModel eventData;

    static Boolean CHOSEN = false;


    private int STATE = 0;
    //-1 - no, 0 - thinking, 1 - yes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        choiseButtons = new Button[3];
        setButtons();


        stateImageView = findViewById(R.id.comingStateImageV);

        refresh();
    }

    private void setButtons() {
        Button[] Buttons = {findViewById(R.id.yesComingButton), findViewById(R.id.notComingButton), findViewById(R.id.maybeComingButton)};


        arraycopy(Buttons, 0, choiseButtons, 0, Buttons.length);
    }

    public void onOffEditMode(View view){
        //TODO
    }


    public void yesClicked(View view) {
        chosen(1);
}

    public void noClicked(View view) {
        chosen(-1);
    }

    public void maybeClicked(View view) {
        chosen(0);
    }

    public void chooseAgain(View view) {
        CHOSEN = false;
        refresh();
    }

    private void chosen(int choise){
        CHOSEN = true;
        STATE = choise;
        refresh();
    }

    public void refresh(){
        setStateImageViewSrc();
        setInvisibility();
        setEventModel();
    }

    private void setEventModel() {
        //TODO setEventModelFromDatabase
    }

    private void setInvisibility() {
        if (CHOSEN){
            for (Button B: choiseButtons)
                B.setVisibility(View.INVISIBLE);
            stateImageView.setVisibility(View.VISIBLE);
        }else{
            for (Button B: choiseButtons)
                B.setVisibility(View.VISIBLE);
            stateImageView.setVisibility(View.INVISIBLE);
        }
    }

    public void setStateImageViewSrc(){
        int drawableId = R.drawable.thumbs_up_64;
        if(STATE == 0)
            drawableId = R.drawable.thinking_50;
        else if (STATE == -1)
            drawableId = R.drawable.thumbs_down_64;


        stateImageView.setImageDrawable(getDrawable(drawableId));
    }


    public void goToHost(View view) {
        Profile.setCurrentUser(eventData.getHost());
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }
}
