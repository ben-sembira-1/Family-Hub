package com.example.sembi.logingui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.internal.FirebaseAppHelper;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;


public class MedicalRecords extends AppCompatActivity {


    private FirebaseDatabase mDatabaseReference;
    private ListView mMedicalRecordsList;
    private ArrayList<MedicalRecordModel> records;
    private MedicalRecordsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_records);

        //connecting the DB
        mDatabaseReference = FirebaseDatabase.getInstance();
        //connecting the layout
        mMedicalRecordsList = findViewById(R.id.medicalRecordsListView);

        DatabaseReference medicalRecordsReference = mDatabaseReference.getReference("MedicalRecords");
        String recordId = medicalRecordsReference.push().getKey();
        MedicalRecordModel medicalRecordModel = new MedicalRecordModel("Test", "Test1");
        medicalRecordsReference.child(recordId).setValue(medicalRecordModel);
//        Query query = medicalRecordsReference.orderByKey();
        records = new ArrayList<>();
//        FirebaseListOptions<MedicalRecordModel> firebaseOptions = new FirebaseListOptions.Builder<MedicalRecordModel>()
//                .setLayout(R.layout.medical_record_item_layout)
//                .setQuery(query , MedicalRecordModel.class)
//                .build();
         adapter = new MedicalRecordsListAdapter(this, records);

        mMedicalRecordsList.setAdapter(adapter);
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
        DatabaseReference databaseReference = mDatabaseReference.getReference("MedicalRecords");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 collectAllRecords((Map<String,Object>)(dataSnapshot.getValue()));
                 adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void collectAllRecords(Map<String, Object> recordsMap) {
        for(Map.Entry<String,Object> entry : recordsMap.entrySet()){
            Map singleRecord = (Map) entry.getValue();
            String content = (String)singleRecord.get("content");
            String title = (String) singleRecord.get("title");
            records.add(new MedicalRecordModel(content,title));

        }

    }

    public void homeClicked(View view) {
        Intent intent = new Intent(this, homeScreen.class);
        startActivity(intent);
    }
}
