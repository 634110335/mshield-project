package com.cuisec.mshield.activity.home;

import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cuisec.mshield.bean.CertInfoBean;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.Base64Utils;
import com.cuisec.mshield.utils.DateUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.T;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.custle.ksmkey.MKeyMacro;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CertDetailActivity extends BaseActivity {
    @BindView(R.id.cert_detail_sn_tv)
    TextView mSnTv;
    @BindView(R.id.cert_detail_subject_tv)
    TextView mSubjectTv;
    @BindView(R.id.cert_detail_issuer_tv)
    TextView mIssuerTv;
    @BindView(R.id.cert_detail_start_tv)
    TextView mStartTv;
    @BindView(R.id.cert_detail_end_tv)
    TextView mEndTv;
    @BindView(R.id.cert_detail_ll)
    LinearLayout mDetailLl;
    @BindView(R.id.cert_detail_loading_iv)
    ImageView mLoadingIv;
    @BindView(R.id.cert_detail_expired_tv)
    TextView mExpiredTv;
    @BindView(R.id.cert_detail_expired_ll)
    LinearLayout mExpiredLl;
    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_cert_detail);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("证书查看");
        mDetailLl.setVisibility(View.GONE);

        // 创建旋转动画
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.loading_rotate);
        mLoadingIv.startAnimation(animation);//开始动画
    }

    @Override
    protected void initializeData() {
        getCertInfo(SPManager.getSDKAuthCode());
    }
    private void getCertInfo(String code) {
        UserInfo userInfo = SPManager.getUserInfo();
        if(userInfo == null)return;
        String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(CertDetailActivity.this, code, strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                mLoadingIv.setVisibility(View.GONE);
                mLoadingIv.clearAnimation();

                if (result.getCode().equals("0")) {
                    CertInfoBean certInfo = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                    if (certInfo != null) {
                        mSubjectTv.setText(certInfo.getCertSubject());
                        mIssuerTv.setText(certInfo.getCertIssuer());
                        mStartTv.setText(certInfo.getStartDate());
                        mEndTv.setText(certInfo.getEndDate());
                        mSnTv.setText(certInfo.getCertSn());
                        L.i(certInfo.getCertSn());
                        mDetailLl.setVisibility(View.VISIBLE);
                        certExpiredTip(certInfo.getEndDate());
                    } else {
                        T.showShort(CertDetailActivity.this, "获取证书失败");
                        finishActivity();
                    }
                } else {

                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }

                    T.showShort(CertDetailActivity.this, result.getMsg());
                    finishActivity();
                }
            }
        });
    }

    private void certExpiredTip(String date) {
        long iDays = DateUtil.getDays(date);
        if (iDays <= Constants.CERT_EXPIRED_DAYS && iDays > 0) {
            mExpiredLl.setVisibility(View.VISIBLE);
            mExpiredTv.setText("证书还有" + iDays + "天失效，请及时进行证书更新!");
        } else if (iDays <= 0) {
            mExpiredLl.setVisibility(View.VISIBLE);
            mExpiredTv.setText("证书已经失效，请进行证书更新!");
        }
    }
}