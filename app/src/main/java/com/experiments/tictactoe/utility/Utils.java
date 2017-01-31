package com.experiments.tictactoe.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;



public class Utils {

    public static final String DATE_FORMAT = "dd/MMM/yyyy hh:mm";

    public static String generateRandomId() {
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId.replaceAll("-", "");
    }

    public static String getCurrentTime() {
        long mills = System.currentTimeMillis();
        Date date = new Date(mills);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        return simpleDateFormat.format(date);
    }
}
