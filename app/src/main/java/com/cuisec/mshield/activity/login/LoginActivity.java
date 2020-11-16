package com.cuisec.mshield.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.cuisec.mshield.activity.common.WebViewActivity;
import com.cuisec.mshield.activity.home.HomeActivity;
import com.cuisec.mshield.bean.BaseBean;
import com.cuisec.mshield.bean.CertInfoBean;
import com.cuisec.mshield.bean.SMSBean;
import com.cuisec.mshield.bean.UserLoginBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.ActivityManager;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.StatusBarUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.ValidateUtil;
import com.cuisec.mshield.widget.AlertDialog;
import com.cuisec.mshield.widget.LoadDialog;
import com.cuisec.mshield.widget.LoginPopDialog;
import com.cuisec.mshield.widget.LoginPopView;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.cuisec.mshield.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import java.net.URLDecoder;
import java.net.URLEncoder;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_phone_et)
    EditText mPhoneET;
    @BindView(R.id.login_code_btn)
    Button mLoginCodeBtn;
    @BindView(R.id.login_code_et)
    EditText mLoginCodeET;

    private LoadDialog mLoadDlg = null;
    private TimeCount mTime;

    private Integer mClickCount = 0;
    private long mClickTime = 0;

    private long mExitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        ActivityManager.getInstance().closeAllActivity();

        // 设置状态栏透明和文本颜色
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.StatusBarLightMode(this);

        // 设置上一次登录的账号
        String account = SPManager.getUserAccount();
        if (account != null && !account.equals("")) {
            mPhoneET.setText(account);
            mPhoneET.requestFocus();      // 将光标设置到最后
        }
    }

    @OnClick({R.id.login_login_btn, R.id.login_agree_ll, R.id.login_server_btn, R.id.layout_login_rl, R.id.login_code_btn, R.id.login_app_logo_iv, R.id.login_more_iv})
    public void onViewClicked(View view) {
        ActivityManager.getInstance().putActivity(this.getLocalClassName(), this);
        switch (view.getId()) {
            case R.id.login_login_btn: {
                String phoneStr = mPhoneET.getText().toString();
                if ("".equals(phoneStr)) {
                    T.showShort(LoginActivity.this, getString(R.string.mobile_tip));
                    return;
                }
                if (!ValidateUtil.validateMobile(phoneStr)) {
                    T.showShort(LoginActivity.this, getString(R.string.mobile_error));
                    return;
                }
                String loginCode = mLoginCodeET.getText().toString();
                if ("".equals(loginCode)) {
                    T.showShort(LoginActivity.this, getString(R.string.code_tip));
                    return;
                }

                // 获取SDK授权码
                Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                sendBroadcast(intent);
                // 用户登录
                userLogin();

                break;
            }
            case R.id.login_agree_ll:
            case R.id.login_server_btn: {
                Intent intent = new Intent(LoginActivity.this, WebViewActivity.class);
                intent.putExtra("title", "服务条款");
                intent.putExtra("url", "file:///android_asset/server_protocol.html");
                startActivity(intent);
                break;
            }
            case R.id.layout_login_rl: {
                hideKeyBoard(); // 隐藏键盘
                break;
            }
            case R.id.login_code_btn: {
                String accountStr = mPhoneET.getText().toString();
                if (accountStr.length() == 0) {
                    T.showShort(this, getString(R.string.mobile_tip));
                    return;
                }

                if (!ValidateUtil.validateMobile(accountStr)) {
                    T.showShort(this, getString(R.string.mobile_error));
                    return;
                }
                getMsgCode();
                break;
            }

            case R.id.login_app_logo_iv: {
                //开发者模式
                mClickCount ++;
                long time = System.currentTimeMillis();
                if ((time - mClickTime) > 1000) {
                    mClickCount = 0;
                    mClickTime = time;
                }
                if (mClickCount == 5 && (time - mClickTime) <= 1000) {
                   Intent intent = new Intent(this, DeveloperActivity.class);
                   startActivity(intent);
                }
                break;
            }
            case R.id.login_more_iv: {
                LoginPopDialog popDialog = new LoginPopDialog(this);
                popDialog.showPopDialog(new LoginPopView.OnPopTempLogin() {
                    @Override
                    public void onPopTempLogin() {
                        Intent intent = new Intent(LoginActivity.this, LoginTempActivity.class);
                        startActivity(intent);
                    }
                });
                break;
            }
        }
    }


    // 用户登录
    private void userLogin() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(LoginActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        try {
            String phone = mPhoneET.getText().toString();
            SPUtils.put(this,Constants.SAVE_USER_PHONE,phone);
            final String strLoginCode = mLoginCodeET.getText().toString();
            String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, phone + "##" + strLoginCode);
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.user_sms_login)
                    .addParams("phone", phone)
                    .addParams("code", URLEncoder.encode(strLoginCode, Config.UTF_8))
                    .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            L.i(e.getMessage());
                            T.showShort(LoginActivity.this, getString(R.string.app_network_error));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                UserLoginBean bean = (UserLoginBean) JsonUtil.toObject(response, UserLoginBean.class);

                                if (bean.getRet() == 0) {
                                    SPManager.setUserAccount(mPhoneET.getText().toString());
                                    SPManager.setUserToken(bean.getData().getToken());
                                    SPManager.setRefreshToken(bean.getData().getRefreshToken());
                                    userQuery();
                                } else {
                                    if (mLoadDlg != null) {
                                        mLoadDlg.dismiss();
                                        mLoadDlg = null;
                                    }
                                    T.showShort(LoginActivity.this, bean.getMsg());
                                }
                            } catch (Exception e) {
                                if (mLoadDlg != null) {
                                    mLoadDlg.dismiss();
                                    mLoadDlg = null;
                                }
                                T.showShort(LoginActivity.this, e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(LoginActivity.this, e.getLocalizedMessage());
        }
    }

    private void userQuery() {
        AppNetUtil.userQuery(LoginActivity.this, new AppNetUtil.userQueryCallBack() {
            @Override
            public void onSuccess(final UserInfo userInfo) {
                SPUtils.put(LoginActivity.this, Constants.YYQ_LOGIN_TYPE_SP, Constants.YYQ_LOGIN_TYPE_OTHER);
                String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                MKeyApi.getInstance(LoginActivity.this, null, strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
                    @Override
                    public void onMKeyApiCallBack(MKeyApiResult result) {
                        if (result.getCode().equals("0")) {
                            final CertInfoBean bean = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                            AppNetUtil.setServerUserCert(bean.getCertSn(), new AppNetUtil.MMCallBack() {
                                @Override
                                public void onStatus(Integer status) {
                                    // 判断是否免密
                                    if (SPManager.getServerMM() && status != 3) {
                                        // 提醒设置免密
                                        new AlertDialog(LoginActivity.this).builder()
                                                .setTitle("免密签名设置")
                                                .setMessage("是否设置免密签名？")
                                                .setPositiveButton("是", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        getKey1(bean.getCertSn());
                                                    }
                                                })
                                                .setNegativeButton("否", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        loginSuccessIntoHome(getString(R.string.login_success));
                                                    }
                                                }).show();
                                    } else {
                                        loginSuccessIntoHome(getString(R.string.login_success));
                                    }
                                }
                            });
                        } else {
                            AppNetUtil.setServerUserCert("", null);
                            loginSuccessIntoHome(getString(R.string.login_success));
                        }
                    }
                });
            }

            @Override
            public void onFailure(String errMsg) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                T.showShort(LoginActivity.this, errMsg);
            }
        });
    }


    private void getKey1(final String strCertSn) {
        AppNetUtil.getAppCode(new AppNetUtil.AppCodeCallBack() {
            @Override
            public void onCode(final String code) {
                AppUtil.signVerifyPin(LoginActivity.this, new AppUtil.VeifyCallBack() {
                    @Override
                    public void onSuccess(String pin) {
                        UserInfo userInfo = SPManager.getUserInfo();
                        final String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                        MKeyApi.getInstance(LoginActivity.this, code, strUserInfo, SPManager.getUserInfo().algVersion).getKey(pin, new MKeyApiCallback() {
                            @Override
                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                if (result.getCode().equals("0")) {
                                    trustConfig(strCertSn, result.getData());
                                } else {
                                    loginSuccessIntoHome(result.getMsg());
                                }
                            }
                        });
                    }
                    @Override
                    public void onFailure(String errMsg) {
                        loginSuccessIntoHome(errMsg);
                    }
                });
            }

            @Override
            public void onFailure(String errMsg) {
                loginSuccessIntoHome(errMsg);
            }
        });
    }

    private void trustConfig(String certSn, String key) {
        try {
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.sign_trust_config)
                    .addHeader("token", SPManager.getUserToken())
                    .addParams("certSn", certSn)
                    .addParams("key", URLEncoder.encode(key, Config.UTF_8))
                    .addParams("alg", "SM2")
                    .addParams("algVersion", SPManager.getUserInfo().algVersion)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            loginSuccessIntoHome(getString(R.string.app_network_error));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                BaseBean bean = (BaseBean) JsonUtil.toObject(response, BaseBean.class);
                                if (bean.getRet() == 0) {
                                    SPManager.setSignFreeStatus(true);
                                    loginSuccessIntoHome("免密设置成功");
                                } else {
                                    loginSuccessIntoHome(bean.getMsg());
                                }
                            } catch (Exception e) {
                                loginSuccessIntoHome(e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            loginSuccessIntoHome(e.getLocalizedMessage());
        }
    }


    private void loginSuccessIntoHome(String strMsg) {
        if (mLoadDlg != null) {
            mLoadDlg.dismiss();
            mLoadDlg = null;
        }
        T.showShort(getApplicationContext(), strMsg);
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }


    public void hideKeyBoard() {
        try {
            if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                if (getCurrentFocus() != null)
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus()
                                            .getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            if (LoginActivity.this.getCurrentFocus() != null) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(LoginActivity.this
                                        .getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    // 获取验证码
    private void getMsgCode() {
        mTime = new TimeCount(60000, 1000);
        mTime.start();//开始计时

        try {
            String phone = mPhoneET.getText().toString();
            String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, phone);

            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.sms_send)
                    .addParams("phone", phone)
                    .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
                    .addParams("smsType", Constants.SMS_SEND_LOGIN)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            mTime.cancel();
                            mTime = null;
                            mLoginCodeBtn.setClickable(true);
                            mLoginCodeBtn.setText(getString(R.string.code_get));
                            mLoginCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                            T.showShort(getApplicationContext(), e.getLocalizedMessage());
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                SMSBean bean = (SMSBean) JsonUtil.toObject(response, SMSBean.class);
                                L.i(response);
                                if (bean != null && bean.getRet() != 0) {
                                    T.showShort(getApplicationContext(), bean.getMsg());

                                    mTime.cancel();
                                    mTime = null;

                                    mLoginCodeBtn.setClickable(true);
                                    mLoginCodeBtn.setText(getString(R.string.code_get));
                                    mLoginCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                }
                            } catch (Exception e) {
                                L.e(e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
    }

    //短信倒计时
    private class TimeCount extends CountDownTimer {
        private TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);   // 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {    // 计时完毕时触发
            mLoginCodeBtn.setClickable(true);
            mLoginCodeBtn.setText(getString(R.string.code_re_get));
            mLoginCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

            mTime.cancel();
            mTime = null;
        }

        @Override
        public void onTick(long millisUntilFinished) {  // 计时过程显示
            mLoginCodeBtn.setClickable(false);
            mLoginCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.font_gray));
            mLoginCodeBtn.setText(getString(R.string.code_re_get) + "(" + millisUntilFinished / 1000 + ")");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTime != null) {
            mTime.cancel();
            mTime = null;
        }
        ActivityManager.getInstance().closeActivity(this.getLocalClassName());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, R.string.app_exit_tip, Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                this.startActivity(intent);
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
