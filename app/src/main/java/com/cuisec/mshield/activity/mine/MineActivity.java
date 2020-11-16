package com.cuisec.mshield.activity.mine;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.ActivityManager;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.Util;
import com.cuisec.mshield.widget.CircleImageView;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.activity.mine.seal.MineSealActivity;
import com.cuisec.mshield.receiver.MineBroadcastReceiver;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;

public class MineActivity extends BaseActivity {

    @BindView(R.id.mine_avatar_iv)
    CircleImageView mAvatarIV;
    @BindView(R.id.mine_nick_tv)
    TextView mNickTV;
    @BindView(R.id.mine_phone_rz_iv)
    ImageView mPhoneRzIV;
    @BindView(R.id.mine_auth_rz_iv)
    ImageView mAuthRzIV;
    @BindView(R.id.mine_phone_tv)
    TextView mPhoneTV;

    private UserInfo mUserInfo;

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_mine);
        ButterKnife.bind(this);

        ActivityManager.getInstance().putActivity(this.getLocalClassName(), this);
    }

    @Override
    protected void initializeViews() {
        showTitle(getString(R.string.mine_title));
    }

    @Override
    protected void initializeData() {
        mUserInfo = SPManager.getUserInfo();
        // 加载用户信息
        loadUserInfo(mUserInfo);

        // 注册我的广播
        registerMineBroadcast();
    }

    @OnClick({R.id.mine_avatar_iv, R.id.mine_auth_tv, R.id.mine_safe_tv, R.id.mine_seal_tv, R.id.mine_msg_tv, R.id.mine_about_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mine_avatar_iv: {
                if (AppUtil.isLogin(this)) {
                    Intent intent = new Intent(this, MineUserActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.mine_auth_tv: {
                if (AppUtil.isLoginWithAuth(this)) {
                    T.showShort(this, getString(R.string.mine_auth_completed));
                }
                break;
            }
            case R.id.mine_safe_tv: {
                if (AppUtil.isLogin(this)) {
                    Intent intent = new Intent(this, MineSafeActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.mine_seal_tv: {
                if (AppUtil.isLogin(this)) {
                    new RxPermissions(this)
                            .request(Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean aBoolean) throws Exception {
                                    if (aBoolean){
                                        // 权限开启
                                        Intent intent = new Intent(MineActivity.this, MineSealActivity.class);
                                        startActivity(intent);
                                    }else{
                                        //权限未开启
                                        Toast.makeText(MineActivity.this, "请在设置->应用管理中打开拍照或录像、读写手机存储权限", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                break;
            }
            case R.id.mine_msg_tv: {
                if (AppUtil.isLogin(this)) {
                    Intent intent = new Intent(this, SettingActivity.class);
                    startActivity(intent);
                }
                break;
            }
            case R.id.mine_about_tv: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    private void registerMineBroadcast() {
        mBroadcastReceiver = new MineBroadcastReceiver(new MineBroadcastReceiver.MineReceiverCallBack() {
            @Override
            public void onMineLoginUpdate(UserInfo userInfo) {
                mUserInfo = userInfo;
                loadUserInfo(userInfo);
            }

            @Override
            public void onMinePhotoUpdate(String photo) {
                if (photo != null && photo.length() != 0) {
                    byte[] byteUserLogo = Base64.decode(photo, Base64.DEFAULT);
                    Bitmap userLogoBitmap = BitmapFactory.decodeByteArray(byteUserLogo, 0, byteUserLogo.length);
                    mAvatarIV.setImageBitmap(userLogoBitmap);
                }
            }

            @Override
            public void onMinePhoneUpdate(String phone) {
                String strPhone = Util.phoneFormat(phone);
                mPhoneTV.setText(strPhone);
            }

            @Override
            public void onMineAuthUpdate(String auth) {
                if (!auth.equals(Constants.AUTH_STATUS_NONE)) {
                    mAuthRzIV.setVisibility(View.VISIBLE);
                } else {
                    mAuthRzIV.setVisibility(View.GONE);
                }
            }
        });
        IntentFilter intentFilter = new IntentFilter(Constants.MINE_UPDATA_BROADCAST);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }


    // 加载用户信息
    private void loadUserInfo(UserInfo userInfo) {
        if (userInfo != null) {
            // 设置头像
            if (userInfo.logo != null && userInfo.logo.length() != 0) {
                if (userInfo.logo.startsWith("http")) {
                    Glide.with(this).load(userInfo.logo).into(mAvatarIV);
                } else {
                    byte[] byteAvatar = Base64.decode(userInfo.logo, Base64.DEFAULT);
                    Bitmap avatarBitmap = BitmapFactory.decodeByteArray(byteAvatar, 0, byteAvatar.length);
                    Glide.with(this).load(avatarBitmap).into(mAvatarIV);
                }
            }

            // 设置昵称
            if (mUserInfo.nickName == null || mUserInfo.nickName.equals("")) {
                if (mUserInfo.userName == null || mUserInfo.userName.equals("")) {
                    mNickTV.setText(getString(R.string.mine_not_nickname));
                } else {
                    mNickTV.setText(mUserInfo.userName);
                }
            } else {
                mNickTV.setText(mUserInfo.nickName);
            }

            // 设置手机号
            String strPhone = Util.phoneFormat(userInfo.phone);
            mPhoneTV.setText(strPhone);
            mPhoneTV.setVisibility(View.VISIBLE);
            // 显示手机图标
            mPhoneRzIV.setVisibility(View.VISIBLE);

            if (!userInfo.authStatus.equals(Constants.AUTH_STATUS_NONE)) {
                mAuthRzIV.setVisibility(View.VISIBLE);
            } else {
                mAuthRzIV.setVisibility(View.GONE);
            }
        } else {
            mAvatarIV.setImageResource(R.mipmap.ico_avatar);
            mNickTV.setText(getString(R.string.mine_not_login));
            mPhoneRzIV.setVisibility(View.GONE);
            mAuthRzIV.setVisibility(View.GONE);
            mPhoneTV.setVisibility(View.GONE);
            mPhoneTV.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);

        ActivityManager.getInstance().closeActivity(this.getLocalClassName());
    }
}
