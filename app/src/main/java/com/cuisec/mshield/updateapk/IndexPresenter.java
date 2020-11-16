package com.cuisec.mshield.updateapk;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.cuisec.mshield.R;
import com.cuisec.mshield.bean.VersionBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.utils.AppInfoUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.T;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class IndexPresenter implements IndexContract.Presenter {
    private IndexContract.View view;
    private ServiceConnection conn;
    private String mVersionCode;
    private String mVersionPath;
    private String mDescriPtion;
    private String mVersionName;
    private Context mContext;
    private boolean mNeedNotify = false;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                view.showUpdate(mVersionName);
            }else if (msg.what == 2){
                T.showShort(mContext, mContext.getString(R.string.update_already_latest));
            }
        }
    };
    public IndexPresenter(IndexContract.View view,Context context) {
        this.view = view;
        this.mContext = context;
    }
    public IndexPresenter(IndexContract.View view,Context context,boolean bNeedNotify) {
        this.view = view;
        this.mContext = context;
        this.mNeedNotify = bNeedNotify;
    }
    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }
    public void geturl(){
    }
    /**
     * 请求网络
     * 获取网络版本号
     * 获取成功后与本地版本号比对
     * 符合更新条件就控制view弹窗
     */
    @Override
    public void checkUpdate(final String local) {
        //假设获取得到最新版本
        //一般还要和忽略的版本做比对。。这里就不累赘了
        try {
            OkHttpClient mClientWithCache = getClientWithCache();
            RequestBody requestBody = FormBody.create(null
                    , "");
            final Request request = new Request.Builder()
                    .post(requestBody)
                    .url(Config.https_base_service_url+Config.version_app)
                    .build();
            mClientWithCache.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }
                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        String string = response.body().string();
                        mVersionCode = string.substring(0, 2);
                        mVersionName = string.substring(3, 8);
                        mVersionPath = string.substring(9, string.length() - 6);
                        mDescriPtion = string.substring(string.length() - 5);
                       if (!mVersionName.equals(local)){
                           mHandler.sendEmptyMessageDelayed(1,0);
                       }else {
                           if (mNeedNotify){
                               mHandler.sendEmptyMessageDelayed(2,0);
                           }
                       }
                    } catch (Exception e) {
                        // mHandler.sendEmptyMessageDelayed(1, 0);
                    }
                }
            });
        }catch (Exception e){
            e.getMessage();
        }
    }
    /**
     * 设置忽略版本
     */
    @Override
    public void setIgnore(String version) {
        SpUtils.getInstance().putString("ignore",version);
    }

    /**
     * 模拟网络下载
     */
    @Override
    public void downApk(Context context) {
        if (conn == null)
            conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
                    DownloadService myService = binder.getService();
                    myService.downApk(mVersionPath, new DownloadService.DownloadCallback() {
                        @Override
                        public void onPrepare() {
                        }
                        @Override
                        public void onProgress(int progress) {
                            view.showProgress(progress);
                        }
                        @Override
                        public void onComplete(File file) {
                            view.showComplete(file);
                        }
                        @Override
                        public void onFail(String msg) {
                            view.showFail(msg);
                        }
                    });
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    //意味中断，较小发生，酌情处理
                }
            };
        Intent intent = new Intent(context,DownloadService.class);
        context.bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }
    @Override
    public void unbind(Context context) {
        if (conn != null)
        context.unbindService(conn);
    }
}