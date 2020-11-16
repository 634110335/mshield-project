package com.cuisec.mshield.utils;

import android.text.TextUtils;
import android.widget.TextView;

import com.google.zxing.common.StringUtils;

public class SensitiveInfoUtils {
    private SensitiveInfoUtils() {
        throw new AssertionError(" 不能产生实例");
    }

    /**
     * 名字脱敏处理
     * @param name 名字
     * @return 脱敏后的名字
     */
    public static String name(String name) {
        if (name == null || name.isEmpty()) { return "*"; }
        char[] chars = name.toCharArray();
        for (int i = 1; i < chars.length; i ++) {
            chars[i] = '*';
        }
        return new String(chars);
    }

    /**
     * 用户姓名脱敏
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String username(String name) {
        if (TextUtils.isEmpty(name) || name.length() == 1) { return "*"; }
        char[] chars = name.toCharArray();
        chars[1] = '*';
        for (int i = 2; i < chars.length - 1; i ++) {
            chars[i] = '*';
        }
        return new String(chars);
    }

    /**
     * 银行卡脱敏 （截取后4位）
     * @param cardNo 卡号
     * @return 脱敏后的卡号
     */
    public static String bankCard(String cardNo) {
        if (TextUtils.isEmpty(cardNo)) { return ""; }
        return cardNo.substring(cardNo.length() - 4);
    }

    /**
     * 手机号脱敏 （中间四位隐藏）
     * @param mobile 手机号
     * @return 脱敏后的手机号
     */
    public static String mobile(String mobile) {
        if (TextUtils.isEmpty(mobile)) { return ""; }
        char[] chars = mobile.toCharArray();
        int i = 4;
        chars[i ++] = '*';
        chars[i ++] = '*';
        chars[i ++] = '*';
        chars[i] = '*';
        return new String(chars);
    }

    /**
     * 将银行卡号内的空格去除
     * @param cardNo 卡号
     * @return 卡号
     */
    public static String trimCardNo(String cardNo) {
        char[] chars = cardNo.toCharArray();
        char[] card = new char[chars.length];
        int i = 0;
        for (char c : chars) {
            if (Character.isDigit(c)) {
                card[i ++] = c;
            }
        }
        return new String(card, 0, i);
    }

    /**
     * 判断字符串是否是数字
     * @param value 字符串
     * @return 为数字则返回<code>true</code>
     */
    public static boolean isNumber(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        int len = value.length();
        for (int i = 0; i < len; i ++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断该字符串是否为空
     * @param value 字符串
     * @return 为空则返回<code>true</code>
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

}
