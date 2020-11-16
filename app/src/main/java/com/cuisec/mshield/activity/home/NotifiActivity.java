package com.cuisec.mshield.activity.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.cuisec.mshield.R;
import com.cuisec.mshield.SeachActivity;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.fragment.ConsultFragment;
import com.cuisec.mshield.utils.T;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotifiActivity extends BaseActivity {
    @BindView(R.id.ll_details)
    LinearLayout mLlDetails;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_notifi);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {

    }

    @Override
    protected void initializeData() {
        showTitle("通知详情");
    }
    @OnClick(R.id.ll_details)
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.ll_details:
                Intent intent = new Intent(NotifiActivity.this, SeachActivity.class);
                startActivity(intent);
                break;
        }
    }
}
