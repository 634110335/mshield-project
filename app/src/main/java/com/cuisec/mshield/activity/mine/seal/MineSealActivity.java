package com.cuisec.mshield.activity.mine.seal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.cuisec.mshield.bean.BaseBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.ActivityManager;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.FileUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.LoadDialog;
import com.cuisec.mshield.widget.MenuDialog;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class MineSealActivity extends BaseActivity {

    private static final int IMAGE_REQUEST_TAG = 2000;
    private static final int PHOTO_REQUEST_TAG = 2001;
    private static final int CAMERA_REQUEST_TAG = 2002;
    private static final int PHOTO_REQUEST_CUT = 2003;

    @BindView(R.id.seal_iv)
    ImageView mSealIv;
    @BindView(R.id.seal_mgr_btn)
    Button mSealMgrBtn;

    private UserInfo mUserInfo;
    private byte[] mSealImg;
    private LoadDialog mLoadDlg;

    private Uri mImageUri;
    private Uri mImageCropUri;

    private String mImagePath;
    private String mImageCropPath;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_mine_seal);

        ActivityManager.getInstance().putActivity(this.getLocalClassName(), this);

        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle(getString(R.string.mine_seal));
    }

    @Override
    protected void initializeData() {
        mUserInfo = SPManager.getUserInfo();

        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(MineSealActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        // 从服务器获取印章
        AppNetUtil.getSeal(new AppNetUtil.GetSealCallBack() {
            @Override
            public void sealValue(String seal) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }

                if (seal != null && seal.length() != 0) {
                    byte[] img = Base64.decode(seal, Base64.NO_WRAP);
                    if (writeSeal(img)) {
                        showSeal(img);
                        mSealImg = img;
                    }
                }
            }
        });

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        mImagePath = path + "/yyq_tmp.jpg";
        mImageCropPath = path + "/yyq_tmp_crop.jpg";
        mImageUri = Uri.fromFile(new File(mImagePath));
        mImageCropUri = Uri.fromFile(new File(mImageCropPath));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMAGE_REQUEST_TAG: {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        byte[] img = bundle.getByteArray("img");
                        if (img != null) {
                            updateSeal(img);
                            showSeal(img);
                        } else {
                            updateSeal(img);
                            showSeal(img);
                        }
                    }
                }
                break;
            }
            case PHOTO_REQUEST_TAG:
            case CAMERA_REQUEST_TAG: {
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    if (requestCode == PHOTO_REQUEST_TAG) {
                        intent.setDataAndType(data.getData(), "image/*");
                    } else {
                        intent.setDataAndType(mImageUri, "image/*");
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 2);// 裁剪框比例
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("outputX", 480);// 输出图片大小
                    intent.putExtra("outputY", 240);
                    intent.putExtra("return-data", false);
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCropUri);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    intent.putExtra("noFaceDetection", true);
                    startActivityForResult(intent, PHOTO_REQUEST_CUT);
                }
                break;
            }
            case PHOTO_REQUEST_CUT: {
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mImageCropUri));
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)) {
                            byte[] img = byteStream.toByteArray();
                            Intent intent = new Intent(MineSealActivity.this, MineSealDealActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putByteArray("img", img);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, IMAGE_REQUEST_TAG);
                        }
                        // 删除临时产生的文件
                        FileUtil.deleteFile(mImagePath);
                        FileUtil.deleteFile(mImageCropPath);
                    } catch (Exception e) {
                        L.e(e.getLocalizedMessage());
                    }
                }
                break;
            }
        }
    }

    @OnClick(R.id.seal_mgr_btn)
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.seal_mgr_btn: {
                new MenuDialog(this, getString(R.string.app_cancel), new String[]{"手写签名", "拍照", "从图库获取"}, true).setOnMyPopClickListener(new MenuDialog.MenuClickListener() {
                    @Override
                    public void onItemClick(int index, String content) {
                        if (index == 0) {
                            // 手写签名
                            Intent intent = new Intent(MineSealActivity.this, MineSignatureActivity.class);
                            startActivityForResult(intent, IMAGE_REQUEST_TAG);
                        } else if (index == 1) {
                            // 拍照
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//action is capture
                            intent.putExtra("return-data", false);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                            intent.putExtra("noFaceDetection", true);
                            startActivityForResult(intent, CAMERA_REQUEST_TAG);

                        } else if (index == 2) {
                            // 相册上传
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.putExtra("return-data", false);
                            startActivityForResult(intent, PHOTO_REQUEST_TAG);
                        }
                    }

                    @Override
                    public void onCancelClick(String content) {
                    }
                });
                break;
            }
        }
    }

    private void saveSeal(byte[] img) {
        try {
            String seal;
            if (img == null) {
                seal = "";

            } else {
                seal = Base64.encodeToString(img, Base64.NO_WRAP);
                seal = URLEncoder.encode(seal, Config.UTF_8);
            }

            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.user_seal)
                    .addHeader("token", SPManager.getUserToken())
                    .addParams("seal", seal)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            L.e(e.getLocalizedMessage());
                            e.printStackTrace();
                            T.showShort(MineSealActivity.this, getString(R.string.app_network_error));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                BaseBean bean = (BaseBean) JsonUtil.toObject(response, BaseBean.class);
                                if (bean != null) {
                                    if (bean.getRet() == 0) {
                                        T.showShort(getApplicationContext(), getString(R.string.seal_update_success));
                                    } else {
                                        T.showShort(getApplicationContext(), bean.getMsg());
                                    }
                                }
                            } catch (Exception e) {
                                T.showShort(getApplicationContext(), e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(getApplicationContext(), e.getLocalizedMessage());
        }
    }

    private void showSeal(byte[] img) {
        try {
            if (img != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                mSealIv.setImageBitmap(bitmap);
                mSealMgrBtn.setText(R.string.seal_update);
            } else {
                mSealIv.setImageBitmap(null);
                // 提示用户写入印章
                mSealMgrBtn.setText(R.string.seal_add);
            }
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
    }

    private void updateSeal(byte[] img) {
        try {
            mSealImg = img;
            writeSeal(img);  // 写到本地缓存
            saveSeal(img);   // 保存到服务器
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
    }

    private boolean writeSeal(byte[] img) {
        try {
            if (img == null) {
                File file = new File(mUserInfo.idNo + ".png");
                if (file.exists()) {
                    file.delete();
                }
            } else {
                FileOutputStream fout = openFileOutput(mUserInfo.idNo + ".png", MODE_PRIVATE);
                fout.write(img);
            }
            return true;
        } catch (FileNotFoundException e) {
            L.e(e.getLocalizedMessage());
            return false;
        } catch (IOException e) {
            L.e(e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().closeActivity(this.getLocalClassName());
    }
}
