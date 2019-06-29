package com.example.sembi.logingui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import static com.example.sembi.logingui.StaticMethods.getFamilyMembers;
import static com.example.sembi.logingui.StaticMethods.prepareStringToDataBase;

public class FamilyMembers extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private ListView familyMembersList;
    private ArrayList<ProfileModel> records, allMyFam;
    private FamilyMembersListAdapter adapter;
    private EditText searchEditT;
    private String search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_members);

        mDatabase = FirebaseDatabase.getInstance();
        familyMembersList = findViewById(R.id.fam_members_list_view);
        records = new ArrayList<>();
        allMyFam = new ArrayList<>();
        search = "";
        searchEditT = findViewById(R.id.searchEditT);
        adapter = new FamilyMembersListAdapter(this, records);
        familyMembersList.setAdapter(adapter);

        DatabaseReference databaseReference = mDatabase.getReference(getString(R.string.public_usersDB));
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allMyFam.clear();
                allMyFam.addAll(getFamilyMembers(FirebaseAuth.getInstance().getCurrentUser().getEmail(), 2));

                for (ProfileModel pm : allMyFam)
                    if (pm.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        allMyFam.remove(pm);
                        break;
                    }

                setRecordsToShow();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        setSearchListener();
    }

    private void setSearchListener() {
        searchEditT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setRecordsToShow();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setRecordsToShow() {
        records.clear();
        for (ProfileModel pm : allMyFam) {
            if (pm.getName().toLowerCase().contains(searchEditT.getText().toString().toLowerCase()))
                records.add(pm);
        }
        TextView header = findViewById(R.id.members_header);
        header.setText("Family Members (" + (records.size() - 1) + ")");
        adapter.notifyDataSetChanged();
    }

    public void goToHomescreen(View view) {
        startActivity(new Intent(this, HomeScreen.class));
    }

    public class FamilyMembersListAdapter extends ArrayAdapter<ProfileModel> {

        private Context mContext;
        private ArrayList<ProfileModel> records;

        public FamilyMembersListAdapter(@NonNull Context context, ArrayList<ProfileModel> records) {
            super(context, 0, records);
            mContext = context;
            this.records = records;
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.family_member_item_layout, null);

            }
            final ProfileModel curr = records.get(position);
//        title.setText(current.getTitle());
//        content.setText(current.getContent());
            TextView name = listItem.findViewById(R.id.headerFamilyMember);
            TextView DOB = listItem.findViewById(R.id.eventItem_date);
            final ImageView userImage = listItem.findViewById(R.id.eventItem_additionalImage);

            name.setText(curr.getName());
            String date = curr.getDate();
            if (curr.getDate().startsWith("%f"))
                date = (curr.getDate().substring(2));

            int years = Calendar.getInstance().get(Calendar.YEAR) - Integer.parseInt(date.substring(date.length() - 4));
            if (Integer.parseInt(date.substring(3, 5)) > Calendar.getInstance().get(Calendar.MONTH)) {
                years--;
            } else if (Integer.parseInt(date.substring(3, 5)) == Calendar.getInstance().get(Calendar.MONTH)) {
                if (Integer.parseInt(date.substring(0, 2)) > Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                    years--;
                }
            }

            DOB.setText(date + " (" + years + " years old)");

            StorageReference ref = FirebaseStorage.getInstance().getReference().child(getString(R.string.profile_images))
                    .child(prepareStringToDataBase(curr.getEmail()) + ".jpg");

            setImage(ref, userImage);


            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(FamilyMembers.this, Profile.class);
                    intent.putExtra(getString(R.string.profile_extra_mail_tag), curr.getEmail());
                    startActivity(intent);
                }
            });
            listItem.setClickable(true);
            return listItem;
        }

        private void setImage(final StorageReference ref, final ImageView userImage) {
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get()
                            .load(uri)
                            .error(getDrawable(R.drawable.logo_with_white))
                            .into(userImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    userImage.setImageDrawable(getDrawable(R.drawable.logo_with_white));
                }
            });
        }
    }

}
