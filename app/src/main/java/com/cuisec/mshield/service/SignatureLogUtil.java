package com.cuisec.mshield.service;

import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.utils.L;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLEncoder;

import okhttp3.Call;

public class SignatureLogUtil {

    public static void writeLog(SignatureLogBean bean) {
//        writeLocalLog(bean);
        writeRemoteLog(bean);
    }

//    private static void writeLocalLog(SignatureLogBean bean) {
//        String action = "";
//        switch (bean.getAction()){
//            case Constants.LOG_TYPE_1:{
//                action = Constants.LOG_TYPE_DESC_1;
//                break;
//            }
//            case Constants.LOG_TYPE_2:{
//                action = Constants.LOG_TYPE_DESC_2;
//                break;
//            }
//            case Constants.LOG_TYPE_3:{
//                action = Constants.LOG_TYPE_DESC_3;
//                break;
//            }
//        }
//        SignLogDB signLog = new SignLogDB(null, bean.getAppId(), action, bean.getMsg(), bean.getDesc(), bean.getSignValue(), bean.getCertSn(), new Date().getTime());
//        SignLogHelper.insert(MyApplication.getInstance(), signLog);
//    }

    private static void writeRemoteLog(SignatureLogBean bean) {
        try {
            OkHttpUtils.post()
                    .url(SPManager.getServerUrl() + Config.sign_log)
                    .addHeader("token", SPManager.getUserToken())
                    .addParams("bizSn", bean.getBizSn())
                    .addParams("appId", bean.getAppId())
                    .addParams("action", bean.getAction())
                    .addParams("msg", URLEncoder.encode(bean.getMsg(), Config.UTF_8))
                    .addParams("signValue", URLEncoder.encode(bean.getSignValue(), Config.UTF_8))
                    .addParams("signAlg", bean.getSignAlg())
                    .addParams("certSn", bean.getCertSn())
                    .build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    L.e(e.getLocalizedMessage());
                }

                @Override
                public void onResponse(String response, int id) {
                    L.d(response);
                }
            });
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
    }
}
