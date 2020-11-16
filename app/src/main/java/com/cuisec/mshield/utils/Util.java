package com.cuisec.mshield.utils;

import android.content.Context;

import com.cuisec.mshield.config.Constants;

/**
 * Created by licz on 2018/6/5.
 */

public class Util {

    public static String phoneFormat(String phone) {
        String strPhone = "";
        if (phone.length() == 11) {
            strPhone = phone.substring(0, 3) + " " + phone.substring(3, 7) + " " + phone.substring(7);
        }
        return strPhone;
    }

    // 解决红房子医院和演示系统证书路径串号的问题  将证书路径：身份证+APPID+密钥版本号 改成 身份证+AppID+_+密钥版本号
    public static void changeCertFolderName(Context context, String userId, String appId) {
        if (context == null || userId == null || userId.length() == 0 || appId == null || appId.length() == 0) {
            return;
        }

        String rootPath = context.getFilesDir().getAbsolutePath() + "/custle.cert/";
        if (!FileUtil.isFileExist(rootPath)) {
            return;
        }

        // 判断二方密钥证书路径是否存在
        String newTwoPath = rootPath + userId + appId + "_" + Constants.YYQ_ALG_VERSION_2;
        if (!FileUtil.isFileExist(newTwoPath)) {
            String twoPath = rootPath + userId + appId + Constants.YYQ_ALG_VERSION_2;
            if (FileUtil.isFileExist(twoPath)) {
                if (FileUtil.copyFolder(twoPath, newTwoPath)) {
                    FileUtil.deleteDirectory(twoPath);
                }
            }
        }

        // 判断三方密钥证书路径是否存在
        String newThreePath = rootPath + userId + appId + "_";
        if (!FileUtil.isFileExist(newThreePath)) {
            String threePath = rootPath + userId + appId;
            if (FileUtil.isFileExist(threePath)) {
                if (FileUtil.copyFolder(threePath, newThreePath)) {
                    FileUtil.deleteDirectory(threePath);
                }
            }
        }
    }
}
