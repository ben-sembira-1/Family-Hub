package com.example.sembi.logingui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class homeScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    TextView navFullName, navNextEvent;
    ImageView navProfilePic;

    private static int currentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        currentId = -1;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                newPost();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        navFullName = headerView.findViewById(R.id.nav_fullName);

        setNav();
    }

    private void newPost() {
        //TODO
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void setNav() {
        setName();
    }

    private void setName() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser()
                        .getUid()).child("personalData").child("name");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                navFullName.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("error", "Failed to read value.", error.toException());
            }
        });
    }

    private void setpic() {
        //TODO
    }

    private void setNextEvent() {
        //TODO
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }else if (id == R.id.action_new){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    private void inflate(int layoutID) {
//        LinearLayout ll = findViewById(R.id.stubLinearLayout);
//        ll.removeAllViews();
//        LayoutInflater LI = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        ll.addView(LI.inflate(layoutID, ll, false));
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {


        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_members && id != currentId) {
            startActivity(new Intent(this, MedicalRecords.class));
        } else if (id == R.id.nav_events && id != currentId) {

            startActivity(new Intent(this, MedicalRecords.class));
//            inflate(R.layout.activity_event);
//            currentId = id;
        } else if (id == R.id.nav_new_event && id != currentId) {
            startActivity(new Intent(this, Event.class));
        } else if (id == R.id.nav_tree && id != currentId) {
            startActivity(new Intent(this, FamilyTree.class));
//            inflate(R.layout.activity_family_tree);
//            currentId = id;
        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "download the family organizer app from the Google Play Store!!");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_log_out){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        return true;
    }

    public void goToProfile(View view){
        Profile.EDIT_MODE = false;
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }


    //EVENT--EVENT--EVENT--EVENT--EVENT--EVENT-EVENT--EVENT
    //EVENT--EVENT--EVENT--EVENT--EVENT--EVENT-EVENT--EVENT
    //EVENT--EVENT--EVENT--EVENT--EVENT--EVENT-EVENT--EVENT
    //EVENT--EVENT--EVENT--EVENT--EVENT--EVENT-EVENT--EVENT
    //EVENT--EVENT--EVENT--EVENT--EVENT--EVENT-EVENT--EVENT

//    public class Event {
//
//        private Button[] choiseButtons;
//        private ImageView stateImageView;
//
//
//
//        private String host; //TODO
//
//        public Boolean getCHOSEN() {
//            return CHOSEN;
//        }
//
//        private Boolean CHOSEN = false;
//
//
//
//        private int STATE = 0;
//        //-1 - no, 0 - thinking, 1 - yes
//
//        protected void onCreateEvent(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_event);
//
//            choiseButtons = new Button[3];
//            setButtons();
//
//
//            stateImageView = findViewById(R.id.comingStateImageV);
//
//            refresh();
//        }
//
//        public int getSTATE() {
//            return STATE;
//        }
//
//        public void setSTATE(int STATE) {
//            this.STATE = STATE;
//        }
//
//        public String getHost() {
//            return host;
//        }
//
//        public void setHost(String host) {
//            this.host = host;
//        }
//
//        private void setButtons() {
//            Button[] Buttons = {findViewById(R.id.yesComingButton), findViewById(R.id.notComingButton), findViewById(R.id.maybeComingButton)};
//
//
//            arraycopy(Buttons, 0, choiseButtons, 0, Buttons.length);
//        }
//
//        public void onOffEditMode(View view){
//            //TODO
//        }
//
//
//        public void yesClicked(View view) {
//            chosen(1);
//        }
//
//        public void noClicked(View view) {
//            chosen(-1);
//        }
//
//        public void maybeClicked(View view) {
//            chosen(0);
//        }
//
//        public void chooseAgain(View view) {
//            CHOSEN = false;
//            refresh();
//        }
//
//        private void chosen(int choise){
//            CHOSEN = true;
//            STATE = choise;
//            refresh();
//        }
//
//        public void refresh(){
//            setStateImageViewSrc();
//            setInvisibility();
//        }
//
//        private void setInvisibility() {
//            if (CHOSEN){
//                for (Button B: choiseButtons)
//                    B.setVisibility(View.INVISIBLE);
//                stateImageView.setVisibility(View.VISIBLE);
//            }else{
//                for (Button B: choiseButtons)
//                    B.setVisibility(View.VISIBLE);
//                stateImageView.setVisibility(View.INVISIBLE);
//            }
//        }
//
//        public void setStateImageViewSrc(){
//            int drawableId = R.drawable.thumbs_up_64;
//            if(STATE == 0)
//                drawableId = R.drawable.thinking_50;
//            else if (STATE == -1)
//                drawableId = R.drawable.thumbs_down_64;
//
//
//            stateImageView.setImageDrawable(getDrawable(drawableId));
//        }
//
//
//        public void goToHost(View view) {
//            Profile.setCurrentUser(host);
//            Intent intent = new Intent(this, Profile.class);
//            startActivity(intent);
//        }
//    }


    //EVENT--EVENT--EVENT--EVENT--EVENT--EVENT-EVENT--EVENT
    //EVENT--EVENT--EVENT--EVENT--EVENT--EVENT-EVENT--EVENT
    //EVENT--EVENT--EVENT--EVENT--EVENT--EVENT-EVENT--EVENT
    //EVENT--EVENT--EVENT--EVENT--EVENT--EVENT-EVENT--EVENT
    //EVENT--EVENT--EVENT--EVENT--EVENT--EVENT-EVENT--EVENT
}
