package com.example.sembi.logingui;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Comparator;
import java.util.LinkedList;

public class StaticMethods {

    public final static String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    public final static String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    public final static int PHONE_INDEX = 0, EMAIL_INDEX = 1, BDAY_INDEX = 2, CITY_INDEX = 3, ADDRESS_INDEX = 4, NAME_INDEX = 5;
    public final static int ADD_KID_INDEX = 0, ADD_PARENT_INDEX = 1, ADD_PARTNER_INDEX = 2;

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

    public static LinkedList<String> getFamilyMembers(String usrEmail, int circle) {
        LinkedList<String> list = new LinkedList<>();
        list.add(usrEmail);
        list.addAll(getFamilyMembers(circle, list));

        LinkedList<String> listToReturn = new LinkedList<>();

        for (String s : list){
            if (!listToReturn.contains(s)){
                listToReturn.add(s);
            }
        }

        listToReturn.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return listToReturn;
    }

    public static LinkedList<String> getFamilyMembers(int circle, final LinkedList<String> previous) {
        if (circle <= 0)
            return previous;

        LinkedList<String> aux = new LinkedList<>();

        for (String s : previous) {
            aux.addAll(get(s, famFields.kids));
            aux.addAll(get(s, famFields.partner));
            aux.addAll(get(s, famFields.parents));
            aux.addAll(get(s, famFields.brothers));
        }

        previous.addAll(aux);

        getFamilyMembers(circle-1, previous);

        return previous;
    }

    public static LinkedList<String> get(String user_email, famFields field){
        LinkedList<String> toReturn = new LinkedList<>();
        DataSnapshot famDataSnapshot = publicUsers.child(prepareStringToDataBase(prepareStringToDataBase(user_email))).child("fam");

        if (field == famFields.kids){
            for (DataSnapshot snapshot : famDataSnapshot.child("kids").getChildren()) {
                toReturn.add(snapshot.getValue().toString());
            }
        }else if (field == famFields.parents){
            for (DataSnapshot snapshot : famDataSnapshot.child("parents").getChildren()) {
                toReturn.add(snapshot.getValue().toString());
            }
        }else if (field == famFields.partner){
            if (famDataSnapshot.child("partner").getValue() == null)
                return toReturn;
            toReturn.add(famDataSnapshot.child("partner").getValue().toString());
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
