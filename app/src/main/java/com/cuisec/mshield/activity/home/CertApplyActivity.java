package com.cuisec.mshield.activity.home;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.Util;
import com.cuisec.mshield.widget.LoadDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.custle.ksmkey.MKeyMacro;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CertApplyActivity extends BaseActivity {

    @BindView(R.id.cert_apply_name_tv)
    TextView mNameTv;
    @BindView(R.id.cert_apply_phone_tv)
    TextView mPhoneTv;
    @BindView(R.id.cert_apply_id_tv)
    TextView mIdTv;
    @BindView(R.id.cert_apply_btn)
    Button mApplyBtn;

    private LoadDialog mLoadDlg = null;
    private UserInfo mUserInfo = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_cert_apply);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("证书申请");
    }

    @Override
    protected void initializeData() {
        mUserInfo = SPManager.getUserInfo();
        if (mUserInfo == null)return;
        mNameTv.setText(mUserInfo.userName);
        String strPhone = Util.phoneFormat(mUserInfo.phone);
        mPhoneTv.setText(strPhone);
        mIdTv.setText(mUserInfo.idNo);
    }

    @OnClick(R.id.cert_apply_btn)
    public void onViewClicked() {
        mApplyBtn.setEnabled(false);
        if (!AppUtil.isLogin(CertApplyActivity.this)) {
            mApplyBtn.setEnabled(true);
            return;
        }

        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(CertApplyActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        String strUserInfo = "{\"name\":\"" + mUserInfo.userName + "\",\"idNo\":\"" + mUserInfo.idNo + "\",\"mobile\":\"" + mUserInfo.phone + "\"}";
        MKeyApi.getInstance(CertApplyActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).applyCert(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                String errInfo = "{\"ret\":\"" + result.getCode() + "\",\"msg\":\"" + result.getMsg() + "\"}";
                AppNetUtil.postAppLog(CertApplyActivity.this, Constants.LOG_APPLY_CERT, errInfo);
                if (result.getCode().equals("0")) {
                    T.showShort(CertApplyActivity.this, "证书申请成功");
                    mApplyBtn.setEnabled(true);
                    finishActivity();
                } else {
                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }
                    mApplyBtn.setEnabled(true);
                    T.showShort(CertApplyActivity.this, result.getMsg());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
