package com.cuisec.mshield.activity.login;

import android.widget.Button;

import com.cuisec.mshield.activity.common.LoginBaseActivity;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeveloperActivity extends LoginBaseActivity {

    @BindView(R.id.developer_btn)
    Button mDevBtn;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_developer);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("开发者模式");
        if (SPManager.getCenterUrl().equals(Config.https_server_url)) {
            mDevBtn.setBackgroundResource(R.mipmap.switch_off);
        } else {
            mDevBtn.setBackgroundResource(R.mipmap.switch_on);
        }
    }
    @Override
    protected void initializeData() {

    }
    @OnClick(R.id.developer_btn)
    public void onViewClicked() {
        if (SPManager.getCenterUrl().equals(Config.https_server_url)) {
            SPManager.setCenterUrl(Config.https_server_url_T);
            mDevBtn.setBackgroundResource(R.mipmap.switch_on);
        } else {
            SPManager.setCenterUrl(Config.https_server_url);
            mDevBtn.setBackgroundResource(R.mipmap.switch_off);
        }
    }
}
