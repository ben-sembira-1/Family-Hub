package com.example.sembi.logingui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

    private boolean loginInProgress;

    FirebaseDatabase database;
    DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first__login);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        loginInProgress = false;

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
            //TODO
            login();
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

    public void loginAttempt(View view) {
        if (loginInProgress){
            Toast.makeText(this, "Login attempt in progress, please wait..", Toast.LENGTH_LONG);
            return;
        }
        if (pass.getText().toString().equals("") || user.getText().toString().equals("")) {
            Toast.makeText(this, "Fill in all fields.", Toast.LENGTH_LONG).show();
            return;
        }
        loginInProgress = true;
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
                            login();
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(LoginActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loginInProgress = false;
                Toast.makeText(LoginActivity.this, "failed to sign in", Toast.LENGTH_LONG).show();
            }
        }).addOnCanceledListener(LoginActivity.this, new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Toast.makeText(LoginActivity.this, "failed to sign in", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void login() {
//        FirebaseDatabase DB = FirebaseDatabase.getInstance();
//        DatabaseReference DBRef = DB.getReference();
//        if (remMe) {
//            DBRef.child("phoneSerialNumbers").child(uid).child("user").setValue(Profile.getMyUserName());
//        } else {
//            DBRef.child("phoneSerialNumbers").child(uid).child("user").setValue("");
//        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        myRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("personalData");
        myRef.child("email").setValue(user.getEmail());

        Intent intent = new Intent(LoginActivity.this, HomeScreen.class);
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
