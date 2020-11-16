package com.cuisec.mshield.activity.cert;

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
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.LoadDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.custle.ksmkey.MKeyMacro;
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

/**
 * @author dy
 * @date 2019/10/30
 * @function
 */
public class CertFindPwdActivity extends BaseActivity {


    @BindView(R.id.cert_find_name_et)
    EditText mCertFindNameEt;
    @BindView(R.id.cert_find_status_et)
    EditText mCertFindStatusEt;
    @BindView(R.id.cert_find_code_btn)
    Button mCertFindCodeBtn;
    @BindView(R.id.cert_find_code_et)
    EditText mCertFindCodeEt;
    private TimeCount mTime;

    private LoadDialog mLoadDlg = null;
    private UserInfo mUserInfo = null;


    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_cert_find_pwd);
        ButterKnife.bind(this);

    }

    @Override
    protected void initializeViews() {
        showTitle("找回证书密码");
    }

    @Override
    protected void initializeData() {
        mUserInfo = SPManager.getUserInfo();
    }

    @OnClick({R.id.cert_find_code_btn, R.id.cert_find_pwd_btn})
    public void onViewClicked(View view) {
        if (!AppUtil.isLogin(CertFindPwdActivity.this)) {
            return;
        }

        switch (view.getId()) {
            case R.id.cert_find_code_btn:
                if (!userAuth()) {
                    T.showShort(this, "请输入正确的姓名和证件号码");
                    return;
                }
                getMsgCode();
                break;
            case R.id.cert_find_pwd_btn:
                if (!userAuth()) {
                    T.showShort(this, "请输入正确的姓名和证件号码");
                    return;
                }

                String code = mCertFindCodeEt.getText().toString();
                if ("".equals(code)) {
                    T.showShort(this, getString(R.string.code_tip));
                    return;
                }
                userCheckCode(code);
                break;
        }
    }

    private boolean userAuth() {
        String name = mCertFindNameEt.getText().toString();
        String status = mCertFindStatusEt.getText().toString();
        if ("".equals(name)) {
            return false;
        }
        if ("".equals(status)) {
            return false;
        }

        if (!name.equals(mUserInfo.userName)) {
            return false;
        }
        if (!status.equals(mUserInfo.idNo)) {
            return false;
        }
        return true;
    }

    // 获取验证码
    private void getMsgCode() {
        mTime = new TimeCount(60000, 1000);
        mTime.start();//开始计时

        try {
            String phone = mUserInfo.phone;
            String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, phone);
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.sms_send)
                    .addParams("phone", phone)
                    .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
                    .addParams("smsType", Constants.SMS_SEND_FORGET)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            mTime.cancel();
                            mTime = null;
                            mCertFindCodeBtn.setClickable(true);
                            mCertFindCodeBtn.setText(getString(R.string.code_get));
                            mCertFindCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                            T.showShort(getApplicationContext(), e.getLocalizedMessage());
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

                                    mCertFindCodeBtn.setClickable(true);
                                    mCertFindCodeBtn.setText(getString(R.string.code_get));
                                    mCertFindCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
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
            mCertFindCodeBtn.setClickable(true);
            mCertFindCodeBtn.setText(getString(R.string.code_re_get));
            mCertFindCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

            mTime.cancel();
            mTime = null;
        }

        @Override
        public void onTick(long millisUntilFinished) {  // 计时过程显示
            mCertFindCodeBtn.setClickable(false);
            mCertFindCodeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.font_gray));
            mCertFindCodeBtn.setText(getString(R.string.code_re_get) + "(" + millisUntilFinished / 1000 + ")");
        }

    }

    private void userCheckCode(String code) {

        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(CertFindPwdActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        try {
            String phone = mUserInfo.phone;
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.sms_check)
                    .addParams("phone", phone)
                    .addParams("code", URLEncoder.encode(code, Config.UTF_8))
                    .addParams("smsType", Constants.SMS_SEND_FORGET)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(CertFindPwdActivity.this, getString(R.string.app_network_error));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                BaseBean bean = (BaseBean) JsonUtil.toObject(response, BaseBean.class);
                                if (bean.getRet() == 0) {
                                    unlockPin();
                                } else {
                                    if (mLoadDlg != null) {
                                        mLoadDlg.dismiss();
                                        mLoadDlg = null;

                                    }
                                    T.showShort(CertFindPwdActivity.this, bean.getMsg());
                                }
                            } catch (Exception e) {
                                if (mLoadDlg != null) {
                                    mLoadDlg.dismiss();
                                    mLoadDlg = null;
                                }
                                T.showShort(CertFindPwdActivity.this, e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(CertFindPwdActivity.this, e.getLocalizedMessage());
        }
    }

    private void unlockPin() {
        String strUserInfo = "{\"name\":\"" + mUserInfo.userName + "\",\"idNo\":\"" + mUserInfo.idNo + "\",\"mobile\":\"" + mUserInfo.phone + "\"}";
        MKeyApi.getInstance(CertFindPwdActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).unlockPin(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                if (result.getCode().equals("0")) {
                    // 清空免密签名状态
                    SPManager.setCertPin("");
                    T.showShort(CertFindPwdActivity.this, "重置密码成功");
                    finishActivity();
                } else {

                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }

                    T.showShort(CertFindPwdActivity.this, result.getMsg());
                }
            }
        });
    }
}
