package com.cuisec.mshield;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.push.PushService;
import com.custle.ksmkey.MKeyApi;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.YAppManager;
import com.cuisec.mshield.push.MiMsgHandle;
import com.cuisec.mshield.utils.NetUtil;
import com.cuisec.mshield.widget.finger.FingerprintIdentify;
import com.cuisec.mshield.widget.lock.LockPatternUtils;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.smtt.sdk.QbSdk;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.https.HttpsUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by licz on 2018/5/25.
 * 初始化application
 */

public class MyApplication extends MultiDexApplication {

    private static MyApplication mInstance;
    public static int mNetWorkState;
    private static MiMsgHandle mMiHandle = null;
    private LockPatternUtils mLockPatternUtils;
    public static MyApplication getInstance() {
        if (mInstance == null) {
            mInstance = new MyApplication();
        }
        return mInstance;
    }
    public static Context context;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        // 安装tinker
        //安装tinker
        Beta.installTinker();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        context = this;
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId,调试时将第三个参数设置为true
        Bugly.init(this, "1c52fee542", false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy( builder.build());
            builder.detectFileUriExposure();
        }
        SPManager.setServerCode("21");
        SPManager.setServerName("联通手机盾");
        //SPManager.setServerUrl("http://61.138.142.30:38080/v1");//正式环境，手机盾
         SPManager.setServerUrl("http://111.203.206.158:18083/v1");//测试环境
        //SPManager.setServerSDKUrl("http://61.138.142.30:38080/sdk/v1");//正式环境
        SPManager.setServerSDKUrl("http://111.203.206.158:18083/sdk/v1");//测试环境
        SPManager.setServerMM(false);
        MKeyApi.initSDK(SPManager.getServerSDKUrl(), SPManager.getServerCode());
        // 获取网络状态
        mNetWorkState = NetUtil.getNetworkState(this);
        // 设置签名空闲状态
        YAppManager.getInstance().setSignBusyStatus(false);
        // 初始化小米推送
        if (mMiHandle == null) {
            mMiHandle = new MiMsgHandle(getApplicationContext());
        }
        initialMiPush();
        // 初始化九宫格锁
        mLockPatternUtils = new LockPatternUtils(this);
        // 初始化OKHttp库
        initOkHttps();
        //初始化腾讯TBS浏览器com.tencent.smtt.sdk.WebView
        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
            }
            @Override
            public void onViewInitFinished(boolean b) {
            }
        });

        if (!SPManager.getFingerIsSupport()) {
            // 判断手机是否支持指纹
            FingerprintIdentify fingerIdentify = new FingerprintIdentify(this, null);
            if (fingerIdentify.isHardwareEnable()) {
                SPManager.setFingerIsSupport(true);
            }
        }
    }

    public LockPatternUtils getLockPatternUtils() {
        return mLockPatternUtils;
    }
    public void initOkHttps() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .authenticator(new TokenAuthenticator())
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
        /*HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .authenticator(new TokenAuthenticator())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();*/
        OkHttpUtils.initClient(okHttpClient);
    }

    public void initialMiPush() {
        if (shouldInit()) {
            MiPushClient.registerPush(MyApplication.this, Constants.MI_APP_ID, Constants.MI_APP_KEY);
        }
    }
    public void unInitialMiPush() {
        MiPushClient.unregisterPush(MyApplication.this);
    }

    public static MiMsgHandle getHandler() {
        return mMiHandle;
    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
                if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}
