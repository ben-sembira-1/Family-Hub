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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;

import static com.example.sembi.logingui.StaticMethods.prepareStringToDataBase;

public class FamilyTree extends AppCompatActivity {


    //String familyTreeString;
    private String currentUser;
    //for ListView
    private FamilyTreeNodeUIListAdapter brothersAdapter, partnersAdapter;
    private DataSnapshot publicUsers;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;

    private LinkedList mParents;
    private LinkedList<LinkedList<String>> mMarried, mDivorced;
    private LinkedList<String> mBrothers;
    private ListView brothersListView, partnersListView;
    private ArrayList<FamilyTreeNodeUIModel> brothersModelsList, partnersModelsList;

    public void setCurrentUser(String currentUser) {
        this.currentUser = prepareStringToDataBase(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_tree);

//        god = new FamilyTreeNode(getString(R.string.god_name));
        //familyTreeString = null;


        intializeFam();
        preperLists();
        setDBListeners();
    }

    private void preperLists() {

        brothersListView = findViewById(R.id.brothersListView);
        partnersListView = findViewById(R.id.partnersListView);
        //brothers
        brothersModelsList = new ArrayList<>();
        brothersAdapter = new FamilyTreeNodeUIListAdapter(this, brothersModelsList);
        brothersListView.setAdapter(brothersAdapter);
        //partners
        partnersModelsList = new ArrayList<>();
        partnersAdapter = new FamilyTreeNodeUIListAdapter(this, partnersModelsList);
        partnersListView.setAdapter(partnersAdapter);
    }

    private void intializeFam(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        setCurrentUser(firebaseUser.getEmail());
        mParents = new LinkedList();
        //(mKids) -> (married/divorced) -> (a partner from this list of partners, list of kids)
        mMarried = new LinkedList<>();
        mDivorced = new LinkedList<>();
    }

    private void setDBListeners() {
        //family tree
        DatabaseReference publicUserReference = getPublicUserReference();
        publicUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                collectAllRecords(dataSnapshot);
                brothersAdapter.notifyDataSetChanged();
                partnersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });

        //all users
        getAllUsersReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                publicUsers = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });


    }

    private void collectAllRecords(DataSnapshot dataSnapshot) {
        for (DataSnapshot currDS : dataSnapshot.child(currentUser).child("fam").child("kids").getChildren()) {
            DataSnapshot currKid = dataSnapshot.child(prepareStringToDataBase(currDS.getValue(String.class)));
            String name = currKid.child(getString(R.string.personal_dataDB))
                    .child("name")
                    .getValue(String.class);
            //TODO
            //Uri uri = new Uri.Builder().build();
            Uri uri = null;
            brothersModelsList.add(new FamilyTreeNodeUIModel(name, uri));
            //TODO Collect correct records
            partnersModelsList.add(new FamilyTreeNodeUIModel(name, uri));
        }
    }


    private DatabaseReference getAllUsersReference(){
        return reference.child(getString(R.string.all_usersDB));
    }

    private void gatherAllCloseFamily(){
        intializeFam();
        DataSnapshot currUserFam = publicUsers.child(firebaseUser.getEmail()).child("fam");
        //partners & kids
        for (DataSnapshot partner:
             currUserFam.child("kids").child("married").getChildren()) {
            LinkedList kids = new LinkedList();
            kids.addFirst(partner.getKey());
            mMarried.add(kids);
            for (DataSnapshot kid:
             partner.getChildren()) {
                kids.addLast(kid.getValue().toString());
            }
        }
        for (DataSnapshot partner:
                currUserFam.child("kids").child("divorced").getChildren()) {
            LinkedList kids = new LinkedList();
            kids.addFirst(partner.getKey());
            mDivorced.add(kids);
            for (DataSnapshot kid:
                    partner.getChildren()) {
                kids.addLast(kid.getValue().toString());
            }
        }
        for(DataSnapshot parent : currUserFam.child("parents").getChildren()) {
            String currParent = parent.getValue().toString();
            mParents.add(currParent);

            //brothers
            String[] positions = {"married","divorced"};
            for (String position:
                    positions) {
                for (DataSnapshot partner :
                        publicUsers.child(currParent).child("kids").child(position).getChildren()) {
                    for (DataSnapshot bro : partner.getChildren()) {
                        if (!modelsListContains(brothersModelsList, bro.getValue().toString())) {
                            brothersModelsList.add(new FamilyTreeNodeUIModel(bro.getValue().toString(), null));
                        }
                    }
                }
            }
        }

    }

    private boolean modelsListContains(ArrayList<FamilyTreeNodeUIModel> modelsList, String toSearch) {
        for(FamilyTreeNodeUIModel curr : modelsList){
            if(curr.getName().equals(toSearch)){
                return true;
            }
        }
        return false;
    }


    private DatabaseReference getPublicUserReference() {
        return reference.child(getString(R.string.public_usersDB));
    }

    public void goHome(View view) {
        startActivity(new Intent(this, HomeScreen.class));
    }


//    public void BuildTree(){
//        String theCurrentTree = familyTreeString;
//        String currRelation = "";
//
//        while (theCurrentTree.length()>0){
//            Character character = theCurrentTree.charAt(0);
//            currRelation += character;
//            theCurrentTree = theCurrentTree.substring(1);
//            if(character.equals('>')){
//                addRelation(currRelation);
//            }
//
//        }
//    }
//
//    private void addRelation(String currRelation) {
//        currRelation = currRelation.substring(1,currRelation.length()-1);
//        String kid = null, partner1 = null, partner2 = null;
//        while(currRelation.charAt(0) != '$'){
//            kid += currRelation.charAt(0);
//            currRelation = currRelation.substring(1);
//        }
//        currRelation = currRelation.substring(1);
//        while(currRelation.charAt(0) != '$'){
//            partner1 += currRelation.charAt(0);
//            currRelation = currRelation.substring(1);
//        }
//        currRelation = currRelation.substring(1);
//        partner2 = currRelation;
//    }

//    public Boolean differentFromAll(Object toCompareTo ,Object[] array_objects){
//        for (Object o:
//             array_objects) {
//            if(toCompareTo.equals(o)){
//                return false;
//            }
//        }
//        return true;
//    }


    public class FamilyTreeNodeUIListAdapter extends ArrayAdapter<FamilyTreeNodeUIModel> {

        private Context mContext;
        private ArrayList<FamilyTreeNodeUIModel> records;

        public FamilyTreeNodeUIListAdapter(@NonNull Context context, ArrayList<FamilyTreeNodeUIModel> records) {
            super(context, 0, records);
            mContext = context;
            this.records = records;
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.family_tree_node_ui, null);

            }
            FamilyTreeNodeUIModel current = records.get(position);
            TextView name = listItem.findViewById(R.id.FamilyTreeNodeUI_TV);
            ImageView IV = listItem.findViewById(R.id.FamilyTreeNodeUI_IV);
            name.setText(current.getName());
            //TODO take care of URI
            //IV.setImageURI(current.getIV());
            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Intents...... make Static,Public Boolean value for Toggle
                }
            });
            return listItem;
        }
    }
}
