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
}
