package com.cuisec.mshield.activity.home;

import android.content.Intent;
import android.view.View;

import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.AlertDialog;
import com.cuisec.mshield.widget.LoadDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.custle.ksmkey.MKeyMacro;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class CertRevokeActivity extends BaseActivity {

    private LoadDialog mLoadDlg = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_cert_revoke);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("证书注销");

    }

    @Override
    protected void initializeData() {

    }

    @OnClick(R.id.cert_revoke_btn)
    public void onViewClicked() {
        if (!AppUtil.isLogin(CertRevokeActivity.this)) {
            return;
        }

        new AlertDialog(CertRevokeActivity.this).builder()
                .setTitle(getString(R.string.cert_zx))
                .setMessage(getString(R.string.cert_zx_tip))
                .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        certRevokeVerifyPin();
                    }
                })
                .setNegativeButton(getString(R.string.app_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    // 验证证书
    private void certRevokeVerifyPin() {
        UserInfo userInfo = SPManager.getUserInfo();
        final String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(CertRevokeActivity.this, "", strUserInfo, SPManager.getUserInfo().algVersion).verifyPin(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(final MKeyApiResult result) {
                if (result.getCode().equals("0")) {
                    certRevokeSign(result.getData());
                } else if (result.getCode().equals(MKeyMacro.ERR_PIN_EXCEPT)) {
                    // 进行注销
                    certRevoke(1);
                } else {
                    AppUtil.postVerifyPinLog(CertRevokeActivity.this, strUserInfo, result.getData(), result.getMsg());
                    T.showShort(CertRevokeActivity.this, result.getMsg());
                }
            }
        });
    }

    private void certRevokeSign(final String pin) {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(CertRevokeActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        final UserInfo userInfo = SPManager.getUserInfo();
        final String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        final String strSrc = "1234567890";
        MKeyApi.getInstance(CertRevokeActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).signature(strSrc, pin, new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(final MKeyApiResult result) {
                if (result.getCode().equals("0")) {
                    certRevokeSignVerify(strSrc, result.getData(), pin);
                } else if (result.getCode().equals(MKeyMacro.ERR_SIGN)) {
                    certRevoke(1);
                } else {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }

                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }

                    T.showShort(CertRevokeActivity.this, result.getMsg());
                }
            }
        });
    }


    private void certRevokeSignVerify(final String signSrc, final String signValue, final String pin) {
        final UserInfo userInfo = SPManager.getUserInfo();
        final String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(CertRevokeActivity.this, "", strUserInfo, SPManager.getUserInfo().algVersion).verifySignature(signSrc, signValue, new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (result.getCode().equals("0")) {
                    certRevoke(0);
                } else if (result.getCode().equals(MKeyMacro.ERR_SIGN_VERIFY)) {
                    certRevoke(1);
                } else {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }

                    T.showShort(CertRevokeActivity.this, result.getMsg());
                    finishActivity();
                }
            }
        });
    }


    // Status: 0: 接口返回成功注销  0: 强制注销
    private void certRevoke(final int iStatus) {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(CertRevokeActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        final UserInfo userInfo = SPManager.getUserInfo();
        final String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(CertRevokeActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).revokeCert(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }

                String errInfo = "{\"ret\":\"" + result.getCode() + "\",\"msg\":\"" + result.getMsg() + "\"}";
                AppNetUtil.postAppLog(CertRevokeActivity.this, Constants.LOG_REVOKE_CERT, errInfo);

                if (result.getCode().equals("0")) {
                    T.showShort(CertRevokeActivity.this, getString(R.string.cert_zx_success));
                    SPManager.setFingerSignStatus(false);
                    SPManager.setCertPin("");
                    if (SPManager.getSignFreeStatus()) {
                        SPManager.setSignFreeStatus(false);
                        trustClean();
                    }

                    // 存储版本信息
                    userInfo.algVersion = Constants.YYQ_ALG_VERSION_2;
                    SPManager.setUserInfo(userInfo);
                } else {
                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }

                    if (iStatus == 1) {
                        // 强制删除
                        MKeyApi.getInstance(CertRevokeActivity.this, "", strUserInfo, SPManager.getUserInfo().algVersion).deleteCert(new MKeyApiCallback() {
                            @Override
                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                if (result.getCode().equals("0")) {
                                    T.showShort(CertRevokeActivity.this, getString(R.string.cert_zx_success));
                                    SPManager.setFingerSignStatus(false);
                                    SPManager.setCertPin("");
                                    if (SPManager.getSignFreeStatus()) {
                                        SPManager.setSignFreeStatus(false);
                                        trustClean();
                                    }

                                    // 存储版本信息
                                    userInfo.algVersion = Constants.YYQ_ALG_VERSION_2;
                                    SPManager.setUserInfo(userInfo);
                                } else {
                                    T.showShort(CertRevokeActivity.this, result.getMsg());
                                }
                            }
                        });
                    } else {
                        T.showShort(CertRevokeActivity.this, result.getMsg());
                    }
                }
                finishActivity();
            }
        });

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
                        }
                        @Override
                        public void onResponse(String response, int id) {
                        }
                    });
        } catch (Exception e) {
        }
    }
}
