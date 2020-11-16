package com.cuisec.mshield.activity.mine.safe;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cuisec.mshield.bean.BaseBean;
import com.cuisec.mshield.bean.SMSBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.ValidateUtil;
import com.cuisec.mshield.widget.LoadDialog;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLDecoder;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class PhoneSetActivity extends BaseActivity {

    @BindView(R.id.safe_set_phone_et)
    EditText mPhoneEt;
    @BindView(R.id.safe_set_phone_code_btn)
    Button mCodeBtn;
    @BindView(R.id.safe_set_phone_code_et)
    EditText mCodeEt;

    private NewTimeCount newTime;

    private UserInfo mUserInfo;

    private LoadDialog mLoadDlg = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_phone_set);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("设置手机号码");
    }

    @Override
    protected void initializeData() {
        mUserInfo = SPManager.getUserInfo();
    }

    @OnClick({R.id.safe_set_phone_code_btn, R.id.safe_set_phone_ok_btn})
    public void onViewClicked(View view) {
        hideKeyBoard();
        switch (view.getId()) {
            case R.id.safe_set_phone_code_btn: {
                String phone = mPhoneEt.getText().toString();
                if (phone.equals("")) {
                    T.showShort(getApplicationContext(), getString(R.string.safe_phone_new_tip));
                    return;
                }
                if (!ValidateUtil.validateMobile(phone)) {
                    T.showShort(getApplicationContext(), getString(R.string.mobile_error));
                    return;
                }
                newTime = new NewTimeCount(60000, 1000);
                newTime.start();
                getMsgCode(phone);
                break;
            }
            case R.id.safe_set_phone_ok_btn: {
                String strPhone = mPhoneEt.getText().toString();
                String strCode = mCodeEt.getText().toString();

                if (strPhone.equals("")) {
                    T.showShort(getApplicationContext(), getString(R.string.safe_phone_new_tip));
                    return;
                }

                if (!ValidateUtil.validateMobile(strPhone)) {
                    T.showShort(getApplicationContext(), getString(R.string.mobile_error));
                    return;
                }

                if (strCode.equals("")) {
                    T.showShort(getApplicationContext(), getString(R.string.safe_phone_code_new_tip));
                    return;
                }
                userChangePhone(strPhone, strCode);
                break;
            }
        }
    }


    // 获取验证码
    private void getMsgCode(String phone) {
        try {
            String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, phone);
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.sms_send)
                    .addParams("phone", phone)
                    .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
                    .addParams("smsType", Constants.SMS_SEND_CHANGE_PHONE)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            newTime.cancel();
                            newTime = null;
                            mCodeBtn.setClickable(true);
                            mCodeBtn.setText(R.string.code_get);
                            mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_color));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                SMSBean bean = (SMSBean) JsonUtil.toObject(response, SMSBean.class);
                                if (bean != null && bean.getRet() != 0) {
                                    T.showShort(getApplicationContext(), bean.getMsg());
                                    newTime.cancel();
                                    newTime = null;
                                    mCodeBtn.setClickable(true);
                                    mCodeBtn.setText(R.string.code_get);
                                    mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_color));
                                }
                            } catch (Exception e) {
                                newTime.cancel();
                                newTime = null;
                                mCodeBtn.setClickable(true);
                                mCodeBtn.setText(R.string.code_get);
                                mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.main_color));
                            }
                        }
                    });
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
    }

    // 用户更换手机号
    private void userChangePhone(final String newPhone, String newCode) {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(PhoneSetActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        try {
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.user_changephone)
                    .addHeader("token", SPManager.getUserToken())
                    .addParams("newPhone", newPhone)
                    .addParams("newCode", newCode)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(PhoneSetActivity.this, getString(R.string.app_network_error));
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
                                if (bean != null) {
                                    if (bean.getRet() == 0) {

                                        T.showShort(PhoneSetActivity.this, getString(R.string.mobile_change_success));

                                        //发送用户信息广播
                                        // 发送我的广播 更新手机
                                        Intent bIntent = new Intent(Constants.MINE_UPDATA_BROADCAST);
                                        bIntent.putExtra("type", "TYPE_PHONE");
                                        bIntent.putExtra("value", newPhone);
                                        sendBroadcast(bIntent);

                                        new Thread() {
                                            @Override
                                            public void run() {
                                                mUserInfo.phone = newPhone;
                                                SPManager.setUserAccount(newPhone);
                                                SPManager.setUserInfo(mUserInfo);
                                            }
                                        }.start();

                                        finish();
                                    } else {
                                        T.showShort(getApplicationContext(), bean.getMsg());
                                    }
                                }
                            } catch (Exception e) {
                                T.showShort(getApplicationContext(), e.getLocalizedMessage());
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
    private class NewTimeCount extends CountDownTimer {
        private NewTimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {//计时完毕时触发
            mCodeBtn.setClickable(true);
            mCodeBtn.setText(getString(R.string.code_re_get));
            mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            newTime.cancel();
            newTime = null;
        }

        @Override
        public void onTick(long millisUntilFinished) {//计时过程显示
            mCodeBtn.setClickable(false);
            mCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.font_gray));
            mCodeBtn.setText(getString(R.string.code_re_get) + "(" + millisUntilFinished / 1000 + ")");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (newTime != null) {
            newTime.cancel();
            newTime = null;
        }
    }
}
