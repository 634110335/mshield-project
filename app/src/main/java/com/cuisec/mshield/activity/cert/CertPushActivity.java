package com.cuisec.mshield.activity.cert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.activity.mine.seal.MineSealActivity;
import com.cuisec.mshield.bean.CertInfoBean;
import com.cuisec.mshield.bean.QRCodeBean;
import com.cuisec.mshield.bean.SignQueryBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.data.YAppManager;
import com.cuisec.mshield.service.SignatureLogBean;
import com.cuisec.mshield.service.SignatureLogUtil;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.DateUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.AlertDialog;
import com.cuisec.mshield.widget.LoadDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.custle.ksmkey.MKeyMacro;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLDecoder;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class CertPushActivity extends BaseActivity {

    @BindView(R.id.cert_push_content_et)
    EditText mContentEt;
    @BindView(R.id.cert_push_desc_et)
    EditText mDescEt;
    @BindView(R.id.cert_push_expired_tv)
    TextView mExpiredTv;
    @BindView(R.id.cert_push_expired_ll)
    LinearLayout mExpiredLl;

    private QRCodeBean mQRCodeBean;
    private UserInfo mUserInfo = null;
    private LoadDialog mLoadDlg = null;

    private CertInfoBean mCertInfo;
    private String mStrUserInfo;

    private String mSignSrc;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_cert_push);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("推送签名");
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

            getContentDesc();

        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
        //获取证书签名
        mStrUserInfo = "{\"name\":\"" + mUserInfo.userName + "\",\"idNo\":\"" + mUserInfo.idNo + "\",\"mobile\":\"" + mUserInfo.phone + "\"}";
        MKeyApi.getInstance(CertPushActivity.this, SPManager.getSDKAuthCode(), mStrUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
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
                        } else {
                        }
                    }
                }
            }
        });

        // 推送签名
        pushSignAction();
    }

    @OnClick(R.id.cert_push_btn)
    public void onViewClicked() {
        // 推送签名
        pushSignAction();
    }

    private void pushSignAction() {
        if (!AppUtil.isLogin(CertPushActivity.this)) {
            return;
        }

        YAppManager.getInstance().setSignBusyStatus(true); //设置签名繁忙

        if (YAppManager.getInstance().isSealStatus()) {
            AppNetUtil.getSeal(new AppNetUtil.GetSealCallBack() {
                @Override
                public void sealValue(String seal) {
                    if (seal != null && seal.length() != 0) {
                        pushCertSign();
                    } else {
                        userSetSeal();
                    }
                }
            });
        } else {
            pushCertSign();
        }
    }

    private void pushCertSign() {
        if (mCertInfo != null && mCertInfo.getEndDate() != null) {
            long lDays = DateUtil.getDays(mCertInfo.getEndDate());
            if (lDays < 0) {
                //证书已失效，请先进行证书更新!
                T.showLong(CertPushActivity.this, getString(R.string.cert_gx_tip));
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                return;
            }

            if (mLoadDlg == null) {
                mLoadDlg = new LoadDialog(CertPushActivity.this, R.style.CustomDialog);
                mLoadDlg.show();
            }

            String code = SPManager.getSDKAuthCode();
            signature(code, mStrUserInfo);
        } else {
            T.showShort("证书查询失败");
            finishActivity();
        }
    }

    private void userSetSeal() {
        new AlertDialog(CertPushActivity.this)
                .builder()
                .setTitle(getString(R.string.app_name))
                .setMessage("请先设置手写签名")
                .setNegativeButton("暂不设置", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                })
                .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CertPushActivity.this, MineSealActivity.class);
                        startActivity(intent);
                    }
                }).show();
    }

    // 调用接口获取签名内容的内容摘要
    private void getContentDesc() {
        try {
            OkHttpUtils.post()
                    .url(SPManager.getServerUrl() + Config.sign_query)
                    .addHeader("token", SPManager.getUserToken())
                    .addParams("bizSn", mQRCodeBean.getBizSn())
                    .build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    L.e(e.getLocalizedMessage());
                }

                @Override
                public void onResponse(String response, int id) {
                    L.d(response);
                    try {
                        response = URLDecoder.decode(response, Config.UTF_8);
                        SignQueryBean bean = (SignQueryBean) JsonUtil.toObject(response, SignQueryBean.class);
                        if (bean != null && bean.getRet() == 0) {
                            L.d(bean.getMsg());
                            mDescEt.setText(bean.getData().getDesc());
                            mQRCodeBean.setDesc(bean.getData().getDesc());
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

    private void signature(final String appCode, final String userInfo) {
        if (mQRCodeBean != null && mQRCodeBean.getMsgWrapper() != null && mQRCodeBean.getMsgWrapper().equals("1")) {
            // PDF签章
            mSignSrc = "KSBASE64:" + mQRCodeBean.getMsg();
        } else {
            mSignSrc = mQRCodeBean.getMsg();
        }

        AppUtil.signVerifyPin(CertPushActivity.this, new AppUtil.VeifyCallBack() {
            @Override
            public void onSuccess(final String pin) {
                MKeyApi.getInstance(CertPushActivity.this, appCode, userInfo, SPManager.getUserInfo().algVersion).signature(mSignSrc, pin, new MKeyApiCallback() {
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

                            T.showShort(CertPushActivity.this, result.getMsg());
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
        MKeyApi.getInstance(CertPushActivity.this, code, userInfo, SPManager.getUserInfo().algVersion).verifySignature(signSrc, signValue, new MKeyApiCallback() {
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
                        AppUtil.postSignVerifyLog(CertPushActivity.this, userInfo, pin, mCertInfo.getCertSn(), signSrc, signValue);
                    }

                    T.showShort(CertPushActivity.this, result.getMsg());
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
            signatureLogBean.setAction(Constants.LOG_TYPE_3);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO:OnCreate Method has been created, run ButterKnife again to generate code
        setContentView(R.layout.activity_cert_push);
        ButterKnife.bind(this);
    }
}
