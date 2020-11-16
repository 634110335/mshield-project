package com.cuisec.mshield.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.cuisec.mshield.R;

public class LoginPopDialog {

    private Context mContext;
    LoginPopView mPopView;

    public LoginPopDialog(Context context) {
        mContext = context;
    }

    public void showPopDialog(final LoginPopView.OnPopTempLogin tempLgoin) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.login_pop_view_dialog, null);

        final Dialog dialog = new Dialog(mContext, R.style.CommonDialogStyle);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();


        mPopView = view.findViewById(R.id.login_pop_view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        mPopView.setmPopTempLogin(new LoginPopView.OnPopTempLogin() {
            @Override
            public void onPopTempLogin() {
                tempLgoin.onPopTempLogin();
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        // 设置相关位置，一定要在 show()之后
//        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }
}
