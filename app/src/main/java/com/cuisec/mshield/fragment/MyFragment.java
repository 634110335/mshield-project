package com.cuisec.mshield.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.mine.AboutActivity;
import com.cuisec.mshield.activity.mine.MineActivity;
import com.cuisec.mshield.activity.mine.MineSafeActivity;
import com.cuisec.mshield.activity.mine.MineUserActivity;
import com.cuisec.mshield.activity.mine.SettingActivity;
import com.cuisec.mshield.activity.mine.seal.MineSealActivity;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.receiver.MineBroadcastReceiver;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.Util;
import com.cuisec.mshield.widget.CircleImageView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class MyFragment extends Fragment implements View.OnClickListener {
    private static MyFragment sMyFragment;

    private UserInfo mUserInfo;

    private BroadcastReceiver mBroadcastReceiver;
    private View view;
    private CircleImageView mMineAvatarIv;
    /**
     * 登录
     */
    private TextView mMineNickTv;
    private ImageView mMinePhoneRzIv;
    private ImageView mMineAuthRzIv;
    private TextView mMinePhoneTv;
    /**
     * 实名认证
     */
    private TextView mMineAuthTv;
    /**
     * 账户安全
     */
    private TextView mMineSafeTv;
    /**
     * 手写签名
     */
    private TextView mMineSealTv;
    /**
     * 系统设置
     */
    private TextView mMineMsgTv;
    /**
     * 关于我们
     */
    private TextView mMineAboutTv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.activity_mine, null);
        //ButterKnife.bind(getActivity());
        initView(inflate);
        initData();

        return inflate;
    }

    private void initData() {
        mUserInfo = SPManager.getUserInfo();
        // 加载用户信息
        loadUserInfo(mUserInfo);

        // 注册我的广播
        registerMineBroadcast();
    }

    public static MyFragment getInstance() {
        if (sMyFragment == null) {
            sMyFragment = new MyFragment();
        }
        return sMyFragment;
    }

    // 加载用户信息
    private void loadUserInfo(UserInfo userInfo) {
        if (userInfo != null) {
            // 设置头像
            if (userInfo.logo != null && userInfo.logo.length() != 0) {
                if (userInfo.logo.startsWith("http")) {
                    Glide.with(this).load(userInfo.logo).into(mMineAvatarIv);
                } else {
                    byte[] byteAvatar = Base64.decode(userInfo.logo, Base64.DEFAULT);
                    Bitmap avatarBitmap = BitmapFactory.decodeByteArray(byteAvatar, 0, byteAvatar.length);
                    Glide.with(this).load(avatarBitmap).into(mMineAvatarIv);
                }
            }

            // 设置昵称
            if (mUserInfo.nickName == null || mUserInfo.nickName.equals("")) {
                if (mUserInfo.userName == null || mUserInfo.userName.equals("")) {
                    mMineNickTv.setText(getString(R.string.mine_not_nickname));
                } else {
                    mMineNickTv.setText(mUserInfo.userName);
                }
            } else {
                mMineNickTv.setText(mUserInfo.nickName);
            }

            // 设置手机号
            String strPhone = Util.phoneFormat(userInfo.phone);
            mMinePhoneTv.setText(strPhone);
            mMinePhoneTv.setVisibility(View.VISIBLE);
            // 显示手机图标
            mMinePhoneRzIv.setVisibility(View.VISIBLE);

            if (!userInfo.authStatus.equals(Constants.AUTH_STATUS_NONE)) {
                mMineAuthRzIv.setVisibility(View.VISIBLE);
            } else {
                mMineAuthRzIv.setVisibility(View.GONE);
            }
        } else {
            mMineAvatarIv.setImageResource(R.mipmap.ico_avatar);
            mMineNickTv.setText(getString(R.string.mine_not_login));
            mMinePhoneRzIv.setVisibility(View.GONE);
            mMineAuthRzIv.setVisibility(View.GONE);
            mMinePhoneTv.setVisibility(View.GONE);
            mMinePhoneTv.setText("");
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
                    mMineAvatarIv.setImageBitmap(userLogoBitmap);
                }
            }

            @Override
            public void onMinePhoneUpdate(String phone) {
                String strPhone = Util.phoneFormat(phone);
                mMinePhoneTv.setText(strPhone);
            }

            @Override
            public void onMineAuthUpdate(String auth) {
                if (!auth.equals(Constants.AUTH_STATUS_NONE)) {
                    mMineAuthRzIv.setVisibility(View.VISIBLE);
                } else {
                    mMineAuthRzIv.setVisibility(View.GONE);
                }
            }
        });
        IntentFilter intentFilter = new IntentFilter(Constants.MINE_UPDATA_BROADCAST);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private void initView(View inflate) {
        mMineAvatarIv = (CircleImageView) inflate.findViewById(R.id.mine_avatar_iv);
        mMineAvatarIv.setOnClickListener(this);
        mMineNickTv = (TextView) inflate.findViewById(R.id.mine_nick_tv);
        mMineNickTv.setOnClickListener(this);
        mMinePhoneRzIv = (ImageView) inflate.findViewById(R.id.mine_phone_rz_iv);
        mMinePhoneRzIv.setOnClickListener(this);
        mMineAuthRzIv = (ImageView) inflate.findViewById(R.id.mine_auth_rz_iv);
        mMineAuthRzIv.setOnClickListener(this);
        mMinePhoneTv = (TextView) inflate.findViewById(R.id.mine_phone_tv);
        mMinePhoneTv.setOnClickListener(this);
        mMineAuthTv = (TextView) inflate.findViewById(R.id.mine_auth_tv);
        mMineAuthTv.setOnClickListener(this);
        mMineSafeTv = (TextView) inflate.findViewById(R.id.mine_safe_tv);
        mMineSafeTv.setOnClickListener(this);
        mMineSealTv = (TextView) inflate.findViewById(R.id.mine_seal_tv);
        mMineSealTv.setOnClickListener(this);
        mMineMsgTv = (TextView) inflate.findViewById(R.id.mine_msg_tv);
        mMineMsgTv.setOnClickListener(this);
        mMineAboutTv = (TextView) inflate.findViewById(R.id.mine_about_tv);
        mMineAboutTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.mine_avatar_iv:
                //T.showShort("功能正在完善中，敬请期待");
                if (AppUtil.isLogin(getActivity())) {
                    Intent intent = new Intent(getActivity(), MineUserActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.mine_auth_tv:
                T.showShort("功能正在完善中，敬请期待");
                /*if (AppUtil.isLoginWithAuth(getActivity())) {
                    T.showShort(getActivity(), getString(R.string.mine_auth_completed));
                }*/
                break;
            case R.id.mine_safe_tv:
                if (AppUtil.isLogin(getActivity())) {
                    Intent intent = new Intent(getActivity(), MineSafeActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.mine_seal_tv:
                T.showShort("功能正在完善中，敬请期待");
                /*if (AppUtil.isLogin(getActivity())) {
                    new RxPermissions(this)
                            .request(Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean aBoolean) throws Exception {
                                    if (aBoolean){
                                        // 权限开启
                                        Intent intent = new Intent(getActivity(), MineSealActivity.class);
                                        startActivity(intent);

                                    }else{
                                        //权限未开启
                                        Toast.makeText(getActivity(), "请在设置->应用管理中打开拍照或录像、读写手机存储权限", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }*/
                break;
            case R.id.mine_msg_tv:
                T.showShort("功能正在完善中，敬请期待");
                if (AppUtil.isLogin(getActivity())) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
                break;
            case R.id.mine_about_tv:
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
                break;
        }
    }
}
