package com.example.sembi.logingui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.LinkedList;

import static java.lang.System.arraycopy;

public class Profile extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //Set TV & ET array length to thr NUMBER_OF_PARAMETERS
    final static int NUMBER_OF_PARAMETERS = 6;
    //0-Name,1-Phone,2-Email,3-Bday,4-City,5-Adress
    TextView[] TextDataArray;
    EditText[] EditTextDataArray;

    //Flag - enable/disable editing
    static Boolean EDIT_MODE = false;

    //Flag - if true, will set EDIT_MODE to true
    private static boolean firstTime = true;

    //Flag - if true, will set EDIT_MODE to false
    private static boolean secondTime = true;

    //Set what user to show
    private static String currentUser;

    String[] publicData;
    Button[] RequestButtonsArray;


    //date dialog
//    private static final String TAG = "MainActivity";
//    private DatePickerDialog.OnDateSetListener mDateSetListener;

    //probably canceling the medical button ---->
    //Button medicIDBtn;
    //ImageView goToMedicBtn;
    ImageView goToHomeBtn;
    ScrollView dataSV;
    ImageView editIcon;
    FirebaseAuth mAuth;
    LinkedList<String> allUsers;
    LinkedList<DataSnapshot> allRequest;
    LinkedList<DataSnapshot> allPermissions;
    Spinner phoneSpinner, emailSpinner, bdaySpinner, citySpinner, addressSpinner;
    private ProfileModel profileData;
    //for adding media
    private StorageReference mStorageRef;
    private Class nextClass;

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(String currentUserNameUid) {
        currentUser = currentUserNameUid;
    }

    public static void setCurrentUserToMyUser() {
        Profile.setCurrentUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        publicData = new String[5];
        setSpinners();

        allUsers = new LinkedList<String>();
        allRequest = new LinkedList<DataSnapshot>();

        mAuth = FirebaseAuth.getInstance();

        profileData = new ProfileModel();

        setMyUserName();

        //uploading img
        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();


        editIcon = findViewById(R.id.editImageV_Button_);

        dataSV = findViewById(R.id.scrollViewProfile);


        //TODO:
        //firstTime = isFirstTime();

        //probably canceling the medical button ---->
        //medicIDBtn = findViewById(R.id.medicalIDBtn);
        //probably canceling the medical button ---->
        //goToMedicBtn = findViewById(R.id.moveOnButton);
        goToHomeBtn = findViewById(R.id.backHomeBtn);

        TextDataArray = new TextView[NUMBER_OF_PARAMETERS];
        EditTextDataArray = new EditText[NUMBER_OF_PARAMETERS];
        RequestButtonsArray = new Button[2];
        //initialize =>
        setRequestButtonsArray();
        setTextDataArrray();
        setEditTextDataArrray();

        setDataUpdaterFromDataBase();
        setPublicDataUpdaterFromDataBase();


        setAllUsersListener();
        setDateListener();
        setRequestsListener();
        setPermissionsListener();
    }

    private void setRequestsListener() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference().child(getString(R.string.public_usersDB))
                .child(preperStringToDataBase(mAuth.getCurrentUser().getEmail()))
                .child("fam").child("requests");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                handleRequests(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    private void handleRequests(DataSnapshot dataSnapshot) {
        allRequest = new LinkedList<DataSnapshot>();
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            allRequest.add(db);
        }

        if (allRequest.size() > 0) {
            showRequests();
        }
    }

    private void showRequests() {
        for (final DataSnapshot s : allRequest) {

            final String email = s.child("email").getValue().toString();
            final String connection = s.child("connection").getValue().toString();
            final String name = s.child("name").getValue().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("is " + name + " your " + connection + "? (" + email + ")");

            // Set up the buttons
            builder.setNeutralButton("Show Profile", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //TODO goto profile.
                }
            });

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!allUsers.contains(email)) {
                        Toast.makeText(Profile.this,
                                "user doesn't exists anymore",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.public_usersDB))
                            .child(preperStringToDataBase(email))
                            .child("fam")
                            .child("permissions").push();
                    ref.child("email")
                            .setValue(mAuth.getCurrentUser().getEmail());
                    ref.child("name")
                            .setValue(profileData.getName());
                    if (connection.equals("parent")) {
                        ref.child("connection")
                                .setValue("kid");

                        DatabaseReference newMember =  FirebaseDatabase.getInstance().getReference()
                                .child(getString(R.string.public_usersDB))
                                .child(preperStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                .child("fam")
                                .child("parents").push();

                        newMember.child(email);

                    }else {
                        ref.child("connection")
                                .setValue("parent");

                        DatabaseReference newMember =  FirebaseDatabase.getInstance().getReference()
                                .child(getString(R.string.public_usersDB))
                                .child(preperStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                .child("fam")
                                .child("kids").push();

                        newMember.setValue(email);
                    }

                    s.getRef().removeValue();
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                    s.getRef().removeValue();
                }
            });

            builder.show();
        }
    }

    private void setPermissionsListener() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference().child(getString(R.string.public_usersDB))
                .child(preperStringToDataBase(mAuth.getCurrentUser().getEmail()))
                .child("fam").child("permissions");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                handlePermissions(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    private void handlePermissions(DataSnapshot dataSnapshot) {
        allPermissions = new LinkedList<DataSnapshot>();
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            allPermissions.add(db);
        }

        if (allPermissions.size() > 0) {
            showPermissions();
        }
    }

    private void showPermissions() {
        for (final DataSnapshot s : allPermissions) {

            final String email = s.child("email").getValue().toString();
            final String connection = s.child("connection").getValue().toString();
            final String name = s.child("name").getValue().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(name + " accepted your request");

// Set up the buttons
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (connection.equals("parent")) {
                        DatabaseReference newMember =  FirebaseDatabase.getInstance().getReference()
                                .child(getString(R.string.public_usersDB))
                                .child(preperStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                .child("fam")
                                .child("parents").push();

                        newMember.setValue(email);
                    }else {
                        DatabaseReference newMember =  FirebaseDatabase.getInstance().getReference()
                                .child(getString(R.string.public_usersDB))
                                .child(preperStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                .child("fam")
                                .child("kids").push();

                        newMember.setValue(email);
                    }

                    s.getRef().removeValue();
                }
            });

            builder.show();
        }
    }

    private void setAllUsersListener() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference().child("allUsers");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                setAllUsersLL(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    private void setAllUsersLL(DataSnapshot dataSnapshot) {
        allUsers = new LinkedList<String>();
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            allUsers.add(db.getValue().toString());
        }
    }

    private void setSpinners() {
        phoneSpinner = findViewById(R.id.phoneSpinner);
        emailSpinner = findViewById(R.id.emailSpinner);
        bdaySpinner = findViewById(R.id.bdaySpinner);
        citySpinner = findViewById(R.id.citySpinner);
        addressSpinner = findViewById(R.id.addressSpinner);
        setSpinner(phoneSpinner);
        setSpinner(emailSpinner);
        setSpinner(bdaySpinner);
        setSpinner(citySpinner);
        setSpinner(addressSpinner);

    }

    private void setSpinner(Spinner spinner) {
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this, R.array.privacyOptions, android.R.layout.simple_spinner_item);
        spinner.setAdapter(arrayAdapter);

    }

    private void setPrivacy(int i, String s) {

        String[] titles = {"name", "phone", "email", "date", "city", "adress"};
        String[] data = {profileData.getName(), profileData.getPhone(), profileData.getEmail(), profileData.getDate(), profileData.getCity(), profileData.getAdress()};

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();
        databaseReference = databaseReference.child("publicUsers").child(preperStringToDataBase(mAuth.getCurrentUser().getEmail())).child("personalData");
        if (s.equals("Only me"))
            databaseReference.child(titles[i]).setValue("");
        else if (s.equals("My family"))
            databaseReference.child(titles[i]).setValue("%f" + data[i]);
        else if (s.equals("Everyone"))
            databaseReference.child(titles[i]).setValue(data[i]);
    }

    private static String preperStringToDataBase(String s) {
        int numOfDotsFound = 0;
        for (int i = 0; i < s.length() - numOfDotsFound; i++) {
            if (s.charAt(i) == '.') {
                s = s.substring(0, i) + "*" + s.substring(i + 1);
                numOfDotsFound++;
                i--;
            }
        }
        return s;
    }

    private void setDateListener() {
        EditTextDataArray[3].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = EditTextDataArray[3].getText().toString();
                if (s.length() == 3 && s.charAt(2) != '/')
                    EditTextDataArray[3].setText(s.substring(0, 2) + "/" + s.charAt(2));
                else if (s.length() == 6 && s.charAt(5) != '/')
                    EditTextDataArray[3].setText(s.substring(0, 5) + "/" + s.charAt(5));
                else if (s.length() > 10)
                    EditTextDataArray[3].setText(s.substring(0, 10));

                EditTextDataArray[3].setSelection(EditTextDataArray[3].getText().length());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (EditTextDataArray[3].getText().toString().length() != 10)
                    EditTextDataArray[3].setTextColor(Color.RED);
                else
                    EditTextDataArray[3].setTextColor(Color.BLACK);
            }
        });
    }

    public void setMyUserName() {
        if (currentUser == null)
            setCurrentUser(FirebaseAuth.getInstance().getCurrentUser().getUid());

    }

    private void setDataUpdaterFromDataBase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        myRef = myRef.child(currentUser).child("personalData");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                setDataUpdaterFromDataBase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });


    }

    private void setDataUpdaterFromDataBase(DataSnapshot dataSnapshot) {
        profileData.setName(dataSnapshot.getValue(ProfileModel.class).getName());
        profileData.setAdress(dataSnapshot.getValue(ProfileModel.class).getAdress());
        profileData.setCity(dataSnapshot.getValue(ProfileModel.class).getCity());
        profileData.setDate(dataSnapshot.getValue(ProfileModel.class).getDate());
        profileData.setEmail(dataSnapshot.getValue(ProfileModel.class).getEmail());
        profileData.setPhone(dataSnapshot.getValue(ProfileModel.class).getPhone());

        refreshFromDataUpdater();
    }

    private void setPublicDataUpdaterFromDataBase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(getString(R.string.public_usersDB));
        String mail = mAuth.getCurrentUser().getEmail();
        myRef = myRef.child(preperStringToDataBase(mail)).child(getString(R.string.personal_dataDB));

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                setPublicDataUpdaterFromDataBase(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });


    }

    private void setPublicDataUpdaterFromDataBase(DataSnapshot dataSnapshot) {
        publicData[0] = dataSnapshot.getValue(ProfileModel.class).getPhone();
        publicData[1] = dataSnapshot.getValue(ProfileModel.class).getEmail();
        publicData[2] = dataSnapshot.getValue(ProfileModel.class).getDate();
        publicData[3] = dataSnapshot.getValue(ProfileModel.class).getCity();
        publicData[4] = dataSnapshot.getValue(ProfileModel.class).getAdress();

        setPrivacySpinners();
    }

    private void refreshFromDataUpdater() {
        refreshFirstTime();
        changeVisibality();
        changeIcon();
        setEditTextFromDataBase();
        saveChangesInTextViews();
    }

    private void refresh(boolean hasChangedData) {
        refreshFirstTime();
        refreshEditMode(hasChangedData);
    }

    private void savePrivacyInDatabase() {
        final String[] selectionsOptions = getResources().getStringArray(R.array.privacyOptions);

        setPrivacy(0, selectionsOptions[2]);
        setPrivacy(1, phoneSpinner.getSelectedItem().toString());
        setPrivacy(2, emailSpinner.getSelectedItem().toString());
        setPrivacy(3, bdaySpinner.getSelectedItem().toString());
        setPrivacy(4, citySpinner.getSelectedItem().toString());
        setPrivacy(5, addressSpinner.getSelectedItem().toString());
    }

    private void refreshEditMode(boolean hasChangedData) {
        changeVisibality();
        changeIcon();
        if (hasChangedData) {
            saveChangesInDataBase();
            savePrivacyInDatabase();
        } else {
            setEditTextFromDataBase();
            setPrivacySpinners();
        }
        saveChangesInTextViews();

    }

    private void setPrivacySpinners() {
//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        DatabaseReference ref = db.getReference().child(getString(R.string.public_usersDB))
//                .child(mAuth.getCurrentUser().getEmail()).child(getString(R.string.personal_dataDB));
        setSelectionPrivacySpinnersPrivate(phoneSpinner, publicData[0]);
        setSelectionPrivacySpinnersPrivate(emailSpinner, publicData[1]);
        setSelectionPrivacySpinnersPrivate(bdaySpinner, publicData[2]);
        setSelectionPrivacySpinnersPrivate(citySpinner, publicData[3]);
        setSelectionPrivacySpinnersPrivate(addressSpinner, publicData[4]);

    }

    private void setSelectionPrivacySpinnersPrivate(Spinner spinner, String spinnerSelection) {
        final String[] selctionsOptions = getResources().getStringArray(R.array.privacyOptions);

        if (spinnerSelection.equals(""))
            spinner.setSelection(0);
        else if (spinnerSelection.substring(0, 2).equals("%f"))
            spinner.setSelection(1);
        else
            spinner.setSelection(2);
    }

    private void refreshFirstTime() {
        //TODO
        if (profileData.getName().equals("")) {
            EDIT_MODE = true;
            for (Button B : RequestButtonsArray) {
                B.setVisibility(View.INVISIBLE);
            }
            //probably canceling the medical button ---->
            //medicIDBtn.setVisibility(View.INVISIBLE);
            //probably canceling the medical button ---->
            //goToMedicBtn.setVisibility(View.VISIBLE);

            setPrivacyFirstTime();
            firstTime = false;
            secondTime = true;
        } else {
            for (Button B : RequestButtonsArray) {
                B.setVisibility(View.VISIBLE);
            }
            //probably canceling the medical button ---->
            //medicIDBtn.setVisibility(View.VISIBLE);
            //probably canceling the medical button ---->
            //goToMedicBtn.setVisibility(View.INVISIBLE);
            goToHomeBtn.setVisibility(View.VISIBLE);
            firstTime = false;
            secondTime = false;
        }

    }

    private void setPrivacyFirstTime() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference();
        ref = ref.child(getString(R.string.public_usersDB))
                .child(preperStringToDataBase(mAuth.getCurrentUser().getEmail()))
                .child(getString(R.string.personal_dataDB));

        ref.child("phone").setValue("%f" + profileData.getPhone());
        ref.child("email").setValue("%f" + profileData.getEmail());
        ref.child("date").setValue("%f" + profileData.getDate());
        ref.child("city").setValue("%f" + profileData.getCity());
        ref.child("adress").setValue("%f" + profileData.getAdress());
    }

    private void changeVisibality() {
        int textVisibality = View.VISIBLE, editTextVisibality = View.GONE;

        if (EDIT_MODE) {
            textVisibality = View.GONE;
            editTextVisibality = View.VISIBLE;
        }

        for (EditText ET : EditTextDataArray) {
            ET.setVisibility(editTextVisibality);
        }

        for (TextView TV : TextDataArray) {
            TV.setVisibility(textVisibality);
            if (TV.getId() == R.id.NameTextV && textVisibality == View.GONE)
                TV.setVisibility(View.INVISIBLE);

        }

        goToHomeBtn.setVisibility(textVisibality);
        //probably canceling the medical button ---->
        //goToMedicBtn.setVisibility(textVisibality);

        phoneSpinner.setVisibility(editTextVisibality);
        emailSpinner.setVisibility(editTextVisibality);
        bdaySpinner.setVisibility(editTextVisibality);
        citySpinner.setVisibility(editTextVisibality);
        addressSpinner.setVisibility(editTextVisibality);


    }

    private void changeIcon() {

        if (EDIT_MODE)
            editIcon.setImageDrawable(getDrawable(R.drawable.ic_mode_edit_on_24dp));
        else
            editIcon.setImageDrawable(getDrawable(R.drawable.ic_mode_edit_off_24dp));
    }

    private void saveChangesInTextViews() {
        TextDataArray[0].setText(profileData.getName());
        TextDataArray[1].setText(profileData.getPhone());
        TextDataArray[2].setText(profileData.getEmail());
        TextDataArray[3].setText(profileData.getDate());
        TextDataArray[4].setText(profileData.getCity());
        TextDataArray[5].setText(profileData.getAdress());
    }

    private void saveChangesInDataBase() {

        if (!EDIT_MODE) {
            profileData = new ProfileModel(getPersonalDataFromEditTexts(0), getPersonalDataFromEditTexts(1),
                    getPersonalDataFromEditTexts(2), getPersonalDataFromEditTexts(3), getPersonalDataFromEditTexts(4), getPersonalDataFromEditTexts(5));

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users");
            myRef.child(mAuth.getCurrentUser().getUid()).child("personalData").setValue(profileData);
        }
    }

    private void setEditTextFromDataBase() {
        EditTextDataArray[0].setText(profileData.getName());
        EditTextDataArray[1].setText(profileData.getPhone());
        EditTextDataArray[2].setText(profileData.getEmail());
        EditTextDataArray[3].setText(profileData.getDate());
        EditTextDataArray[4].setText(profileData.getCity());
        EditTextDataArray[5].setText(profileData.getAdress());
    }

    public String getPersonalDataFromEditTexts(int i) {
        if (i < 0 || i > 5) {
            Toast.makeText(this, "problem in code!!!", Toast.LENGTH_LONG).show();
            return "___BUG________";
        }
        return EditTextDataArray[i].getText().toString();

    }

    private boolean isFirstTime() {
        //TODO get from dataBase.
        return true;
    }

    private void setEditTextDataArrray() {
        EditText[] ET_id_s = {findViewById(R.id.editNameV),
                findViewById(R.id.editPhoneV),
                findViewById(R.id.editEmailV),
                findViewById(R.id.editBdayV),
                findViewById(R.id.editCityV),
                findViewById(R.id.editAdressV)};

        for (int i = 0; i < NUMBER_OF_PARAMETERS; i++) {
            EditTextDataArray[i] = ET_id_s[i];
        }
    }

    private void setTextDataArrray() {
        TextView[] TV_id_s = {findViewById(R.id.NameTextV),
                findViewById(R.id.PhoneTextV),
                findViewById(R.id.EmailTextV),
                findViewById(R.id.BdayTextV),
                findViewById(R.id.cityTextV),
                findViewById(R.id.AdressTextV)};

        for (int i = 0; i < NUMBER_OF_PARAMETERS; i++) {
            TextDataArray[i] = TV_id_s[i];
        }
    }

    private void setRequestButtonsArray() {
        Button[] B_id_s = {
                findViewById(R.id.addChildBtn),
                findViewById(R.id.addParentBtn)};

        for (int i = 0; i < B_id_s.length; i++) {
            RequestButtonsArray[i] = B_id_s[i];
        }
    }

    public void onOffEditMode(View view) {
        onOffEditModePrivate();
    }

//    private void cheatTheSystem() {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("users");
//        myRef = myRef.child(mAuth.getCurrentUser().getUid());
//        myRef = myRef.child("CTS");
//        myRef.setValue("CTS");
//    }

    private void onOffEditModePrivate() {
        this.EDIT_MODE = !EDIT_MODE;
        refreshEditMode(!EDIT_MODE);
    }

    public void goToMedicalAtempt(View view) {
        nextClass = MedicalRecords.class;
        if (!allFieldsCompleted())
            createEmptyFieldsDialog();
        else
            viBtnMoveOn(nextClass);

    }

    private void viBtnMoveOn(Class s) {
        setDefaultName();
        if (EDIT_MODE)
            onOffEditModePrivate();
        else
            refresh(false);
        if (dataIsLegal()) {
            goTo(s);
        }
    }

    private void setDefaultName() {
        if (profileData.getName() == "")
            profileData.setName("VERY LAZY");
    }

    private void createEmptyFieldsDialog() {
        // TODO
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.empty_fields_dialog_message)
                .setTitle(R.string.empty_fields_dialog_title);

        builder.setPositiveButton(R.string.keep_on_with_un_filled_fields_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                viBtnMoveOn(nextClass);
            }
        });
        builder.setNegativeButton(R.string.cancel_keep_on_with_un_filled_fields_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                return;
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private boolean allFieldsCompleted() {
        // TODO
        for (EditText ET : EditTextDataArray) {
            if (ET.getText().equals(""))
                return false;
        }
        return true;
    }

    private boolean dataIsLegal() {
        return checkData() == 0;
    }

    private int checkData() {
        //TODO 0-good 1-problem in 1 2- problem in 2 3-...
        return 0;
    }

    public void goToHomeAtempt(View view) {
        nextClass = homeScreen.class;
        if (!allFieldsCompleted())
            createEmptyFieldsDialog();
        else
            viBtnMoveOn(nextClass);
    }

//    public void setBDay(View view) {
//        Calendar cal = Calendar.getInstance();
//        int year = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH);
//        int day = cal.get(Calendar.DAY_OF_MONTH);
//
//        DatePickerDialog dialog = new DatePickerDialog(this,
//                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
//                mDateSetListener,
//                year,month,day);
//        dialog.setOnDateSetListener();
//    }

    private void goTo(Class S) {
        Intent intent = new Intent(this, S);
        startActivity(intent);
    }

    //functionality:

    public void sendEmail(View view) {
        if (EDIT_MODE)
            return;
        Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
        mailIntent.setType("*/*").setData(Uri.parse("mailto:" + profileData.getEmail()))
                .putExtra(Intent.EXTRA_SUBJECT, "subject");
        if (mailIntent.resolveActivity(getPackageManager()) != null)
            startActivity(mailIntent);
    }

    public void call(View view) {
        if (EDIT_MODE)
            return;
        String phoneNumber = TextDataArray[1].getText().toString();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        final int REQUEST_PHONE_CALL = 1;
        callIntent.setData(Uri.parse("tel:" + phoneNumber));//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
            } else {
                startActivity(callIntent);
            }
        } else {
            startActivity(callIntent);
        }
    }

    public void navigate(View view) {
        if (EDIT_MODE)
            return;
        Uri geoLocation = Uri.parse("geo:0,0?q="
                + setStringsCompatibleWithUri(profileData.getCity())
                + "+" + setStringsCompatibleWithUri(profileData.getAdress()));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

    }

    private static String setStringsCompatibleWithUri(String s) {
        String compString = "";
        s = s.trim();
        while ((s.length() > 0)) {
            int nextSpace = s.indexOf(" ");
            if (nextSpace < 0)
                nextSpace = s.length();
            compString += s.substring(0, nextSpace) + "+";
            s = s.substring(nextSpace);
            s = s.trim();
        }

        compString = compString.substring(0, compString.lastIndexOf("+"));

        return compString;
    }

    public void addParent(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fill in your fathers Email:");

        // Set up the input
        final EditText input = new EditText(this);
        // Specifies the type of input expected; this, sets the input as a text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Data-Base play
                sendFatherRequestToKid(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void sendFatherRequestToKid(String mail) {
        if (!allUsers.contains(mail)) {
            Toast.makeText(Profile.this,
                    "user doesn't exists",
                    Toast.LENGTH_LONG).show();
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.public_usersDB))
                .child(preperStringToDataBase(mail))
                .child("fam")
                .child("requests").push();
        ref.child("email")
                .setValue(mAuth.getCurrentUser().getEmail());
        ref.child("connection")
                .setValue("kid");
        ref.child("name")
                .setValue(profileData.getName());
    }

    public void addKid(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fill in your parent Email:");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!allUsers.contains(input.getText().toString())) {
                    Toast.makeText(Profile.this,
                            "user doesn't exists",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.public_usersDB))
                        .child(preperStringToDataBase(input.getText().toString()))
                        .child("fam")
                        .child("requests").push();
                ref.child("email")
                        .setValue(mAuth.getCurrentUser().getEmail());
                ref.child("connection")
                        .setValue("parent");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

//    public void changeImage(View view) {
//        Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
//        StorageReference riversRef = mStorageRef.child("images/rivers.jpg");
//
//        riversRef.putFile(file)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        // Get a URL to the uploaded content
//                        Uri downloadUrl = taskSnapshot.();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        // Handle unsuccessful uploads
//                        // ...
//                    }
//                });
//    }


}

