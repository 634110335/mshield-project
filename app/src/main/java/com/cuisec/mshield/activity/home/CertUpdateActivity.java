package com.cuisec.mshield.activity.home;

import android.content.Intent;

import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.DateUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.LoadDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.custle.ksmkey.MKeyMacro;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class CertUpdateActivity extends BaseActivity {

    private LoadDialog mLoadDlg = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_cert_update);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("证书更新");
    }

    @Override
    protected void initializeData() {

    }

    @OnClick(R.id.cert_update_btn)
    public void onViewClicked() {
        if (!AppUtil.isLogin(CertUpdateActivity.this)) {
            return;
        }

        String endData = getIntent().getStringExtra("cert_end_date");
        if (endData != null) {
            long lDays = DateUtil.getDays(endData);
            if (lDays > Constants.CERT_EXPIRED_DAYS) {
                T.showLong(CertUpdateActivity.this, "证书过期前" + Constants.CERT_EXPIRED_DAYS + "天才能进行更新!");
                return;
            }
        }

        certUpdate();
    }

    private void certUpdate() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(CertUpdateActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        UserInfo userInfo = SPManager.getUserInfo();
        String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(CertUpdateActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).updateCert(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }

                String errInfo = "{\"ret\":\"" + result.getCode() + "\",\"msg\":\"" + result.getMsg() + "\"}";
                AppNetUtil.postAppLog(CertUpdateActivity.this, Constants.LOG_UPDATE_CERT, errInfo);

                if (result.getCode().equals("0")) {
                    T.showShort(CertUpdateActivity.this, getString(R.string.cert_gx_success));
                    finishActivity();
                } else {

                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }
                    T.showShort(CertUpdateActivity.this, result.getMsg());
                }
            }
        });
    }
}
