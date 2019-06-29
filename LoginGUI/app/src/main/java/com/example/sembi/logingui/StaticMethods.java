package com.example.sembi.logingui;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;

public class StaticMethods {

    public final static String[] DAYS = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    public final static String[] MONTHS = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    public final static int PHONE_INDEX = 0, EMAIL_INDEX = 1, BDAY_INDEX = 2, CITY_INDEX = 3, ADDRESS_INDEX = 4, NAME_INDEX = 5;
    public final static int ADD_KID_INDEX = 0, ADD_PARENT_INDEX = 1, ADD_PARTNER_INDEX = 2;
    public final static String coming = "COMING", notComing = "NOT_COMING", thinking = "THINKING";
    public static LinkedList<ValueEventListenerAndRef> valueEventListenerAndRefLinkedList = new LinkedList<>();

    public enum famFields{
        parents,
        kids,
        brothers,
        partner
    }

    public static DataSnapshot getPublicUsers() {
        return publicUsers;
    }

    public static void setPublicUsers(DataSnapshot publicUsers) {
        StaticMethods.publicUsers = publicUsers;
    }

    public static StorageReference getProfileImageRef(String mail) {
        return FirebaseStorage.getInstance().getReference().child("profileImages")
                .child(prepareStringToDataBase(mail) + ".jpg");
    }

    public static ProfileModel getProfileModel(String mail){
        return publicUsers.child(prepareStringToDataBase(mail)).child("personalData").getValue(ProfileModel.class);
    }

    private static DataSnapshot publicUsers;

    public static void setPublicUsersListener(){
        FirebaseDatabase.getInstance().getReference("publicUsers")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        publicUsers = dataSnapshot;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    //TODO change circles by settings
    public static LinkedList<ProfileModel> getFamilyMembers(String usrEmail, int circlesDeep) {
        LinkedList<ProfileModel> allFamMembersWithRepeats = new LinkedList<>();

        allFamMembersWithRepeats.add(publicUsers.child(prepareStringToDataBase(usrEmail))
                .child("personalData").getValue(ProfileModel.class));

        getFamilyMembers(circlesDeep, allFamMembersWithRepeats);

        LinkedList<ProfileModel> listToReturn = new LinkedList<>();
        LinkedList<String> listOfMails = new LinkedList<>();
        for (ProfileModel pm : allFamMembersWithRepeats) {
            if (!listOfMails.contains(pm.getEmail())) {
                listOfMails.add(pm.getEmail());
                if (pm.getEmail().startsWith("%f"))
                    pm.setEmail(pm.getEmail().substring(2));

                listToReturn.add(pm);
            }
        }

        listToReturn.sort(new Comparator<ProfileModel>() {
            @Override
            public int compare(ProfileModel o1, ProfileModel o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return listToReturn;
    }

    private static LinkedList<ProfileModel> getFamilyMembers(int circle, final LinkedList<ProfileModel> previous) {
        if (circle <= 0) {
            return previous;
        }

        LinkedList<ProfileModel> aux = new LinkedList<>();

        for (ProfileModel s : previous) {
            aux.addAll(get(s.getEmail(), famFields.kids));
            aux.addAll(get(s.getEmail(), famFields.partner));
            aux.addAll(get(s.getEmail(), famFields.parents));
            aux.addAll(get(s.getEmail(), famFields.brothers));
        }

        previous.addAll(aux);

        getFamilyMembers(circle-1, previous);

        return previous;
    }

    public static LinkedList<String> fromProfileModelColectionToStringEmailsLinkedList(Collection<ProfileModel> collection) {
        LinkedList<String> toReturn = new LinkedList<>();
        for (ProfileModel pm : collection) {
            toReturn.add(pm.getEmail());
        }
        return toReturn;
    }

    public static LinkedList<ProfileModel> get(String user_email, famFields field) {
        LinkedList<ProfileModel> toReturn = new LinkedList<>();
        DataSnapshot famDataSnapshot = publicUsers.child(prepareStringToDataBase(prepareStringToDataBase(user_email))).child("fam");

        if (field == famFields.kids){
            for (DataSnapshot snapshot : famDataSnapshot.child("kids").getChildren()) {

                ProfileModel curr = publicUsers.child(prepareStringToDataBase(snapshot.getValue().toString()))
                        .child("personalData").getValue(ProfileModel.class);

                toReturn.add(curr);
            }
        }else if (field == famFields.parents){
            for (DataSnapshot snapshot : famDataSnapshot.child("parents").getChildren()) {
                ProfileModel curr = publicUsers.child(prepareStringToDataBase(snapshot.getValue().toString()))
                        .child("personalData").getValue(ProfileModel.class);

                toReturn.add(curr);
            }
        }else if (field == famFields.partner){
            if (famDataSnapshot.child("partner").getValue() == null)
                return toReturn;
            ProfileModel curr = publicUsers.child(prepareStringToDataBase(famDataSnapshot.child("partner").getValue().toString()))
                    .child("personalData").getValue(ProfileModel.class);

            toReturn.add(curr);
//            for (DataSnapshot snapshot : famDataSnapshot.child("partner").getChildren()) {
//                toReturn.add(snapshot.getValue().toString());
//            }
        }if (field == famFields.brothers){
            for (DataSnapshot ds : famDataSnapshot.child("parents").getChildren()) {
                toReturn.addAll(get(ds.getValue().toString(), famFields.kids));
            }
        }

        return toReturn;
    }

    public static String prepareStringToDataBase(String s) {
        int numOfDotsFound = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '.') {
                s = s.substring(0, i) + "*" + s.substring(i + 1);
                numOfDotsFound++;
                i--;
            }
        }
        return s;
    }
}
