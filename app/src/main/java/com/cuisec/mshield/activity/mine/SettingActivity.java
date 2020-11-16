package com.cuisec.mshield.activity.mine;

import android.widget.Button;

import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.xiaomi.mipush.sdk.MiPushClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.setting_push_btn)
    Button mPushBtn;

    private boolean mEnablePush;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle(getString(R.string.mine_msg));
    }

    @Override
    protected void initializeData() {
        // 获取消息推送状态
        mEnablePush = SPManager.getMsgPush();
        setPushState();
    }

    @OnClick(R.id.setting_push_btn)
    public void onViewClicked() {
        mEnablePush = !mEnablePush;
        setPushState();
    }


    private void setPushState() {
        try {
            UserInfo userInfo = SPManager.getUserInfo();
            if (mEnablePush) {
                mPushBtn.setBackgroundResource(R.mipmap.switch_on);
                if (userInfo != null && !"".equals(userInfo.phone)) {
                    MiPushClient.setAlias(getApplicationContext(), SecurityUtil.sha256(userInfo.phone + SPManager.getServerCode()), null);
                }
            } else {
                mPushBtn.setBackgroundResource(R.mipmap.switch_off);
                if (userInfo != null && !"".equals(userInfo.phone)) {
                    MiPushClient.unsetAlias(getApplicationContext(), SecurityUtil.sha256(userInfo.phone + SPManager.getServerCode()), null);
                    SPManager.setSignFreeStatus(false);
                }
            }
            SPManager.setMsgPush(mEnablePush);
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
    }
}
