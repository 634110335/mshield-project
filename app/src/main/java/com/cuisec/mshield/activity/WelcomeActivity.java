package com.cuisec.mshield.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.bean.UserLoginBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.StatusBarUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.home.HomeActivity;
import com.cuisec.mshield.activity.login.LoginActivity;
import com.cuisec.mshield.activity.mine.safe.GestureVerifyActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;

import okhttp3.Call;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // 设置状态栏透明和文本颜色
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.StatusBarLightMode(this);

        if (SPManager.getLoginState() && (SPUtils.get(WelcomeActivity.this, Constants.YYQ_LOGIN_TYPE_SP, Constants.YYQ_LOGIN_TYPE_OTHER) == Constants.YYQ_LOGIN_TYPE_TEMP)) {
            long currentTime = new Date().getTime(); // 获取当前时间
            long endTime = (long)SPUtils.get(WelcomeActivity.this, Constants.YYQ_TEMP_TEMP_SP, (long)0);
            if (currentTime > endTime) {
                SPManager.setLoginState(false);
                T.showShort(WelcomeActivity.this, "临时登录已到期!");
                startLogin();
                return;
            }
        }

        if (SPManager.getLoginState()) {
            intoActivity();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startLogin();
                }
            }, 2000);
        }
    }

    private void intoActivity() {
        try {
            String refreshToken = SPManager.getRefreshToken();
            String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, refreshToken);
            // 同步调用token刷新接口
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.user_refresh)
                    .addParams("refreshToken", refreshToken)
                    .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            startLogin();
                        }
                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                UserLoginBean bean = (UserLoginBean) JsonUtil.toObject(response, UserLoginBean.class);
                                if (bean != null) {
                                    if (bean.getRet() == 0 && bean.getData() != null) {
                                        // 保存Token
                                        SPManager.setUserToken(bean.getData().getToken());
                                        SPManager.setRefreshToken(bean.getData().getRefreshToken());
                                        boolean bStatus = SPManager.getFingerLockStatus();
                                        if (SPManager.getLoginState()) {
                                            // 判断是否设置指纹或手势
                                            if (MyApplication.getInstance().getLockPatternUtils().savedPatternExists() || bStatus) {
                                                Intent intent = new Intent(WelcomeActivity.this, GestureVerifyActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        } else {
                                            startLogin();
                                        }
                                    } else {
                                        // 清除登录状态
                                        SPManager.setLoginState(false);
                                        startLogin();
                                    }
                                } else {
                                    startLogin();
                                }
                            } catch (Exception e) {
                                startLogin();
                            }
                        }
                    });
        } catch (Exception e) {
            startLogin();
        }
    }

    private void startLogin() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}