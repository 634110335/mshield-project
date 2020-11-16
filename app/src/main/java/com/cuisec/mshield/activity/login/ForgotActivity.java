package com.cuisec.mshield.activity.login;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.cuisec.mshield.activity.common.LoginBaseActivity;
import com.cuisec.mshield.bean.BaseBean;
import com.cuisec.mshield.bean.SMSBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.ValidateUtil;
import com.cuisec.mshield.widget.LoadDialog;
import com.cuisec.mshield.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class ForgotActivity extends LoginBaseActivity {

    @BindView(R.id.register_phone_et)
    EditText mPhoneEt;
    @BindView(R.id.forgot_code_btn)
    Button mCodeBtn;
    @BindView(R.id.forgot_code_et)
    EditText mCodeEt;
    @BindView(R.id.forgot_eye_iv)
    ImageView mEyeIv;
    @BindView(R.id.forgot_password_et)
    EditText mPasswordEt;

    boolean bPwdEye = false;
    boolean mIsSelDept = false;  // 是否选择机构

    private TimeCount mTime;

    private LoadDialog mLoadDlg = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_forgot);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("找回密码");
    }

    @Override
    protected void initializeData() {

    }

    @OnClick({R.id.forgot_code_btn, R.id.forgot_eye_iv, R.id.forgot_sure_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.forgot_code_btn: {
                String accountStr = mPhoneEt.getText().toString();
                if (accountStr.length() == 0) {
                    T.showShort(ForgotActivity.this, getString(R.string.mobile_tip));
                    return;
                }

                if (!ValidateUtil.validateMobile(accountStr)) {
                    T.showShort(ForgotActivity.this, getString(R.string.mobile_error));
                    return;
                }

                getMsgCode();
                break;
            }
            case R.id.forgot_eye_iv: {
                if (bPwdEye) {
                    //隐藏密码
                    mEyeIv.setImageResource(R.mipmap.ico_eye_shut);
                    mPasswordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    bPwdEye = false;

                } else {
                    //显示密码
                    mEyeIv.setImageResource(R.mipmap.ico_eye_open);
                    mPasswordEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    bPwdEye = true;
                }
                break;
            }
            case R.id.forgot_sure_btn: {
                String phoneStr = mPhoneEt.getText().toString();
                if (phoneStr.length() == 0) {
                    T.showShort(getApplicationContext(), getString(R.string.mobile_tip));
                    return;
                }

                if (!ValidateUtil.validateMobile(phoneStr)) {
                    T.showShort(getApplicationContext(), getString(R.string.mobile_error));
                    return;
                }

                String pwdStr = mPasswordEt.getText().toString();
                if (pwdStr.length() == 0) {
                    T.showShort(getApplicationContext(), getString(R.string.pwd_tip));
                    return;
                }

                if (pwdStr.length() < 8) {
                    T.showShort(getApplicationContext(), getString(R.string.pwd_low));
                    return;
                }

                if (!ValidateUtil.isLetterDigit(pwdStr)) {
                    T.showShort(getApplicationContext(), getString(R.string.pwd_comb));
                    return;
                }

                String msgCodeStr = mCodeEt.getText().toString();
                if (msgCodeStr.length() == 0) {
                    T.showShort(getApplicationContext(), getString(R.string.code_tip));
                    return;
                }

                userRecoverPassword();

                break;
            }
        }
    }

    // 获取验证码
    private void getMsgCode() {
        mTime = new TimeCount(60000, 1000);
        mTime.start();//开始计时

        try {
            String phone = mPhoneEt.getText().toString();
            String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, phone);
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() +  Config.sms_send)
                    .addParams("phone", phone)
                    .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
                    .addParams("smsType", Constants.SMS_SEND_FORGET)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                            mTime.cancel();
                            mTime = null;
                            mCodeBtn.setClickable(true);
                            mCodeBtn.setText(getString(R.string.code_get));
                            mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>" + response);
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


    private void userRecoverPassword() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(ForgotActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytePwd = digest.digest(mPasswordEt.getText().toString().getBytes());
            String strPwd = Base64.encodeToString(bytePwd, Base64.NO_WRAP);

            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.user_recoverpwd)
                    .addParams("phone", mPhoneEt.getText().toString())
                    .addParams("code", mCodeEt.getText().toString())
                    .addParams("password", URLEncoder.encode(strPwd, Config.UTF_8))
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(ForgotActivity.this, getString(R.string.app_network_error));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }

                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                BaseBean bean = (BaseBean) JsonUtil.toObject(response, BaseBean.class);
                                if (bean.getRet() == 0) {
                                    Intent intent = new Intent();
                                    intent.putExtra("user_account", mPhoneEt.getText().toString());
                                    setResult(1001, intent);
                                    finish();

                                    T.showShort(ForgotActivity.this, getString(R.string.forgot_success));
                                } else {
                                    T.showShort(ForgotActivity.this, bean.getMsg());
                                }
                            } catch (Exception e) {
                                T.showShort(ForgotActivity.this, e.getLocalizedMessage());
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

    //短信倒计时
    private class TimeCount extends CountDownTimer {
        private TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);   // 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {    // 计时完毕时触发
            mCodeBtn.setClickable(true);
            mCodeBtn.setText(getString(R.string.code_re_get));
            mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

            mTime.cancel();
            mTime = null;
        }

        @Override
        public void onTick(long millisUntilFinished) {  // 计时过程显示
            mCodeBtn.setClickable(false);
            mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.font_gray));
            mCodeBtn.setText(getString(R.string.code_re_get) + "(" + millisUntilFinished / 1000 + ")");
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
