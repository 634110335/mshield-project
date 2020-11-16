package com.cuisec.mshield.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ValidateUtil {

    // 检测是否是手机号码
    public static boolean validateMobile(String mobile) {
        Pattern pattern = Pattern
                .compile("^1\\d{10}$");
        Matcher matcher = pattern.matcher(mobile);
        return matcher.matches();
    }

    // 检测身份证号合法性
    public static boolean validateIdentityCard(String identity) {
        // 判断18位身份证号
        Pattern pattern = Pattern
                .compile("^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$");
        Matcher matcher = pattern.matcher(identity);

        if (!matcher.matches()) {
            // 判断15位身份证号
            pattern = Pattern
                    .compile("^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$");
            matcher = pattern.matcher(identity);
        }

        return matcher.matches();
    }

    public static boolean isLetterDigit(String str) {
        boolean isDigit = false;
        boolean isLetter = false;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                isDigit = true;
            } else if (Character.isLetter(str.charAt(i))) {
                isLetter = true;
            }
        }
        String regex = "^[a-zA-Z0-9]+$";
        boolean isRight = isDigit && isLetter && str.matches(regex);
        return isRight;
    }
}
