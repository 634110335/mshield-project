package com.cuisec.mshield.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.login.LoginActivity;
import com.cuisec.mshield.activity.mine.MineAuthActivity;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.widget.AlertDialog;
import com.cuisec.mshield.widget.finger.FingerprintIdentify;
import com.cuisec.mshield.widget.finger.base.BaseFingerprint;

/**
 * Created by licz on 2018/6/5.
 */

public class AppUtil {

    // 是否登录
    public static boolean isLogin(Activity activity) {
        if (!SPManager.getLoginState() || SPManager.getUserInfo() == null) {
            Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
            activity.finish();
            return false;
        }
        return true;
    }

    // 是否登录和实名认证
    public static boolean isLoginWithAuth(Activity activity) {
        return isLoginWithAuth(activity, false, null, null);
    }

    public static boolean isLoginWithAuth(final Activity activity, boolean bAlert, String title, String message) {

        if (!SPManager.getLoginState() || SPManager.getUserInfo() == null) {
            Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
            activity.finish();
            return false;
        }

        if (SPManager.getUserInfo().authStatus.equals(Constants.AUTH_STATUS_NONE)) {
            if (bAlert) {
                new AlertDialog(activity).builder()
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(activity.getString(R.string.app_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(activity, MineAuthActivity.class);
                                activity.startActivity(intent);
                                activity.finish();
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.app_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
            } else {
                Intent intent = new Intent(activity, MineAuthActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public static void fingerVerify(Context context, final VeifyCallBack callBack) {
        final FingerprintIdentify fingerIdentify = new FingerprintIdentify(context, null);
        if (!fingerIdentify.isRegisteredFingerprint()) {
            callBack.onFailure("请先录入指纹");
            return;
        }

        final AlertDialog dialog = new AlertDialog(context)
                .builder()
                .setImage(R.mipmap.ico_zhiwen)
                .setMessage("验证已有手机指纹")
                .setPositiveButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fingerIdentify.cancelIdentify();
                        callBack.onFailure("取消");
                    }
                });
        dialog.show();

        fingerIdentify.startIdentify(1, new BaseFingerprint.FingerprintIdentifyListener() {
            @Override
            public void onSucceed() {
                callBack.onSuccess("");
                dialog.dismiss();
            }

            @Override
            public void onNotMatch(int availableTimes) {
                callBack.onFailure("匹配错误");
                dialog.dismiss();
            }

            @Override
            public void onFailed(boolean isDeviceLocked) {
                callBack.onFailure("验证失败");
                dialog.dismiss();
            }

            @Override
            public void onStartFailedByDeviceLocked() {
                callBack.onFailure("验证失败");
                dialog.dismiss();
            }
        });
    }

    public static void signVerifyPin(final Context context, final VeifyCallBack callBack) {
        if (SPManager.getFingerSignStatus()) {
            final FingerprintIdentify fingerIdentify = new FingerprintIdentify(context, null);
            if (!fingerIdentify.isRegisteredFingerprint()) {
                callBack.onFailure("请先录入指纹");
                return;
            }

            final AlertDialog dialog = new AlertDialog(context)
                    .builder()
                    .setImage(R.mipmap.ico_zhiwen)
                    .setMessage("验证已有手机指纹")
                    .setPositiveButton("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fingerIdentify.cancelIdentify();
                            if (callBack != null) {
                                callBack.onFailure("取消");
                            }
                        }
                    });
            dialog.show();

            fingerIdentify.startIdentify(1, new BaseFingerprint.FingerprintIdentifyListener() {
                @Override
                public void onSucceed() {
                    if (callBack != null) {
                        callBack.onSuccess(SPManager.getCertPin());
                    }
                    dialog.dismiss();
                }

                @Override
                public void onNotMatch(int availableTimes) {
                    if (callBack != null) {
                        callBack.onFailure("匹配错误");
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailed(boolean isDeviceLocked) {
                    if (callBack != null) {
                        callBack.onFailure("验证失败");
                    }
                    dialog.dismiss();
                }

                @Override
                public void onStartFailedByDeviceLocked() {
                    if (callBack != null) {
                        callBack.onFailure("验证失败");
                    }
                    dialog.dismiss();
                }
            });
        } else {
            final UserInfo userInfo = SPManager.getUserInfo();
            final String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
            MKeyApi.getInstance(context, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).verifyPin(new MKeyApiCallback() {
                @Override
                public void onMKeyApiCallBack(final MKeyApiResult result) {
                    if (result.getCode().equals("0")) {
                        if (callBack != null) {
                            callBack.onSuccess(result.getData());
                        }
                    } else {
                        if (callBack != null) {
                            callBack.onFailure(result.getMsg());
                        }

                        postVerifyPinLog(context, strUserInfo, result.getData(), result.getMsg());
                    }
                }
            });
        }
    }

    public interface VeifyCallBack {
        void onSuccess(String pin);
        void onFailure(String errMsg);
    }


    public static void postVerifyPinLog(final Context context, String userInfo, final String inputPin, final String errMsg) {
//        MKeyApi.getInstance(context, "", userInfo, SPManager.getUserInfo().algVersion).getHashPin(new MKeyApiCallback() {
//            @Override
//            public void onMKeyApiCallBack(MKeyApiResult checkResult) {
//                String errInfo = "{\"input_pin\":\"" + inputPin + "\",\"check_pin\":\"" + checkResult.getData() + "\",\"result_msg\":\"" + errMsg + "\"}";
//                AppNetUtil.postAppLog(Context, Constants.LOG_VERIFY_PIN, errInfo);
//            }
//        });
    }


    public static void postSignVerifyLog(final Context context, String userInfo, String pin, final String certSn, final String signSrc, final String signData) {
        MKeyApi.getInstance(context, SPManager.getSDKAuthCode(), userInfo, SPManager.getUserInfo().algVersion).getKey(pin, new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                String errInfo = "{\"certSn\":\"" + certSn + "\",\"key\":\"" + result.getData() + "\",\"signSrc\":\"" + signSrc + "\",\"signData\":\"" + signData + "\"}";
                AppNetUtil.postAppLog(context, Constants.LOG_SIGN_VERIFY, errInfo);
            }
        });
    }
}
