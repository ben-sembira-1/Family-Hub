package com.example.sembi.logingui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

import static com.example.sembi.logingui.StaticMethods.ADDRESS_INDEX;
import static com.example.sembi.logingui.StaticMethods.ADD_KID_INDEX;
import static com.example.sembi.logingui.StaticMethods.ADD_PARENT_INDEX;
import static com.example.sembi.logingui.StaticMethods.ADD_PARTNER_INDEX;
import static com.example.sembi.logingui.StaticMethods.BDAY_INDEX;
import static com.example.sembi.logingui.StaticMethods.CITY_INDEX;
import static com.example.sembi.logingui.StaticMethods.EMAIL_INDEX;
import static com.example.sembi.logingui.StaticMethods.NAME_INDEX;
import static com.example.sembi.logingui.StaticMethods.PHONE_INDEX;
import static com.example.sembi.logingui.StaticMethods.famFields;
import static com.example.sembi.logingui.StaticMethods.get;
import static com.example.sembi.logingui.StaticMethods.getFamilyMembers;
import static com.example.sembi.logingui.StaticMethods.prepareStringToDataBase;
import static com.example.sembi.logingui.StaticMethods.valueEventListenerAndRefLinkedList;

public class Profile extends AppCompatActivity {

    //Set TV & ET array length to thr NUMBER_OF_PARAMETERS
    final static int NUMBER_OF_PARAMETERS = 6;


    private TextView[] TextDataArray;
    private EditText[] EditTextDataArray;

    //Flag - enable/disable editing
    static Boolean EDIT_MODE = false;

    //Flag - if true, will set EDIT_MODE to true
    private static boolean firstTime = true;

    //Flag - if true, will set EDIT_MODE to false
    private static boolean secondTime = true;

    private LinkedList<String> requestSentTo;
    private LinkedList<String> kids;
    private LinkedList<String> parents;
    private String partner;
    //Set what user to show
    private String currentUser;

    private String[] publicData;
    private Button[] RequestButtonsArray;

    //for uploading image
    private static int RESULT_LOAD_IMAGE = 1;
    private ImageView profileImage;

    private ImageView goToHomeBtn;
    private ImageView editIcon;
    private Spinner phoneSpinner, bdaySpinner, citySpinner, addressSpinner;
    private FirebaseAuth mAuth;
    private LinkedList<String> allUsers;
    private LinkedList<DataSnapshot> allRequests;
    private LinkedList<DataSnapshot> allPermissions;
    private boolean shownUsrIsCurrentUsr;
    private boolean replacedPhoto;
    private ProfileModel profileData;
    //for adding media
    private StorageReference mStorageRef;
    private Class nextClass;
    private LinkedList<String> alertsShownKeys;
    private boolean bUploadingPhoto;

    private void setCurrentUserToMyUser() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String usrToShow; //get from putExtra
        Intent intent = getIntent();
        usrToShow = intent.getExtras().getString("USER_MAIL");

        if (usrToShow != null
                && prepareStringToDataBase(usrToShow).equals(
                prepareStringToDataBase(
                        FirebaseAuth.getInstance().getCurrentUser().getEmail()
                )
        )
        ) {
            setCurrentUserToMyUser();
            shownUsrIsCurrentUsr = true;
        } else {
            currentUser = usrToShow;
            shownUsrIsCurrentUsr = false;
        }

        replacedPhoto = false;
        bUploadingPhoto = false;
        publicData = new String[NUMBER_OF_PARAMETERS];
        alertsShownKeys = new LinkedList<>();
        setSpinners();
        allPermissions = new LinkedList<>();

        profileImage = findViewById(R.id.profile_imageView);

        //for safety
        allUsers = new LinkedList<>();
        allRequests = new LinkedList<>();
        requestSentTo = new LinkedList<>();
        kids = new LinkedList<>();
        parents = new LinkedList<>();
        partner = null;

        mAuth = FirebaseAuth.getInstance();

        profileData = new ProfileModel();

        setMyUserName();

        //uploading img
        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();

        editIcon = findViewById(R.id.editImageV_Button_);
        if (!shownUsrIsCurrentUsr) {
            editIcon.setVisibility(View.GONE);
        }

        goToHomeBtn = findViewById(R.id.backHomeBtn);

        TextDataArray = new TextView[NUMBER_OF_PARAMETERS];
        EditTextDataArray = new EditText[NUMBER_OF_PARAMETERS];
        RequestButtonsArray = new Button[3];
        //initialize =>
        setRequestButtonsArray();
        setTextDataArrray();
        setEditTextDataArrray();

        setDataUpdaterFromDataBase();
        setPublicDataUpdaterFromDataBase();
        setFamilyDataUpdaterFromDataBase();


        setAllUsersListener();
        setDateListener();
        setRequestsListener();
        setPermissionsListener();

        setProfileImageFromStorage();
    }

    private void setProfileImageFromStorage() {
        String mail = currentUser;
        if (shownUsrIsCurrentUsr)
            mail = mAuth.getCurrentUser().getEmail();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(getString(R.string.profile_images))
                .child(prepareStringToDataBase(mail) + ".jpg");

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                int resizeTO = 180;
                Picasso.get()
                        .load(uri)
                        .error(getDrawable(R.drawable.logo))
                        .into(profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                profileImage.setImageDrawable(getDrawable(R.drawable.logo_with_white));
                profileImage.setAdjustViewBounds(true);
            }
        });
    }

    private void setFamilyDataUpdaterFromDataBase() {
        String mail = currentUser;

        if (shownUsrIsCurrentUsr)
            mail = mAuth.getCurrentUser().getEmail();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.public_usersDB))
                .child(prepareStringToDataBase(mail))
                .child("fam");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setFamilyDataFromDataBase(dataSnapshot);
                setTopButtons();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.addValueEventListener(valueEventListener);
        valueEventListenerAndRefLinkedList.add(new ValueEventListenerAndRef(ref, valueEventListener));
    }

    private void setFamilyDataFromDataBase(DataSnapshot dataSnapshot) {
        setRequestsSentToListFromDataBase(dataSnapshot.child("requestsSentTo"));
        setKidsFromDataBase(dataSnapshot.child("kids"));
        setParentsFromDataBase(dataSnapshot.child("parents"));
        setPartnerFromDataBase(dataSnapshot.child("partner"));
    }

    private void setPartnerFromDataBase(DataSnapshot partner) {
        this.partner = partner.getValue(String.class);
    }

    private void setParentsFromDataBase(DataSnapshot parents) {
        this.parents = new LinkedList<>();
        for (DataSnapshot ds : parents.getChildren()) {
            this.parents.add(ds.getValue().toString());
        }
    }

    private void setKidsFromDataBase(DataSnapshot kids) {
        this.kids = new LinkedList<>();
        for (DataSnapshot ds : kids.getChildren()) {
            this.kids.add(ds.getValue().toString());
        }
    }

    private void setTopButtons() {
        if (shownUsrIsCurrentUsr)
            return;
        RequestButtonsArray[ADD_KID_INDEX].setText("Add as a\nkid");
        RequestButtonsArray[ADD_PARENT_INDEX].setText("Add as a\nparent");
        RequestButtonsArray[ADD_PARTNER_INDEX].setText("Add as a\npartner");

        LinkedList<String> mailList = new LinkedList<>();
        for (ProfileModel pm : getFamilyMembers(mAuth.getCurrentUser().getEmail(), 1)) {
            mailList.add(pm.getEmail());
        }

        if (mailList.contains(currentUser)) {

            String myEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            TextView relationText = findViewById(R.id.profileRelationTextView);
            relationText.setVisibility(View.VISIBLE);
            if (kids.contains(myEmail))
                relationText.setText("My Parent");
            if (parents.contains(myEmail))
                relationText.setText("My Kid");
            if (partner != null && partner.equals(myEmail))
                relationText.setText("My Love");
            LinkedList<ProfileModel> brothersModels = get(currentUser, famFields.brothers);
            for (ProfileModel pm : brothersModels) {
                if (pm.getEmail().equals(myEmail))
                    relationText.setText("My Sibling");
            }

            for (Button b : RequestButtonsArray)
                b.setVisibility(View.GONE);
        }
    }

    private void setRequestsListener() {
        if (!shownUsrIsCurrentUsr)
            return;

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference().child(getString(R.string.public_usersDB))
                .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()))
                .child("fam").child("requests");
        ValueEventListener valueEventListener = new ValueEventListener() {
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
        };
        myRef.addValueEventListener(valueEventListener);

        valueEventListenerAndRefLinkedList.add(new ValueEventListenerAndRef(myRef, valueEventListener));
    }

    private void handleRequests(DataSnapshot dataSnapshot) {
        allRequests = new LinkedList<>();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            allRequests.add(ds);
        }

        if (allRequests.size() > 0) {
            showRequests();
        }
    }

    private void showRequests() {
        for (final DataSnapshot s : allRequests) {

            if (!alertsShownKeys.contains(s.getKey()) && s.child("connection").getValue() != null) {
                alertsShownKeys.add(s.getKey());

                final String email = s.child("email").getValue().toString();
                final String connection = s.child("connection").getValue().toString();
                final String name = s.child("name").getValue().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("is " + name + " your " + connection + "? (" + email + ")");

                // Set up the buttons
                builder.setNeutralButton("Show Profile", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Profile.this, Profile.class);
                        intent.putExtra(getString(R.string.profile_extra_mail_tag), email);
                        startActivity(intent);
                    }
                });

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!allUsers.contains(email)) {
                            Toast.makeText(Profile.this,
                                    "user doesn't exists anymore",
                                    Toast.LENGTH_LONG).show();
                            s.getRef().removeValue();
                            return;
                        }
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                                .child(getString(R.string.public_usersDB))
                                .child(prepareStringToDataBase(email))
                                .child("fam")
                                .child("permissions").push();
                        ref.child("email")
                                .setValue(mAuth.getCurrentUser().getEmail());
                        ref.child("name")
                                .setValue(profileData.getName());
                        if (connection.equals("parent")) {
                            ref.child("connection")
                                    .setValue("kid");

                            DatabaseReference newMember = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.public_usersDB))
                                    .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                    .child("fam")
                                    .child("parents").push();

                            newMember.setValue(email);

                        } else if (connection.equals("kid")) {
                            ref.child("connection")
                                    .setValue("parent");

                            DatabaseReference newMember = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.public_usersDB))
                                    .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                    .child("fam")
                                    .child("kids").push();

                            newMember.setValue(email);
                        } else {
                            ref.child("connection")
                                    .setValue("partner");

                            DatabaseReference newMember = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.public_usersDB))
                                    .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                    .child("fam")
                                    .child("partner");

                            newMember.setValue(email);
                        }

                        removeRequestSentTo(email);
                        s.getRef().removeValue();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        removeRequestSentTo(email);
                        s.getRef().removeValue();
                    }
                });

                builder.show();
            }
        }
    }

    private void handlePermissions(DataSnapshot dataSnapshot) {
        int i = 0;
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            boolean flag = true;
            for (DataSnapshot ds2 : allPermissions) {
                if (ds2.getKey().equals(ds.getKey()))
                    flag = false;
            }
            if (flag) {
                allPermissions.add(ds);
                i++;
            }
        }

        if (i > 0 && allPermissions.size() > 0) {
            showPermissions();
        }
    }

    private void setPermissionsListener() {
        if (!shownUsrIsCurrentUsr)
            return;

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference().child(getString(R.string.public_usersDB))
                .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()))
                .child("fam").child("permissions");
        ValueEventListener valueEventListener = new ValueEventListener() {
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
        };
        myRef.addValueEventListener(valueEventListener);
        valueEventListenerAndRefLinkedList.add(new ValueEventListenerAndRef(myRef, valueEventListener));
    }

    private void setAllUsersListener() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference().child("allUsers");
        ValueEventListener valueEventListener = new ValueEventListener() {
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
        };
        myRef.addValueEventListener(valueEventListener);
        valueEventListenerAndRefLinkedList.add(new ValueEventListenerAndRef(myRef, valueEventListener));
    }

    private void setAllUsersLL(DataSnapshot dataSnapshot) {
        allUsers = new LinkedList<String>();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            allUsers.add(ds.getValue().toString());
        }
    }

    private void setSpinners() {
        phoneSpinner = findViewById(R.id.phoneSpinner);
        bdaySpinner = findViewById(R.id.bdaySpinner);
        citySpinner = findViewById(R.id.citySpinner);
        addressSpinner = findViewById(R.id.addressSpinner);
        if (!shownUsrIsCurrentUsr) {
            phoneSpinner.setVisibility(View.GONE);
            bdaySpinner.setVisibility(View.GONE);
            citySpinner.setVisibility(View.GONE);
            addressSpinner.setVisibility(View.GONE);
            return;
        }
        setSpinner(phoneSpinner);
        setSpinner(bdaySpinner);
        setSpinner(citySpinner);
        setSpinner(addressSpinner);

    }

    private void setSpinner(Spinner spinner) {
        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this, R.array.privacyOptions, android.R.layout.simple_spinner_item);
        spinner.setAdapter(arrayAdapter);

    }

    private void showPermissions() {
        for (final DataSnapshot s : allPermissions) {

            if (!alertsShownKeys.contains(s.getKey()) && s.child("connection").getValue() != null) {
                alertsShownKeys.add(s.getKey());

                final String email = s.child("email").getValue().toString();
                final String connection = s.child("connection").getValue().toString();
                final String name = s.child("name").getValue().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(name + " accepted your request");

// Set up the buttons
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!allUsers.contains(email)) {
                            Toast.makeText(Profile.this,
                                    "user doesn't exists anymore",
                                    Toast.LENGTH_LONG).show();
                            s.getRef().removeValue();
                            return;
                        }
                        if (connection.equals("parent")) {
                            DatabaseReference newMember = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.public_usersDB))
                                    .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                    .child("fam")
                                    .child("parents").push();

                            newMember.setValue(email);
                        } else if (connection.equals("kid")) {
                            DatabaseReference newMember = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.public_usersDB))
                                    .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                    .child("fam")
                                    .child("kids").push();

                            newMember.setValue(email);

                            //TODO sendKid_newParentRequest(email, partner);

                        } else if (connection.equals("kid")) {
                            DatabaseReference newMember = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.public_usersDB))
                                    .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                    .child("fam")
                                    .child("parents").push();

                            newMember.setValue(email);
                        } else {
                            DatabaseReference newMember = FirebaseDatabase.getInstance().getReference()
                                    .child(getString(R.string.public_usersDB))
                                    .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()))
                                    .child("fam")
                                    .child("partner"); //TODO add date to know who's the current partner

                            newMember.setValue(email);
                        }

                        allPermissions.remove(s);
                        s.getRef().removeValue();
                    }
                });

                builder.show();
            }
        }
    }

    private void setPrivacy(int i, String privacy) {

        String[] titles = {"phone", "email", "date", "city", "adress", "name"};
        String[] data = {profileData.getPhone(), profileData.getEmail(), profileData.getDate(), profileData.getCity(), profileData.getAddress(), profileData.getName()};

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();
        databaseReference = databaseReference.child("publicUsers").child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail())).child("personalData");
        if (privacy.equals("Only me"))
            databaseReference.child(titles[i]).setValue("");
        else if (privacy.equals("My family"))
            databaseReference.child(titles[i]).setValue("%f" + data[i]);
        else if (privacy.equals("Everyone"))
            databaseReference.child(titles[i]).setValue(data[i]);
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
            setCurrentUserToMyUser();

    }

    private void setDataUpdaterFromDataBase() {
        if (!shownUsrIsCurrentUsr) {
            //a different user
            return;
        }
        FirebaseDatabase databaseRef = FirebaseDatabase.getInstance();
        DatabaseReference myRef = databaseRef
                .getReference("users")
                .child(currentUser)
                .child("personalData");
        ValueEventListener valueEventListener = new ValueEventListener() {
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
        };
        myRef.addValueEventListener(valueEventListener);
        valueEventListenerAndRefLinkedList.add(new ValueEventListenerAndRef(myRef, valueEventListener));


    }

    //will work only if shownUsrIsCurrentUsr == true
    private void setDataUpdaterFromDataBase(DataSnapshot dataSnapshot) {
        profileData.setName(dataSnapshot.getValue(ProfileModel.class).getName());
        profileData.setAddress(dataSnapshot.getValue(ProfileModel.class).getAddress());
        profileData.setCity(dataSnapshot.getValue(ProfileModel.class).getCity());
        profileData.setDate(dataSnapshot.getValue(ProfileModel.class).getDate());
        profileData.setEmail(dataSnapshot.getValue(ProfileModel.class).getEmail());
        profileData.setPhone(dataSnapshot.getValue(ProfileModel.class).getPhone());
        refreshFromDataUpdater();
    }

    private void setPublicDataUpdaterFromDataBase() {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(getString(R.string.public_usersDB));
        String mail = currentUser;

        if (shownUsrIsCurrentUsr)
            mail = mAuth.getCurrentUser().getEmail();

        myRef = myRef.child(prepareStringToDataBase(mail)).child(getString(R.string.personal_dataDB));

        ValueEventListener valueEventListener = new ValueEventListener() {
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
        };
        myRef.addValueEventListener(valueEventListener);
        valueEventListenerAndRefLinkedList.add(new ValueEventListenerAndRef(myRef, valueEventListener));
    }

    private void setRequestsSentToListFromDataBase(DataSnapshot dataSnapshot) {
        requestSentTo = new LinkedList<>();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            requestSentTo.add(ds.getValue().toString());
        }
    }

    private void setPublicDataUpdaterFromDataBase(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() == null)
            return;
        publicData[PHONE_INDEX] = dataSnapshot.getValue(ProfileModel.class).getPhone();
        publicData[EMAIL_INDEX] = dataSnapshot.getValue(ProfileModel.class).getEmail();
        publicData[BDAY_INDEX] = dataSnapshot.getValue(ProfileModel.class).getDate();
        publicData[CITY_INDEX] = dataSnapshot.getValue(ProfileModel.class).getCity();
        publicData[ADDRESS_INDEX] = dataSnapshot.getValue(ProfileModel.class).getAddress();
        publicData[NAME_INDEX] = dataSnapshot.getValue(ProfileModel.class).getName();

        saveChangesInTextViews();

        setPrivacySpinners();
    }

    private void refreshFromDataUpdater() {
        refreshFirstTime();
        changeVisibility();
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

        setPrivacy(NAME_INDEX, selectionsOptions[2]);
        setPrivacy(PHONE_INDEX, phoneSpinner.getSelectedItem().toString());
        setPrivacy(EMAIL_INDEX, selectionsOptions[2]);
        setPrivacy(BDAY_INDEX, bdaySpinner.getSelectedItem().toString());
        setPrivacy(CITY_INDEX, citySpinner.getSelectedItem().toString());
        setPrivacy(ADDRESS_INDEX, addressSpinner.getSelectedItem().toString());
    }

    private void refreshEditMode(boolean hasChangedData) {
        changeVisibility();
        changeIcon();
        if (hasChangedData) {
            saveChangesInDataBase();
            savePrivacyInDatabase();
            savePhotoInDataBase();
        } else {
            setEditTextFromDataBase();
            setPrivacySpinners();
        }
        saveChangesInTextViews();

    }

    private void savePhotoInDataBase() {
        if (!replacedPhoto)
            return;
        replacedPhoto = false;
        bUploadingPhoto = true;
        final ProgressBar profileImageProgressBar = findViewById(R.id.profileImageProgressBar);
        profileImageProgressBar.setVisibility(View.VISIBLE);
        profileImageProgressBar.setEnabled(true);
        profileImage.setDrawingCacheEnabled(true);
        profileImage.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(getString(R.string.profile_images))
                .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()) + ".jpg");
        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                bUploadingPhoto = false;
                goTo(nextClass);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                profileImageProgressBar.setEnabled(false);
                profileImageProgressBar.setVisibility(View.GONE);
                bUploadingPhoto = false;
                goTo(nextClass);
            }


        });

    }

    private void setPrivacySpinners() {
        if (shownUsrIsCurrentUsr) {
            setSelectionPrivacySpinnersPrivate(phoneSpinner, publicData[0]);
            setSelectionPrivacySpinnersPrivate(bdaySpinner, publicData[2]);
            setSelectionPrivacySpinnersPrivate(citySpinner, publicData[3]);
            setSelectionPrivacySpinnersPrivate(addressSpinner, publicData[4]);
        } else {
            phoneSpinner.setVisibility(View.GONE);
            bdaySpinner.setVisibility(View.GONE);
            citySpinner.setVisibility(View.GONE);
            addressSpinner.setVisibility(View.GONE);
        }

    }

    private void setSelectionPrivacySpinnersPrivate(Spinner spinner, String spinnerSelection) {
        if (spinnerSelection.equals(""))
            spinner.setSelection(0);
        else if (spinnerSelection.substring(0, 2).equals("%f"))
            spinner.setSelection(1);
        else
            spinner.setSelection(2);
    }

    private void refreshFirstTime() {
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
                .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail()))
                .child(getString(R.string.personal_dataDB));

        ref.child("phone").setValue("%f" + profileData.getPhone());
        ref.child("email").setValue("%f" + profileData.getEmail());
        ref.child("date").setValue("%f" + profileData.getDate());
        ref.child("city").setValue("%f" + profileData.getCity());
        ref.child("adress").setValue("%f" + profileData.getAddress());
    }

    private void changeVisibility() {
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

        findViewById(R.id.replaceProfileImage_Button_).setVisibility(editTextVisibality);
        goToHomeBtn.setVisibility(textVisibality);

        phoneSpinner.setVisibility(editTextVisibality);
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
        if (!shownUsrIsCurrentUsr) {
            TextDataArray[NAME_INDEX].setText(getPublicField(NAME_INDEX));
            TextDataArray[PHONE_INDEX].setText(getPublicField(PHONE_INDEX));
            TextDataArray[EMAIL_INDEX].setText(getPublicField(EMAIL_INDEX));
            TextDataArray[BDAY_INDEX].setText(getPublicField(BDAY_INDEX));
            TextDataArray[CITY_INDEX].setText(getPublicField(CITY_INDEX));
            TextDataArray[ADDRESS_INDEX].setText(getPublicField(ADDRESS_INDEX));
            return;
        }
        TextDataArray[NAME_INDEX].setText(profileData.getName());
        TextDataArray[PHONE_INDEX].setText(profileData.getPhone());
        TextDataArray[EMAIL_INDEX].setText(profileData.getEmail());
        TextDataArray[BDAY_INDEX].setText(profileData.getDate());
        TextDataArray[CITY_INDEX].setText(profileData.getCity());
        TextDataArray[ADDRESS_INDEX].setText(profileData.getAddress());
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
        EditTextDataArray[5].setText(profileData.getAddress());
    }

    public String getPersonalDataFromEditTexts(int i) {
        if (i < 0 || i > 5) {
            Toast.makeText(this, "problem in code!!!", Toast.LENGTH_LONG).show();
            return "___BUG________";
        }
        return EditTextDataArray[i].getText().toString();

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
        TextView[] TV_id_s = {findViewById(R.id.PhoneTextV),
                findViewById(R.id.EmailTextV),
                findViewById(R.id.BdayTextV),
                findViewById(R.id.cityTextV),
                findViewById(R.id.AdressTextV),
                findViewById(R.id.NameTextV)};

        for (int i = 0; i < NUMBER_OF_PARAMETERS; i++) {
            TextDataArray[i] = TV_id_s[i];
        }

        findViewById(R.id.NameTextV).setSelected(true);
    }

    private void setRequestButtonsArray() {
        Button[] B_id_s =
                {
                        findViewById(R.id.addChildBtn),
                        findViewById(R.id.addParentBtn),
                        findViewById(R.id.addPartnerBtn)
                };

        for (int i = 0; i < B_id_s.length; i++) {
            RequestButtonsArray[i] = B_id_s[i];
        }
    }

    public void onOffEditMode(View view) {
        if (!shownUsrIsCurrentUsr)
            return;
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
        EDIT_MODE = !EDIT_MODE;
        refreshEditMode(!EDIT_MODE);
    }

    private void viBtnMoveOn(Class s) {
        if (!shownUsrIsCurrentUsr)
            goTo(s);

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
        for (EditText ET : EditTextDataArray) {
            if (ET.getText().toString().equals(""))
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

    public void goToHomeAttempt(View view) {
        nextClass = HomeScreen.class;
        if (shownUsrIsCurrentUsr && !allFieldsCompleted())
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
        if (bUploadingPhoto) {
            nextClass = S;
            return;
        }
        Intent intent = new Intent(this, S);
        startActivity(intent);
    }

    private String getPublicField(int index) {
        if (index < 0 || index >= NUMBER_OF_PARAMETERS)
            return null;

        LinkedList<String> mailList = new LinkedList<>();
        for (ProfileModel pm : getFamilyMembers(mAuth.getCurrentUser().getEmail(), 1)) {
            mailList.add(pm.getEmail());
        }

        if (publicData[index] == null
                || publicData[index].length() == 0
                || (!shownUsrIsCurrentUsr && publicData[index].startsWith("%f") && !mailList.contains(currentUser))
        ) {
            return "";
        }

        if (publicData[index].startsWith("%f"))
            return publicData[index].substring(2);

        return publicData[index];
    }

    //functionality:
    public void sendEmail(View view) {

        if (EDIT_MODE)
            return;

        String mail = TextDataArray[EMAIL_INDEX].getText().toString();

        Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
        mailIntent.setType("*/*").setData(Uri.parse("mailto:" + mail))
                .putExtra(Intent.EXTRA_SUBJECT, "subject");
        if (mailIntent.resolveActivity(getPackageManager()) != null)
            startActivity(mailIntent);
    }

    public void call(View view) {
        if (EDIT_MODE)
            return;

        String phoneNumber = TextDataArray[PHONE_INDEX].getText().toString();
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

        String city = TextDataArray[CITY_INDEX].getText().toString(),
                address = TextDataArray[ADDRESS_INDEX].getText().toString();

        if (city.length() == 0 && address.length() == 0) {
            return;
        }

        city = setStringsCompatibleWithUri(city);
        address = setStringsCompatibleWithUri(address);

        if (address.length() != 0 && city.length() != 0)
            address = "+" + address;

        Uri geoLocation = Uri.parse("geo:0,0?q=" + city + address);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

    }

    private static String setStringsCompatibleWithUri(String s) {
        if (s.length() == 0) {
            return "";
        }
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
        if (!shownUsrIsCurrentUsr) {

            sendFather_newKidRequest(currentUser, mAuth.getCurrentUser().getEmail());

            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fill in your parents email:");

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
                sendFather_newKidRequest(input.getText().toString(), mAuth.getCurrentUser().getEmail());
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

    public boolean isRequestToUserPermitted(String user_mail){
        if (!allUsers.contains(user_mail)) {
            Toast.makeText(Profile.this,
                    "User doesn't exists",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (requestSentTo.contains(user_mail)) {
            Toast.makeText(Profile.this,
                    "User already has a pending request.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (user_mail.equals(partner)){
            Toast.makeText(Profile.this,
                    "User is already your partner.",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (parents.contains(user_mail)){
            Toast.makeText(Profile.this,
                    "User is already your parent.",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (kids.contains(user_mail)){
            Toast.makeText(Profile.this,
                    "User is already your kid.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        LinkedList<ProfileModel> brothersModels = get(currentUser, famFields.brothers);
        for (ProfileModel pm : brothersModels) {
            if (pm.getEmail().equals(mAuth.getCurrentUser().getEmail())) {
                Toast.makeText(Profile.this,
                        "User is already your sibling.",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

    public void sendFather_newKidRequest(String mail, String senderMail) {
        if (!isRequestToUserPermitted(mail))
            return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.public_usersDB))
                .child(prepareStringToDataBase(mail))
                .child("fam")
                .child("requests").push();
        ref.child("email")
                .setValue(mAuth.getCurrentUser().getEmail());
        ref.child("connection")
                .setValue("kid");
        ref.child("name")
                .setValue(profileData.getName());

        addRequestSentTo(mail, senderMail);
        Toast.makeText(Profile.this, "request sent to " + mail, Toast.LENGTH_LONG).show();
    }

    public void addKid(View view) {
        if (!shownUsrIsCurrentUsr) {
            sendKid_newParentRequest(currentUser, mAuth.getCurrentUser().getEmail());
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fill in your kids email:");

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
                sendKid_newParentRequest(input.getText().toString(), mAuth.getCurrentUser().getEmail());
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

    public void sendKid_newParentRequest(String mail, String senderMail) {
        if (!isRequestToUserPermitted(mail))
            return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.public_usersDB))
                .child(prepareStringToDataBase(mail))
                .child("fam")
                .child("requests").push();
        ref.child("email")
                .setValue(mAuth.getCurrentUser().getEmail());
        ref.child("connection")
                .setValue("parent");
        ref.child("name")
                .setValue(profileData.getName());

        addRequestSentTo(mail, senderMail);
        Toast.makeText(Profile.this, "request sent to " + mail, Toast.LENGTH_LONG).show();
    }

    public void addPartner(View view) {
        if (!shownUsrIsCurrentUsr) {
            sendPartner_newPartnerRequest(currentUser, mAuth.getCurrentUser().getEmail());
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fill in your partners email:");

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
                sendPartner_newPartnerRequest(input.getText().toString(), mAuth.getCurrentUser().getEmail());
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

    public void sendPartner_newPartnerRequest(String mail, String senderMail) {
        if (!isRequestToUserPermitted(mail))
            return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.public_usersDB))
                .child(prepareStringToDataBase(mail))
                .child("fam")
                .child("requests").push();
        ref.child("email")
                .setValue(senderMail);
        ref.child("connection")
                .setValue("partner");
        if (senderMail.equals(mAuth.getCurrentUser().getEmail()))
            ref.child("name").setValue(profileData.getName());
        else
            ref.child("name").setValue(senderMail.substring(0,senderMail.indexOf('@')));

        addRequestSentTo(mail, senderMail);
        Toast.makeText(Profile.this, "request sent to " + mail, Toast.LENGTH_LONG).show();
    }

    private void addRequestSentTo(String mail, String senderMail) {
        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.public_usersDB))
                .child(prepareStringToDataBase(senderMail))
                .child("fam").child("requestsSentTo")
                .child(prepareStringToDataBase(mail)).setValue(mail);
    }

    private void removeRequestSentTo(String mail) {
        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.public_usersDB))
                .child(prepareStringToDataBase(mail))
                .child("fam").child("requestsSentTo")
                .child(prepareStringToDataBase(mAuth.getCurrentUser().getEmail())).removeValue();
    }

    public void replaceProfileImage(View view) {
        replacedPhoto = true;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK) {
                    if (requestCode == 100) {
                        // Get the url from data
                        final Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            // Get the path from the Uri
                            String path = getPathFromURI(selectedImageUri);
                            Log.i("TAG", "Image Path : " + path);
                            // Set the image in ImageView
                            findViewById(R.id.profile_imageView).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ImageView) findViewById(R.id.profile_imageView)).setImageURI(selectedImageUri);
                                }
                            });

                        }
                    }
                }
            }
        }).start();
    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
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

