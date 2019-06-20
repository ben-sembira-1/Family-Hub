package com.example.sembi.logingui;

import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class FamilyMembers extends AppCompatActivity {
    private FirebaseDatabase mDatabase;
    private ListView familyMembersList;
    private ArrayList<MedicalRecordModel> records;
    private FamilyMembersListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_members);

        //connecting the DB
        mDatabase = FirebaseDatabase.getInstance();
        //connecting the layout
        familyMembersList = findViewById(R.id.fam_members_list_view);

        DatabaseReference medicalRecordsReference = mDatabase.getReference("MedicalRecords");
        String recordId = medicalRecordsReference.push().getKey();
        MedicalRecordModel medicalRecordModel = new MedicalRecordModel("Test", "Test1");
        medicalRecordsReference.child(recordId).setValue(medicalRecordModel);
//        Query query = medicalRecordsReference.orderByKey();
        records = new ArrayList<>();
//        FirebaseListOptions<MedicalRecordModel> firebaseOptions = new FirebaseListOptions.Builder<MedicalRecordModel>()
//                .setLayout(R.layout.medical_record_item_layout)
//                .setQuery(query , MedicalRecordModel.class)
//                .build();
        adapter = new FamilyMembersListAdapter(this, records);

        familyMembersList.setAdapter(adapter);
//        mMedicalRecordsList.setAdapter(new FirebaseListAdapter<MedicalRecordModel>(firebaseOptions) {
//
//            @Override
//            protected void populateView(View v, MedicalRecordModel model, int position) {
//                TextView title = v.findViewById(R.id.headerMedicalRecord);
//                TextView content = v.findViewById(R.id.dataMedicalRecord);
//                title.setText(model.getTitle());
//                content.setText(model.getContent());
//
//
//            }
//        });
        DatabaseReference databaseReference = mDatabase.getReference("MedicalRecords");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                collectAllRecords((Map<String, Object>) (dataSnapshot.getValue()));
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void collectAllRecords(Map<String, Object> recordsMap) {
        for (Map.Entry<String, Object> entry : recordsMap.entrySet()) {
            Map singleRecord = (Map) entry.getValue();
            String content = (String) singleRecord.get("content");
            String title = (String) singleRecord.get("title");
            records.add(new MedicalRecordModel(content, title));

        }

    }
}
