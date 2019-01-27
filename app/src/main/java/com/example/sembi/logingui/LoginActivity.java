package com.example.sembi.logingui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    ImageView eye;
    EditText pass;
    EditText user;
    Boolean show;
    private FirebaseAuth mAuth;

    RadioButton b;

    boolean remMe = false;
    String serial_user = "";
    String serial_ASI = "";
    String uid = "";

    FirebaseDatabase database;
    DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first__login);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        b = findViewById(R.id.toggleButtonRemMe);
        b.setChecked(false);

        mAuth = FirebaseAuth.getInstance();
        eye = findViewById(R.id.eyeImageV);
        pass = findViewById(R.id.passEditT);
        user = findViewById(R.id.usernameEditT);
        show = false;

//        getCurrentPhoneSerial();
//        setPhoneSerialUserListener();
//        setAutoSignInListener();
//        cheatTheSystem();
//        autoSignIn();
    }

    private void autoSignIn() {
        if (serial_ASI != null && serial_user != null && uid != "" && serial_ASI != "" && serial_user != "") {
            Profile.setCurrentUserToMyUser();
            logIn();
        }
    }

//    private void cheatTheSystem(){
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference().child("phoneSerialNumbers").child(uid).child("user");
//        myRef.setValue();
//    }

    private void getCurrentPhoneSerial() {
        final int REQUEST_PHONE_STATE = 1;
        TelephonyManager tManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PHONE_STATE);
        } else {
            uid = tManager.getDeviceId();
        }
    }

    private void setPhoneSerialUserListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("phoneSerialNumbers").child(uid).child("user");


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                serial_user = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //TODO
            }
        });
    }

    private void setAutoSignInListener() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("phoneSerialNumbers").child(uid).child("AutoSignIn");


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                serial_ASI = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //TODO
            }
        });
    }

    public void showPass(View view) {
        String STRpassword = pass.getText().toString();


        if (show) {
            show = !show;
            eye.setImageDrawable((getDrawable(R.drawable.eye)));
            pass.setInputType( InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_VARIATION_PASSWORD);


        }
        else {
            show = !show;
            eye.setImageDrawable((getDrawable(R.drawable.eye_enabled)));
            pass.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            pass.setText(STRpassword);
            pass.setTextDirection(View.TEXT_DIRECTION_LTR);

        }

        pass.setSelection(pass.getText().length());
    }

    public void newAcount(View view){
        Intent intent = new Intent(this, newAcount.class);
        startActivity(intent);
    }

    public void logInAttempt(View view) {
        pass.setTextColor(getColor(R.color.grey));
        user.setTextColor(getColor(R.color.grey));
        if (user.getText().toString().length() == 0){
            Toast.makeText(this, "some fields left empty.", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        mAuth.signInWithEmailAndPassword(user.getText().toString(),pass.getText().toString())
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pass.setTextColor(Color.BLACK);
                        user.setTextColor(Color.BLACK);
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Signing In..", Toast.LENGTH_SHORT).show();
                            logIn();
                        } else {
                            Toast.makeText(LoginActivity.this, "Authntication Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void logIn() {
//        FirebaseDatabase DB = FirebaseDatabase.getInstance();
//        DatabaseReference DBRef = DB.getReference();
//        if (remMe) {
//            DBRef.child("phoneSerialNumbers").child(uid).child("user").setValue(Profile.getMyUserName());
//        } else {
//            DBRef.child("phoneSerialNumbers").child(uid).child("user").setValue("");
//        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        myRef = database.getInstance().getReference().child("users").child(user.getUid()).child("personalData");
        myRef.child("email").setValue(user.getEmail());


        Profile.setCurrentUserToMyUser();
        Intent intent = new Intent(LoginActivity.this, homeScreen.class);
        startActivity(intent);
    }


    public void rememberMeClicked(View view) {
        remMe = b.isChecked();

        int RM = -1;
        if (remMe)
            RM = 1;

        myRef.child("phoneSerialNumbers").child(uid).child("AutoSignIn").setValue(String.valueOf(RM));

    }
}
