package com.cuisec.mshield.activity.mine.safe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.activity.home.HomeActivity;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.AlertDialog;
import com.cuisec.mshield.widget.CircleImageView;
import com.cuisec.mshield.widget.finger.FingerprintIdentify;
import com.cuisec.mshield.widget.finger.base.BaseFingerprint;
import com.cuisec.mshield.widget.lock.LockPatternUtils;
import com.cuisec.mshield.widget.lock.LockPatternView;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.login.LoginActivity;

import java.util.List;


public class GestureVerifyActivity extends AppCompatActivity implements View.OnClickListener {

    private LockPatternView mLockPatternView;       //手势view
    private int mFailedPatternAttemptsSinceLastTimeout = 0;
    private CountDownTimer mCountdownTimer = null;
    private Handler mHandler = new Handler();
    private TextView mTipTV;
    private Animation mShakeAnim;

    //指纹参数
    private FingerprintIdentify mFingerprintIdentify;
    private static final int MAX_AVAILABLE_TIMES = 3;
    private AlertDialog mAlertDialog;

    private RelativeLayout mGestrueRL;
    private RelativeLayout mFingerRL;
    private CircleImageView mLogoIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_verify);

        //手势设置
        mLockPatternView = findViewById(R.id.gesture_verify_lock_pattern);
        mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
        mLockPatternView.setTactileFeedbackEnabled(false);  //关闭震动

        mTipTV = findViewById(R.id.mine_security_gesture_verify_tip_tv);

        mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_x);

        findViewById(R.id.gesture_verify_forget_btn).setOnClickListener(this);
        findViewById(R.id.finger_verify_ll).setOnClickListener(this);
        findViewById(R.id.finger_verify_forget_btn).setOnClickListener(this);

        mGestrueRL = findViewById(R.id.gesture_verify_rl);
        mFingerRL = findViewById(R.id.finger_verify_rl);
        mLogoIV = findViewById(R.id.finger_logo_rl);

        if (MyApplication.getInstance().getLockPatternUtils().savedPatternExists()) {
            mGestrueRL.setVisibility(View.VISIBLE);
        } else {
            mFingerRL.setVisibility(View.VISIBLE);
            // 设置头像
            UserInfo userInfo = SPManager.getUserInfo();
            if (userInfo != null && userInfo.logo != null && userInfo.logo.length() != 0) {
                if (userInfo.logo.startsWith("http") || userInfo.logo.startsWith("https")) {
                    Glide.with(this).load(userInfo.logo).into(mLogoIV);
                } else {
                    byte[] byteAvatar = Base64.decode(userInfo.logo, Base64.DEFAULT);
                    Bitmap avatarBitmap = BitmapFactory.decodeByteArray(byteAvatar, 0, byteAvatar.length);
                    Glide.with(this).load(avatarBitmap).into(mLogoIV);
                }
            }
        }

        mFingerprintIdentify = new FingerprintIdentify(this, null);
        if (mFingerprintIdentify.isHardwareEnable()) {
            //判断指纹设置状态
            Boolean bStatus = SPManager.getFingerLockStatus();
            if (bStatus) {
                startFinger();
                mAlertDialog = new AlertDialog(this)
                        .builder()
                        .setImage(R.mipmap.ico_zhiwen)
                        .setMessage(getString(R.string.finger_verify_tip))
                        .setPositiveButton(getString(R.string.app_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mFingerprintIdentify.cancelIdentify();
                            }
                        });
                mAlertDialog.show();
            }
        }
    }


    private void startFinger() {
        mFingerprintIdentify.startIdentify(MAX_AVAILABLE_TIMES, new BaseFingerprint.FingerprintIdentifyListener() {
            @Override
            public void onSucceed() {
                T.showShort(GestureVerifyActivity.this, getString(R.string.finger_match));
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    mAlertDialog = null;
                }
                mFingerprintIdentify.cancelIdentify();
                Intent intent = new Intent(GestureVerifyActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onNotMatch(int availableTimes) {
                T.showShort(GestureVerifyActivity.this, getString(R.string.finger_not_match));
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    mAlertDialog = null;
                }

                fingerVerify();
            }

            @Override
            public void onFailed(boolean isDeviceLocked) {
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    mAlertDialog = null;
                }

                fingerVerify();
            }

            @Override
            public void onStartFailedByDeviceLocked() {
                if (mAlertDialog != null) {
                    mAlertDialog.dismiss();
                    mAlertDialog = null;
                }

                fingerVerify();
            }
        });

    }

    private void fingerVerify() {
        mFingerprintIdentify.cancelIdentify();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startFinger();
                mAlertDialog = new AlertDialog(GestureVerifyActivity.this)
                        .builder()
                        .setImage(R.mipmap.ico_zhiwen)
                        .setMessage(getString(R.string.finger_verify_tip))
                        .setPositiveButton(getString(R.string.app_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                mAlertDialog.show();
            }
        }, 1000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountdownTimer != null)
            mCountdownTimer.cancel();
    }


    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            patternInProgress();
        }

        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (pattern == null)
                return;
            if (MyApplication.getInstance().getLockPatternUtils().checkPattern(pattern)) {
                mLockPatternView
                        .setDisplayMode(LockPatternView.DisplayMode.Correct);

                mLockPatternView.clearPattern();

                Intent intent = new Intent(GestureVerifyActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();

            } else {
                mLockPatternView
                        .setDisplayMode(LockPatternView.DisplayMode.Wrong);

                if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                    mFailedPatternAttemptsSinceLastTimeout++;
                    int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT
                            - mFailedPatternAttemptsSinceLastTimeout;
                    if (retry >= 0) {
                        if (retry == 0)
                            T.showShort(GestureVerifyActivity.this, "您已5次输错密码，请30秒后再试");
                        mTipTV.setText("密码错误，还可以再输入" + retry + "次");
                        mTipTV.setTextColor(Color.RED);
                        mTipTV.startAnimation(mShakeAnim);
                    }

                } else {
                    T.showShort(GestureVerifyActivity.this, "输入长度不够，请重试");
                }

                if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
                    mHandler.postDelayed(attemptLockout, 2000);
                } else {
                    mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
                }
            }
        }

        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

        }

        private void patternInProgress() {
        }
    };

    Runnable attemptLockout = new Runnable() {

        @Override
        public void run() {
            mLockPatternView.clearPattern();
            mLockPatternView.setEnabled(false);
            mCountdownTimer = new CountDownTimer(
                    LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS + 1, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    int secondsRemaining = (int) (millisUntilFinished / 1000) - 1;
                    if (secondsRemaining > 0) {
                        mTipTV.setText(secondsRemaining + " 秒后重试");
                    } else {
                        mTipTV.setText("请绘制手势密码");
                        mTipTV.setTextColor(Color.parseColor("#3190e8"));
                    }

                }

                @Override
                public void onFinish() {
                    mLockPatternView.setEnabled(true);
                    mFailedPatternAttemptsSinceLastTimeout = 0;
                }
            }.start();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gesture_verify_forget_btn: {
                new AlertDialog(GestureVerifyActivity.this)
                        .builder()
                        .setImage(R.mipmap.app_logo)
                        .setMessage(getString(R.string.login_re_login_tip))
                        .setNegativeButton(getString(R.string.app_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 手势取消
                                MyApplication.getInstance().getLockPatternUtils().saveLockPattern(null);
                                // 指纹取消
                                SPManager.setFingerLockStatus(false);
                                // 清除登录状态
                                SPManager.setLoginState(false);

                                Intent intent = new Intent(GestureVerifyActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setPositiveButton(getString(R.string.app_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                break;
            }
            case R.id.finger_verify_ll: {
                if (mFingerprintIdentify.isHardwareEnable()) {
                    //判断指纹设置状态
                    Boolean bStatus = SPManager.getFingerLockStatus();
                    if (bStatus) {
                        startFinger();
                        mAlertDialog = new AlertDialog(this)
                                .builder()
                                .setImage(R.mipmap.ico_zhiwen)
                                .setMessage(getString(R.string.finger_verify_tip))
                                .setPositiveButton(getString(R.string.app_cancel), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mFingerprintIdentify.cancelIdentify();
                                    }
                                });
                        mAlertDialog.show();
                    }
                }
                break;
            }
            case R.id.finger_verify_forget_btn: {
                new AlertDialog(GestureVerifyActivity.this)
                        .builder()
                        .setImage(R.mipmap.app_logo)
                        .setMessage(getString(R.string.login_re_login_tip))
                        .setNegativeButton(getString(R.string.app_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 清除指纹状态
                                SPManager.setFingerLockStatus(false);
                                // 清除登录状态
                                SPManager.setLoginState(false);

                                Intent intent = new Intent(GestureVerifyActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setPositiveButton(getString(R.string.app_cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
                break;
            }
        }
    }
}