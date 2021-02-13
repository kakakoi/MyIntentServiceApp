package com.example.myintentserviceapp.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Date {
    public static final String getTime(){
        Calendar calendar = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return df.format(calendar.getTime());
    }

    public static final String format(java.util.Date date){
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return df.format(date);
    }

    public static final String format(long l){
        java.util.Date date = new java.util.Date(l);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return df.format(date);
    }

    public static final String formatMediaDateAdded(int secondsInt){
        long milliSeconds = secondsInt * 1000L;
        java.util.Date date = new java.util.Date(milliSeconds);
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return df.format(date);
    }

    public static final String getYear(String dateS) {
        return dateS.substring(0,4);
    }

    public static final String getMonth(String dateS) {
        return dateS.substring(5,7);
    }
}
