package com.cuisec.mshield.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.utils.AppNetUtil;

public class HomeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constants.HOME_SDK_CODE_BROADCAST.equals(action)) {
            AppNetUtil.getAppCode(null);
        }
    }
}