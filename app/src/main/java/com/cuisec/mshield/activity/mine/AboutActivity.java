package com.cuisec.mshield.activity.mine;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.update.UpdateManager;
import com.cuisec.mshield.updateapk.IndexContract;
import com.cuisec.mshield.updateapk.IndexPresenter;
import com.cuisec.mshield.utils.AppInfoUtil;
import com.cuisec.mshield.utils.NetUtil;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.activity.common.WebViewActivity;
import com.cuisec.mshield.utils.T;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.os.Process.killProcess;


public class AboutActivity extends BaseActivity implements IndexContract.View{

    @BindView(R.id.about_version_tv)
    TextView mVersionTv;
    @BindView(R.id.about_protocol_btn)
    Button mProtocolBtn;
    private IndexPresenter mPresenter;
    private Dialog mDialog;
    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        mPresenter = new IndexPresenter(this,this,true);
    }

    @Override
    protected void initializeViews() {
        showTitle("关于我们");

        // 临时button添加下划线
        mProtocolBtn.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mProtocolBtn.getPaint().setAntiAlias(true);

    }

    @Override
    protected void initializeData() {
        try {
            String appName = AppInfoUtil.getAppName(this);
            String versionName = AppInfoUtil.getVersionName(this);
            mVersionTv.setText(appName + "  " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.about_app_update_tv, R.id.about_protocol_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.about_app_update_tv: {
                if (MyApplication.mNetWorkState != NetUtil.NETWORN_NONE) {
                    // 检查更新
                    PackageInfo pi = null;
                    try {
                        pi = getPackageManager().getPackageInfo(getPackageName(),0);
                        String local = pi.versionName;
                        int code = pi.versionCode;
                        mPresenter.checkUpdate(local);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                  /*  UpdateManager updateManager = new UpdateManager(this, true);
                    updateManager.checkUpdate();*/
                }
                break;
            }
            case R.id.about_protocol_btn: {
                Intent intent = new Intent(AboutActivity.this, WebViewActivity.class);
                intent.putExtra("title", "隐私政策");
                intent.putExtra("url", "file:///android_asset/server_protocol.html");
                //intent.putExtra("url", "http://cuisec.com/");
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void showUpdate(final String version) {
        if (mDialog == null)
            mDialog =   new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("检测到有新版本")
                    .setMessage("当前版本:"+version)
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPresenter.downApk(AboutActivity.this);
                        }
                    })
                    .setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPresenter.setIgnore(version);
                        }
                    })
                    .create();

        //重写这俩个方法，一般是强制更新不能取消弹窗
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK && mDialog != null && mDialog.isShowing();
            }
        });

        mDialog.show();
    }

    @Override
    public void showProgress(int progress) {

    }

    @Override
    public void showFail(String msg) {

    }

    @Override
    public void showComplete(File file) {
        try {
            String authority = getApplicationContext().getPackageName() + ".fileProvider";
            Uri fileUri = FileProvider.getUriForFile(this, authority, file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            //7.0以上需要添加临时读取权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            } else {
                Uri uri = Uri.fromFile(file);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            }

            startActivity(intent);

            //弹出安装窗口把原程序关闭。
            //避免安装完毕点击打开时没反应
            killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.unbind(this);
    }
}
