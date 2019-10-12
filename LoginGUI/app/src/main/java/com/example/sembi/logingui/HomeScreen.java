package com.example.sembi.logingui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;

import static com.example.sembi.logingui.StaticMethods.ADDRESS_INDEX;
import static com.example.sembi.logingui.StaticMethods.BDAY_INDEX;
import static com.example.sembi.logingui.StaticMethods.CITY_INDEX;
import static com.example.sembi.logingui.StaticMethods.DAYS;
import static com.example.sembi.logingui.StaticMethods.EMAIL_INDEX;
import static com.example.sembi.logingui.StaticMethods.MONTHS;
import static com.example.sembi.logingui.StaticMethods.NAME_INDEX;
import static com.example.sembi.logingui.StaticMethods.PHONE_INDEX;
import static com.example.sembi.logingui.StaticMethods.getFamilyMembers;
import static com.example.sembi.logingui.StaticMethods.prepareStringToDataBase;
import static com.example.sembi.logingui.StaticMethods.setPublicUsersListener;
import static com.example.sembi.logingui.StaticMethods.valueEventListenerAndRefLinkedList;

public class HomeScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final long SMALL_ANIMATIONS_DURATION = 300;

    private static int currentId;
    private TextView navFullName;
    private ImageView navProfilePic;
    private Post newPost;
    private int circles;
    private StorageReference allPostsRef;

    //feedView
    private LinkedList<DatabaseReference> weeksToShowRefs;
    private LinkedList<String> allWeeks;


    private FirebaseDatabase mDatabase;
    private ListView mFeedPostsList;
    private ArrayList<Post> records;
    private HomeScreenFeedPostUIListAdapter adapter;
    private ImageView newPostImage;
    private boolean isWritingNewPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        isWritingNewPost = false;
        newPostImage = findViewById(R.id.newPost_imgPreviewIV);
        newPostImage.setImageDrawable(getDrawable(R.drawable.logo_with_white));
        allPostsRef = FirebaseStorage.getInstance().getReference().child(getString(R.string.post_images));
        circles = 0;
        currentId = -1;

        setPublicUsersListener();

        weeksToShowRefs = new LinkedList<>();
        allWeeks = new LinkedList<>();

        mDatabase = FirebaseDatabase.getInstance();

        mFeedPostsList = findViewById(R.id.homeScreenListView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NumberPicker numberPicker = findViewById(R.id.numberPicker2);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(10);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                circles = newVal;
            }
        });


        FloatingActionButton fab = findViewById(R.id.fab);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        navFullName = headerView.findViewById(R.id.nav_fullName);
        navProfilePic = headerView.findViewById(R.id.nav_profileImageView);

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(getString(R.string.profile_images))
                .child(prepareStringToDataBase(FirebaseAuth.getInstance().getCurrentUser().getEmail()) + ".jpg");

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                int resizeTO = 180;
                Picasso.get()
                        .load(uri)
                        .error(getDrawable(R.drawable.logo))
                        .into(navProfilePic);

//        Glide.with(this /* context */)
//                .load(ref)
//                .into(navProfilePic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                navProfilePic.setImageDrawable(getDrawable(R.drawable.logo_with_white));
                navProfilePic.setAdjustViewBounds(true);
            }
        });


        setNav();

        mFeedPostsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if (totalItemCount - (firstVisibleItem + visibleItemCount) == 0) {
//                    int i = weeksToShowStrings.size();
//                    if (i == allWeeks.size())
//                        return;
//                    weeksToShowStrings = new LinkedList<>();
//                    for (int j = 0; j <= i; j++) {
//                        weeksToShowStrings.add(allWeeks.get(j));
//                    }
//                }
//                setFeed();
            }
        });

        DatabaseReference myFeedRef = mDatabase.getReference().child(getString(R.string.public_usersDB)).child(prepareStringToDataBase(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                .child(getString(R.string.userFeedDB));

        myFeedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                collectAllWeeks(dataSnapshot);
                setFeed();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.homeScreen_feedSwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setFeed();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setFeed() {
        //connecting the DB
        mDatabase = FirebaseDatabase.getInstance();
        //connecting the layout
        mFeedPostsList = findViewById(R.id.homeScreenListView);


        Calendar c = Calendar.getInstance();

        weeksToShowRefs = new LinkedList<>();
        for (String week : allWeeks) {
            weeksToShowRefs.add(
                    mDatabase.getReference().child(getString(R.string.public_usersDB)).child(prepareStringToDataBase(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                            .child(getString(R.string.userFeedDB))
                            .child(week)
            );
            //week: c.get(Calendar.YEAR) + "," + c.get(Calendar.MONTH) + "," + c.get(Calendar.WEEK_OF_MONTH)
        }

        records = new ArrayList<>();
        adapter = new HomeScreenFeedPostUIListAdapter(this, records);

        mFeedPostsList.setAdapter(adapter);

        for (DatabaseReference ref : weeksToShowRefs) {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (collectAllRecords(dataSnapshot))
                        adapter.notifyDataSetChanged(); //TODO add in settings: auto update (like this)
                    //TODO or update with pullToRefresh (move this to pullToRefresh listener)
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }

    private void collectAllWeeks(DataSnapshot dataSnapshot) {
        allWeeks = new LinkedList<>();
        for (DataSnapshot d : dataSnapshot.getChildren()) {
            allWeeks.add(d.getKey());
        }
        allWeeks.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int[] iArr1 = getWeekArr(o2), iArr2 = getWeekArr(o1);
                if (iArr1[0] != iArr2[0])
                    return iArr2[0] - iArr1[0];

                if (iArr1[1] != iArr2[1])
                    return iArr2[1] - iArr1[1];

                if (iArr1[2] != iArr2[2])
                    return iArr2[2] - iArr1[2];

                return 0;
            }
        });

    }

    private void shrinkFeedLinkedList(ArrayList<Post> allPosts) {
        ArrayList<Post> shrinked = new ArrayList<>();
        ArrayList<String> shrinked_Strings = new ArrayList<>();
        for (Post p : allPosts) {
            if (!shrinked_Strings.contains("" + p.getmPublishDate().getTime())) {
                shrinked.add(p);
                shrinked_Strings.add("" + p.getmPublishDate().getTime());
            }
        }
        allPosts.clear();
        allPosts.addAll(shrinked);
    }

    private Boolean collectAllRecords(DataSnapshot dataSnapshot) {
        for (DataSnapshot entry : dataSnapshot.getChildren()) {

            String mPublisherStr = entry.child("mPublisherStr").getValue(String.class);
            String mContentStr = entry.child("mContentStr").getValue(String.class);
            DateReadyForDB dateReadyForDB = entry
                    .child(getString(R.string.userFeedDateDB))
                    .getValue(DateReadyForDB.class);
            if (dateReadyForDB == null)
                return false;
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

                return -1 * c1.compareTo(c2);
            }
        });

        shrinkFeedLinkedList(records);

        return true;
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

//    public void insertLink(View view) {
//        fadeView(
//                findViewById(R.id.newPost_linkPasteET)
//                , FADE.in
//        );
//
//        fadeView(
//                findViewById(R.id.newPost_addLinkBtn)
//                , FADE.in
//        );
//    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (findViewById(R.id.newPost_linkPasteET).getAlpha() != 0.0f) {

            fadeOutLinkBox();

        } else if (findViewById(R.id.greyScreen).getAlpha() != 0) {

            fadeOutNewPostBox();
            isWritingNewPost = false;

        } else {
            super.onBackPressed();
        }
    }

    private void fadeOutLinkBox() {
        if (findViewById(R.id.newPost_linkPasteET).getAlpha() != 0.0f) {
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

        }
    }

    private void fadeOutNewPostBox() {
        if (findViewById(R.id.greyScreen).getAlpha() != 0) {

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
        }
    }

    public void insertImage(View view) {
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
                            newPostImage.post(new Runnable() {
                                @Override
                                public void run() {
                                    newPostImage.setImageURI(selectedImageUri);
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

            startActivity(new Intent(this, UpcomingEvents.class));
//            inflate(R.layout.activity_event);
//            currentId = id;
        } else if (id == R.id.nav_new_event && id != currentId) {
            startActivity(new Intent(this, Event.class));
        } else if (id == R.id.nav_tree && id != currentId) {
            FamilyTree.bGoToProfile = false;
            startActivity(new Intent(this, FamilyTree.class).putExtra(getString(R.string.profile_extra_mail_tag)
                    , FirebaseAuth.getInstance().getCurrentUser().getEmail()));
//            inflate(R.layout.activity_family_tree);
//            currentId = id;
        } else if (id == R.id.nav_send) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "I got a bit stuck in the SUPER-COOL Family-Hub app, can you help me??");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_log_out) {
            FirebaseAuth.getInstance().signOut();
            for (ValueEventListenerAndRef valueEventListenerAndRef : valueEventListenerAndRefLinkedList)
                valueEventListenerAndRef.getRef().removeEventListener(valueEventListenerAndRef.getValueEventListener());
            valueEventListenerAndRefLinkedList.clear();
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

    private void setNextEvent() {
        //TODO
    }

    public void goToProfile(View view) {
        Profile.EDIT_MODE = false;
        Intent intent = new Intent(this, Profile.class);
        intent.putExtra(getString(R.string.profile_extra_mail_tag), FirebaseAuth.getInstance().getCurrentUser().getEmail());
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
//        if (id == R.id.action_settings) {
//            startActivity(new Intent(this, SettingsActivity.class));
//            return true;
//        } else if (id == R.id.action_new) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void newPost(View view) {
        isWritingNewPost = true;
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

//    public void addLink(View view) {
//        ((TextView) findViewById(R.id.newPost_linkPreviewTV))
//                .setText(
//                        ((EditText) findViewById(R.id.newPost_linkPasteET))
//                                .getText().toString()
//                );
//
//        fadeOutLinkBox();
//    }


//    public void followLink(View view) {
//        if (!(view instanceof TextView))
//            return;
//        String link = ((TextView) view).getText().toString();
//        if (link == null || link.length() == 0)
//            return;
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse(link));
//        startActivity(intent);
//    }

    public void createNewPost(View view) {
        newPost = new Post(FirebaseAuth.getInstance().getCurrentUser().getEmail()
                , ((EditText) findViewById(R.id.newPost_contentET)).getText().toString()
                , new Date()
                , null);

        ((EditText) findViewById(R.id.newPost_contentET)).setText(null);

        PostReadyForDB postReadyForDB = new PostReadyForDB(newPost);
        DateReadyForDB dateReadyForDB = new DateReadyForDB(newPost.getmPublishDate());

        DatabaseReference databasePublicUsersReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.public_usersDB));

        Calendar c = Calendar.getInstance();
        c.setTime(newPost.getmPublishDate());


        for (ProfileModel pm : getFamilyMembers(FirebaseAuth.getInstance().getCurrentUser().getEmail(), circles)) {
            DatabaseReference auxRef = databasePublicUsersReference.child(prepareStringToDataBase(pm.getEmail())).child(getString(R.string.userFeedDB))
                    .child(dateReadyForDB.getYear() + "," + dateReadyForDB.getMonth() + "," + dateReadyForDB.getWeek()).child(dateReadyForDB.toString());
            auxRef.setValue(postReadyForDB);
            auxRef.child("date").setValue(dateReadyForDB);
        }

        savePhotoInStorage(dateReadyForDB);

        fadeOutNewPostBox();
    }

    private void savePhotoInStorage(DateReadyForDB dateReadyForDB) {
//        final ProgressBar profileImageProgressBar = findViewById(R.id.profileImageProgressBar);
//        profileImageProgressBar.setVisibility(View.VISIBLE);
//        profileImageProgressBar.setEnabled(true);
        newPostImage.setDrawingCacheEnabled(true);
        newPostImage.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) newPostImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference postRef = allPostsRef.child(dateReadyForDB + "%" + newPost.getmPublisherStr() + ".jpg");

        UploadTask uploadTask = postRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//                profileImageProgressBar.setEnabled(false);
//                profileImageProgressBar.setVisibility(View.GONE);
            }
        });
        newPostImage.setImageDrawable(getDrawable(R.drawable.logo_with_white));
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

        private ProfileModel getPublicProfileModel(ProfileModel profileModel, String mail) {
            String[] publicData = new String[6];
            publicData[PHONE_INDEX] = profileModel.getPhone();
            publicData[EMAIL_INDEX] = profileModel.getEmail();
            publicData[BDAY_INDEX] = profileModel.getDate();
            publicData[CITY_INDEX] = profileModel.getCity();
            publicData[ADDRESS_INDEX] = profileModel.getAddress();
            publicData[NAME_INDEX] = profileModel.getName();

            LinkedList<String> mailList = new LinkedList<>();
            for (ProfileModel pm : getFamilyMembers(mail, 2)) {
                mailList.add(pm.getEmail());
            }
            for (int index = 0; index < Profile.NUMBER_OF_PARAMETERS; index++) {
                if (publicData[index] == null
                        || publicData[index].length() == 0
                        || (publicData[index].startsWith("%f") && !mailList.contains(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                ) {
                    publicData[index] = "";
                }

                if (publicData[index].startsWith("%f"))
                    publicData[index] = publicData[index].substring(2);
            }

            profileModel.setPhone(publicData[PHONE_INDEX]);
            profileModel.setEmail(publicData[EMAIL_INDEX]);
            profileModel.setDate(publicData[BDAY_INDEX]);
            profileModel.setCity(publicData[CITY_INDEX]);
            profileModel.setAddress(publicData[ADDRESS_INDEX]);
            profileModel.setName(publicData[NAME_INDEX]);

            return profileModel;
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
//            final TextView reedMore = listItem.findViewById(R.id.post_reedMoreTV);
            final ImageView profileImg = listItem.findViewById(R.id.post_profilePhotoIV);
            final ImageView additionalImg = listItem.findViewById(R.id.post_additionalPhotoIV);

            final ProfileModel profileModel = getPublicProfileModel(StaticMethods.getProfileModel(current.getmPublisherStr()), current.getmPublisherStr());
            name.setText(profileModel.getName());
            content.setText(current.getmContentStr());
            Calendar c = Calendar.getInstance();
            c.setTime(current.getmPublishDate());

            String minute = "" + c.get(Calendar.MINUTE);
            String hour = "" + ((c.get(Calendar.HOUR_OF_DAY) + 3) % 24);
            if (c.get(Calendar.MINUTE) < 10)
                minute = "0" + c.get(Calendar.MINUTE);
            if (c.get(Calendar.HOUR_OF_DAY) < 3) {
                c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
            }
            date.setText(DAYS[c.get(Calendar.DAY_OF_WEEK)] + ", " + MONTHS[c.get(Calendar.MONTH)]
                    + " " + c.get(Calendar.DAY_OF_MONTH) + ", " + c.get(Calendar.YEAR) + ", " + hour + ":" + minute);
//            if (content.getLineCount() > 5)
//                reedMore.setVisibility(View.VISIBLE);

//            reedMore.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    content.setMaxLines(100);
//                }
//            });

            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isWritingNewPost)
                        return;
                    Intent intent = new Intent(HomeScreen.this, Profile.class);
                    intent.putExtra(getString(R.string.profile_extra_mail_tag), profileModel.getEmail());
                    startActivity(intent);
                }
            });


//            Uri uri = FirebaseStorage
//                    .getInstance()
//                    .getReference()
//                    .child(getString(R.string.post_images))
//                    .child(current.getmImagePathStr())
//                    .getDownloadUrl()
//                    .getResult();
            //additionalImg.setImageURI(uri);

            StorageReference ref = FirebaseStorage.getInstance().getReference().child(getString(R.string.profile_images))
                    .child(prepareStringToDataBase(profileModel.getEmail()) + ".jpg");

            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    int resizeTO = 180;
                    Picasso.get()
                            .load(uri)
                            .error(getDrawable(R.drawable.logo))
                            .into(profileImg);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    profileImg.setImageDrawable(getDrawable(R.drawable.logo_with_white));
                }
            });

            DateReadyForDB dateReadyForDB = new DateReadyForDB(current.getmPublishDate());
            ref = allPostsRef.child(dateReadyForDB + "%" + current.getmPublisherStr() + ".jpg");

            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    int resizeTO = 180;
                    Picasso.get()
                            .load(uri)
                            .error(getDrawable(R.drawable.logo))
                            .into(additionalImg);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });

            listItem.setClickable(false);
            return listItem;
        }
    }
}