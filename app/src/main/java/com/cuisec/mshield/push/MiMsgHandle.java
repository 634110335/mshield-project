package com.cuisec.mshield.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.cuisec.mshield.bean.QRCodeBean;
import com.cuisec.mshield.activity.cert.CertLoginActivity;
import com.cuisec.mshield.activity.cert.CertPushActivity;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.YAppManager;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.T;

public class MiMsgHandle extends Handler {
    private Context context;

    public MiMsgHandle(Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            String s = (String) msg.obj;
            if (s != null || !"".equals(s)) {
                QRCodeBean QRCodeBean = (QRCodeBean) JsonUtil.toObject(s, QRCodeBean.class);
                if (QRCodeBean != null) {
                    if (Constants.QRCODE_SIGN.equals(QRCodeBean.getAction())) {
                        if (YAppManager.getInstance().isSignBusyStatus()) {
                            T.showShort("正在签名中，请稍后进行推送");
                            return;
                        }

                        Intent intent = new Intent(context, CertPushActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Bundle signBundle = new Bundle();
                        signBundle.putString("data", s);
                        intent.putExtras(signBundle);
                        context.startActivity(intent);
                    } else if (Constants.QRCODE_LOGIN.equals(QRCodeBean.getAction())) {
                        if (YAppManager.getInstance().isSignBusyStatus()) {
                            T.showShort("正在签名中，请稍后进行推送");
                            return;
                        }
                        Intent intent = new Intent(context, CertLoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Bundle signBundle = new Bundle();
                        signBundle.putString("data", s);
                        intent.putExtras(signBundle);
                        context.startActivity(intent);
                    }
                }
            }
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
    }
}
