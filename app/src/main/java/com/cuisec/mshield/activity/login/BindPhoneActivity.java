package com.cuisec.mshield.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.cuisec.mshield.activity.common.LoginBaseActivity;
import com.cuisec.mshield.activity.home.HomeActivity;
import com.cuisec.mshield.bean.CertInfoBean;
import com.cuisec.mshield.bean.SMSBean;
import com.cuisec.mshield.bean.UserLoginBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.ActivityManager;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.ValidateUtil;
import com.cuisec.mshield.widget.LoadDialog;
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

public class BindPhoneActivity extends LoginBaseActivity {

    @BindView(R.id.bind_phone_account_et)
    EditText mPhoneET;     //手机号框
    @BindView(R.id.bind_phone_code_et)
    EditText mCodeET;    //验证码框
    @BindView(R.id.bind_phone_code_btn)
    Button mCodeBtn;         //获取验证码按钮

    private TimeCount mTime;
    private LoadDialog mLoadDlg = null;
    private String openId;

    private UserInfo mUserInfo;

    boolean mIsSelDept = false;  // 是否选择机构


    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_bind_phone);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("绑定手机号");
    }

    @Override
    protected void initializeData() {

    }


    @OnClick({R.id.bind_phone_rl, R.id.bind_phone_code_btn, R.id.bind_phone_submit_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bind_phone_rl: {
                hideKeyBoard();
                break;
            }
            case R.id.bind_phone_code_btn: {
                hideKeyBoard();
                String accountStr = mPhoneET.getText().toString();
                if (accountStr == null || accountStr.length() == 0) {
                    T.showShort(getApplicationContext(), getString(R.string.mobile_tip));
                    return;
                }

                if (!ValidateUtil.validateMobile(accountStr)) {
                    T.showShort(getApplicationContext(), getString(R.string.mobile_error));
                    return;
                }

                getMsgCode();

                break;
            }
            case R.id.bind_phone_submit_btn: {
                hideKeyBoard();
                String phoneStr = mPhoneET.getText().toString();
                if ("".equals(phoneStr)) {
                    T.showShort(getApplicationContext(), getString(R.string.mobile_tip));
                    return;
                }
                if (!ValidateUtil.validateMobile(phoneStr)) {
                    T.showShort(getApplicationContext(), getString(R.string.mobile_error));
                    return;
                }
                String msgCodeStr = mCodeET.getText().toString();
                if (msgCodeStr.length() == 0) {
                    T.showShort(getApplicationContext(), getString(R.string.code_tip));
                    return;
                }

                wechatUserRegister();

                break;
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
                    .addParams("smsType", Constants.SMS_SEND_BIND_PHONE)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                            mTime.cancel();
                            mTime = null;
                            mCodeBtn.setClickable(true);
                            mCodeBtn.setText(getString(R.string.code_get));
//                            mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                SMSBean bean = (SMSBean) JsonUtil.toObject(response, SMSBean.class);
                                if (bean != null && bean.getRet() != 0) {
                                    T.showShort(getApplicationContext(), bean.getMsg());

                                    mTime.cancel();
                                    mTime = null;

                                    mCodeBtn.setClickable(true);
                                    mCodeBtn.setText(getString(R.string.code_get));
                                    mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
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


    private void wechatUserRegister() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(BindPhoneActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        try {
            String phone = mPhoneET.getText().toString();
            String code = mCodeET.getText().toString();
            String openId = getIntent().getStringExtra("WX_OPEN_ID");

            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.wx_user_register)
                    .addParams("phone", phone)
                    .addParams("code", code)
                    .addParams("openId", openId)
                    .addParams("clientType", "1")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(BindPhoneActivity.this, getString(R.string.app_network_error));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                UserLoginBean bean = (UserLoginBean) JsonUtil.toObject(response, UserLoginBean.class);
                                if (bean.getRet() == 0) {
                                    SPManager.setUserToken(bean.getData().getToken());
                                    SPManager.setRefreshToken(bean.getData().getRefreshToken());

                                    userQuery();
                                } else {
                                    if (mLoadDlg != null) {
                                        mLoadDlg.dismiss();
                                        mLoadDlg = null;
                                    }
                                    T.showShort(getApplicationContext(), bean.getMsg());
                                }
                            } catch (Exception e) {
                                if (mLoadDlg != null) {
                                    mLoadDlg.dismiss();
                                    mLoadDlg = null;
                                }
                                T.showShort(getApplicationContext(), getString(R.string.app_error));
                            }
                        }
                    });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(getApplicationContext(), e.getLocalizedMessage());
        }
    }

    private void userQuery() {
        AppNetUtil.userQuery(BindPhoneActivity.this, new AppNetUtil.userQueryCallBack() {
            @Override
            public void onSuccess(final UserInfo userInfo) {
                SPUtils.put(BindPhoneActivity.this, Constants.YYQ_LOGIN_TYPE_SP, Constants.YYQ_LOGIN_TYPE_OTHER);
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }

                String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                MKeyApi.getInstance(BindPhoneActivity.this, null, strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
                    @Override
                    public void onMKeyApiCallBack(MKeyApiResult result) {
                        if (result.getCode().equals("0")) {
                            CertInfoBean bean = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                            if (bean != null) {
                                AppNetUtil.setServerUserCert(bean.getCertSn(), null);
                            }
                        } else {
                            AppNetUtil.setServerUserCert("", null);
                        }

                        // 发送我的广播 登录成功
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user_info", userInfo);
                        Intent intent = new Intent(Constants.MINE_UPDATA_BROADCAST);
                        intent.putExtra("type", "TYPE_LOGIN");
                        intent.putExtras(bundle);
                        sendBroadcast(intent);

                        ActivityManager.getInstance().closeAllActivity();
                        T.showShort(getApplicationContext(), getString(R.string.login_success));
                        Intent homeIntent = new Intent(BindPhoneActivity.this, HomeActivity.class);
                        startActivity(homeIntent);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(String errMsg) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                T.showShort(BindPhoneActivity.this, errMsg);
            }
        });
    }


    //短信倒计时
    private class TimeCount extends CountDownTimer {
        private TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() { // 计时完毕时触发
            mCodeBtn.setClickable(true);
            mCodeBtn.setText(getString(R.string.code_re_get));
            mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_color));

            mTime.cancel();
            mTime = null;
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            mCodeBtn.setClickable(false);
            mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.font_gray));
            mCodeBtn.setText(getString(R.string.code_re_get) + "(" + millisUntilFinished / 1000 + ")");
        }
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
            if (BindPhoneActivity.this.getCurrentFocus() != null) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(BindPhoneActivity.this
                                        .getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTime != null) {
            mTime.cancel();
            mTime = null;
        }
    }
}
