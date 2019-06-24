package com.example.sembi.logingui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import static com.example.sembi.logingui.StaticMethods.*;

public class FamilyTree extends AppCompatActivity {


    //String familyTreeString;
    private String currentUser;
    //for ListView
    private FamilyTreeNodeUIListAdapter brothersAdapter, parentsAdapter, kidsAdapter;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;
    private ListView brothersListView, kidsListView;
    private ArrayList<FamilyTreeNodeUIModel> brothersModelsList, parentsModelsList, kidsModelsList;

    public void setCurrentUser(String currentUser) {
        this.currentUser = prepareStringToDataBase(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_tree);

//        god = new FamilyTreeNode(getString(R.string.god_name));
        //familyTreeString = null;


        setPublicUsersListener();


        intializeFam();
        preppierLists();
        setDBListeners();
        setNodesVisibility(View.GONE, View.GONE, View.GONE);

        getPublicUserReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setDBListeners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setNodesVisibility(int leftParent, int rightParent, int partner) {
        if ((leftParent != View.GONE && leftParent != View.VISIBLE && leftParent != View.INVISIBLE)
                || (rightParent != View.GONE && rightParent != View.VISIBLE && rightParent != View.INVISIBLE)
                || (partner != View.GONE && partner != View.VISIBLE && partner != View.INVISIBLE)) {

            Toast.makeText(this, "visibility put isn't gone,visible ot invisible", Toast.LENGTH_LONG).show();
            return;
        }

        setLeftParentVisibility(leftParent);
        setRightParentVisibility(rightParent);
        setPartnerVisibility(partner);
    }

    private void setLeftParentVisibility(int leftParent) {
        if ((leftParent != View.GONE && leftParent != View.VISIBLE && leftParent != View.INVISIBLE)) {

            Toast.makeText(this, "visibility put isn't gone,visible ot invisible", Toast.LENGTH_LONG).show();
            return;
        }
        ImageView leftParentIV = findViewById(R.id.leftParentIV);

        TextView leftParentTV = findViewById(R.id.leftParentNameTV);

        leftParentIV.setVisibility(leftParent);
        leftParentTV.setVisibility(leftParent);
    }

    private void setRightParentVisibility(int rightParent) {
        if ((rightParent != View.GONE && rightParent != View.VISIBLE && rightParent != View.INVISIBLE)) {

            Toast.makeText(this, "visibility put isn't gone,visible ot invisible", Toast.LENGTH_LONG).show();
            return;
        }
        ImageView rightParentIV = findViewById(R.id.rightParentIV);

        TextView rightParentTV = findViewById(R.id.rightParentNameTV);

        rightParentIV.setVisibility(rightParent);
        rightParentTV.setVisibility(rightParent);;
    }

    private void setPartnerVisibility(int partner) {
        if ((partner != View.GONE && partner != View.VISIBLE && partner != View.INVISIBLE)) {

            Toast.makeText(this, "visibility put isn't gone,visible ot invisible", Toast.LENGTH_LONG).show();
            return;
        }
        ImageView partnerIV = findViewById(R.id.partnerIV),
                partnerFrameIV = findViewById(R.id.partnerFrameIV);

        TextView partnerTV = findViewById(R.id.partnerNameTV);

        partnerFrameIV.setVisibility(partner);
        partnerIV.setVisibility(partner);
        partnerTV.setVisibility(partner);
    }

    private void preppierLists() {

        brothersListView = findViewById(R.id.brothersListView);
        kidsListView = findViewById(R.id.kidsListView);
        //brothers
        brothersModelsList = new ArrayList<>();
        brothersAdapter = new FamilyTreeNodeUIListAdapter(this, brothersModelsList);
        brothersListView.setAdapter(brothersAdapter);
        //kids
        kidsModelsList = new ArrayList<>();
        kidsAdapter = new FamilyTreeNodeUIListAdapter(this, kidsModelsList);
        kidsListView.setAdapter(kidsAdapter);
        //parents
        parentsModelsList = new ArrayList<>();
        //partner

    }

    private void intializeFam() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        setCurrentUser(firebaseUser.getEmail());
        //(mKids) -> (married/divorced) -> (a partner from this list of partners, list of kids)
    }

    private void setDBListeners() {
        //family tree
        DatabaseReference publicUserReference = getPublicUserReference();
        LinkedList<String> brothers = get(currentUser, famFields.brothers);
        for (String brother : brothers) {
            publicUserReference.child(prepareStringToDataBase(brother)).child(getString(R.string.personal_dataDB)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    FamilyTreeNodeUIModel familyTreeNodeUIModel = new FamilyTreeNodeUIModel(dataSnapshot.child("name").getValue().toString(), null);
                    brothersModelsList.add(familyTreeNodeUIModel);
                    shrinkFamilyTreeNodeUIModelArrayList(brothersModelsList);
                    brothersAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value

                }
            });
        }
        LinkedList<String> partners = get(currentUser, famFields.partner);
        for (String partner : partners) {
            publicUserReference.child(prepareStringToDataBase(partner)).child(getString(R.string.personal_dataDB)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    FamilyTreeNodeUIModel familyTreeNodeUIModel = new FamilyTreeNodeUIModel(dataSnapshot.child("name").getValue().toString(), null);
                    TextView partnerName = findViewById(R.id.partnerNameTV);
                    partnerName.setText(familyTreeNodeUIModel.getName());
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value

                }
            });
        }
        LinkedList<String> kids = get(currentUser, famFields.kids);
        for (String kid : kids) {
            publicUserReference.child(prepareStringToDataBase(kid)).child(getString(R.string.personal_dataDB)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    FamilyTreeNodeUIModel familyTreeNodeUIModel = new FamilyTreeNodeUIModel(dataSnapshot.child("name").getValue().toString(), null);
                    kidsModelsList.add(familyTreeNodeUIModel);
                    shrinkFamilyTreeNodeUIModelArrayList(kidsModelsList);
                    kidsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value

                }
            });
        }
        publicUserReference.child(prepareStringToDataBase(currentUser)).child(getString(R.string.famDB)).child("parents").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                parentsModelsList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    FamilyTreeNodeUIModel familyTreeNodeUIModel = new FamilyTreeNodeUIModel(ds.child("name").getValue().toString(), null);
                    parentsModelsList.add(familyTreeNodeUIModel);
                }

                for (int i = 0; i < parentsModelsList.size() && i < 2; i++) {
                    TextView parent = null;

                    if (i == 1) parent = findViewById(R.id.rightParentNameTV);
                    else if (i == 0) parent = findViewById(R.id.leftParentNameTV);

                    parent.setText(parentsModelsList.get(i).getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });

        if (parentsModelsList.size() >= 1)
            setLeftParentVisibility(View.VISIBLE);
        if (parentsModelsList.size() >= 2)
            setRightParentVisibility(View.VISIBLE);
        if (partners.size() > 0)
            setPartnerVisibility(View.VISIBLE);
    }

    private void shrinkFamilyTreeNodeUIModelArrayList(ArrayList<FamilyTreeNodeUIModel> modelsList) {
        ArrayList<FamilyTreeNodeUIModel> shrinked = new ArrayList<>();
        ArrayList<String> shrinked_Strings = new ArrayList<>();
        for (FamilyTreeNodeUIModel f : modelsList) {
            if (!shrinked_Strings.contains(f.getName())) {
                shrinked.add(f);
                shrinked_Strings.add(f.getName());
            }
        }
        modelsList.clear();
        modelsList.addAll(shrinked);
    }

//    private void collectAllRecords(DataSnapshot dataSnapshot) {
//        for (DataSnapshot currDS : dataSnapshot.child(currentUser).child("fam").child("kids").getChildren()) {
//            DataSnapshot currKid = dataSnapshot.child(prepareStringToDataBase(currDS.getValue(String.class)));
//            String name = currKid.child(getString(R.string.personal_dataDB))
//                    .child("name")
//                    .getValue(String.class);
//            //TODO
//            //Uri uri = new Uri.Builder().build();
//            Uri uri = null;
//            brothersModelsList.add(new FamilyTreeNodeUIModel(name, uri));
//            //TODO Collect correct records
//        }
//    }

    private DatabaseReference getAllUsersReference() {
        return reference.child(getString(R.string.all_usersDB));
    }

//    private void gatherAllCloseFamily(){
//
//        intializeFam();
//
//        DataSnapshot currUserFam = publicUsers.child(firebaseUser.getEmail()).child("fam");
//        //partners & kids
//        for (DataSnapshot partner:
//             currUserFam.child("kids").child("married").getChildren()) {
//            LinkedList kids = new LinkedList();
//            kids.addFirst(partner.getKey());
//            for (DataSnapshot kid:
//             partner.getChildren()) {
//                kids.addLast(kid.getValue().toString());
//            }
//        }
//        for (DataSnapshot partner:
//                currUserFam.child("kids").child("divorced").getChildren()) {
//            LinkedList kids = new LinkedList();
//            kids.addFirst(partner.getKey());
//            for (DataSnapshot kid:
//                    partner.getChildren()) {
//                kids.addLast(kid.getValue().toString());
//            }
//        }
//        for(DataSnapshot parent : currUserFam.child("parents").getChildren()) {
//            String currParent = parent.getValue().toString();
//            mParents.add(currParent);
//
//            //brothers
//            String[] positions = {"married","divorced"};
//            for (String position:
//                    positions) {
//                for (DataSnapshot partner :
//                        publicUsers.child(currParent).child("kids").child(position).getChildren()) {
//                    for (DataSnapshot bro : partner.getChildren()) {
//                        if (!modelsListContains(brothersModelsList, bro.getValue().toString())) {
//                            brothersModelsList.add(new FamilyTreeNodeUIModel(bro.getValue().toString(), null));
//                        }
//                    }
//                }
//            }
//        }
//
//    }

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
