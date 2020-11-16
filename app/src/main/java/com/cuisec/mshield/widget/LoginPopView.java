package com.cuisec.mshield.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.cuisec.mshield.R;

public class LoginPopView extends RelativeLayout implements View.OnClickListener {

    private OnPopTempLogin mPopTempLogin;

    public LoginPopView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 加载布局
        LayoutInflater.from(context).inflate(R.layout.login_pop_view, this);

        findViewById(R.id.login_pop_temp_btn).setOnClickListener(this);
    }

    public OnPopTempLogin getmPopTempLogin() {
        return mPopTempLogin;
    }

    public void setmPopTempLogin(OnPopTempLogin mPopTempLogin) {
        this.mPopTempLogin = mPopTempLogin;
    }

    @Override
    public void onClick(View v) {
        if (mPopTempLogin != null) {
            mPopTempLogin.onPopTempLogin();
        }
    }

    public interface OnPopTempLogin {
        void onPopTempLogin();
    }
}
