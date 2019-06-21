package com.example.sembi.logingui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

public class homeScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final long SMALL_ANIMATIONS_DURATION = 300;

    private static int currentId;
    private TextView navFullName, navNextEvent;
    private ImageView navProfilePic;
    private Post newPost;
    private Uri newPostImagePath;

    //feedView
    private LinkedList<DatabaseReference> weeksToShowRefs;
    private LinkedList<String> weeksToShowStrings;
    private LinkedList<String> allWeeks;


    private FirebaseDatabase mDatabase;
    private ListView mFeedPostsList;
    private ArrayList<Post> records;
    private HomeScreenFeedPostUIListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        newPostImagePath = Uri.parse("android.resource://com.example.sembi.logingui/" + R.drawable.ok_hand);
        ((ImageView) findViewById(R.id.newPost_imgPreviewIV)).setImageURI(newPostImagePath);

        currentId = -1;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        navFullName = headerView.findViewById(R.id.nav_fullName);

        setNav();


        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.homeScreen_feedSwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setFeed();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        mFeedPostsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount - (firstVisibleItem + visibleItemCount) == 0) {
                    int i = weeksToShowStrings.size();
                    if (i == allWeeks.size())
                        return;
                    weeksToShowStrings = new LinkedList<>();
                    for (int j = 0; j <= i; j++) {
                        weeksToShowStrings.add(allWeeks.get(j));
                    }
                }
            }
        });

        setFeed();
    }

    private void setFeed() {
        //connecting the DB
        mDatabase = FirebaseDatabase.getInstance();
        //connecting the layout
        mFeedPostsList = findViewById(R.id.homeScreenListView);


        Calendar c = Calendar.getInstance();

        weeksToShowRefs = new LinkedList<>();
        for (String week : weeksToShowStrings) {
            weeksToShowRefs.add(
                    mDatabase.getReference().child(getString(R.string.public_usersDB)).child(Profile.prepareStringToDataBase(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                            .child(getString(R.string.userFeedDB))
                            .child(week)
            );
            //week: c.get(Calendar.YEAR) + "," + c.get(Calendar.MONTH) + "," + c.get(Calendar.WEEK_OF_MONTH)
        }

        records = new ArrayList<>();
        adapter = new HomeScreenFeedPostUIListAdapter(this, records);

        mFeedPostsList.setAdapter(adapter);

        for (DatabaseReference ref : weeksToShowRefs) {
            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    collectAllRecords(dataSnapshot);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    collectAllRecords(dataSnapshot);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    collectAllRecords(dataSnapshot);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    collectAllRecords(dataSnapshot);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        DatabaseReference myFeedRef = mDatabase.getReference().child(getString(R.string.public_usersDB)).child(Profile.prepareStringToDataBase(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                .child(getString(R.string.userFeedDB));

        myFeedRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                collectAllWeeks(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                collectAllWeeks(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                collectAllWeeks(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                collectAllWeeks(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void collectAllWeeks(DataSnapshot dataSnapshot) {
        allWeeks = new LinkedList<>();
        for (DataSnapshot d : dataSnapshot.getChildren()) {
            allWeeks.add(d.getKey());
        }
        allWeeks.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int[] iArr1 = getWeekArr(o1), iArr2 = getWeekArr(o2);
                if (iArr1[0] != iArr2[0])
                    return iArr1[0] - iArr2[0];

                if (iArr1[1] != iArr2[1])
                    return iArr1[1] - iArr2[1];

                if (iArr1[2] != iArr2[2])
                    return iArr1[2] - iArr2[2];

                return 0;
            }
        });
    }

    private void collectAllRecords(DataSnapshot dataSnapshot) {
        for (DataSnapshot entry : dataSnapshot.getChildren()) {

            String mPublisherStr = entry.child("mPublisherStr").getValue(String.class);
            String mContentStr = entry.child("mContentStr").getValue(String.class);
            DateReadyForDB dateReadyForDB = entry.child(getString(R.string.userFeedDateDB)).getValue(DateReadyForDB.class);
            String mLinkStr = entry.child("mLinkStr").getValue(String.class);

            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(dateReadyForDB.getYear()), Integer.parseInt(dateReadyForDB.getMonth())
                    , Integer.parseInt(dateReadyForDB.getDay()), Integer.parseInt(dateReadyForDB.getHour())
                    , Integer.parseInt(dateReadyForDB.getMinute()), Integer.parseInt(dateReadyForDB.getSecond()));
            c.set(Calendar.MILLISECOND, Integer.parseInt(dateReadyForDB.getMillisecond()));

            records.add(new Post(mPublisherStr, mContentStr, c.getTime(), mLinkStr));

        }

        records.sort(new Comparator<Post>() {
            @Override
            public int compare(Post o1, Post o2) {
                Calendar c1 = Calendar.getInstance(), c2 = Calendar.getInstance();
                c1.setTime(o1.getmPublishDate());
                c2.setTime(o2.getmPublishDate());

//                if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
//                    return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
//                if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
//                    return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
//                if (c1.get(Calendar.DAY_OF_MONTH) != c2.get(Calendar.DAY_OF_MONTH))
//                    return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
//                if (c1.get(Calendar.HOUR_OF_DAY) != c2.get(Calendar.HOUR_OF_DAY))
//                    return c1.get(Calendar.HOUR_OF_DAY) - c2.get(Calendar.HOUR_OF_DAY);
//                if (c1.get(Calendar.MINUTE) != c2.get(Calendar.MINUTE))
//                    return c1.get(Calendar.MINUTE) - c2.get(Calendar.MINUTE);
//                if (c1.get(Calendar.SECOND) != c2.get(Calendar.SECOND))
//                    return c1.get(Calendar.SECOND) - c2.get(Calendar.SECOND);
//                if (c1.get(Calendar.MILLISECOND) != c2.get(Calendar.MILLISECOND))
//                    return c1.get(Calendar.MILLISECOND) - c2.get(Calendar.MILLISECOND);

                return c1.compareTo(c2);
            }
        });

    }

    private int[] getWeekArr(String week) {
        int[] weekArr = new int[3];
        weekArr[0] = Integer.parseInt(week.substring(0, 4));
        if (week.charAt(6) == ',') {
            weekArr[1] = Integer.parseInt(week.substring(5, 6));
        } else if (week.charAt(7) == ',') {
            weekArr[1] = Integer.parseInt(week.substring(5, 7));
        } else {
            throw new RuntimeException("Week string isn't legal: not in format yyyy_m_w or yyyy_mm_w");
        }
        weekArr[2] = week.charAt(week.length() - 1);

        return weekArr;
    }

    public void insertLink(View view) {
        fadeView(
                findViewById(R.id.newPost_linkPasteET)
                , FADE.in
        );

        fadeView(
                findViewById(R.id.newPost_addLinkBtn)
                , FADE.in
        );
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (findViewById(R.id.newPost_linkPasteET).getAlpha() != 0.0f) {
            Animation a1 = findViewById(R.id.newPost_linkPasteET).getAnimation(),
                    a2 = findViewById(R.id.newPost_addLinkBtn).getAnimation();

            if (a1 != null)
                a1.cancel();
            if (a2 != null)
                a2.cancel();

            fadeView(
                    findViewById(R.id.newPost_linkPasteET)
                    , FADE.out
            );
            fadeView(
                    findViewById(R.id.newPost_addLinkBtn)
                    , FADE.out
            );

        } else if (findViewById(R.id.greyScreen).getAlpha() != 0) {

            fadeView(
                    findViewById(R.id.greyScreen)
                    , FADE.out);
            fadeView(
                    findViewById(R.id.newPostContainer)
                    , FADE.out);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.newPostContainer).setVisibility(View.GONE);
                }
            }, SMALL_ANIMATIONS_DURATION / 2);

        } else {
            super.onBackPressed();
        }
    }

    public void insertImage(View view) {
        //TODO
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {


        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_members && id != currentId) {
            startActivity(new Intent(this, FamilyMembers.class));
        } else if (id == R.id.nav_events && id != currentId) {

            startActivity(new Intent(this, MedicalRecords.class));
//            inflate(R.layout.activity_event);
//            currentId = id;
        } else if (id == R.id.nav_new_event && id != currentId) {
            startActivity(new Intent(this, Event.class));
        } else if (id == R.id.nav_tree && id != currentId) {
            startActivity(new Intent(this, FamilyTree.class));
//            inflate(R.layout.activity_family_tree);
//            currentId = id;
        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "download the family organizer app from the Google Play Store!!");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_log_out) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        return true;
    }

    private void setNav() {
        setName();
    }

    private void setName() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser()
                        .getUid()).child("personalData").child("name");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                navFullName.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("error", "Failed to read value.", error.toException());
            }
        });
    }

    private void setpic() {
        //TODO
    }

    private void setNextEvent() {
        //TODO
    }

    public void goToProfile(View view) {
        Profile.EDIT_MODE = false;
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_new) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void newPost(View view) {
        findViewById(R.id.greyScreen).animate().alpha(0.3f).setDuration(SMALL_ANIMATIONS_DURATION);
        findViewById(R.id.newPostContainer).setAlpha(0.0f);
        findViewById(R.id.newPostContainer).setVisibility(View.VISIBLE);
        fadeView(
                findViewById(R.id.newPostContainer)
                , FADE.in);
        //TODO: init all fields
    }

    private void fadeView(View v, FADE fade) {
        if (fade.equals(FADE.in)) {
            v.animate().alpha(1.0f).setDuration(SMALL_ANIMATIONS_DURATION);
        } else if (fade.equals(FADE.out)) {
            v.animate().alpha(0.0f).setDuration(SMALL_ANIMATIONS_DURATION / 2);
        }

    }

    public void addLink(View view) {
        ((TextView) findViewById(R.id.newPost_linkPreviewTV))
                .setText(
                        ((EditText) findViewById(R.id.newPost_linkPasteET))
                                .getText().toString()
                );
    }

    public void followLink(View view) {
        if (!(view instanceof TextView))
            return;
        String link = ((TextView) view).getText().toString();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        startActivity(intent);
    }

    public void createNewPost(View view) {
        newPost = new Post(FirebaseAuth.getInstance().getCurrentUser().getEmail()
                , ((EditText) findViewById(R.id.newPost_contentET)).getText().toString()
                , new Date()
                , ((TextView) findViewById(R.id.newPost_linkPreviewTV)).getText().toString());

        PostReadyForDB postReadyForDB = new PostReadyForDB(newPost);
        DateReadyForDB dateReadyForDB = new DateReadyForDB(newPost.getmPublishDate());

        DatabaseReference databasePublicUsersReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.public_usersDB));

        Calendar c = Calendar.getInstance();
        c.setTime(newPost.getmPublishDate());


        for (String mail : StaticMethods.getFamilyMembers(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
            DatabaseReference auxRef = databasePublicUsersReference.child(Profile.prepareStringToDataBase(mail)).child(getString(R.string.userFeedDB))
                    .child(dateReadyForDB.getYear() + "," + dateReadyForDB.getMonth() + "," + dateReadyForDB.getWeek()).child(dateReadyForDB.toString());
            auxRef.setValue(postReadyForDB);
            auxRef.child("date").setValue(dateReadyForDB);
        }

        FirebaseStorage.getInstance().getReference().child(getString(R.string.post_photos)).child(dateReadyForDB + "%" + newPost.getmPublisherStr())
                .putFile(newPostImagePath);

    }

    private enum FADE {
        in,
        out
    }


//    public int pos = 0; //saves the curent index of border in the array
//    public int[] borderArr = {R.drawable.ok_hand, R.drawable.done, R.drawable.thumbs_down_64, R.drawable.thumbs_up_64};
//    public ImageView IV = findViewById(R.id.imageView);
//    public void changeBorder(View view){
//        pos = (pos+1)%borderArr.length;
//        IV.setImageDrawable(getDrawable(borderArr[pos]));
//    }

    public class HomeScreenFeedPostUIListAdapter extends ArrayAdapter<Post> {

        private Context mContext;
        private ArrayList<Post> records;

        public HomeScreenFeedPostUIListAdapter(@NonNull Context context, ArrayList<Post> records) {
            super(context, 0, records);
            mContext = context;
            this.records = records;
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.feed_post_item_layout, null);

            }
            Post current = records.get(position);
            TextView name = listItem.findViewById(R.id.post_profileNameTV);
            TextView date = listItem.findViewById(R.id.post_dateTV);
            final TextView content = listItem.findViewById(R.id.post_contentTV);
            TextView reedMore = listItem.findViewById(R.id.post_reedMoreTV);
            ImageView profileImg = listItem.findViewById(R.id.post_profilePhotoIV);
            ImageView additionalImg = listItem.findViewById(R.id.post_additionalPhotoIV);

            name.setText(current.getmPublisherStr());
            content.setText(current.getmContentStr());
            Calendar c = Calendar.getInstance();
            c.setTime(current.getmPublishDate());

            date.setText(StaticMethods.DAYS[c.get(Calendar.DAY_OF_WEEK)] + ", " + StaticMethods.MONTHS[c.get(Calendar.MONTH)] + " " + c.get(Calendar.DAY_OF_MONTH) + ", " + c.get(Calendar.YEAR));
            if (content.getLineCount() > 5)
                reedMore.setVisibility(View.VISIBLE);

            reedMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    content.setMaxLines(100);
                }
            });


//            Uri uri = FirebaseStorage
//                    .getInstance()
//                    .getReference()
//                    .child(getString(R.string.post_photos))
//                    .child(current.getmImagePathStr())
//                    .getDownloadUrl()
//                    .getResult();
            //additionalImg.setImageURI(uri);

            //TODO take care of URI for profile
            //IV.setImageURI(current.getIV());
//            listItem.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //TODO Intents...... make Static,Public Boolean value for Toggle
//                }
//            });
            listItem.setClickable(false);
            return listItem;
        }
    }
}