package com.cuisec.mshield.activity.home;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cuisec.mshield.activity.cert.CertFindPwdActivity;
import com.cuisec.mshield.bean.BaseBean;
import com.cuisec.mshield.bean.CertInfoBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.ActivityManager;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.LoadDialog;
import com.cuisec.mshield.widget.MenuDialog;
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

public class CertSetActivity extends BaseActivity {

    @BindView(R.id.cert_set_sign_lock_ll)
    LinearLayout mLockLL;
    @BindView(R.id.cert_set_sign_lock_btn)
    Button mLockBtn;
    @BindView(R.id.cert_set_sign_free_btn)
    Button mFreeBtn;

    private LoadDialog mLoadDlg = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_cert_set);
        ButterKnife.bind(this);

        ActivityManager.getInstance().putActivity(this.getLocalClassName(), this);
    }

    @Override
    protected void initializeViews() {
        showTitle(getString(R.string.cert_sz));

        if (SPManager.getFingerIsSupport()) {
            if (SPManager.getFingerSignStatus()) {
                mLockBtn.setBackgroundResource(R.mipmap.switch_on);
            } else {
                mLockBtn.setBackgroundResource(R.mipmap.switch_off);
            }
            mLockLL.setVisibility(View.VISIBLE);
        }

        if (SPManager.getSignFreeStatus()) {
            mFreeBtn.setBackgroundResource(R.mipmap.switch_on);
        } else {
            mFreeBtn.setBackgroundResource(R.mipmap.switch_off);
        }
    }

    @Override
    protected void initializeData() {

    }

    @OnClick({R.id.cert_set_pin_verify, R.id.cert_set_pin_change, R.id.cert_set_pin_unlock, R.id.cert_set_sign_lock_btn, R.id.cert_set_sign_free_btn})
    public void onViewClicked(View view) {
        if (!AppUtil.isLogin(CertSetActivity.this)) {
            return;
        }

        switch (view.getId()) {
            case R.id.cert_set_pin_verify: {
                verifyPin();
                break;
            }
            case R.id.cert_set_pin_change: {
                changePin();
                break;
            }
            case R.id.cert_set_pin_unlock: {

                new MenuDialog(this, getString(R.string.app_cancel), new String[]{"短信找回", "授权码找回"}, true).setOnMyPopClickListener(new MenuDialog.MenuClickListener() {
                    @Override
                    public void onItemClick(int index, String content) {
                        if (index == 0) {
                            Intent intent = new Intent(CertSetActivity.this, CertFindPwdActivity.class);
                            startActivity(intent);
                        } else if (index == 1) {
                            Intent intent = new Intent(CertSetActivity.this, CertForgotActivity.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelClick(String content) {
                    }
                });

                break;
            }
            case R.id.cert_set_sign_lock_btn: {
                fingerSignSwitch();
                break;
            }
            case R.id.cert_set_sign_free_btn: {
                freeSignSwitch();
                break;
            }
        }
    }

    private void verifyPin() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(CertSetActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        final UserInfo userInfo = SPManager.getUserInfo();
        final String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(CertSetActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).verifyPin(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(final MKeyApiResult result) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                if (result.getCode().equals("0")) {
                    T.showShort(CertSetActivity.this, "证书密码正确");
                } else {
                    T.showShort(CertSetActivity.this, result.getMsg());

                    AppUtil.postVerifyPinLog(CertSetActivity.this, strUserInfo, result.getData(), result.getMsg());
                }
            }
        });
    }

    private void changePin() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(CertSetActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        final UserInfo userInfo = SPManager.getUserInfo();
        final String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(CertSetActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).modifyPin(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(final MKeyApiResult result) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                if (result.getCode().equals("0")) {
                    SPManager.setCertPin("");
                    SPManager.setFingerSignStatus(false);
                    mLockBtn.setBackgroundResource(R.mipmap.switch_off);
                    T.showShort(CertSetActivity.this, "修改证书密码成功");
                } else {
                    T.showShort(CertSetActivity.this, result.getMsg());

                    AppUtil.postVerifyPinLog(CertSetActivity.this, strUserInfo, result.getData(), result.getMsg());
                }
            }
        });
    }

    // 指纹签名设置
    private void fingerSignSwitch() {
        if (SPManager.getFingerSignStatus()) {
            mLockBtn.setBackgroundResource(R.mipmap.switch_off);
            SPManager.setFingerSignStatus(false);
            SPManager.setCertPin("");
            T.showShort("指纹签名取消成功");
//            AppUtil.fingerVerify(CertSetActivity.this, new AppUtil.VeifyCallBack() {
//                @Override
//                public void onSuccess() {
//                    mLockBtn.setBackgroundResource(R.mipmap.switch_off);
//                    SPManager.setFingerSignStatus(false);
//                    T.showShort("指纹签名取消成功");
//                }
//                @Override
//                public void onFailure(String errMsg) {
//                    T.showShort(errMsg);
//                }
//            });
        } else {
            if (mLoadDlg == null) {
                mLoadDlg = new LoadDialog(CertSetActivity.this, R.style.CustomDialog);
                mLoadDlg.show();
            }

            UserInfo userInfo = SPManager.getUserInfo();
            String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
            MKeyApi.getInstance(CertSetActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).verifyPin(new MKeyApiCallback() {
                @Override
                public void onMKeyApiCallBack(final MKeyApiResult result) {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }

                    if (result.getCode().equals("0")) {
                        AppUtil.fingerVerify(CertSetActivity.this, new AppUtil.VeifyCallBack() {
                            @Override
                            public void onSuccess(String pin) {
                                // 保存证书密码
                                SPManager.setCertPin(result.getData());
                                mLockBtn.setBackgroundResource(R.mipmap.switch_on);
                                SPManager.setFingerSignStatus(true);
                                T.showShort("指纹签名设置成功");
                            }
                            @Override
                            public void onFailure(String errMsg) {
                                T.showShort(errMsg);
                            }
                        });
                    } else {

                        if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                            Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                            sendBroadcast(intent);
                        }

                        T.showShort(CertSetActivity.this, result.getMsg());
                    }
                }
            });
        }
    }

    // 免密签名设置
    private void freeSignSwitch() {
        if (SPManager.getSignFreeStatus()) {
            trustClean();
        } else {
            AppUtil.signVerifyPin(this, new AppUtil.VeifyCallBack() {
                @Override
                public void onSuccess(String pin) {
                    if (mLoadDlg == null) {
                        mLoadDlg = new LoadDialog(CertSetActivity.this, R.style.CustomDialog);
                        mLoadDlg.show();
                    }
                    getKey(pin);
                }

                @Override
                public void onFailure(String errMsg) {
                    T.showShort(errMsg);
                }
            });
        }
    }


    private void getKey(String pin) {
        UserInfo userInfo = SPManager.getUserInfo();
        final String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(CertSetActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getKey(pin, new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (result.getCode().equals("0")) {
                    getCert(strUserInfo, result.getData());
                } else {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }

                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }

                    T.showShort(result.getMsg());
                }
            }
        });
    }

    private void getCert(String userInfo, final String key) {
        MKeyApi.getInstance(CertSetActivity.this, SPManager.getSDKAuthCode(), userInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (result.getCode().equals("0")) {
                    CertInfoBean bean = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                    trustConfig(bean.getCertSn(), key);
                } else {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }

                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }
                    T.showShort(result.getMsg());
                }
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
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(getString(R.string.app_network_error));
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
                                    SPManager.setSignFreeStatus(true);
                                    mFreeBtn.setBackgroundResource(R.mipmap.switch_on);
                                    T.showShort(getString(R.string.cert_pin_no_ok));
                                } else {
                                    T.showShort(bean.getMsg());
                                }
                            } catch (Exception e) {
                                T.showShort(e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(e.getLocalizedMessage());
        }
    }

    private void trustClean() {
        try {
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.sign_trust_clean)
                    .addHeader("token", SPManager.getUserToken())
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(getString(R.string.app_network_error));
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
                                    SPManager.setSignFreeStatus(false);
                                    mFreeBtn.setBackgroundResource(R.mipmap.switch_off);
                                    T.showShort("免密签名取消成功");
                                } else {
                                    T.showShort(bean.getMsg());
                                }
                            } catch (Exception e) {
                                T.showShort(e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(e.getLocalizedMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().closeActivity(this.getLocalClassName());
    }
}