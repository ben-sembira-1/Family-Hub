package com.example.sembi.logingui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    EditText usernameEditT, passwordEditT, checkPasswordEditT;
    Button signUpBtn;
    TextView comment;
    FirebaseAuth mAuth;
    int passA, passB;

    private FirebaseDatabase mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mDatabaseReference = FirebaseDatabase.getInstance();


        mAuth = FirebaseAuth.getInstance();
        usernameEditT = findViewById(R.id.emailEditT);
        passwordEditT = findViewById(R.id.passEditT);
        checkPasswordEditT = findViewById(R.id.reEnterPassEditT);
        signUpBtn = findViewById(R.id.signUpBtn);
        comment = findViewById(R.id.commentTextV);


        setPasswordsInVars();
    }


    private void setPasswordsInVars() {

        if (isLegalPassword(passwordEditT.getText().toString(), 6)
                && isLegalPassword(checkPasswordEditT.getText().toString(), 6)) {
            passA = Integer.parseInt(passwordEditT.getText().toString());
            passB = Integer.parseInt(checkPasswordEditT.getText().toString());
        } else {
            passA = passB = -1;
        }
    }

    private boolean isLegalPassword(String pass, int length) {

        if (pass.length() != length)
            return false;


        for (int i = 0; i < pass.length(); i++) {
            if ((int) (pass.charAt(i)) < 48 || (int) (pass.charAt(i)) > 57) {
                return false;
            }
        }

        return true;

    }

    public void setETColor(int c, int hc) {
        passwordEditT.setHintTextColor(hc);
        checkPasswordEditT.setHintTextColor(hc);
        usernameEditT.setHintTextColor(hc);

        passwordEditT.setTextColor(c);
        checkPasswordEditT.setTextColor(c);
        usernameEditT.setTextColor(c);
    }

    //trying to sign up
    //options:
    //1. sign up will procceed
    //2. a message will go back and ask the user for different sign up details.

    public void signUpAtempt(View view) {
        setPasswordsInVars();

        setETColor(getColor(R.color.grey), getColor(R.color.grey));

        signUp();
    }

    private void signUp() {
        comment.setVisibility(View.INVISIBLE);


        int usrLgl = usernameLegal(usernameEditT.getText().toString());
        if (usrLgl != 0) {
            data_not_proper_message_generator(usrLgl);
        }
        else {
            int PM = passMatch(passA, passB);

            if (PM != 0) {
                data_not_proper_message_generator(PM);
            } else {


                    mAuth.createUserWithEmailAndPassword(usernameEditT.getText().toString(), passwordEditT.getText().toString())
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        comment.setVisibility(View.INVISIBLE);
//                                        Date date = new Date();
//                                        date.setYear(Calendar.getInstance().get(Calendar.YEAR));
//                                        date.setMonth(Calendar.getInstance().get(Calendar.MONTH));
//                                        date.setDate(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                                        Profile.EDIT_MODE = true;

                                        ProfileModel profileModel = new ProfileModel("","", mAuth.getCurrentUser().getEmail(), "", "", "");

                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference myRef = database.getReference("users");
                                        myRef.child(mAuth.getCurrentUser().getUid()).child("personalData").setValue(profileModel);

                                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                        FirebaseUser FBuser = mAuth.getCurrentUser();

                                        myRef = myRef.child(FBuser.getUid()).child("personalData");
                                        myRef.child("email").setValue(FBuser.getEmail());

                                        myRef = database.getReference().child("allUsers")
                                                .child(StaticMethods.prepareStringToDataBase(FBuser.getEmail()));
                                        myRef.setValue(FBuser.getEmail());

                                        Intent intent = new Intent(SignUp.this, Profile.class);
                                        intent.putExtra("USER_MAIL", FBuser.getEmail());
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(SignUp.this, "email has been used or is not valid", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

            }
        }



    }

    private int usernameLegal(String usrName) {
        boolean flag = true;
        String temp = usrName;

        if (usrName.contains("\\")) {
            return 4;
        }

        for (int i = 0; flag && i < temp.length(); i++) {
            if (temp.charAt(i) == ' ')
                return 4;
            if (temp.charAt(i) == '@') {
                flag = false;
                temp = temp.substring(i + 1);
                if (i == 0)
                    return 4;
            }
        }

        if (flag)
            return 4;

        flag = true;
        for (int i = 0; flag && i < temp.length(); i++) {
            if (temp.charAt(i) == ' ')
                return 4;
            if (temp.charAt(i) == '.') {
                flag = false;
                temp = temp.substring(i + 1);
                if (i == 0)
                    return 4;
            }
        }

        if (flag)
            return 4;

        int i;
        for (i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) == ' ')
                return 4;
        }
        if (i == 0) {
            return 4;
        }


        return 0;
    }

    //0-user created
    //1-username exists
    //2-password not proper
    //3-passwords not match
    //4-username not proper
    public void data_not_proper_message_generator(int message) {

        EditText[] editTextsArray = {usernameEditT, passwordEditT, checkPasswordEditT, usernameEditT};
        String[] messagegArray = {getString(R.string.eror_in_system), getString(R.string.user_taken),
                getString(R.string.pass_not_proper), getString(R.string.pass_not_match),
                getString(R.string.user_not_proper)};

        if (message == 0) {
            Toast.makeText(this, messagegArray[message], Toast.LENGTH_LONG).show();
            return;
        }
        showErorInSignUp(editTextsArray[message - 1]); //1 because there is not editText for message = 0;
        comment.setText(messagegArray[message]);
        comment.setVisibility(View.VISIBLE);


    }

    private void showErorInSignUp(EditText t) {
        changeColor(t, Color.RED);
    }

    private void changeColor(EditText t, int color) {
        t.setTextColor(color);
        t.setHintTextColor(color);
    }


    //0-user created
    //1-username exists
    //2-password not proper
    //3-passwords not match
    //4-username not proper


    //checks if the passwords matches.
    //2 - pass not legal
    //3 - pass not match
    //0 - pass good.
    private int passMatch(int passA, int passB) {
        if (passA == -1)
            return 2;
        if (passA != passB)
            return 3;

        return 0;
    }
}