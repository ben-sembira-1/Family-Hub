package com.example.sembi.logingui;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ProfileTest {

    @Test
    public void prepareStringToDataBase() {
        System.out.println(StaticMethods.prepareStringToDataBase(".sembiraben@gmail.com........."));
        assertTrue(StaticMethods.prepareStringToDataBase(".sembiraben@gmail.com.").equals("*sembiraben@gmail*com*"));
        assertTrue(StaticMethods.prepareStringToDataBase("..").equals("**"));
        assertTrue(StaticMethods.prepareStringToDataBase("").equals(""));
        assertTrue(StaticMethods.prepareStringToDataBase(".").equals("*"));
    }
}