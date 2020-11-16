package com.cuisec.mshield.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Created by licz on 2018/6/8.
 */

public class AppInfoUtil {


    private AppInfoUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");

    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) throws NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(
                context.getPackageName(), 0);
        int labelRes = packageInfo.applicationInfo.labelRes;
        return context.getResources().getString(labelRes);
    }

    /**
     * 获取应用程序版本号
     *
     * @param context
     * @return 当前应用的版本号
     * * @throws NameNotFoundException
     */
    public static int getVersionCode(Context context) throws NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(
                context.getPackageName(), 0);
        return packageInfo.versionCode;
    }

    /**
     * 获取应用程序版本名称信息
     *
     * @param context
     * @return 当前应用的版本名称
     * @throws NameNotFoundException
     */
    public static String getVersionName(Context context) throws NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(
                context.getPackageName(), 0);
        return packageInfo.versionName;
    }

    /**
     * 获取应用程序包名
     *
     * @param context
     * @return 当前应用程序包名
     * @throws NameNotFoundException
     */
    public static String getPackageName(Context context) throws NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(
                context.getPackageName(), 0);
        return packageInfo.packageName;
    }
}
