package com.cuisec.mshield.activity.cert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import com.cuisec.mshield.utils.L;
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

public class CertSignActivity extends BaseActivity {

    @BindView(R.id.cert_sign_content_et)
    EditText mContentEt;
    @BindView(R.id.cert_sign_expired_tv)
    TextView mExpiredTv;
    @BindView(R.id.cert_sign_expired_ll)
    LinearLayout mExpiredLl;

    private QRCodeBean mQRCodeBean;
    private UserInfo mUserInfo = null;
    private LoadDialog mLoadDlg = null;

    private CertInfoBean mCertInfo;
    private String mStrUserInfo;

    private String mSignSrc;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_cert_sign);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("数字签名");
    }

    @Override
    protected void initializeData() {
        mUserInfo = SPManager.getUserInfo();
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                T.showShort(this, getString(R.string.cert_qrcode_error));
                finishActivity();
            }
            String data = bundle.getString("data");
            if (data == null) {
                T.showShort(this, getString(R.string.cert_qrcode_error));
                finishActivity();
            }
            mQRCodeBean = (QRCodeBean) JsonUtil.toObject(data, QRCodeBean.class);
            if (mQRCodeBean == null) {
                T.showShort(this, getString(R.string.cert_qrcode_error));
                finishActivity();
            }
            mQRCodeBean.urlDecode();
            mContentEt.setText(mQRCodeBean.getMsg());

        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }

        mStrUserInfo = "{\"name\":\"" + mUserInfo.userName + "\",\"idNo\":\"" + mUserInfo.idNo + "\",\"mobile\":\"" + mUserInfo.phone + "\"}";
        MKeyApi.getInstance(CertSignActivity.this, SPManager.getSDKAuthCode(), mStrUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
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
                } else {
                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }
                }
            }
        });

        // 证书签名
        certSignAction();
    }

    @OnClick(R.id.cert_sign_btn)
    public void onViewClicked() {
        // 证书签名
        certSignAction();
    }

    private void certSignAction() {
        if (!AppUtil.isLogin(CertSignActivity.this)) {
            return;
        }

        YAppManager.getInstance().setSignBusyStatus(true); //设置签名繁忙

        if (mCertInfo != null && mCertInfo.getEndDate() != null) {
            long lDays = DateUtil.getDays(mCertInfo.getEndDate());
            if (lDays < 0) {
                T.showLong(CertSignActivity.this, getString(R.string.cert_gx_tip));
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                return;
            }

            if (mLoadDlg == null) {
                mLoadDlg = new LoadDialog(CertSignActivity.this, R.style.CustomDialog);
                mLoadDlg.show();
            }

            String code = SPManager.getSDKAuthCode();
            signature(code, mStrUserInfo);
        } else {
            T.showShort("证书查询失败");//证书查询平时阿
            finishActivity();
        }
    }

    private void signature(final String appCode, final String userInfo) {
        if (mQRCodeBean != null && mQRCodeBean.getMsgWrapper() != null && mQRCodeBean.getMsgWrapper().equals("1")) {
            // PDF签章
            mSignSrc = "KSBASE64:" + mQRCodeBean.getMsg();
        } else {
            mSignSrc = mQRCodeBean.getMsg();
        }

        AppUtil.signVerifyPin(CertSignActivity.this, new AppUtil.VeifyCallBack() {
            @Override
            public void onSuccess(final String pin) {
                MKeyApi.getInstance(CertSignActivity.this, appCode, userInfo, SPManager.getUserInfo().algVersion).signature(mSignSrc, pin, new MKeyApiCallback() {
                    @Override
                    public void onMKeyApiCallBack(final MKeyApiResult result) {
                        if (result.getCode().equals("0")) {
                            // 验证签名
                            verifySignature(appCode, userInfo, mSignSrc, result.getData(), pin);
                        } else {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }

                            if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                sendBroadcast(intent);
                            }

                            T.showShort(CertSignActivity.this, result.getMsg());
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
        MKeyApi.getInstance(CertSignActivity.this, code, userInfo, SPManager.getUserInfo().algVersion).verifySignature(signSrc, signValue, new MKeyApiCallback() {
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
                        AppUtil.postSignVerifyLog(CertSignActivity.this, userInfo, pin, mCertInfo.getCertSn(), signSrc, signValue);
                    }

                    T.showShort(CertSignActivity.this, result.getMsg());
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
            signatureLogBean.setAction(Constants.LOG_TYPE_2);
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
                    T.showShort(getApplicationContext(), getString(R.string.cert_sign_failed));
                    finishActivity();
                }

                @Override
                public void onResponse(String response, int id) {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }
                    T.showShort(getApplicationContext(), getString(R.string.cert_sign_success));
                    finishActivity();
                }
            });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(getApplicationContext(), getString(R.string.cert_sign_failed));
            finishActivity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YAppManager.getInstance().setSignBusyStatus(false); //设置签名空闲
    }
}
