package com.example.sembi.logingui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.sembi.logingui.StaticMethods.coming;
import static com.example.sembi.logingui.StaticMethods.getFamilyMembers;
import static com.example.sembi.logingui.StaticMethods.getProfileModel;
import static com.example.sembi.logingui.StaticMethods.notComing;
import static com.example.sembi.logingui.StaticMethods.prepareStringToDataBase;
import static com.example.sembi.logingui.StaticMethods.thinking;
import static java.lang.System.arraycopy;

public class Event extends AppCompatActivity {

    private Button[] choiseButtons;
    TextView eventNameTV;
    TextView eventHostTV;
    private ImageView stateImageView;
    TextView eventDateTV;
    TextView eventLocationTV;
    TextView eventDescriptionTV;
    EditText eventNameET;
    EditText eventDateET;
    EditText eventLocationET;
    EditText eventDescriptionET;
    private TextView[] textViews;
    private EditText[] editTexts;

    private EventUIModel eventData;
    private boolean CHOSEN = false;

    private boolean EDIT_MODE;
    private boolean shownUsrIsCurrentUsr;

    private int STATE = 0;
    private int circle;
    //-1 - no, 0 - thinking, 1 - yes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        CHOSEN = true;
        STATE = 0;

        String[] eventToShow; //get from putExtra
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            eventToShow = null;
        else
            eventToShow = intent.getExtras().getStringArray(getString(R.string.event_extra_tag));

        if (eventToShow == null) {
            EDIT_MODE = true;
        } else if (prepareStringToDataBase(eventToShow[1]).equals(
                prepareStringToDataBase(
                        FirebaseAuth.getInstance().getCurrentUser().getEmail()
                )
        )
        ) {
            EDIT_MODE = false;
            shownUsrIsCurrentUsr = true;
        } else {
            EDIT_MODE = false;
            shownUsrIsCurrentUsr = false;
        }

        if (EDIT_MODE == false)
            setDBListenerForEvent(eventToShow[0]);


        eventNameTV = findViewById(R.id.event_nameTextV);
        eventHostTV = findViewById(R.id.hostDataTextV);
        eventDateTV = findViewById(R.id.dateDataTextV);
        eventLocationTV = findViewById(R.id.locationDataTextV);
        eventDescriptionTV = findViewById(R.id.event_descriptionTV);
        eventNameET = findViewById(R.id.event_nameEditText);
        eventDateET = findViewById(R.id.event_dateEditText);
        eventLocationET = findViewById(R.id.event_locationEditText);
        eventDescriptionET = findViewById(R.id.eventDescriptionEditText);

        setButtons();
        setTextAndEditTextViews();
        stateImageView = findViewById(R.id.comingStateImageV);
        refresh();
        setNumberPicker();
    }

    private void setNumberPicker() {
        NumberPicker np = findViewById(R.id.numberPicker);

        np.setMinValue(0);
        np.setMaxValue(10);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                circle = newVal;
            }
        });
    }

    private void setDBListenerForEvent(String key) {
        FirebaseDatabase.getInstance().getReference(getString(R.string.public_usersDB))
                .child(prepareStringToDataBase(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                .child(getString(R.string.userEventsDB))
                .child(key)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        eventData = dataSnapshot.getValue(EventUIModel.class);
                        refresh();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setTextAndEditTextViews() {
        textViews = new TextView[4];

        TextView[] TVaux = {eventNameTV,
                eventHostTV,
                eventDateTV,
                eventLocationTV};

        for (int i = 0; i < TVaux.length; i++)
            textViews[i] = TVaux[i];

        editTexts = new EditText[4];

        EditText[] ETaux = {eventNameET,
                eventDateET,
                eventLocationET,
                eventDescriptionET};

        for (int i = 0; i < ETaux.length; i++)
            editTexts[i] = ETaux[i];
    }

    private void setButtons() {
        choiseButtons = new Button[3];
        Button[] Buttons = {findViewById(R.id.yesComingButton), findViewById(R.id.notComingButton), findViewById(R.id.maybeComingButton)};
        arraycopy(Buttons, 0, choiseButtons, 0, Buttons.length);
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
        return;
        //TODO make choosing
//        CHOSEN = false;
//        refresh();
    }

    private void chosen(int choice) {
        CHOSEN = true;
        STATE = choice;
        refresh();
    }

    public void refresh() {
        setStateImageViewSrc();
        setInvisibility();
        setTV_AndET_FromEventModel();
    }

    private void setTV_AndET_FromEventModel() {
        if (eventData == null) {
            eventHostTV.setText(getProfileModel(FirebaseAuth.getInstance().getCurrentUser().getEmail()).getName());
            return;
        }

        eventDescriptionTV.setText(eventData.getDescription());
        eventNameET.setText(eventData.getEventName());
        eventNameTV.setText(eventData.getEventName());
        eventDateET.setText(eventData.getDate());
        eventDateTV.setText(eventData.getDate());
        eventDescriptionET.setText(eventData.getDescription());
        eventHostTV.setText(getProfileModel(eventData.getHostEmail()).getName());
        eventLocationET.setText(eventData.getLocation());
        eventLocationTV.setText(eventData.getLocation());

    }

    private void uploadNewEvent() {
        String coming = thinking;
        if (STATE < 0)
            coming = notComing;
        else if (STATE > 0)
            coming = StaticMethods.coming;

        eventData = new EventUIModel(eventNameET.getText().toString(),
                FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                eventDateET.getText().toString(),
                eventLocationET.getText().toString(),
                eventDescriptionET.getText().toString(), coming, "");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.public_usersDB))
                .child(prepareStringToDataBase(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                .child(getString(R.string.userEventsDB)).push();

        ref.setValue(eventData);

        for (ProfileModel profileModel : getFamilyMembers(FirebaseAuth.getInstance().getCurrentUser().getEmail(), circle)) {
            if (!profileModel.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                FirebaseDatabase.getInstance().getReference(getString(R.string.public_usersDB))
                        .child(prepareStringToDataBase(profileModel.getEmail()))
                        .child(getString(R.string.userEventsDB))
                        .push().setValue(eventData);
            }
        }
    }

    private void setInvisibility() {
        if (CHOSEN) {
            for (Button B : choiseButtons)
                B.setVisibility(View.GONE);
            stateImageView.setVisibility(View.VISIBLE);
        } else {
            for (Button B : choiseButtons)
                B.setVisibility(View.VISIBLE);
            stateImageView.setVisibility(View.GONE);
        }

        if (EDIT_MODE) {

            for (Button B : choiseButtons)
                B.setVisibility(View.GONE);
            stateImageView.setVisibility(View.GONE);
            findViewById(R.id.comingTextView).setVisibility(View.INVISIBLE);

            findViewById(R.id.saveEventBtn).setVisibility(View.VISIBLE);

            for (EditText et : editTexts)
                et.setVisibility(View.VISIBLE);
            for (TextView tv : textViews)
                tv.setVisibility(View.GONE);

            findViewById(R.id.event_descriptionTV).setVisibility(View.INVISIBLE);
            findViewById(R.id.eventDescriptionEditText).setVisibility(View.VISIBLE);
            findViewById(R.id.event_circleTV).setVisibility(View.VISIBLE);

        } else {

            if (shownUsrIsCurrentUsr) {
                for (Button B : choiseButtons)
                    B.setVisibility(View.GONE);
                stateImageView.setVisibility(View.GONE);
                findViewById(R.id.comingTextView).setVisibility(View.INVISIBLE);
            }

            findViewById(R.id.numberPicker).setVisibility(View.GONE);

            for (EditText et : editTexts)
                et.setVisibility(View.GONE);
            for (TextView tv : textViews)
                tv.setVisibility(View.VISIBLE);

            findViewById(R.id.event_descriptionTV).setVisibility(View.VISIBLE);
            findViewById(R.id.eventDescriptionEditText).setVisibility(View.GONE);
            findViewById(R.id.event_circleTV).setVisibility(View.GONE);
        }

        eventHostTV.setVisibility(View.VISIBLE);
    }

    public void setStateImageViewSrc() {
        STATE = 0;
        if (eventData != null) {
            switch ((eventData.getComing())) {
                case (coming):
                    STATE = 1;
                case (notComing):
                    STATE = -1;
            }
        }
        int drawableId = R.drawable.thumbs_up_64;
        if (STATE == 0)
            drawableId = R.drawable.thinking_50;
        else if (STATE == -1)
            drawableId = R.drawable.thumbs_down_64;


        stateImageView.setImageDrawable(getDrawable(drawableId));
    }


    public void goToHost(View view) {
        Intent intent = new Intent(this, Profile.class);
        intent.putExtra("USER_MAIL", eventData.getHostEmail());
        startActivity(intent);
    }

    public void saveEvent(View view) {
        uploadNewEvent();
        Toast.makeText(this, "Event created!", Toast.LENGTH_LONG).show();
        goHome(null);
    }

    public void goHome(View view) {
        startActivity(new Intent(this, HomeScreen.class));
    }
}
