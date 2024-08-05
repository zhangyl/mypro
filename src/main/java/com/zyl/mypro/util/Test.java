package com.zyl.mypro.util;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

public class Test {
    public static void main(String[] args) {
        long i = 1720882800000L;
        Date date = new Date(i);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        System.out.println(sdf.format(date));

        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");

        sdf.setTimeZone(timeZone);

        System.out.println(sdf.format(date));

    }
}
