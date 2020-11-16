package com.cuisec.mshield.activity.mine.seal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cuisec.mshield.utils.ImageUtil;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class MineSealDealActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    @BindView(R.id.seal_iv)
    ImageView mSealIv;
    @BindView(R.id.black_rb)
    RadioButton mBlackRb;
    @BindView(R.id.threshold_sb)
    SeekBar mThresholdSb;
    @BindView(R.id.threshold_tv)
    TextView mThresholdTv;

    private byte[] mSealImg;
    private Bitmap mSealBitmap;

    @ColorInt
    private int mColor = 0XFF000000;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_mine_seal_deal);

        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle(getString(R.string.seal_deal));

        mThresholdSb.setMax(255);
        mThresholdSb.setKeyProgressIncrement(5);
        mThresholdSb.setProgress(160);
        mThresholdTv.setText(String.valueOf(160));

        mThresholdSb.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void initializeData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        mSealImg = bundle.getByteArray("img");
//        dealImage();
        myHandler.sendEmptyMessage(1);

        // 默认为黑色，加大15像素
        ViewGroup.LayoutParams layoutParams = mBlackRb.getLayoutParams();
        layoutParams.width += 15;
        layoutParams.height += 15;
        mBlackRb.setLayoutParams(layoutParams);
    }

    @OnClick({R.id.seal_ok_btn})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.seal_ok_btn: {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                if (mSealBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)) {
                    byte[] ss = byteStream.toByteArray();
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("img", ss);
                    resultIntent.putExtras(bundle);
                    this.setResult(RESULT_OK, resultIntent);
                    this.finish();
                }
            }
        }
    }

    @OnCheckedChanged({R.id.black_rb, R.id.red_rb, R.id.blue_rb})
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ViewGroup.LayoutParams layoutParams = buttonView.getLayoutParams();
        if (isChecked) {
            // 选中时加大15像素
            layoutParams.width += 15;
            layoutParams.height += 15;
            buttonView.setLayoutParams(layoutParams);
            //设置选中按钮的大小，设置颜色
            switch (buttonView.getId()) {
                case R.id.black_rb: {
                    mColor = 0XFF000000;
                    break;
                }
                case R.id.red_rb: {
                    mColor = 0XFFFF0000;
                    break;
                }
                case R.id.blue_rb: {
                    mColor = 0XFF0000FF;
                    break;
                }
            }
//            dealImage();
            myHandler.sendEmptyMessage(1);
        } else {
            // 取消选中时减小15像素
            layoutParams.width -= 15;
            layoutParams.height -= 15;
            buttonView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mThresholdTv.setText(String.valueOf(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
//        dealImage();
        myHandler.sendEmptyMessage(1);
    }

//    Handler handler = new Handler()  {
//        public void handleMessage(android.os.Message msg) {
//            if(msg.what==0x123)
//            {
//                tv.setText("更新后的TextView");
//            }
//        };
//    };

    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 1: {
                    mSealBitmap = BitmapFactory.decodeByteArray(mSealImg, 0, mSealImg.length);
                    mSealBitmap = ImageUtil.transparency(mSealBitmap, mThresholdSb.getProgress(), mColor);
                    mSealIv.setImageBitmap(mSealBitmap);
                    break;
                }
                default:
                    break;
            }
        }
    };


//    private void dealImage() {
//        mSealBitmap = BitmapFactory.decodeByteArray(mSealImg, 0, mSealImg.length);
//        mSealBitmap = ImageUtil.transparency(mSealBitmap, mThresholdSb.getProgress(), mColor);
//        mSealIv.setImageBitmap(mSealBitmap);
//    }
}
