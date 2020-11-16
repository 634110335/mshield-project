package com.cuisec.mshield.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.bean.CitrsBean;
import com.cuisec.mshield.utils.T;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends BaseActivity {

    @BindView(R.id.project_name)
    TextView mProjectName;
    @BindView(R.id.project_type)
    TextView mProjectType;
    @BindView(R.id.project_time)
    TextView mProjectTime;
    @BindView(R.id.project_web)
    TextView mProjectWeb;
    @BindView(R.id.web_copy)
    TextView mWebCopy;
    private CitrsBean.DomainListBean mData;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("项目详情");
    }

    @Override
    protected void initializeData() {
        if (getIntent() != null) {
            mData = (CitrsBean.DomainListBean) getIntent().getExtras().get("data");
        }
        mProjectName.setText(mData.getTitle());
        mProjectTime.setText(mData.getReleasetime().substring(0, 11));
        mProjectWeb.setText(mData.getDetailUrl());
        mProjectType.setText(mData.getBidType());
        mWebCopy.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyText(mProjectWeb.getText().toString());
                return false;
            }
        });
    }
    private void copyText(String copiedText) {
        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, copiedText));
        //T.showShort("Copy Text: " + copiedText);
        T.showShort("已复制");
    }
}
