package com.cuisec.mshield.update;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.bean.SourceBean;
import com.cuisec.mshield.bean.VersionBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.utils.AppInfoUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.OkHtpp3Utils;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.BuildConfig;
import com.cuisec.mshield.R;
import com.cuisec.mshield.widget.MessageSourcePopupwindow;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Environment.getExternalStorageDirectory;

public class UpdateManager {
    private Context mContext;
    private VersionBean mVersionBean;
    private int mVersionCode;
    private boolean mNeedNotify = false;
    private String mDescriPtion;
    private String mVersionPath;
    private MyHandler mHandler = new MyHandler(this);
    private OkHttpClient mClientWithCache;
    private MyApplication mMyApplication;
    private class MyHandler extends Handler {
        private WeakReference<UpdateManager> mUpdateManagerWeakReference;

        public MyHandler(UpdateManager updateManager) {
            mUpdateManagerWeakReference = new WeakReference<UpdateManager>(updateManager);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
           /* if (msg.what == 1) {
                T.showLong(mContext, mContext.getString(R.string.update_server_error));
            }*/
           switch (msg.what){
               case 0:
                   T.showLong(mContext, mContext.getString(R.string.update_server_error));
                   break;
               case 1:

                   break;
           }
        }
    }
    public UpdateManager(Context context) {
        this.mContext = context;
        try {
            mVersionCode = AppInfoUtil.getVersionCode(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UpdateManager(Context context, boolean bNeedNotify) {
        this.mContext = context;
        this.mNeedNotify = bNeedNotify;
        try {
            mVersionCode = AppInfoUtil.getVersionCode(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkUpdate() {
       /* mClientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(null
                , "");
        final Request request = new Request.Builder()
                .post(requestBody)
                .url("https://wxapp.uni-ca.com.cn:38888/app/versioninfo")
                .build();
        mClientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response)  {
                try {
                    String string = response.body().string();
                    Log.i("ssss", "onResponse: "+string);
                    String versionCode = string.substring(0, 2);
                    Log.i("ssss", "onResponse: "+versionCode);
                    String versionName = string.substring(3, 8);
                    mVersionPath = string.substring(9, string.length() - 6);
                    mDescriPtion = string.substring(string.length() - 5);
                    if (Integer.parseInt(versionCode)<= mVersionCode){
                        if (mNeedNotify) {
                            T.showLong(mContext, mContext.getString(R.string.update_already_latest));
                        }
                    } else {
                        // 弹出升级对话框
                        showUpdateDialog();
//                                    showUpdateDialog1();
                    }
                            *//*response = URLDecoder.decode(response, Config.UTF_8);
                            mVersionBean = (VersionBean) JsonUtil.toObject(response, VersionBean.class);
                            if (mVersionBean != null && mVersionBean.getRet() == 0) {   // 获取升级信息成功
                                if (Integer.parseInt(mVersionBean.getData().getVersionCode()) <= mVersionCode) {
                                    if (mNeedNotify) {
                                        T.showLong(mContext, mContext.getString(R.string.update_already_latest));
                                    }
                                } else {
                                    // 弹出升级对话框
                                    showUpdateDialog();
//                                    showUpdateDialog1();
                                }
                            } else {
                                T.showLong(mContext, mContext.getString(R.string.update_server_error));
                            }*//*
                } catch (Exception e) {
                   // mHandler.sendEmptyMessageDelayed(1, 0);
                }

            }
        });*/

        OkHttpUtils.post()
                //.url("https://wxapp.uni-ca.com.cn:38888/app/versioninfo")
               .url("https://61.138.142.30:38081/app/versioninfo")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        T.showShort(mContext, e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            String versionCode = response.substring(0, 2);
                            String versionName = response.substring(3, 8);
                            mVersionPath = response.substring(9, response.length() - 6);
                            mDescriPtion = response.substring(response.length() - 5);
                            if (Integer.parseInt(versionCode)<= mVersionCode){
                                    if (mNeedNotify) {
                                        T.showLong(mContext, mContext.getString(R.string.update_already_latest));
                                    }
                                } else {
                                    // 弹出升级对话框
                                    showUpdateDialog();
//                                    showUpdateDialog1();
                                }
                          /*  response = URLDecoder.decode(response, Config.UTF_8);
                            mVersionBean = (VersionBean) JsonUtil.toObject(response, VersionBean.class);
                            if (mVersionBean != null && mVersionBean.getRet() == 0) {   // 获取升级信息成功
                                if (Integer.parseInt(mVersionBean.getData().getVersionCode()) <= mVersionCode) {
                                    if (mNeedNotify) {
                                        T.showLong(mContext, mContext.getString(R.string.update_already_latest));
                                    }
                                } else {
                                    // 弹出升级对话框
                                    showUpdateDialog();
//                                    showUpdateDialog1();
                                }
                            } else {
                                T.showLong(mContext, mContext.getString(R.string.update_server_error));
                            }*/
                        } catch (Exception e) {
                            T.showLong(mContext, mContext.getString(R.string.update_server_error));
                        }
                    }
                });
      /*  OkHttpUtils.post()
                .url("https://wxapp.uni-ca.com.cn:38888/app/versioninfo")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        T.showShort(mContext, e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            response = URLDecoder.decode(response, Config.UTF_8);
                            mVersionBean = (VersionBean) JsonUtil.toObject(response, VersionBean.class);
                            if (mVersionBean != null && mVersionBean.getRet() == 0) {   // 获取升级信息成功
                                if (Integer.parseInt(mVersionBean.getData().getVersionCode()) <= mVersionCode) {
                                    if (mNeedNotify) {
                                        T.showLong(mContext, mContext.getString(R.string.update_already_latest));
                                    }
                                } else {
                                    // 弹出升级对话框
                                    showUpdateDialog();
//                                    showUpdateDialog1();
                                }
                            } else {
                                T.showLong(mContext, mContext.getString(R.string.update_server_error));
                            }
                           // String path = mVersionBean.getData().getUrl();
                            String path = "https://wxapp.uni-ca.com.cn:38888/pdf/app-release.apk";
                            Log.i("sss", "downLoadApk: "+path);
                            String[] fileNames = path.split("/");
                            for (int i = 0; i <fileNames.length ; i++) {
                                Log.i("sss", "onResponse: "+fileNames[i]);
                            }

                        } catch (Exception e) {
                            T.showLong(mContext, mContext.getString(R.string.update_server_error));
                        }
                    }
                });*/
    }

    /*
     *
	 * 弹出对话框通知用户更新程序 弹出对话框的步骤： 1.创建alertDialog的builder. 2.要给builder设置属性,
	 * 对话框的内容,样式,按钮 3.通过builder 创建一个对话框 4.对话框show()出来
	 */
    private void showUpdateDialog() {
        AlertDialog.Builder builer = new AlertDialog.Builder(mContext);
        builer.setTitle(R.string.update_title);
        //builer.setMessage(mVersionBean.getData().getDescription());
        builer.setMessage(mDescriPtion);
        // 点击确定按钮
        builer.setPositiveButton(R.string.update_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                downLoadApk();
            }
        });

        builer.setNegativeButton(R.string.update_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builer.create();
        dialog.show();
    }
    //定义个更新速率，避免更新通知栏过于频繁导致卡顿
    private float rate = .0f;
    private void downLoadApk() {
        final ProgressDialog pd; // 进度条对话框
        pd = new ProgressDialog(mContext);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage(mContext.getString(R.string.update_downing));
        pd.show();

        //String path = mVersionBean.getData().getUrl();
        //String path = "https://wxapp.uni-ca.com.cn:38888/pdf/app-release.apk";
        String path = "https://61.138.142.30:38081/system/provinceData/list";
        mHandler.sendEmptyMessage(0);
        Request request = new Request.Builder().url(path).build();
        OkHttpClient clientWithCache = getClientWithCache();

        clientWithCache .newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = e.getMessage();
                mHandler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = "下载错误";
                    mHandler.sendMessage(message);
                    return;
                }
                InputStream is = null;
                byte[] buff = new byte[2048];
                int len;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = createFile();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buff)) != -1) {
                        fos.write(buff,0,len);
                        sum+=len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        if (rate != progress) {
                            Message message = Message.obtain();
                            message.what = 2;
                            message.obj = progress;
                            mHandler.sendMessage(message);
                            rate = progress;
                        }
                    }
                    fos.flush();
                    Message message = Message.obtain();
                    message.what = 3;
                    message.obj = file.getAbsoluteFile();
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null)
                            is.close();
                        if (fos != null)
                            fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        String[] fileNames = path.split("/");
        final String fileName = fileNames[fileNames.length - 1];
        OkHttpUtils.get().url(path)
                .build()
                .execute(new FileCallBack(getExternalStorageDirectory().getAbsolutePath(), fileName) {
                    @Override
                    public void inProgress(float progress, long total, int id) {
                        pd.setProgress((int) (progress * total/1000));
                        pd.setMax((int) total/1000);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        L.e(e.getLocalizedMessage());
                        T.showLong(mContext, mContext.getString(R.string.update_download_error));
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        Log.i("ssss", "onResponse: "+response);
                        pd.dismiss();
                        installApk(response);
                    }
                });
    }
    /**
     * 路径为根目录
     * 创建文件名称为 updateDemo.apk
     */
    private File createFile() {
        String root = Environment.getExternalStorageDirectory().getPath();
        File file = new File(root,"updateDemo.apk");
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null ;
    }
    // 安装apk
    private void installApk(File file) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {  // 判读版本是否在7.0以上
            Uri apkUri = FileProvider.getUriForFile(
                    mContext
                    , "com.cuisec.mshield.fileprovider"
                    , file);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        }else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }

    //这个是你的包名
    private static final String apkName = "apk";

    private void showUpdateDialog1() {

        UpdateUtils.clearDownload();

        //设置自定义下载文件路径
        UpdateUtils.APP_UPDATE_DOWN_APK_PATH = "apk" + File.separator + "downApk";
        String url =  mVersionBean.getData().getUrl();
        String desc = mContext.getResources().getString(R.string.update_content_info);
        /*
         * @param isForceUpdate             是否强制更新
         * @param desc                      更新文案
         * @param url                       下载链接
         * @param apkFileName               apk下载文件路径名称
         * @param packName                  包名
         */
        UpdateFragment.showFragment(mActivity,
                false, url, apkName, desc, BuildConfig.APPLICATION_ID);
    }

    private FragmentActivity mActivity;

    public UpdateManager(FragmentActivity activity, Context context, boolean bNeedNotify) {
        this.mActivity = activity;
        this.mContext = context;
        this.mNeedNotify = bNeedNotify;
        try {
            mVersionCode = AppInfoUtil.getVersionCode(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }

}
