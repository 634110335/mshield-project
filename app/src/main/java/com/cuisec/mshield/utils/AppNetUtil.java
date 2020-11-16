package com.cuisec.mshield.utils;

import android.content.Context;
import android.util.Log;

import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.bean.MMBean;
import com.cuisec.mshield.bean.SMSBean;
import com.cuisec.mshield.bean.SealBean;
import com.cuisec.mshield.bean.SetSealBean;
import com.cuisec.mshield.bean.UserQueryBean;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;

import okhttp3.Call;

/**
 * Created by licz on 2018/6/5.
 */

public class AppNetUtil {


    public interface userQueryCallBack {
        void onSuccess(UserInfo userInfo);
        void onFailure(String errMsg);
    }

    // 获取用户信息
    public static void userQuery(final Context context, final userQueryCallBack callBack) {
        OkHttpUtils
                .post()
                .url(SPManager.getServerUrl() + Config.user_query)
                .addHeader("token", SPManager.getUserToken())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (callBack != null) {
                            callBack.onFailure(e.getLocalizedMessage());
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            response = URLDecoder.decode(response, Config.UTF_8);
                            UserQueryBean bean = (UserQueryBean) JsonUtil.toObject(response, UserQueryBean.class);
                            if (bean.getRet() == 0 && bean.getData() != null) {
                                final UserInfo userInfo = new UserInfo();
                                userInfo.userId = bean.getData().getUserId();
                                userInfo.uuid = bean.getData().getUuid();
                                userInfo.userName = bean.getData().getUserName();
                                userInfo.nickName = bean.getData().getNickName();
                                userInfo.userType = bean.getData().getUserType();
                                userInfo.idNo = bean.getData().getIdNo();
                                userInfo.email = bean.getData().getEmail();
                                userInfo.phone = bean.getData().getPhone();
                                userInfo.company = bean.getData().getCompany();
                                userInfo.province = bean.getData().getProvince();
                                userInfo.city = bean.getData().getCity();
                                userInfo.address = bean.getData().getAddress();
                                userInfo.logo = bean.getData().getLogo();
                                userInfo.industryCode = bean.getData().getIndustryCode();
                                userInfo.authStatus = bean.getData().getAuthStatus();
                                userInfo.createTime = bean.getData().getCreateTime();
                                userInfo.wxId = bean.getData().getWechatId();

                                // 修改证书存储路径，解决证书文件夹串号的问题
                                Util.changeCertFolderName(context, userInfo.idNo, SPManager.getServerCode());

                                // 默认3方方案
                                if (bean.getData().getAlgVersion() != null && bean.getData().getAlgVersion().equals(Constants.YYQ_ALG_VERSION_2)) {
                                    userInfo.algVersion = Constants.YYQ_ALG_VERSION_2;
                                } else {
                                    userInfo.algVersion = "";
                                }

                                // 服务是2方方案，若有3方方案有证书，则兼容3方方案
                                if (userInfo.algVersion.equals(Constants.YYQ_ALG_VERSION_2)) {
                                    String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                                    MKeyApi.getInstance(MyApplication.getInstance(), null, strUserInfo, Constants.YYQ_ALG_VERSION_1).getCert(new MKeyApiCallback() {
                                        @Override
                                        public void onMKeyApiCallBack(MKeyApiResult result) {
                                            if (result.getCode().equals("0") && result.getData() != null && result.getData().length() != 0) {
                                                userInfo.algVersion = "";
                                            }

                                            // 存储用户信息
                                            SPManager.setLoginState(true);
                                            SPManager.setUserInfo(userInfo);

                                            if (callBack != null) {
                                                callBack.onSuccess(userInfo);
                                            }
                                        }
                                    });
                                } else {
                                    // 存储用户信息
                                    SPManager.setLoginState(true);
                                    SPManager.setUserInfo(userInfo);

                                    if (callBack != null) {
                                        callBack.onSuccess(userInfo);
                                    }
                                }

                                // 小米推送设置别名
                                if (SPManager.getMsgPush()) {
                                    MiPushClient.setAlias(MyApplication.getInstance(), SecurityUtil.sha256(userInfo.phone + SPManager.getServerCode()), null);
                                } else {
                                    MiPushClient.unsetAlias(MyApplication.getInstance(), SecurityUtil.sha256(userInfo.phone + SPManager.getServerCode()), null);
                                }
                            } else {
                                if (callBack != null) {
                                    callBack.onFailure(bean.getMsg());
                                }
                            }
                        } catch (Exception e) {
                            if (callBack != null) {
                                callBack.onFailure(e.getLocalizedMessage());
                            }
                        }
                    }
                });
    }

    public interface AppCodeCallBack {
        void onCode(String code);
        void onFailure(String errMsg);
    }

    // 获取应用SDK授权码
    public static void getAppCode(final AppCodeCallBack callBack) {
        try {
            String time = DateUtil.dateToString(new Date());
            String packageName = AppInfoUtil.getPackageName(MyApplication.getInstance().getApplicationContext());
            String indata = time + "##" + packageName + "##" + SecurityUtil.SIGN_ALGORITHMS;
            String signValue = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.sdk_auth)
                    .addHeader("token", SPManager.getUserToken())
                    .addParams("time", time)
                    .addParams("packageName", packageName)
                    .addParams("signAlg", SecurityUtil.SIGN_ALGORITHMS)
                    .addParams("sign", URLEncoder.encode(signValue, Config.UTF_8))
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (callBack != null) {
                                callBack.onFailure(e.getLocalizedMessage());
                            }
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                SMSBean bean = (SMSBean) JsonUtil.toObject(response, SMSBean.class);
                                if (bean.getRet() == 0 && bean.getData() != null) {
                                    SPManager.setSDKAuthCode(bean.getData().getCode());
                                    if (callBack != null) {
                                        callBack.onCode(bean.getData().getCode());
                                    }
                                } else {
                                    if (callBack != null) {
                                        callBack.onFailure(bean.getMsg());
                                    }
                                }
                            } catch (Exception e) {
                                if (callBack != null) {
                                    callBack.onFailure(e.getLocalizedMessage());
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            if (callBack != null) {
                callBack.onFailure(e.getLocalizedMessage());
            }
        }
    }

    // 免密状态回调:  1:未设置免密  2:已设置免密，需要确认  3:已设置免密，不需要确认  0：接口异常
    public interface MMCallBack {
        void onStatus(Integer status);
    }

    // 设置服务器当前用户证书，获取用户当前免密状态
    public static void setServerUserCert(String certSn, final MMCallBack callBack) {
        OkHttpUtils
                .post()
                .url(SPManager.getServerUrl() + Config.user_certloginstatus)
                .addHeader("token", SPManager.getUserToken())
                .addParams("certSn", certSn)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (callBack != null) {
                            callBack.onStatus(0);
                        }
                    }
                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            response = URLDecoder.decode(response, Config.UTF_8);
                            MMBean bean = (MMBean) JsonUtil.toObject(response, MMBean.class);
                            if (bean.getRet() == 0) {
                                if (bean.getData().getTrust() == 3) {
                                    SPManager.setSignFreeStatus(true);
                                } else {
                                    SPManager.setSignFreeStatus(false);
                                }
                                if (callBack != null) {
                                    callBack.onStatus(bean.getData().getTrust());
                                }
                            } else {
                                if (callBack != null) {
                                    callBack.onStatus(0);
                                }
                            }
                        } catch (Exception e) {
                            if (callBack != null) {
                                callBack.onStatus(0);
                            }
                        }
                    }
                });
    }

    // 签名提醒回调：true: 提示 false: 不提示
    public interface SetSealCallBack {
        void onStatus(boolean bStatus);
    }

    // 获取应用是否设置手写签名提醒
    public static void GetSealTipStatus(final SetSealCallBack callBack) {
        OkHttpUtils
                .post()
                .url(SPManager.getServerUrl() + Config.get_seal_tip_status)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (callBack != null) {
                            callBack.onStatus(false);
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            response = URLDecoder.decode(response, Config.UTF_8);
                            SetSealBean bean = (SetSealBean) JsonUtil.toObject(response, SetSealBean.class);
                            if (bean != null && bean.getRet() == 0 && bean.getData() != null) {
                                if (callBack != null) {
                                    callBack.onStatus(bean.getData().getSeal());
                                }
                            } else {
                                if (callBack != null) {
                                    callBack.onStatus(false);
                                }
                            }
                        } catch (Exception e) {
                            if (callBack != null) {
                                callBack.onStatus(false);
                            }
                        }
                    }
                });
    }

    public interface GetSealCallBack {
        void sealValue(String seal);
    }

    // 获取手写签名图片
    public static void getSeal(final GetSealCallBack callBack) {
        try {
            OkHttpUtils
                    .get()
                    .url(SPManager.getServerUrl() + Config.user_seal)
                    .addHeader("token", SPManager.getUserToken())
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (callBack != null) {
                                callBack.sealValue("");
                            }
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                SealBean bean = (SealBean) JsonUtil.toObject(response, SealBean.class);
                                if (bean != null && bean.getRet() == 0 && bean.getData() != null && bean.getData().getSeal() != null && bean.getData().getSeal().length() != 0) {
                                    if (callBack != null) {
                                        callBack.sealValue(bean.getData().getSeal());
                                    }
                                } else {
                                    if (callBack != null) {
                                        callBack.sealValue("");
                                    }
                                }
                            } catch (Exception e) {
                                if (callBack != null) {
                                    callBack.sealValue("");
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            if (callBack != null) {
                callBack.sealValue("");
            }
        }
    }

    public static void postAppLog(Context context, String strType, String errInfo) {
        try {
            String strDate = DateUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss");
            String appVersion = AppInfoUtil.getVersionName(context);
            String strUuid = MKeyApi.getUuid();
            String phontModel = android.os.Build.BRAND + ";" + android.os.Build.MODEL + ";" + android.os.Build.VERSION.RELEASE;
            String strMsg = "{\"date\":\"" + strDate + "\",\"phone\":\"" + SPManager.getUserInfo().phone + "\",\"type\":\"" + strType + "\",\"android\":\"" + phontModel + "\",\"uuid\":\"" + strUuid + "\",\"version\":\"" + appVersion + "\",\"data\":" + errInfo + "}";
            Log.d("mkey_log", "log param: " + strMsg);
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.base_log)
                    .addHeader("token", SPManager.getUserToken())
                    .addParams("logmsg", URLEncoder.encode(strMsg, Config.UTF_8))
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(String response, int id) {
                        }
                    });
        } catch (Exception e) {

        }
    }
}
