package com.example.sembi.logingui;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

public class DateReadyForDB {
    private String year;
    private String month;
    private String week;
    private String day;
    private String dayOfWeek;
    private String hour;
    private String minute;
    private String second;
    private String millisecond;

    public DateReadyForDB(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        this.dayOfWeek = "" + c.get(Calendar.DAY_OF_WEEK);
        this.year = "" + c.get(Calendar.YEAR);
        this.month = "" + c.get(Calendar.MONTH);
        this.week = "" + c.get(Calendar.WEEK_OF_MONTH);
        this.day = "" + c.get(Calendar.DAY_OF_MONTH);
        this.hour = "" + c.get(Calendar.HOUR_OF_DAY);
        this.minute = "" + c.get(Calendar.MINUTE);
        this.second = "" + c.get(Calendar.SECOND);
        this.millisecond = "" + c.get(Calendar.MILLISECOND);
    }

    public DateReadyForDB() {
    }

    public DateReadyForDB(String year, String month, String week, String day, String dayOfWeek, String hour, String minute, String second, String millisecond) {
        this.dayOfWeek = dayOfWeek;
        this.year = year;
        this.month = month;
        this.week = week;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millisecond = millisecond;
    }

    @NonNull
    @Override
    public String toString() {
        return this.year + "_" + this.month + "_" + this.day + "_" + this.hour + "_" + this.minute + "_" + this.second + "_" + this.millisecond;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getMillisecond() {
        return millisecond;
    }

    public void setMillisecond(String millisecond) {
        this.millisecond = millisecond;
    }
}
