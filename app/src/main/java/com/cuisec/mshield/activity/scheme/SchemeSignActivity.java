package com.cuisec.mshield.activity.scheme;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.bean.CertInfoBean;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.DateUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.widget.LoadDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.custle.ksmkey.MKeyMacro;
import com.cuisec.mshield.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SchemeSignActivity extends BaseActivity {

    @BindView(R.id.scheme_sign_content_et)
    EditText mContentEt;
    @BindView(R.id.scheme_sign_expired_tv)
    TextView mExpiredTV;
    @BindView(R.id.scheme_sign_expired_ll)
    LinearLayout mExpiredLL;

    private UserInfo mUserInfo = null;
    private LoadDialog mLoadDlg = null;

    private CertInfoBean mCertInfo;
    private String mStrUserInfo;

    private String mType;
    private String mSignSrc;
    private String mAppPackName;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_scheme_sign);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {

    }

    @Override
    protected void initializeData() {
        Intent intent = getIntent();
        mType = intent.getStringExtra("type");
        mSignSrc = intent.getStringExtra("signSrc");
        if (mSignSrc != null) {
            mContentEt.setText(mSignSrc);
        }
        mAppPackName = intent.getStringExtra("packName");

        mUserInfo = SPManager.getUserInfo();
        mStrUserInfo = "{\"name\":\"" + mUserInfo.userName + "\",\"idNo\":\"" + mUserInfo.idNo + "\",\"mobile\":\"" + mUserInfo.phone + "\"}";
        MKeyApi.getInstance(SchemeSignActivity.this, SPManager.getSDKAuthCode(), mStrUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (result.getCode().equals("0")) {
                    mCertInfo = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                    if (mCertInfo != null) {
                        long iDays = DateUtil.getDays(mCertInfo.getEndDate());
                        if (iDays <= Constants.CERT_EXPIRED_DAYS && iDays > 0) {
                            mExpiredLL.setVisibility(View.VISIBLE);
                            mExpiredTV.setText("证书还有" + iDays + "天失效，请及时进行证书更新!");
                        } else if (iDays <= 0) {
                            mExpiredLL.setVisibility(View.VISIBLE);
                            mExpiredTV.setText("证书已经失效，请进行证书更新!");
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
    }

    @OnClick(R.id.scheme_sign_btn)
    public void onViewClicked() {
        if (!SPManager.getLoginState() || mUserInfo == null) {
            schemeSignResp(Constants.SCHEME_ERR_LOGIN, "用户未登录");
            return;
        }

        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(SchemeSignActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        final String code = SPManager.getSDKAuthCode();
        if (mCertInfo != null && mCertInfo.getEndDate() != null) {
            long lDays = DateUtil.getDays(mCertInfo.getEndDate());
            if (lDays < 0) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                schemeSignResp(Constants.SCHEME_ERR_CERT_EXPIRED, getString(R.string.cert_gx_tip));
                return;
            }

            signature(code, mStrUserInfo);
        } else {
            MKeyApi.getInstance(SchemeSignActivity.this, code, mStrUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
                @Override
                public void onMKeyApiCallBack(MKeyApiResult result) {
                    if (result.getCode().equals("0")) {
                        mCertInfo = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                        if (mCertInfo != null) {
                            long lDays = DateUtil.getDays(mCertInfo.getEndDate());
                            if (lDays < 0) {
                                if (mLoadDlg != null) {
                                    mLoadDlg.dismiss();
                                    mLoadDlg = null;
                                }
                                schemeSignResp(Constants.SCHEME_ERR_CERT_EXPIRED, getString(R.string.cert_gx_tip));
                                return;
                            }

                            // 签名
                            signature(code, mStrUserInfo);
                        } else {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            schemeSignResp(Constants.SCHEME_ERR_CERT_NOT, getString(R.string.cert_no_tip));
                        }
                    } else {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }

                        if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                            Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                            sendBroadcast(intent);
                        }

                        schemeSignResp(Constants.SCHEME_ERR_CERT_NOT, result.getMsg());
                    }
                }
            });
        }
    }

    private void signature(final String appCode, final String userInfo) {

        AppUtil.signVerifyPin(SchemeSignActivity.this, new AppUtil.VeifyCallBack() {
            @Override
            public void onSuccess(String pin) {
                MKeyApi.getInstance(SchemeSignActivity.this, appCode, userInfo, SPManager.getUserInfo().algVersion).signature(mSignSrc, pin, new MKeyApiCallback() {
                    @Override
                    public void onMKeyApiCallBack(final MKeyApiResult result) {
                        if (result.getCode().equals("0")) {
                            schemeSignResp(Constants.SCHEME_ERR_SUCCESS, "签名成功", mCertInfo.getCert(), result.getData());
                        } else {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }

                            if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                sendBroadcast(intent);
                            }

                            schemeSignResp(Constants.SCHEME_ERR_SIGN, result.getMsg());
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
                schemeSignResp(Constants.SCHEME_ERR_SIGN, errMsg);
            }
        });
    }

    private void schemeSignResp(String errCode, String errMsg) {
        schemeSignResp(errCode, errMsg, "", "");
    }

    private void schemeSignResp(String errCode, String errMsg, String cert, String signValue) {
        Intent intent = new Intent();
        intent.setClassName(mAppPackName, Constants.THREE_APP_SIGN_RESP);
        intent.putExtra("type", mType);
        intent.putExtra("errCode", errCode);
        intent.putExtra("errMsg", errMsg);
        intent.putExtra("cert", cert);
        intent.putExtra("signValue", signValue);
        startActivity(intent);
        finishActivity();
    }
}
