package com.example.sembi.logingui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import static com.example.sembi.logingui.StaticMethods.prepareStringToDataBase;


public class UpcomingEvents extends AppCompatActivity {


    private FirebaseDatabase mDatabase;
    private ListView mUpcomingEventsList;
    private ArrayList<EventUIModel> records;
    private UpcomingEventsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_events);

        mDatabase = FirebaseDatabase.getInstance();
        mUpcomingEventsList = findViewById(R.id.events_listView);
        records = new ArrayList<>();
        adapter = new UpcomingEventsListAdapter(this, records);
        mUpcomingEventsList.setAdapter(adapter);

        DatabaseReference eventsReference = mDatabase.getReference(getString(R.string.public_usersDB))
                .child(StaticMethods.prepareStringToDataBase(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                .child(getString(R.string.userEventsDB));

        eventsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                collectAllRecords(dataSnapshot);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void collectAllRecords(DataSnapshot dataSnapshot) {
        records.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            EventUIModel eventUIModel = ds.getValue(EventUIModel.class);
            eventUIModel.setKey(ds.getKey());
            records.add(eventUIModel);
        }

    }

    public void homeClicked(View view) {
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }

    public void newEvent(View view) {
        startActivity(new Intent(this, Event.class));
    }


    public class UpcomingEventsListAdapter extends ArrayAdapter<EventUIModel> {

        private Context mContext;
        private ArrayList<EventUIModel> records;

        public UpcomingEventsListAdapter(@NonNull Context context, ArrayList<EventUIModel> records) {
            super(context, 0, records);
            mContext = context;
            this.records = records;
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.event_item_model, null);

            }
            final EventUIModel current = records.get(position);
            TextView titleTV = listItem.findViewById(R.id.headerEventItem);
            TextView hostTV = listItem.findViewById(R.id.hostEventItemTextView);
            TextView dateTV = listItem.findViewById(R.id.eventItem_date);
//            ImageView coming = listItem.findViewById(R.id.eventItemComingStateImageView);
            ImageView additionalImage = listItem.findViewById(R.id.eventItem_additionalImage);

            titleTV.setText(current.getEventName());
            if (StaticMethods.prepareStringToDataBase(current.getHostEmail())
                    .equals(StaticMethods.prepareStringToDataBase(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
            ) {
                hostTV.setText("By You");
            } else
                hostTV.setText("By " + StaticMethods.getProfileModel(current.getHostEmail()).getName());
            dateTV.setText(current.getDate());
//            switch (current.getComing()){
//                case (StaticMethods.coming):
//                    coming.setImageDrawable(getDrawable(R.drawable.thumbs_up_64));
//                case (StaticMethods.notComing):
//                    coming.setImageDrawable(getDrawable(R.drawable.thumbs_down_64));
//                case (StaticMethods.thinking):
//                    coming.setImageDrawable(getDrawable(R.drawable.thinking_50));
//            }

            //TODO addImage (not sure)
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(getString(R.string.profile_images))
                    .child(prepareStringToDataBase(current.getHostEmail()) + ".jpg");

            setImage(ref, additionalImage);
//        title.setText(current.getTitle());
//        content.setText(current.getContent());

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] arr = {current.getKey(), current.getHostEmail()};
                    Intent intent = new Intent(UpcomingEvents.this, Event.class);
                    intent.putExtra(getString(R.string.event_extra_tag), arr);
                    startActivity(intent);
                }
            });

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


        //TODO move to event
//        private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
//        String phoneNo;
//        String message;
//
//        protected void sendSMSMessage() {
//            if (ContextCompat.checkSelfPermission(UpcomingEvents.this,
//                    Manifest.permission.SEND_SMS)
//                    != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(UpcomingEvents.this,
//                        Manifest.permission.SEND_SMS)) {
//                } else {
//                    ActivityCompat.requestPermissions(UpcomingEvents.this,
//                            new String[]{Manifest.permission.SEND_SMS},
//                            MY_PERMISSIONS_REQUEST_SEND_SMS);
//                }
//            }
//        }
//
//        @Override
//        public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
//            switch (requestCode) {
//                case MY_PERMISSIONS_REQUEST_SEND_SMS: {
//                    if (grantResults.length > 0
//                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        SmsManager smsManager = SmsManager.getDefault();
//                        smsManager.sendTextMessage(phoneNo, null, message, null, null);
//                        Toast.makeText(getApplicationContext(), "SMS sent.",
//                                Toast.LENGTH_LONG).show();
//                    } else {
//                        Toast.makeText(getApplicationContext(),
//                                "SMS faild, please try again.", Toast.LENGTH_LONG).show();
//                        return;
//                    }
//                }
//            }
//
//        }
    }
}
