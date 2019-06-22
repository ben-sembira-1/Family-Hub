package com.example.sembi.logingui;

import java.util.LinkedList;

public class StaticMethods {

    public static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    public static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    public final static int PHONE_INDEX = 0, EMAIL_INDEX = 1, BDAY_INDEX = 2, CITY_INDEX = 3, ADDRESS_INDEX = 4, NAME_INDEX = 5;
    public final static int ADD_KID_INDEX = 0, ADD_PARENT_INDEX = 1, ADD_PARTNER_INDEX = 2;

    public static LinkedList<String> getFamilyMembers(String usrEmail) {
        LinkedList<String> list = new LinkedList<String>();
        list.add("sembiraben@gmail.com");
        return list;
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
