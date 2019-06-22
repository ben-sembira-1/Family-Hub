package com.example.sembi.logingui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class NavHeader extends AppCompatActivity {


//    TextView navFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_header_home_screen);

        //TODO make external function ->

//        navFullName = findViewById(R.id.nav_fullName);
//        //setNav();
    }

//    private void setNav() {
//        setName();
//    }
//
//    private void setName() {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("personalData").child("name");
//
//        // Read from the database
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
//                navFullName.setText(value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w("error", "Failed to read value.", error.toException());
//            }
//        });
//    }
}
