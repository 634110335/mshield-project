package com.cuisec.mshield.activity.mine;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cuisec.mshield.bean.ServerListBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.ActivityManager;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.widget.CircleImageView;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.activity.login.LoginActivity;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MineUserActivity extends BaseActivity {

    @BindView(R.id.mine_user_avatar_iv)
    CircleImageView mAvatarIv;
    @BindView(R.id.mine_user_account_tv)
    TextView mAccountTv;
    @BindView(R.id.mine_user_name_tv)
    TextView mNameTv;
    @BindView(R.id.mine_user_id_tv)
    TextView mIdTv;
    @BindView(R.id.mine_real_user_info_ll)
    LinearLayout mRealUserInfoLl;
    @BindView(R.id.mine_user_dept_tv)
    TextView mDeptTv;

    private UserInfo mUserInfo;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_mine_user);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("基本信息");

        mUserInfo = SPManager.getUserInfo();
        if (mUserInfo != null) {
            if (mUserInfo.logo != null && mUserInfo.logo.length() != 0) {
                if (mUserInfo.logo.startsWith("http")) {
                    Glide.with(this).load(mUserInfo.logo).into(mAvatarIv);
                } else {
                    byte[] byteAvatar = Base64.decode(mUserInfo.logo, Base64.DEFAULT);
                    Bitmap avatarBitmap = BitmapFactory.decodeByteArray(byteAvatar, 0, byteAvatar.length);
                    Glide.with(this).load(avatarBitmap).into(mAvatarIv);
                }
            }

            mAccountTv.setText(mUserInfo.phone);

            String userName = mUserInfo.userName;
            String userID = mUserInfo.idNo;
            try {
                if (userName != null && !userName.equals("") && userID != null && !userID.equals("")) {
                    mNameTv.setText(userName);
                    userID = userID.substring(0, 4) + "**********" + userID.substring(11);
                    mIdTv.setText(userID);
                    Log.e("userID", "initializeViews: "+userID );
                    mRealUserInfoLl.setVisibility(View.VISIBLE);
                }
            }catch (Exception e){

            }

            mDeptTv.setText(SPManager.getServerName());
            ServerListBean bean = (ServerListBean) JsonUtil.toObject(SPManager.getServerList(), ServerListBean.class);
            if (bean != null && bean.getData() != null) {
                List<ServerListBean.ServerDetail> serverList = bean.getData();
                if (serverList.size() >= 2) {
                    Drawable image = getResources().getDrawable(R.mipmap.ico_arrow_r);
                    image.setBounds(0, 0, image.getMinimumWidth(), image.getMinimumHeight());
                    mDeptTv.setCompoundDrawables(null, null, image, null);
                    mDeptTv.setCompoundDrawablePadding(3);
                }
            }

        } else {
            AppUtil.isLogin(this);
        }
    }

    @Override
    protected void initializeData() {
    }

    @OnClick({R.id.mine_user_avatar_ll, R.id.mine_logout_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mine_user_avatar_ll: {
                break;
            }
            case R.id.mine_logout_btn: {

                ActivityManager.getInstance().closeAllActivity();

                try {
                    UserInfo userInfo = SPManager.getUserInfo();
                    if (userInfo != null && !userInfo.phone.equals("")) {
                        // 注销小米推送别名
                        MiPushClient.unsetAlias(getApplicationContext(), SecurityUtil.sha256(userInfo.phone + SPManager.getServerCode()), null);
                    }
                    // 调用登出接口

                    OkHttpUtils
                            .post()
                            .addHeader("token", SPManager.getUserToken())
                            .url(SPManager.getServerUrl() + Config.user_logout)
                            .build()
                            .execute();
                } catch (Exception e) {
                }

                // 清除登录状态
                SPManager.setLoginState(false);

                Intent loginIntent = new Intent(MineUserActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finishActivity();
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
