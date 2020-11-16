package com.cuisec.mshield.activity.login;

import android.content.Intent;
import android.widget.EditText;

import com.cuisec.mshield.activity.home.HomeActivity;
import com.cuisec.mshield.bean.CertInfoBean;
import com.cuisec.mshield.bean.UserLoginBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.ActivityManager;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.DateUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.ValidateUtil;
import com.cuisec.mshield.widget.LoadDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.LoginBaseActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLDecoder;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class LoginTempActivity extends LoginBaseActivity {

    @BindView(R.id.login_temp_phone_et)
    EditText mPhoneET;
    @BindView(R.id.login_temp_code_et)
    EditText mCodeET;

    private LoadDialog mLoadDlg = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_login_temp);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("临时登录");
        // 设置上一次登录的账号
        String account = SPManager.getUserAccount();
        if (account != null && !account.equals("")) {
            mPhoneET.setText(account);
            mPhoneET.requestFocus();      // 将光标设置到最后
        }
    }

    @Override
    protected void initializeData() {

    }

    @OnClick(R.id.login_temp_login_btn)
    public void onViewClicked() {
        String phoneStr = mPhoneET.getText().toString();
        if ("".equals(phoneStr)) {
            T.showShort(LoginTempActivity.this, getString(R.string.mobile_tip));
            return;
        }
        if (!ValidateUtil.validateMobile(phoneStr)) {
            T.showShort(LoginTempActivity.this, getString(R.string.mobile_error));
            return;
        }
        String loginCode = mCodeET.getText().toString();
        if ("".equals(loginCode)) {
            T.showShort(LoginTempActivity.this, getString(R.string.temp_auth_code_tip));
            return;
        }

        // 获取SDK授权码
        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
        sendBroadcast(intent);
        // 用户登录
        userLogin();
    }


    // 用户登录
    private void userLogin() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(LoginTempActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        try {
            String phone = mPhoneET.getText().toString();
            final String strCode = mCodeET.getText().toString();
            String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, phone + "##" + strCode);

            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.user_login)
                    .addParams("phone", phone)
                    .addParams("code", URLEncoder.encode(strCode, Config.UTF_8))
                    .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
                    .addParams("clientType", "1")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(LoginTempActivity.this, getString(R.string.app_network_error));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            SPUtils.put(LoginTempActivity.this,Constants.SAVE_USER_PHONE,mPhoneET.getText().toString());
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                UserLoginBean bean = (UserLoginBean) JsonUtil.toObject(response, UserLoginBean.class);
                                if (bean.getRet() == 0) {
                                    SPManager.setUserAccount(mPhoneET.getText().toString());
                                    SPManager.setUserToken(bean.getData().getToken());
                                    SPManager.setRefreshToken(bean.getData().getRefreshToken());
                                    long lTime = DateUtil.stringToDate(bean.getData().getLoginEndTime(), "yyyy-MM-dd HH:mm:ss").getTime();
                                    SPUtils.put(LoginTempActivity.this, Constants.YYQ_TEMP_TEMP_SP, lTime);
                                    userQuery();
                                } else {
                                    if (mLoadDlg != null) {
                                        mLoadDlg.dismiss();
                                        mLoadDlg = null;
                                    }
                                    T.showShort(LoginTempActivity.this, bean.getMsg());
                                }
                            } catch (Exception e) {
                                if (mLoadDlg != null) {
                                    mLoadDlg.dismiss();
                                    mLoadDlg = null;
                                }
                                T.showShort(LoginTempActivity.this, e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(LoginTempActivity.this, e.getLocalizedMessage());
        }
    }


    private void userQuery() {
        AppNetUtil.userQuery(LoginTempActivity.this, new AppNetUtil.userQueryCallBack() {
            @Override
            public void onSuccess(final UserInfo userInfo) {
                SPUtils.put(LoginTempActivity.this, Constants.YYQ_LOGIN_TYPE_SP, Constants.YYQ_LOGIN_TYPE_TEMP);
                String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                MKeyApi.getInstance(LoginTempActivity.this, null, strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
                    @Override
                    public void onMKeyApiCallBack(MKeyApiResult result) {
                        if (result.getCode().equals("0")) {
                            final CertInfoBean bean = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                            AppNetUtil.setServerUserCert(bean.getCertSn(), null);
                        } else {
                            AppNetUtil.setServerUserCert("", null);
                        }

                        loginSuccessIntoHome(getString(R.string.login_success));
                    }
                });
            }

            @Override
            public void onFailure(String errMsg) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                T.showShort(LoginTempActivity.this, errMsg);
            }
        });
    }

    private void loginSuccessIntoHome(String strMsg) {
        if (mLoadDlg != null) {
            mLoadDlg.dismiss();
            mLoadDlg = null;
        }
        ActivityManager.getInstance().closeAllActivity();
        T.showShort(getApplicationContext(), strMsg);
        Intent homeIntent = new Intent(LoginTempActivity.this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }

}
