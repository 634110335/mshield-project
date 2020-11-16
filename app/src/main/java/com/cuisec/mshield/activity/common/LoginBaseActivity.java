package com.cuisec.mshield.activity.common;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cuisec.mshield.utils.ScreenUtil;
import com.cuisec.mshield.R;

public abstract class LoginBaseActivity extends AppCompatActivity {

    public RelativeLayout nav_bar_rl;       // 导航栏
    public Button nav_back_btn;             // 导航返回
    public TextView nav_title_tv;           // 导航标题

    protected LinearLayout body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_base);

        //判断当前设备版本号是否为4.4以上，如果是，则通过调用setTranslucentStatus让状态栏变透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ScreenUtil.setTranslucentStatus(this, false);
        }

        // 动态设置状态栏高度
        View paddingView = findViewById(R.id.login_status_bar_view);
        ViewGroup.LayoutParams params = paddingView.getLayoutParams();
        params.height = ScreenUtil.getStatusHeight(this);

        initHeader();
        setContentView();
        initializeViews();
        initializeData();
    }

    protected void initHeader() {
        nav_bar_rl = findViewById(R.id.login_nav_bar_rl);
        nav_back_btn = findViewById(R.id.login_nav_bar_back_btn);
        nav_title_tv = findViewById(R.id.login_nav_bar_title_tv);

        body = findViewById(R.id.login_base_body);
    }

    @Override
    public void setContentView(int layoutResID) {
        if (layoutResID == R.layout.activity_login_base)
            super.setContentView(layoutResID);
        else {
            getLayoutInflater().inflate(layoutResID, body);
        }
    }
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        body.removeAllViews();
        body.addView(view, params);
    }
    protected abstract void setContentView();
    protected abstract void initializeViews();
    protected abstract void initializeData();

    /**
     * 隐藏键盘
     */
    public void hideKeyBoard() {
        try {
            if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                if (getCurrentFocus() != null)
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus()
                                            .getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            if (LoginBaseActivity.this.getCurrentFocus() != null) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(LoginBaseActivity.this
                                        .getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
    public void showTitle(String title) {
        nav_title_tv.setText(title);

        nav_back_btn.setVisibility(View.VISIBLE);
        nav_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard();
                onBackPressed();
            }
        });
    }
    /**
     * 开启页面
     */
    public void startActivity(Class<?> clazz) {
        Intent intent = new Intent();
        intent.setClass(LoginBaseActivity.this, clazz);
        LoginBaseActivity.this.startActivity(intent);
    }
    /**
     * 关闭当前Activity
     */
    public void finishActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 320);
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        finishActivity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
