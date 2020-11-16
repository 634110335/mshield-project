package com.cuisec.mshield.activity.cert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cuisec.mshield.bean.CertInfoBean;
import com.cuisec.mshield.bean.QRCodeBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.data.YAppManager;
import com.cuisec.mshield.service.SignatureLogBean;
import com.cuisec.mshield.service.SignatureLogUtil;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.DateUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.LoadDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.custle.ksmkey.MKeyMacro;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class CertLoginActivity extends BaseActivity {

    @BindView(R.id.cert_login_expired_tv)
    TextView mExpiredTv;
    @BindView(R.id.cert_login_expired_ll)
    LinearLayout mExpiredLl;

    private QRCodeBean mQRCodeBean;
    private UserInfo mUserInfo = null;
    private LoadDialog mLoadDlg = null;

    private CertInfoBean mCertInfo;
    private String mStrUserInfo;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_cert_login);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("证书登录");
    }

    @Override
    protected void initializeData() {
        mUserInfo = SPManager.getUserInfo();

        Bundle bundle = getIntent().getExtras();
        String data = bundle.getString("data");
        mQRCodeBean = (QRCodeBean) JsonUtil.toObject(data, QRCodeBean.class);
        if (mQRCodeBean == null) {
            T.showShort(this, getString(R.string.cert_qrcode_error));
            finishActivity();
        }
        mQRCodeBean.urlDecode();

        mStrUserInfo = "{\"name\":\"" + mUserInfo.userName + "\",\"idNo\":\"" + mUserInfo.idNo + "\",\"mobile\":\"" + mUserInfo.phone + "\"}";
        MKeyApi.getInstance(CertLoginActivity.this, SPManager.getSDKAuthCode(), mStrUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (result.getCode().equals("0")) {
                    mCertInfo = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                    if (mCertInfo != null) {
                        long iDays = DateUtil.getDays(mCertInfo.getEndDate());
                        if (iDays <= Constants.CERT_EXPIRED_DAYS && iDays > 0) {
                            mExpiredLl.setVisibility(View.VISIBLE);
                            mExpiredTv.setText("证书还有" + iDays + "天失效，请及时进行证书更新!");
                        } else if (iDays <= 0) {
                            mExpiredLl.setVisibility(View.VISIBLE);
                            mExpiredTv.setText("证书已经失效，请进行证书更新!");
                        }
                    }
                }
            }
        });

        // 签名
        loginSignAction();
    }

    @OnClick(R.id.cert_login_btn)
    public void onViewClicked() {
        // 签名
        loginSignAction();
    }

    private void loginSignAction() {
        if (!AppUtil.isLogin(CertLoginActivity.this)) {
            return;
        }

        YAppManager.getInstance().setSignBusyStatus(true); //设置签名繁忙

        if (mCertInfo != null && mCertInfo.getEndDate() != null) {
            long lDays = DateUtil.getDays(mCertInfo.getEndDate());
            if (lDays < 0) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                T.showLong(CertLoginActivity.this, getString(R.string.cert_gx_tip));
                return;
            }

            if (mLoadDlg == null) {
                mLoadDlg = new LoadDialog(CertLoginActivity.this, R.style.CustomDialog);
                mLoadDlg.show();
            }

            String code = SPManager.getSDKAuthCode();
            signature(code, mStrUserInfo);
        } else {
            T.showShort("证书查询失败");
            finishActivity();
        }
    }

    private void signature(final String appCode, final String userInfo) {
        AppUtil.signVerifyPin(CertLoginActivity.this, new AppUtil.VeifyCallBack() {
            @Override
            public void onSuccess(final String pin) {
                MKeyApi.getInstance(CertLoginActivity.this, appCode, userInfo, SPManager.getUserInfo().algVersion).signature(mQRCodeBean.getMsg(), pin, new MKeyApiCallback() {
                    @Override
                    public void onMKeyApiCallBack(final MKeyApiResult result) {
                        if (result.getCode().equals("0")) {
                            // 验证签名
                            verifySignature(appCode, userInfo, mQRCodeBean.getMsg(), result.getData(), pin);
                        } else {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }

                            if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                sendBroadcast(intent);
                            }

                            T.showShort(CertLoginActivity.this, result.getMsg());
                            finishActivity();
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
                T.showShort(errMsg);
            }
        });
    }

    private void verifySignature(final String code, final String userInfo, final String signSrc, final String signValue, final String pin) {
        MKeyApi.getInstance(CertLoginActivity.this, code, userInfo, SPManager.getUserInfo().algVersion).verifySignature(signSrc, signValue, new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (result.getCode().equals("0")) {
                    postSignature(signValue);
                } else {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }

                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    } else {
                        AppUtil.postSignVerifyLog(CertLoginActivity.this, userInfo, pin, mCertInfo.getCertSn(), signSrc, signValue);
                    }

                    T.showShort(CertLoginActivity.this, result.getMsg());
                    finishActivity();
                }
            }
        });
    }

    private void postSignature(String signValue) {
        try {
            // 保存日志
            SignatureLogBean signatureLogBean = new SignatureLogBean();
            signatureLogBean.setBizSn(mQRCodeBean.getBizSn());
            signatureLogBean.setAppId(mQRCodeBean.getAppId());
            signatureLogBean.setAction(Constants.LOG_TYPE_1);
            signatureLogBean.setMsg(mQRCodeBean.getMsg());
            signatureLogBean.setDesc(mQRCodeBean.getDesc());
            signatureLogBean.setSignValue(signValue);
            signatureLogBean.setSignAlg("SM3withSM2");
            signatureLogBean.setCertSn(mCertInfo.getCertSn());
            SignatureLogUtil.writeLog(signatureLogBean);

            PostFormBuilder postFormBuilder;
            if (mQRCodeBean.getMode().equals(Constants.SIGN_MODE_REDIRECT)) {
                postFormBuilder = OkHttpUtils.post()
                        .url(SPManager.getServerUrl() + Config.sign_redirect)
                        .addHeader("token", SPManager.getUserToken())
                        .addParams("appId", mQRCodeBean.getAppId())
                        .addParams("action", mQRCodeBean.getAction())
                        .addParams("bizSn", mQRCodeBean.getBizSn())
                        .addParams("url", URLEncoder.encode(mQRCodeBean.getUrl(), Config.UTF_8))
                        .addParams("cert", URLEncoder.encode(mCertInfo.getCert(), Config.UTF_8))
                        .addParams("signAlg", "SM3withSM2")
                        .addParams("signValue", URLEncoder.encode(signValue, Config.UTF_8));
            } else {
                postFormBuilder = OkHttpUtils.post()
                        .url(mQRCodeBean.getUrl())
                        .addParams("action", mQRCodeBean.getAction())
                        .addParams("bizSn", mQRCodeBean.getBizSn())
                        .addParams("cert", URLEncoder.encode(mCertInfo.getCert(), Config.UTF_8))
                        .addParams("signAlg", "SM3withSM2")
                        .addParams("signValue", URLEncoder.encode(signValue, Config.UTF_8))
                        .addParams("id", mUserInfo.uuid);
            }

            postFormBuilder.build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }
                    T.showShort(getApplicationContext(), getString(R.string.cert_login_failed));
                    finishActivity();
                }

                @Override
                public void onResponse(String response, int id) {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }
                    T.showShort(getApplicationContext(), getString(R.string.cert_login_success));
                    finishActivity();
                }
            });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(getApplicationContext(), getString(R.string.cert_login_failed));
            finishActivity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YAppManager.getInstance().setSignBusyStatus(false); //设置签名空闲
    }
}
