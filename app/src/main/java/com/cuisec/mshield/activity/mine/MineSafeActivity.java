package com.cuisec.mshield.activity.mine;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Handler;

import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.ActivityManager;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.AlertDialog;
import com.cuisec.mshield.widget.finger.FingerprintIdentify;
import com.cuisec.mshield.widget.finger.base.BaseFingerprint;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.activity.mine.safe.CreateGestureActivity;
import com.cuisec.mshield.activity.mine.safe.PhoneVerifyActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MineSafeActivity extends BaseActivity {

    @BindView(R.id.lock_btn)
    Button mLockBtn;
    @BindView(R.id.finger_btn)
    Button mFingerBtn;
    @BindView(R.id.mine_safe_lock_rl)
    RelativeLayout mSafeLockRl;
    @BindView(R.id.mine_safe_finger_rl)
    RelativeLayout mSafeFingerRl;

    private FingerprintIdentify mFingerprintIdentify;
    private AlertDialog mAlertDialog;
    private static final int MAX_AVAILABLE_TIMES = 3;

    private UserInfo mUserInfo;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_mine_safe);
        ButterKnife.bind(this);

        ActivityManager.getInstance().putActivity(this.getLocalClassName(), this);
    }

    @Override
    protected void initializeViews() {
        showTitle(getString(R.string.mine_safe));
    }

    @Override
    protected void initializeData() {
        mFingerprintIdentify = new FingerprintIdentify(this, null);

        // 手势密码初始化设置
        if (MyApplication.getInstance().getLockPatternUtils().savedPatternExists()) {
            mLockBtn.setBackgroundResource(R.mipmap.switch_on);
        } else {
            mLockBtn.setBackgroundResource(R.mipmap.switch_off);
        }

        // 指纹初始化设置
        if (SPManager.getFingerIsSupport()) {
            mSafeLockRl.setVisibility(View.GONE);
            mSafeFingerRl.setVisibility(View.VISIBLE);
            //判断指纹设置状态
            Boolean bStatus = SPManager.getFingerLockStatus();
            if (bStatus) {
                mFingerBtn.setBackgroundResource(R.mipmap.switch_on);
            } else {
                mFingerBtn.setBackgroundResource(R.mipmap.switch_off);
            }
        } else {
            mSafeLockRl.setVisibility(View.VISIBLE);
            mSafeFingerRl.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.safe_pin_change_tv, R.id.safe_phone_change_btn, R.id.lock_btn, R.id.finger_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.safe_pin_change_tv: {
                Intent intent = new Intent(MineSafeActivity.this, SafeChangePinActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.lock_btn: {
                if (MyApplication.getInstance().getLockPatternUtils().savedPatternExists()) {
                    MyApplication.getInstance().getLockPatternUtils().saveLockPattern(null);
                    mLockBtn.setBackgroundResource(R.mipmap.switch_off);
                } else {
                    Intent intent = new Intent(MineSafeActivity.this, CreateGestureActivity.class);
                    startActivityForResult(intent, 2000);
                }
                break;
            }
            case R.id.finger_btn: {
                onFingerBtnClick();
                break;
            }
            case R.id.safe_phone_change_btn: {
                Intent intent = new Intent(MineSafeActivity.this, PhoneVerifyActivity.class);
                startActivity(intent);
                break;
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2000 && resultCode == Activity.RESULT_OK) {
            mLockBtn.setBackgroundResource(R.mipmap.switch_on);
        }
    }

    private void onFingerBtnClick() {
        if (!mFingerprintIdentify.isRegisteredFingerprint()) {
            T.showShort(this, getString(R.string.finger_set_tip));
            return;
        }

        startFinger();

        Boolean bStatus = SPManager.getFingerLockStatus();
        mAlertDialog = new AlertDialog(this)
                .builder()
                .setImage(R.mipmap.ico_zhiwen)
                .setMessage(bStatus ? getString(R.string.finger_close_tip) : getString(R.string.finger_open_tip))
                .setPositiveButton(getString(R.string.app_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFingerprintIdentify.cancelIdentify();
                    }
                });
        mAlertDialog.show();
    }

    private void startFinger() {
        mFingerprintIdentify.startIdentify(MAX_AVAILABLE_TIMES, new BaseFingerprint.FingerprintIdentifyListener() {
            @Override
            public void onSucceed() {
                T.showShort(MineSafeActivity.this, getString(R.string.finger_match));
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    mAlertDialog = null;
                }

                //判断指纹设置状态
                Boolean bStatus = SPManager.getFingerLockStatus();
                if (bStatus) {
                    mFingerBtn.setBackgroundResource(R.mipmap.switch_off);
                    SPManager.setFingerLockStatus(false);
                } else {
                    mFingerBtn.setBackgroundResource(R.mipmap.switch_on);
                    SPManager.setFingerLockStatus(true);
                }
            }

            @Override
            public void onNotMatch(int availableTimes) {
                T.showShort(MineSafeActivity.this, getString(R.string.finger_not_match));
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    mAlertDialog = null;
                }

                fingerVerify(2000);
            }

            @Override
            public void onFailed(boolean isDeviceLocked) {
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    mAlertDialog = null;
                }

                fingerVerify(2000);
            }

            @Override
            public void onStartFailedByDeviceLocked() {
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    mAlertDialog = null;
                }

                fingerVerify(2000);
            }
        });
    }

    private void fingerVerify(long delayMillis) {
        final Boolean bStatus = SPManager.getFingerLockStatus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startFinger();
                mAlertDialog = new AlertDialog(MineSafeActivity.this)
                        .builder()
                        .setImage(R.mipmap.ico_zhiwen)
                        .setMessage(bStatus ? getString(R.string.finger_close_tip) : getString(R.string.finger_open_tip))
                        .setPositiveButton(getString(R.string.app_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        });
                mAlertDialog.show();
            }
        }, delayMillis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().closeActivity(this.getLocalClassName());
    }
}
