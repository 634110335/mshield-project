package com.cuisec.mshield.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by licz on 2018/6/8.
 */

public class DateUtil {

    public static final String FORMART1 = "yyyyMMddHHmmss";
    public static final String FORMART2 = "yyyy-MM-dd HH:mm:ss";

    public static String getCurrentTime(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        String currentTime = sdf.format(date);
        return currentTime;
    }

    public static long getDays(String dateStr) {
        try {
            SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String beginTime = getCurrentTime("yyyy-MM-dd HH:mm:ss");

            Date beginDate = DateFormat.parse(beginTime);
            Date endDate = DateFormat.parse(dateStr);
            long interval = endDate.getTime() - beginDate.getTime();
            long days = interval/(24 * 3600 * 1000);
            return days;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 将字符串转换成为日期类型
     *
     * @param date 需要转换成日期的字符串
     * @return 转换后的日期
     * @throws IllegalArgumentException 参数不合法异常
     * @throws java.text.ParseException           解析日期异常
     */
    public static Date stringToDate(String date)
            throws IllegalArgumentException, java.text.ParseException {
        if (date == null || date.equalsIgnoreCase("")) {
            throw new IllegalArgumentException(
                    "parameter date is not valid");
        }
        SimpleDateFormat formater = new SimpleDateFormat(FORMART1);
        try {
            return formater.parse(date);
        } catch (java.text.ParseException exception) {
            throw exception;
        }
    }

    /**
     * 将字符串转换成为日期类型
     *
     * @param date   需要转换成日期的字符串
     * @param format 制式
     * @return 转换后的日期
     * @throws IllegalArgumentException 参数不合法异常
     * @throws java.text.ParseException           解析日期异常
     */
    public static Date stringToDate(String date, String format)
            throws IllegalArgumentException, java.text.ParseException {
        if (date == null || date.equalsIgnoreCase("")) {
            throw new IllegalArgumentException(
                    "parameter date is not valid");
        }
        SimpleDateFormat formater = new SimpleDateFormat(format);
        try {
            return formater.parse(date);
        } catch (java.text.ParseException exception) {
            throw exception;
        }
    }

    /**
     * 将日期类型转换为字符串的形式
     *
     * @param date 需要转换的日期
     * @return 转换后的字符串
     * @throws IllegalArgumentException
     */
    public static String dateToString(Date date)
            throws IllegalArgumentException {
        if (date == null) {
            throw new IllegalArgumentException(
                    "parameter date is not valid");
        }
        SimpleDateFormat formater = new SimpleDateFormat(FORMART1);
        return formater.format(date);
    }

    /**
     * 将日期类型转换为字符串的形式
     *
     * @param date   需要转换的日期
     * @param format 制式
     * @return 转换后的字符串
     * @throws IllegalArgumentException
     */
    public static String dateToString(Date date, String format)
            throws IllegalArgumentException {
        if (date == null) {
            throw new IllegalArgumentException(
                    "parameter date is not valid");
        }
        SimpleDateFormat formater = new SimpleDateFormat(format);
        return formater.format(date);
    }

    /**
     * 时间增加分钟
     *
     * @param minute 增加的分钟数
     * @return Date 增加后的时间
     */
    public static Date addMinute(int minute, Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minute);
        return calendar.getTime();
    }

}
