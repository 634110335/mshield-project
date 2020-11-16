package com.cuisec.mshield.activity.mine.seal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cuisec.mshield.utils.ImageUtil;
import com.cuisec.mshield.widget.signature.views.SignaturePad;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class MineSignatureActivity extends BaseActivity implements SignaturePad.OnSignedListener {

    @BindView(R.id.signature_pad)
    SignaturePad mSignaturePad;
    @BindView(R.id.clear_btn)
    Button mClearBtn;
    @BindView(R.id.save_btn)
    Button mSaveBtn;
    @BindView(R.id.color_rg)
    RadioGroup mColorRg;
    @BindView(R.id.black_rb)
    RadioButton mBlackRb;
    @BindView(R.id.red_rb)
    RadioButton mRedRb;
    @BindView(R.id.blue_rb)
    RadioButton mBlueRb;

    private Boolean isBitmapEmpty;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_mine_signature);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle(getString(R.string.seal_write));
        mSignaturePad.setOnSignedListener(this);
//        showSeal(getIntent().getByteArrayExtra("image"));
    }

    @Override
    protected void initializeData() {
        isBitmapEmpty = true;
        // 默认为黑色，加大15像素
        ViewGroup.LayoutParams layoutParams = mBlackRb.getLayoutParams();
        layoutParams.width += 15;
        layoutParams.height += 15;
        mBlackRb.setLayoutParams(layoutParams);
    }

    @Override
    public void onStartSigning() {
        //mSignaturePad.setSignatureBitmap(null,0,0);
    }

    @Override
    public void onSigned() {
        mSaveBtn.setEnabled(true);
        mClearBtn.setEnabled(true);
        isBitmapEmpty = false;
    }

    @Override
    public void onClear() {
        //mSaveBtn.setEnabled(false);
        mClearBtn.setEnabled(false);
        isBitmapEmpty = true;
    }

    @OnClick({R.id.clear_btn, R.id.save_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.clear_btn: {
                mSignaturePad.clear();
                break;
            }
            case R.id.save_btn: {
                mSaveBtn.setEnabled(false);
                mClearBtn.setEnabled(false);
                Bitmap signatureBitmap = mSignaturePad.getTransparentSignatureBitmap();
                signatureBitmap = ImageUtil.scaleBitmap(signatureBitmap, 480);

                if (isBitmapEmpty) {
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("img", null);
                    resultIntent.putExtras(bundle);
                    this.setResult(RESULT_OK, resultIntent);
                    this.finish();
                } else {
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    if (signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)) {
                        byte[] ss = byteStream.toByteArray();
                        Intent resultIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putByteArray("img", ss);
                        resultIntent.putExtras(bundle);
                        this.setResult(RESULT_OK, resultIntent);
                        this.finish();
                    }
                }


//                if (signatureBitmap.getWidth() == signaturePadWidth && signatureBitmap.getHeight() == signaturePadHeight) {
//                    Log.e("onViewClicked 3", "onViewClicked: " + signatureBitmap.getConfig());
//                }
//                Log.e("onViewClicked 5", "isBitmapEmpty: " + ImageUtil.isBitmapEmpty(signatureBitmap));


                //signatureBitmap = ImageUtil.scaleBitmap(signatureBitmap, 480);
                // 保存图片后台
//                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//                if (signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)) {
//                    byte[] ss = byteStream.toByteArray();
//                    Intent resultIntent = new Intent();
//                    Bundle bundle = new Bundle();
//                    bundle.putByteArray("img", ss);
//                    resultIntent.putExtras(bundle);
//                    this.setResult(RESULT_OK, resultIntent);
//                    this.finish();
//                }
                break;
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
                    mSignaturePad.setPenColorRes(R.color.black);
                    break;
                }
                case R.id.red_rb: {
                    mSignaturePad.setPenColorRes(R.color.red);
                    break;
                }
                case R.id.blue_rb: {
                    mSignaturePad.setPenColorRes(R.color.blue);
                    break;
                }
            }
        } else {
            // 取消选中时减小15像素
            layoutParams.width -= 15;
            layoutParams.height -= 15;
            buttonView.setLayoutParams(layoutParams);
        }
    }

//    private void showSeal(byte[] img) {
//        try {
//            if (img != null) {
//                Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
//                mSignaturePad.setSignatureBitmap(bitmap, signaturePadWidth, signaturePadHeight);
//                isBitmapEmpty = false;
//            } else {
//                isBitmapEmpty = true;
//            }
//        } catch (Exception e) {
//            L.e(e.getLocalizedMessage());
//        }
//    }
//
//
//    private int signaturePadWidth;
//    private int signaturePadHeight;
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        signaturePadWidth = mSignaturePad.getWidth();
//        signaturePadHeight = mSignaturePad.getHeight();
//        showSeal(getIntent().getByteArrayExtra("image"));
//
//    }
}
