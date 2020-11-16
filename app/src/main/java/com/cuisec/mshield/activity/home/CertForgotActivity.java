package com.cuisec.mshield.activity.home;

import android.content.Intent;
import android.widget.EditText;

import com.cuisec.mshield.bean.BaseBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.AppUtil;
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
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLDecoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class CertForgotActivity extends BaseActivity {

    @BindView(R.id.cert_forgot_et)
    EditText mCodeET;

    private LoadDialog mLoadDlg = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_cert_forgot);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("找回证书密码");
    }

    @Override
    protected void initializeData() {

    }

    @OnClick(R.id.cert_forgot_btn)
    public void onViewClicked() {
        hideKeyBoard();

        if (!AppUtil.isLogin(CertForgotActivity.this)) {
            return;
        }


        String strCode = mCodeET.getText().toString();
        if (strCode == null || strCode.length() == 0) {
            T.showShort("请输入授权码");
            return;
        }

        verifyCertAuthCode(strCode);
    }

    private void verifyCertAuthCode(String code) {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(CertForgotActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        OkHttpUtils
                .post()
                .url(SPManager.getServerUrl() + Config.user_check)
                .addHeader("token", SPManager.getUserToken())
                .addParams("certCode", code)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                        T.showShort(CertForgotActivity.this, getString(R.string.app_network_error));

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
                                T.showShort(bean.getMsg());
                            }
                        } catch (Exception e) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(e.getLocalizedMessage());
                        }
                    }
                });
    }

    private void unlockPin() {
        UserInfo userInfo = SPManager.getUserInfo();
        String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(CertForgotActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).unlockPin(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                if (result.getCode().equals("0")) {
                    SPManager.setCertPin("");
                    SPManager.setFingerSignStatus(false);
                    T.showShort(CertForgotActivity.this, "设置密码成功");
                    finishActivity();
                } else {

                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }
                    T.showShort(CertForgotActivity.this, result.getMsg());
                }
            }
        });
    }
}
