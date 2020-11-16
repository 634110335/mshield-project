package com.cuisec.mshield.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cuisec.mshield.R;

public class AlertDialog {
    private Context mContext;
    private Dialog mDialog;
    private TextView mTitleTv;
    private ImageView mAlertIv;
    private TextView mMsgTv;
    private Button mNegBtn;
    private Button mPosBtn;
    private TextView mLineTv;
    private Display mDisplay;
    private boolean showTitle = false;
    private boolean showImage = false;
    private boolean showMsg = false;
    private boolean showPosBtn = false;
    private boolean showNegBtn = false;

    public AlertDialog(Context context) {
        this.mContext = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();
    }

    public AlertDialog builder() {
        // 获取Dialog布局
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.layout_alert_dialog, null);

        // 获取自定义Dialog布局中的控件
        LinearLayout layout_bg = (LinearLayout) view.findViewById(R.id.lLayout_bg);
        mTitleTv = (TextView) view.findViewById(R.id.txt_title);
        mTitleTv.setVisibility(View.GONE);
        mAlertIv = (ImageView) view.findViewById(R.id.alert_image);
        mAlertIv.setVisibility(View.GONE);
        mMsgTv = (TextView) view.findViewById(R.id.txt_msg);
        mMsgTv.setVisibility(View.GONE);
        mNegBtn = (Button) view.findViewById(R.id.btn_neg);
        mNegBtn.setVisibility(View.GONE);
        mPosBtn = (Button) view.findViewById(R.id.btn_pos);
        mPosBtn.setVisibility(View.GONE);
        mLineTv = (TextView) view.findViewById(R.id.vec_line_tv);
        mLineTv.setVisibility(View.GONE);

        // 定义Dialog布局和参数
        mDialog = new Dialog(mContext, R.style.AlertDialogStyle);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(false);

        // 调整dialog背景大小
        layout_bg.setLayoutParams(new FrameLayout.LayoutParams((int) (mDisplay
                .getWidth() * 0.85), LayoutParams.WRAP_CONTENT));

        return this;
    }

    public AlertDialog setTitle(String title) {
        showTitle = true;
        if ("".equals(title)) {
            mTitleTv.setVisibility(View.GONE);
        } else {
            mTitleTv.setText(title);
        }
        return this;
    }

    public AlertDialog setImage(int imageId) {
        showImage = true;
        mAlertIv.setImageResource(imageId);
        return this;
    }

    public AlertDialog setMessage(String msg) {
        showMsg = true;
        if ("".equals(msg)) {
            mMsgTv.setVisibility(View.GONE);
        } else {
            mMsgTv.setText(msg);
        }
        return this;
    }

    public AlertDialog setCancelable(boolean cancel) {
        mDialog.setCancelable(cancel);
        return this;
    }

    public AlertDialog setPositiveButton(String text,
                                         final OnClickListener listener) {
        showPosBtn = true;
        if ("".equals(text)) {
            mPosBtn.setText(mContext.getString(R.string.app_ok));
        } else {
            mPosBtn.setText(text);
        }
        mPosBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                mDialog.dismiss();
            }
        });
        return this;
    }

    public AlertDialog setNegativeButton(String text,
                                         final OnClickListener listener) {
        showNegBtn = true;
        if ("".equals(text)) {
            mNegBtn.setText(mContext.getString(R.string.app_cancel));
        } else {
            mNegBtn.setText(text);
        }
        mNegBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                mDialog.dismiss();
            }
        });
        return this;
    }

    private void setLayout() {
        if (!showTitle && !showMsg) {
            mTitleTv.setText(mContext.getString(R.string.app_tip));
            mTitleTv.setVisibility(View.VISIBLE);
        }

        if (showTitle) {
            mTitleTv.setVisibility(View.VISIBLE);
        }

        if (showImage) {
            mAlertIv.setVisibility(View.VISIBLE);
        }

        if (showMsg) {
            mMsgTv.setVisibility(View.VISIBLE);
        }

        if (!showPosBtn && !showNegBtn) {
            mPosBtn.setText(mContext.getString(R.string.app_ok));
            mPosBtn.setVisibility(View.VISIBLE);
            mPosBtn.setBackgroundResource(R.drawable.btn_alert_dlg_single);
            mPosBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
        }

        if (showPosBtn && showNegBtn) {
            mPosBtn.setVisibility(View.VISIBLE);
            mPosBtn.setBackgroundResource(R.drawable.btn_alert_dlg_right);
            mNegBtn.setVisibility(View.VISIBLE);
            mNegBtn.setBackgroundResource(R.drawable.btn_alert_dlg_left);
            mLineTv.setVisibility(View.VISIBLE);
        }

        if (showPosBtn && !showNegBtn) {
            mPosBtn.setVisibility(View.VISIBLE);
            mPosBtn.setBackgroundResource(R.drawable.btn_alert_dlg_single);
        }

        if (!showPosBtn && showNegBtn) {
            mNegBtn.setVisibility(View.VISIBLE);
            mNegBtn.setBackgroundResource(R.drawable.btn_alert_dlg_single);
        }
    }

    public void show() {
        setLayout();
        mDialog.show();
    }

    public void dismiss() {
        mDialog.dismiss();
    }
}
