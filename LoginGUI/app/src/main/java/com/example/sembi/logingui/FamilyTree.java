package com.example.sembi.logingui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;

public class FamilyTree extends AppCompatActivity {

    private FamilyTreeNode god;
    //String familyTreeString;
    private DataSnapshot publicUsers;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;

    private LinkedList mParents;
    private LinkedList<LinkedList<String>> mMarried, mDivorced;
    private LinkedList<String> mBrothers;



    //for ListView
    private FamilyTreeNodeUIListAdapter adapter;
    private ListView brothersLV, kidsLV;
    private ArrayList<FamilyTreeNodeUIModel> brothersModelsList, kidsModelsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_tree);

        god = new FamilyTreeNode(getString(R.string.god_name));
        //familyTreeString = null;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        brothersLV = findViewById(R.id.brothersListView);
        kidsLV = findViewById(R.id.kidsListView);

        intializeFam();
        preperLists();
        setDBListeners();
    }

    private void preperLists() {



        adapter = new FamilyTreeNodeUIListAdapter(this, brothersModelsList);

        brothersLV.setAdapter(adapter);
    }

    private void intializeFam(){
        mParents = new LinkedList();
        //(mKids) -> (married/divorced) -> (a partner from this list of partners, list of kids)
        mMarried = new LinkedList<>();
        mDivorced = new LinkedList<>();
    }

    private void setDBListeners() {
//        family tree
//        getFamilyTreeReference().addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                familyTreeString = dataSnapshot.getValue(String.class);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//
//            }
//        });

        //all users
        getAllUsersReference().addValueEventListener(new ValueEventListener() {
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


//    private DatabaseReference getFamilyTreeReference(){
//        return reference.child(getString(R.string.family_treeDB));
//    }
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
}
