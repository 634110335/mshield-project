package com.cuisec.mshield.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.utils.JsonUtil;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by licz on 2018/6/4.
 */

public class SPManager {

    private static final String APP_INFO_PREFERENCES = "app_info_preferences";
    private static final String APP_INFO_FIRST_OPEN = "app_info_first_open";
    private static final String APP_INFO_CENTER_URL = "app_info_center_url";
    private static final String APP_INFO_SERVER_LIST = "app_info_server_list";
    private static final String APP_INFO_SERVER_CODE = "app_info_server_code";
    private static final String APP_INFO_SERVER_NAME = "app_info_server_name";
    private static final String APP_INFO_SERVER_URL = "app_info_server_url";
    private static final String APP_INFO_SERVER_SDK_URL = "app_info_server_sdk_url";
    private static final String APP_INFO_SERVER_MM = "app_info_server_mm";
    private static final String APP_SDK_AUTH_CODE = "app_sdk_auth_code";

    private static final String LOGIN_INFO_PREFERENCES = "login_info_preferences";
    private static final String LOGIN_INFO_STATE = "login_info_state";
    private static final String LOGIN_INFO_TOKEN = "login_info_token";
    private static final String LOGIN_INFO_REFRESH_TOKEN = "login_info_refresh_token";

    private static final String USER_INFO_PREFERENCES = "user_info_preferences";
    private static final String USER_INFO_LAST = "user_info_last";
    private static final String USER_INFO_ACCOUNT = "user_info_account";

    private static final String CERT_INFO_PREFERENCES = "cert_info_preferences";
    private static final String CERT_INFO_CODE = "cert_info_code";

    private static final String CONFIG_INFO_PREFERENCES = "config_info_preferences";
    private static final String CONFIG_INFO_MSG_PUSH = "config_info_msg_push";
    private static final String CONFIG_INFO_CERT_PIN = "config_info_cert_pin";
    private static final String CONFIG_INFO_SIGN_FREE = "config_info_sign_free";        // 免密签名
    private static final String CONFIG_INFO_FINGER_SUPPORT = "config_info_finger_support";  //设备是否支持指纹
    private static final String CONFIG_INFO_FINGER_SIGN = "config_info_finger_sign";        // 指纹签名
    private static final String CONFIG_INFO_FINGER_LOCK = "config_info_finger_lock";    // 指纹解锁状态


    /**
     * 获取sharepreference 编辑
     */
    private static SharedPreferences.Editor getEditor(String preName) {
        return getSharedPreferences(preName).edit();
    }

    /**
     * 获取 SharedPreferences
     */
    private static SharedPreferences getSharedPreferences(String preName) {
        SharedPreferences preferences = MyApplication.getInstance().getSharedPreferences(preName, Context.MODE_PRIVATE);
        return preferences;
    }

    /**
     * 是否第一次打开应用
     * true:第一次打开
     * false:非第一次打开
     */
    public static boolean getFirstOpen() {
        SharedPreferences preferences = getSharedPreferences(APP_INFO_PREFERENCES);
        return preferences.getBoolean(APP_INFO_FIRST_OPEN, true);
    }

    public static void setFirstOpen(Boolean firstOpen) {
        SharedPreferences.Editor editor = getEditor(APP_INFO_PREFERENCES);
        editor.putBoolean(APP_INFO_FIRST_OPEN, firstOpen);
        editor.commit();
    }

    /**
     * 机构LIST
     */
    public static String getCenterUrl() {
        SharedPreferences preferences = getSharedPreferences(APP_INFO_PREFERENCES);
        return preferences.getString(APP_INFO_CENTER_URL, Config.https_server_url);
    }

    public static void setCenterUrl(String url) {
        SharedPreferences.Editor editor = getEditor(APP_INFO_PREFERENCES);
        editor.putString(APP_INFO_CENTER_URL, url);
        editor.commit();
    }

    /**
     * 机构LIST
     */
    public static String getServerList() {
        SharedPreferences preferences = getSharedPreferences(APP_INFO_PREFERENCES);
        return preferences.getString(APP_INFO_SERVER_LIST, "");
    }

    public static void setServerList(String serverList) {
        SharedPreferences.Editor editor = getEditor(APP_INFO_PREFERENCES);
        editor.putString(APP_INFO_SERVER_LIST, serverList);
        editor.commit();
    }

    /**
     * 机构CODE
     */
    public static String getServerCode() {
        SharedPreferences preferences = getSharedPreferences(APP_INFO_PREFERENCES);
        return preferences.getString(APP_INFO_SERVER_CODE, "");
    }

    public static void setServerCode(String serverCode) {
        SharedPreferences.Editor editor = getEditor(APP_INFO_PREFERENCES);
        editor.putString(APP_INFO_SERVER_CODE, serverCode);
        editor.commit();
    }

    /**
     * 机构名称
     */
    public static String getServerName() {
        SharedPreferences preferences = getSharedPreferences(APP_INFO_PREFERENCES);
        return preferences.getString(APP_INFO_SERVER_NAME, "");
    }

    public static void setServerName(String serverName) {
        SharedPreferences.Editor editor = getEditor(APP_INFO_PREFERENCES);
        editor.putString(APP_INFO_SERVER_NAME, serverName);
        editor.commit();
    }

    /**
     * 机构URL
     */
    public static String getServerUrl() {
        SharedPreferences preferences = getSharedPreferences(APP_INFO_PREFERENCES);
        return preferences.getString(APP_INFO_SERVER_URL, "");
    }

    public static void setServerUrl(String serverUrl) {
        SharedPreferences.Editor editor = getEditor(APP_INFO_PREFERENCES);
        editor.putString(APP_INFO_SERVER_URL, serverUrl);
        editor.commit();
    }

    /**
     * 机构SDKURL
     */
    public static String getServerSDKUrl() {
        SharedPreferences preferences = getSharedPreferences(APP_INFO_PREFERENCES);
        return preferences.getString(APP_INFO_SERVER_SDK_URL, "");
    }

    public static void setServerSDKUrl(String sdkUrl) {
        SharedPreferences.Editor editor = getEditor(APP_INFO_PREFERENCES);
        editor.putString(APP_INFO_SERVER_SDK_URL, sdkUrl);
        editor.commit();
    }

    /**
     * 服务器URL
     */
    public static Boolean getServerMM() {
        SharedPreferences preferences = getSharedPreferences(APP_INFO_PREFERENCES);
        return preferences.getBoolean(APP_INFO_SERVER_MM, false);
    }

    public static void setServerMM(Boolean status) {
        SharedPreferences.Editor editor = getEditor(APP_INFO_PREFERENCES);
        editor.putBoolean(APP_INFO_SERVER_MM, status);
        editor.commit();
    }

    /**
     * SDK授权码
     */
    public static String getSDKAuthCode() {
        SharedPreferences preferences = getSharedPreferences(APP_INFO_PREFERENCES);
        return preferences.getString(APP_SDK_AUTH_CODE, "");
    }

    public static void setSDKAuthCode(String code) {
        SharedPreferences.Editor editor = getEditor(APP_INFO_PREFERENCES);
        editor.putString(APP_SDK_AUTH_CODE, code);
        editor.commit();
    }


    /**
     * 获取登录状态
     */
    public static boolean getLoginState() {

        SharedPreferences preferences = getSharedPreferences(LOGIN_INFO_PREFERENCES);
        Boolean isLogin = preferences.getBoolean(LOGIN_INFO_STATE, false);
        return isLogin;
    }

    /**
     * 设置登陆状态，当设置登录状态false时，清空用户信息
     */
    public static void setLoginState(Boolean state) {
        if (!state) {
            setUserInfo(null);
            setUserToken("");
            setRefreshToken("");
            setCertPin("");
            setFingerLockStatus(false);   // 清除指纹解锁
            setFingerSignStatus(false);   // 清除指纹签名
            setSignFreeStatus(false);     // 清除免密签名
            MyApplication.getInstance().getLockPatternUtils().saveLockPattern(null);                // 清除手势设置
        }
        SharedPreferences.Editor editor = getEditor(LOGIN_INFO_PREFERENCES);
        editor.putBoolean(LOGIN_INFO_STATE, state);
        editor.commit();
    }

    /**
     * 用户token
     */
    public static String getUserToken() {
        SharedPreferences preferences = getSharedPreferences(LOGIN_INFO_PREFERENCES);
        String token = preferences.getString(LOGIN_INFO_TOKEN, "");
        return token;
    }

    public static void setUserToken(String token) {
        SharedPreferences.Editor editor = getEditor(LOGIN_INFO_PREFERENCES);
        editor.putString(LOGIN_INFO_TOKEN, token);
        editor.commit();
    }

    /**
     * 刷新token
     */
    public static String getRefreshToken() {
        SharedPreferences preferences = getSharedPreferences(LOGIN_INFO_PREFERENCES);
        String token = preferences.getString(LOGIN_INFO_REFRESH_TOKEN, "");
        return token;
    }

    public static void setRefreshToken(String refreshToken) {
        SharedPreferences.Editor editor = getEditor(LOGIN_INFO_PREFERENCES);
        editor.putString(LOGIN_INFO_REFRESH_TOKEN, refreshToken);
        editor.commit();
    }

    /**
     * 用户信息
     */
    public static UserInfo getUserInfo() {
        UserInfo userInfo = null;
        try {
            SharedPreferences preferences = getSharedPreferences(USER_INFO_PREFERENCES);
            String userInfoJson = preferences.getString(USER_INFO_LAST, "");
            if (userInfoJson != null && !"".equals(userInfoJson)) {
                Type userInfoType = new TypeToken<UserInfo>() {
                }.getType();
                userInfo = (UserInfo) JsonUtil.toObject(userInfoJson, userInfoType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userInfo;
    }

    public static void setUserInfo(UserInfo userInfo) {
        SharedPreferences.Editor editor = getEditor(USER_INFO_PREFERENCES);
        if (userInfo != null) {
            editor.putString(USER_INFO_LAST, JsonUtil.toJson(userInfo));
        } else {
            editor.putString(USER_INFO_LAST, "");
        }
        editor.commit();
    }

    /**
     * 账户信息
     */
    public static String getUserAccount() {
        SharedPreferences preferences = getSharedPreferences(USER_INFO_PREFERENCES);
        String pin = preferences.getString(USER_INFO_ACCOUNT, "");
        return pin;
    }

    public static void setUserAccount(String account) {
        SharedPreferences.Editor editor = getEditor(USER_INFO_PREFERENCES);
        editor.putString(USER_INFO_ACCOUNT, account);
        editor.commit();
    }


    /**
     * 设置免密签名状态
     */
    public static boolean getSignFreeStatus() {
        SharedPreferences preferences = getSharedPreferences(CONFIG_INFO_PREFERENCES);
        return preferences.getBoolean(CONFIG_INFO_SIGN_FREE, false);
    }

    public static void setSignFreeStatus(boolean bStatus) {
        SharedPreferences.Editor editor = getEditor(CONFIG_INFO_PREFERENCES);
        editor.putBoolean(CONFIG_INFO_SIGN_FREE, bStatus);
        editor.commit();
    }

    /**
     * 设置设置是否支持指纹
     */
    public static boolean getFingerIsSupport() {
        SharedPreferences preferences = getSharedPreferences(CONFIG_INFO_PREFERENCES);
        return preferences.getBoolean(CONFIG_INFO_FINGER_SUPPORT, false);
    }

    public static void setFingerIsSupport(boolean bStatus) {
        SharedPreferences.Editor editor = getEditor(CONFIG_INFO_PREFERENCES);
        editor.putBoolean(CONFIG_INFO_FINGER_SUPPORT, bStatus);
        editor.commit();
    }

    /**
     * 设置指纹签名状态
     */
    public static boolean getFingerSignStatus() {
        SharedPreferences preferences = getSharedPreferences(CONFIG_INFO_PREFERENCES);
        return preferences.getBoolean(CONFIG_INFO_FINGER_SIGN, false);
    }

    public static void setFingerSignStatus(boolean bStatus) {
        SharedPreferences.Editor editor = getEditor(CONFIG_INFO_PREFERENCES);
        editor.putBoolean(CONFIG_INFO_FINGER_SIGN, bStatus);
        editor.commit();
    }

    /**
     * 设置指纹解锁状态
     */
    public static boolean getFingerLockStatus() {
        SharedPreferences preferences = getSharedPreferences(CONFIG_INFO_PREFERENCES);
        return preferences.getBoolean(CONFIG_INFO_FINGER_LOCK, false);
    }

    public static void setFingerLockStatus(boolean bStatus) {
        SharedPreferences.Editor editor = getEditor(CONFIG_INFO_PREFERENCES);
        editor.putBoolean(CONFIG_INFO_FINGER_LOCK, bStatus);
        editor.commit();
    }

    /**
     * 消息推送状态
     */
    public static boolean getMsgPush() {
        SharedPreferences preferences = getSharedPreferences(CONFIG_INFO_PREFERENCES);
        Boolean bMsgPush = preferences.getBoolean(CONFIG_INFO_MSG_PUSH, true);
        return bMsgPush;
    }

    public static void setMsgPush(Boolean msgPush) {
        SharedPreferences.Editor editor = getEditor(CONFIG_INFO_PREFERENCES);
        editor.putBoolean(CONFIG_INFO_MSG_PUSH, msgPush);
        editor.commit();
    }

    /**
     * 缓存的证书密码
     */
    public static String getCertPin() {
        SharedPreferences preferences = getSharedPreferences(CONFIG_INFO_PREFERENCES);
        String pin = preferences.getString(CONFIG_INFO_CERT_PIN, "");
        return pin;
    }

    public static void setCertPin(String pin) {
        SharedPreferences.Editor editor = getEditor(CONFIG_INFO_PREFERENCES);
        editor.putString(CONFIG_INFO_CERT_PIN, pin);
        editor.commit();
    }
}
