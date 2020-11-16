package com.cuisec.mshield.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;

import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.utils.NetUtil;

import java.util.ArrayList;

/**
 * Created by licz on 2018/6/8.
 */

public class NetworkStatusReceiver extends BroadcastReceiver {

    public static ArrayList<NetworkEventHandler> mListeners = new ArrayList<NetworkEventHandler>();
    private static String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                MyApplication.mNetWorkState = NetUtil.getNetworkState(context);

                if (mListeners.size() > 0)// 通知接口完成加载
                    for (NetworkEventHandler handler : mListeners) {
                        handler.onNetworkChange();
                    }
            }
        } else {
            if (intent.getAction().equals(NET_CHANGE_ACTION)) {
                MyApplication.mNetWorkState = NetUtil.getNetworkState(context);

                if (mListeners.size() > 0)// 通知接口完成加载
                    for (NetworkEventHandler handler : mListeners) {
                        handler.onNetworkChange();
                    }
            }
        }
    }

    public interface NetworkEventHandler {
        void onNetworkChange();
    }

}
