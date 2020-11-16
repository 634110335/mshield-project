package com.cuisec.mshield.utils;

import android.text.TextUtils;

import com.google.zxing.common.StringUtils;

public class PhoneUtils {
    /**
     * 手机号格式校验正则
     */
    public static final String PHONE_REGEX = "^1(3[0-9]|4[57]|5[0-35-9]|7[0135678]|8[0-9])\\d{8}$";

    /**
     * 手机号脱敏筛选正则
     */
    public static final String PHONE_BLUR_REGEX = "(\\d{3})\\d{4}(\\d{4})";

    /**
     * 手机号脱敏替换正则
     */
    public static final String PHONE_BLUR_REPLACE_REGEX = "$1****$2";


    /**
     * 手机号格式校验
     * @param phone
     * @return
     */
    public static final boolean checkPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        return phone.matches(PHONE_REGEX);
    }
    /**
     * 手机号脱敏处理
     * @param phone
     * @return
     */
    public static final String phoneEncrypt(String phone) {
        boolean checkFlag = checkPhone(phone);
        if (!checkFlag) {
            //throw new IllegalArgumentException("手机号格式不正确!");
            return phone;
        }
        return phone.replaceAll(PHONE_BLUR_REGEX, PHONE_BLUR_REPLACE_REGEX);
    }

}
