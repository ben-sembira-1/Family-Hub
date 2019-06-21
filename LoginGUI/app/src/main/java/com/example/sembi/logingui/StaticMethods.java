package com.example.sembi.logingui;

import java.util.LinkedList;

public class StaticMethods {

    public static final String[] DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    public static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    public static LinkedList<String> getFamilyMembers(String usrEmail) {
        LinkedList<String> list = new LinkedList<String>();
        list.add("sembiraben@gmail.com");
        return list;
    }
}
