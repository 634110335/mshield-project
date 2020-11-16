package com.cuisec.mshield.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.UserInfo;

/**
 * Created by licz on 2018/6/5.
 */

public class MineBroadcastReceiver extends BroadcastReceiver {

    private static final String TYPE_LOGIN = "TYPE_LOGIN";  //登录状态改变
    private static final String TYPE_PHOTO = "TYPE_PHOTO";  //头像改变
    private static final String TYPE_PHONE = "TYPE_PHONE";  //手机号改变
    private static final String TYPE_AUTH = "TYPE_AUTH";    //实名认证改成

    private MineReceiverCallBack mCallBack;
    public MineBroadcastReceiver(MineReceiverCallBack callBack) {
        mCallBack = callBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constants.MINE_UPDATA_BROADCAST.equals(action)) {
            String type = intent.getStringExtra("type");
            switch (type) {
                case TYPE_LOGIN:
                    UserInfo userInfo = (UserInfo)intent.getSerializableExtra("user_info");
                    mCallBack.onMineLoginUpdate(userInfo);
                    break;
                case TYPE_PHOTO:
                    String photoStr = (String)intent.getSerializableExtra("value");
                    mCallBack.onMinePhotoUpdate(photoStr);
                    break;
                case TYPE_PHONE:
                    mCallBack.onMinePhoneUpdate(intent.getStringExtra("value"));
                    break;
                case TYPE_AUTH:
                    mCallBack.onMineAuthUpdate(intent.getStringExtra("value"));
                    break;
                default:
                    break;
            }
        }
    }

    public interface MineReceiverCallBack {
        void onMineLoginUpdate(UserInfo userInfo);
        void onMinePhotoUpdate(String photo);
        void onMinePhoneUpdate(String phone);
        void onMineAuthUpdate(String auth);
    }
}
