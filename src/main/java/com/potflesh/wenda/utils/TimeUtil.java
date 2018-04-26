package com.potflesh.wenda.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bazinga on 17/04/2018.
 */
public class TimeUtil {

    public static String getCurrentTimeUsingDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getCurrentTimeUsingCalendar() {
        Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("YY:mm::dd HH:mm:ss");
        String formattedDate=dateFormat.format(date);
        return formattedDate;
    }
}
