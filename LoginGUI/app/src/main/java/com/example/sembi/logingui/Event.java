package com.example.sembi.logingui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import static java.lang.System.*;

public class Event extends AppCompatActivity {

    private Button[] choiseButtons;
    private ImageView stateImageView;



    private String host; //TODO

    static Boolean CHOSEN = false;



    private static int STATE = 0;
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

    public static int getSTATE() {
        return STATE;
    }

    public static void setSTATE(int STATE) {
        Event.STATE = STATE;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
        Profile.setCurrentUser(host);
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }
}
