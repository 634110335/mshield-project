package com.cuisec.mshield.activity.home;

import android.Manifest;
import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.activity.mine.MineSafeActivity;
import com.cuisec.mshield.bean.CertInfoBean;
import com.cuisec.mshield.bean.IpassOrderBean;
import com.cuisec.mshield.bean.IpassTheadNoticeBean;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.data.YAppManager;
import com.cuisec.mshield.design.MyBottomView;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.fragment.CertiiCateFragment;
import com.cuisec.mshield.fragment.ConsultFragment;
import com.cuisec.mshield.fragment.MyFragment;
import com.cuisec.mshield.fragment.ReturnPayResult;
import com.cuisec.mshield.fragment.ServiceFragment;
import com.cuisec.mshield.fragment.UpdateFragment;
import com.cuisec.mshield.push.PushService;
import com.cuisec.mshield.receiver.OnePixelReceiver;
import com.cuisec.mshield.updateapk.IndexContract;
import com.cuisec.mshield.updateapk.IndexPresenter;
import com.cuisec.mshield.utils.ActivityManager;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.NetUtil;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.AlertDialog;
import com.cuisec.mshield.widget.LoadDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.cuisec.mshield.R;
import com.cuisec.mshield.receiver.HomeBroadcastReceiver;
import com.cuisec.mshield.receiver.NetworkStatusReceiver;
import com.cuisec.mshield.receiver.NetworkStatusReceiver.NetworkEventHandler;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;
import org.greenrobot.eventbus.EventBus;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import io.reactivex.functions.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Process.killProcess;
public class HomeActivity extends AppCompatActivity implements NetworkEventHandler, MyBottomView.OnBottomClick, IndexContract.View {
    private long mExitTime = 0;

    private LoadDialog mLoadDlg = null;

    private BroadcastReceiver mBroadcastReceiver;

    private RxPermissions rxPermissions;

    private NetworkStatusReceiver mNetReceiver;
    private IndexPresenter mPresenter;
    private FrameLayout mFrameLayout;
    private MyBottomView mBottomView;
    private FragmentManager mManager;
    private final int CONSULT = 1, CERTIFICATE = 2, SERVICE = 3, UPDATE = 4, MY = 5;
    private Fragment mCertiCate, mConsult, mFragment, mService, mUpdate;
    private LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;
    private Dialog mDialog;
    private static Context sContext = null;
    private OnePixelReceiver mOnepxReceiver;
    public static ConsultFragment mInstance;
    private String mSavePhone;
    private String mSaveBindPhone;
    private String mSaveType;
    private Thread mThread;
    private volatile boolean mIsDestroy = false;
    private int mCount;

    public static Context getContext() {
        return sContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_home);
        ButterKnife.bind(this);
        mSaveBindPhone = (String) SPUtils.get(this, Constants.SAVE_PASS_BIND_PHONE, "");
        mSaveType = (String) SPUtils.get(this, Constants.SAVE_PASS_BIND_TYPE, "");
        mSavePhone = (String) SPUtils.get(this, Constants.SAVE_USER_PHONE, "");
        sContext = this;
        mInstance = ConsultFragment.getInstance();
        initView();
      /*  //注册监听屏幕的广播
        mOnepxReceiver = new OnePixelReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        registerReceiver(mOnepxReceiver, intentFilter);*/
        //int delaytime = 10000;
        //PushService.addNotification(delaytime,"tick","title","text");
//        // 设置状态栏透明和文本颜色
//        StatusBarUtil.transparencyBar(this);
//        StatusBarUtil.StatusBarLightMode(this);
        ActivityManager.getInstance().putActivity(this.getLocalClassName(), this);
        rxPermissions = new RxPermissions(this);
        // 注册首页广播
        registerHomeBroadcast();
        if (SPManager.getServerCode().length() != 0) {
            MKeyApi.initSDK(SPManager.getServerSDKUrl(), SPManager.getServerCode());
        }
        //网络监控
        NetworkStatusReceiver.mListeners.add(this);
        // 网络监听通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //实例化IntentFilter对象
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            mNetReceiver = new NetworkStatusReceiver();
            //注册广播接收
            registerReceiver(mNetReceiver, filter);
        }
        if (MyApplication.mNetWorkState != NetUtil.NETWORN_NONE) {
            // 当前服务设置手写签名状态
            AppNetUtil.GetSealTipStatus(new AppNetUtil.SetSealCallBack() {
                @Override
                public void onStatus(boolean bStatus) {
                    YAppManager.getInstance().setSealStatus(bStatus);
                }
            });
            // 获取用户信息
            if (SPManager.getLoginState()) {
                AppNetUtil.userQuery(HomeActivity.this, new AppNetUtil.userQueryCallBack() {
                    @Override
                    public void onSuccess(final UserInfo userInfo) {
                        AppNetUtil.getAppCode(null);
                        // 设置服务器当前使用的证书
                        String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                        MKeyApi.getInstance(HomeActivity.this, null, strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
                            @Override
                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                if (result.getCode().equals("0")) {
                                    CertInfoBean bean = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                                    if (bean != null) {
                                        AppNetUtil.setServerUserCert(bean.getCertSn(), null);
                                    }
                                } else {
                                    AppNetUtil.setServerUserCert("", null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errMsg) {
                        SPManager.setLoginState(false);
                        T.showShort(getApplicationContext(), errMsg);
                    }
                });
            }
            rxPermissions
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean)  {
                            if (aBoolean) {
                                // 检查更新
                                PackageInfo pi = null;
                                try {
                                    pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                                    String local = pi.versionName;
                                    int code = pi.versionCode;
                                    mPresenter.checkUpdate(local);
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
            // 提醒设置指纹和手势，若未设置，则间隔时间进行提醒
            if (SPManager.getLoginState()) {
                // 指纹状态
                Boolean bFingerStatus = SPManager.getFingerLockStatus();
                // 手势状态
                Boolean bGestureStatus = MyApplication.getInstance().getLockPatternUtils().savedPatternExists();
                if (!bFingerStatus && !bGestureStatus) {
                    long dTime = (long) SPUtils.get(HomeActivity.this, Constants.LOCK_TIP_TIMES_SP, (long) 0); // 获取上次弹框时间
                    Long currentTime = new Date().getTime(); // 获取当前时间
                    if (currentTime - dTime >= Constants.YYQ_LOCK_TIP_DAYS * 24 * 60 * 60 * 1000) {
                        SPUtils.put(HomeActivity.this, Constants.LOCK_TIP_TIMES_SP, currentTime);   // 设置当前弹框时间
                        new AlertDialog(HomeActivity.this)
                                .builder()
                                .setTitle(getString(R.string.app_name))
                                .setMessage("为了您的账号安全，建议您设置手势或指纹")
                                .setNegativeButton("暂不设置", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                    }
                                })
                                .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(HomeActivity.this, MineSafeActivity.class);
                                        startActivity(intent);
                                    }
                                }).show();
                    }
                }
            }
        } else {
            T.showLong(this, "网络断开");
        }
    }

    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }

    private void initView() {
        mPresenter = new IndexPresenter(this, this);
        mFrameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        mBottomView = (MyBottomView) findViewById(R.id.bottom_view);
        mBottomView.setBottomBg(Color.WHITE);
        mBottomView.setBottomTextSize(this, 10f);
        mBottomView.setOnBottomClickListener(this);
        mManager = getSupportFragmentManager();
        showFragment(CONSULT);
        mLocationClient = new LocationClient(this);
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION
        )
                != PackageManager.PERMISSION_GRANTED) {//未开启定位权限
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
            }, 200);
        } else {
            InitLocation();
            mLocationClient.start();
        }

    }

    /* public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if (keyCode == KeyEvent.KEYCODE_BACK )  {
             System.exit(0);
             //android.os.Process.killProcess(android.os.Process.myPid());
         }
         return false;
     }*/
    private int currentIndex = 6;

    private void showFragment(int index) {
        if (currentIndex == index) return;
        else currentIndex = index;
        final FragmentTransaction fragmentTransaction = mManager.beginTransaction();
        hideFragment(fragmentTransaction);
        switch (index) {
            case CONSULT:
                if (mConsult != null) {
                    fragmentTransaction.show(mConsult);
                } else {
                    mConsult = ConsultFragment.getInstance();
                    fragmentTransaction.add(R.id.frame_layout, mConsult);
                }
                break;
            case CERTIFICATE:
                if (mCertiCate != null) {
                    fragmentTransaction.show(mCertiCate);
                   /* new AlertDialog(HomeActivity.this)
                            .builder()
                            .setTitle(getString(R.string.app_name))
                            .setMessage("功能正在完善中，敬请期待")
                            .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();*/
                } else {
                    mCertiCate = CertiiCateFragment.getInstance();
                    fragmentTransaction.add(R.id.frame_layout, mCertiCate);
                  /*  new AlertDialog(HomeActivity.this)
                            .builder()
                            .setTitle(getString(R.string.app_name))
                            .setMessage("功能正在完善中，敬请期待")
                            .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();*/
                }
                break;
            case SERVICE:
                if (mService != null) {
                    fragmentTransaction.show(mService);
                } else {
                    mService = ServiceFragment.getInstance();
                    fragmentTransaction.add(R.id.frame_layout, mService);
                }
                break;
            case UPDATE:
                if (mUpdate != null) {
                    fragmentTransaction.show(mUpdate);
                } else {
                    mUpdate = UpdateFragment.getInstance();
                    fragmentTransaction.add(R.id.frame_layout, mUpdate);
                }
                break;
            case MY:
                if (mFragment != null) {
                    fragmentTransaction.show(mFragment);
                } else {
                    mFragment = MyFragment.getInstance();
                    fragmentTransaction.add(R.id.frame_layout, mFragment);
                }
                break;
        }
        fragmentTransaction.commit();
    }

    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if (mConsult != null) fragmentTransaction.hide(mConsult);
        if (mCertiCate != null) fragmentTransaction.hide(mCertiCate);
        if (mService != null) fragmentTransaction.hide(mService);
        if (mFragment != null) fragmentTransaction.hide(mFragment);
        if (mUpdate != null) fragmentTransaction.hide(mUpdate);
    }

    public class MyLocationListener implements BDLocationListener {
        private String mCity = null;

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            if (location.getCity() != null) {
                mCity = location.getCity();
                EventBus.getDefault().post(new ReturnPayResult(location.getCity(), 1));
            }
        }
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getSupportFragmentManager().getFragments() != null && getSupportFragmentManager().getFragments().size() > 0) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment mFragment : fragments) {
                mFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }*/

    private void InitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        //LocationMode.Hight_Accuracy 高精度定位模式下，会同时使用GPS、Wifi和基站定位，返回的是当前条件下精度最好的定位结果
        option.setCoorType("gcj02");//返回的定位结果是百度经纬度，默认值gcj02
        //可选项："gcj02"国策局加密经纬度坐标
        //"bd09ll"百度加密经纬度坐标
        //"bd09"百度加密墨卡托坐标
        option.setIsNeedAddress(true);//反编译获得具体位置，只有网络定位才可以
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 200://刚才的识别码
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//用户同意权限,执行我们的操作
                    InitLocation();
                    mLocationClient.start();
                } else {//用户拒绝之后,当然我们也可以弹出一个窗口,直接跳转到系统设置页面
                    Toast.makeText(HomeActivity.this, "未开启定位权限,请手动到设置去开启权限", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }
  /*  private void userSetSeal() {
        new AlertDialog(HomeActivity.this)
                .builder()
                .setTitle(getString(R.string.app_name))
                .setMessage("请先设置手写签名")
                .setNegativeButton("暂不设置", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                })
                .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, MineSealActivity.class);
                        startActivity(intent);
                    }
                }).show();
    }*/


    // 注册广播
    private void registerHomeBroadcast() {
        mBroadcastReceiver = new HomeBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.HOME_SDK_CODE_BROADCAST);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }


   /* @OnClick({R.id.home_scan_rl, R.id.home_mine_btn, R.id.home_cert_apply_rl, R.id.home_cert_detail_rl, R.id.home_cert_revoke_rl, R.id.home_cert_update_rl, R.id.home_cert_log_rl, R.id.home_cert_set_rl})
    public void onViewClicked(final View view) {

        if (view.getId() == R.id.home_scan_rl) {

            rxPermissions
                    .request(Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean){
                                // 权限开启
                                if (!AppUtil.isLoginWithAuth(HomeActivity.this, true, getString(R.string.app_scan), getString(R.string.auth_no_tip))) {
                                    return;
                                }

                                if (YAppManager.getInstance().isSealStatus()) {
                                    AppNetUtil.getSeal(new AppNetUtil.GetSealCallBack() {
                                        @Override
                                        public void sealValue(String seal) {
                                            if (seal != null && seal.length() != 0) {
                                                qrscanMethod();
                                            } else {
                                                userSetSeal();
                                            }
                                        }
                                    });
                                } else {
                                    qrscanMethod();
                                }
                            }else{
                                //权限未开启
                                Toast.makeText(HomeActivity.this, "请在设置->应用管理中打开拍照或录像、读写手机存储权限", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else if (view.getId() == R.id.home_mine_btn) {
            if (!AppUtil.isLogin(HomeActivity.this)) {
                return;
            }

            Intent intent = new Intent(HomeActivity.this, MineActivity.class);
            startActivity(intent);
        } else {

            rxPermissions
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean){
                                //权限开启
                                if (!AppUtil.isLoginWithAuth(HomeActivity.this, true, "证书管理", getString(R.string.auth_no_tip))) {
                                    return;
                                }

                                if (mLoadDlg == null) {
                                    mLoadDlg = new LoadDialog(HomeActivity.this, R.style.CustomDialog);
                                    mLoadDlg.show();
                                }

                                UserInfo userInfo = SPManager.getUserInfo();
                                String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                                switch (view.getId()) {
                                    case R.id.home_cert_apply_rl: {
                                        MKeyApi.getInstance(HomeActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
                                            @Override
                                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                                if (mLoadDlg != null) {
                                                    mLoadDlg.dismiss();
                                                    mLoadDlg = null;
                                                }
                                                if (result.getCode().equals("0")) {
                                                    showNoCertAlert(getString(R.string.cert_sq), getString(R.string.cert_exist_tip), CertDetailActivity.class);
                                                } else {

                                                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                                        sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        Intent intent = new Intent(HomeActivity.this, CertApplyActivity.class);
                                                        startActivity(intent);
                                                    } else {
                                                        T.showShort(HomeActivity.this, result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    case R.id.home_cert_detail_rl: {
                                        MKeyApi.getInstance(HomeActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
                                            @Override
                                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                                if (mLoadDlg != null) {
                                                    mLoadDlg.dismiss();
                                                    mLoadDlg = null;
                                                }
                                                if (result.getCode().equals("0")) {
                                                    Intent intent = new Intent(HomeActivity.this, CertDetailActivity.class);
                                                    startActivity(intent);
                                                } else {

                                                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                                        sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        showNoCertAlert(getString(R.string.cert_ck), getString(R.string.cert_no_tip), CertApplyActivity.class);
                                                    } else {
                                                        T.showShort(HomeActivity.this, result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    case R.id.home_cert_update_rl: {
                                        MKeyApi.getInstance(HomeActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
                                            @Override
                                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                                if (mLoadDlg != null) {
                                                    mLoadDlg.dismiss();
                                                    mLoadDlg = null;
                                                }
                                                if (result.getCode().equals("0")) {
                                                    CertInfoBean bean = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                                                    Intent intent = new Intent(HomeActivity.this, CertUpdateActivity.class);
                                                    intent.putExtra("cert_end_date", bean.getEndDate());
                                                    startActivity(intent);
                                                } else {

                                                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                                        sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        showNoCertAlert(getString(R.string.cert_gx), getString(R.string.cert_no_tip), CertApplyActivity.class);
                                                    } else {
                                                        T.showShort(HomeActivity.this, result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    case R.id.home_cert_revoke_rl: {
                                        MKeyApi.getInstance(HomeActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
                                            @Override
                                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                                if (mLoadDlg != null) {
                                                    mLoadDlg.dismiss();
                                                    mLoadDlg = null;
                                                }
                                                if (result.getCode().equals("0")) {
                                                    Intent intent = new Intent(HomeActivity.this, CertRevokeActivity.class);
                                                    startActivity(intent);
                                                } else {

                                                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                                        sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        showNoCertAlert(getString(R.string.cert_zx), getString(R.string.cert_no_tip), CertApplyActivity.class);
                                                    } else {
                                                        T.showShort(HomeActivity.this, result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    case R.id.home_cert_set_rl: {
                                        MKeyApi.getInstance(HomeActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
                                            @Override
                                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                                if (mLoadDlg != null) {
                                                    mLoadDlg.dismiss();
                                                    mLoadDlg = null;
                                                }
                                                if (result.getCode().equals("0")) {
                                                    Intent intent = new Intent(HomeActivity.this, CertSetActivity.class);
                                                    startActivity(intent);
                                                } else {

                                                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                                        sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        showNoCertAlert(getString(R.string.cert_sz), getString(R.string.cert_no_tip), CertApplyActivity.class);
                                                    } else {
                                                        T.showShort(HomeActivity.this, result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    case R.id.home_cert_log_rl: {
                                        MKeyApi.getInstance(HomeActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
                                            @Override
                                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                                if (mLoadDlg != null) {
                                                    mLoadDlg.dismiss();
                                                    mLoadDlg = null;
                                                }
                                                if (result.getCode().equals("0")) {
                                                    userCode();
                                                } else {

                                                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                                        sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        showNoCertAlert(getString(R.string.cert_rz), getString(R.string.cert_no_tip), CertApplyActivity.class);
                                                    } else {
                                                        T.showShort(HomeActivity.this, result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    default:
                                        break;
                                }
                            }else{
                                //权限未开启
                                Toast.makeText(HomeActivity.this, "请在设置->应用管理中打开读写手机存储权限", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }*/

  /*  // 二维码扫码
    private void qrscanMethod() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(HomeActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        UserInfo userInfo = SPManager.getUserInfo();
        String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(HomeActivity.this, SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                if (result.getCode().equals("0")) {
                    // 打开二维码扫描
                    Intent intent = new Intent(HomeActivity.this, CaptureActivity.class);
                    startActivityForResult(intent, 0);
                } else {

                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        sendBroadcast(intent);
                    }

                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                        showNoCertAlert(getString(R.string.app_scan), getString(R.string.cert_no_tip), CertApplyActivity.class);
                    } else {
                        T.showShort(HomeActivity.this, result.getMsg());
                    }
                }
            }
        });
    }


    private void showNoCertAlert(String title, String msg, final Class<?> cls) {
        new AlertDialog(this).builder()
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, cls);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.app_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }


    private void userCode() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(HomeActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        try {
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.user_code)
                    .addHeader("token", SPManager.getUserToken())
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(HomeActivity.this, getString(R.string.app_network_error));
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                UserCodeBean bean = (UserCodeBean) JsonUtil.toObject(response, UserCodeBean.class);
                                if (bean != null) {
                                    if (bean.getRet() == 0) {
                                        String url = SPManager.getServerUrl() + Config.log_page + "?code=" + bean.getData().getCode();
                                        Intent intent = new Intent(HomeActivity.this, WebViewActivity.class);
                                        intent.putExtra("title", getString(R.string.cert_rz));
                                        intent.putExtra("url", url);
                                        startActivity(intent);
                                    } else {
                                        T.showShort(HomeActivity.this, bean.getMsg());
                                    }
                                }
                            } catch (Exception e) {
                                T.showShort(HomeActivity.this, e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(HomeActivity.this, e.getLocalizedMessage());
        }
    }*/


    /* // 扫码签名和扫码登录的时候，调用此接口通知后台扫码完成
     private void qrcodeConfirm(QRCodeBean bean) {
         try {
             PostFormBuilder postFormBuilder;
             if (bean.getMode().equals(Constants.SIGN_MODE_REDIRECT)) {
                 postFormBuilder = OkHttpUtils.post()
                         .url(SPManager.getServerUrl() + Config.sign_redirect)
                         .addHeader("token", SPManager.getUserToken())
                         .addParams("appId", bean.getAppId())
                         .addParams("action", Constants.QRCODE_CONFIRM)
                         .addParams("bizSn", bean.getBizSn())
                         .addParams("url", URLEncoder.encode(bean.getUrl(), Config.UTF_8));
             } else {
                 postFormBuilder = OkHttpUtils.post()
                         .url(URLDecoder.decode(bean.getUrl(), Config.UTF_8))
                         .addParams("action", Constants.QRCODE_CONFIRM)
                         .addParams("bizSn", bean.getBizSn())
                         .addParams("id", SPManager.getUserInfo().uuid);
             }

             postFormBuilder.build().execute(new StringCallback() {
                 @Override
                 public void onError(Call call, Exception e, int id) {
                     L.e(e.getLocalizedMessage());
                 }

                 @Override
                 public void onResponse(String response, int id) {
                     L.d(response);
                 }
             });
         } catch (Exception e) {
             L.e(e.getLocalizedMessage());
         }
     }


     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == Activity.RESULT_OK) {
             Bundle bundle = data.getExtras();
             String result = bundle.getString("result");
             if (result == null || "".equals(result)) {
                 T.showShort(HomeActivity.this, "获取数据失败");
                 return;
             }

             try {
                 result = URLDecoder.decode(result, Config.UTF_8);
                 QRCodeBean QRCodeBean = (QRCodeBean) JsonUtil.toObject(result, QRCodeBean.class);
                 if (QRCodeBean != null) {
                     qrcodeConfirm(QRCodeBean);
                     if (Constants.QRCODE_SIGN.equals(QRCodeBean.getAction())) {
                         Intent intent = new Intent(this, CertSignActivity.class);
                         Bundle signBundle = new Bundle();
                         signBundle.putString("data", result);
                         intent.putExtras(signBundle);
                         startActivity(intent);
                     } else if (Constants.QRCODE_LOGIN.equals(QRCodeBean.getAction())) {
                         Intent intent = new Intent(this, CertLoginActivity.class);
                         Bundle signBundle = new Bundle();
                         signBundle.putString("data", result);
                         intent.putExtras(signBundle);
                         startActivity(intent);
                     } else {
                         T.showShort(HomeActivity.this, "敬请期待");
                     }
                 } else {
                     T.showShort(HomeActivity.this, "数据格式错误");
                 }
             } catch (Exception e) {
                 T.showShort(HomeActivity.this, e.getLocalizedMessage());
             }
         }
     }
 */
    @Override
    public void onNetworkChange() {
        if (MyApplication.mNetWorkState == NetUtil.NETWORN_NONE) {
            T.showShort(this, "网络断开");
        } else {
            AppNetUtil.getAppCode(null);

            // 当前服务设置手写签名状态
            AppNetUtil.GetSealTipStatus(new AppNetUtil.SetSealCallBack() {
                @Override
                public void onStatus(boolean bStatus) {
                    YAppManager.getInstance().setSealStatus(bStatus);
                }
            });
            // 获取用户信息
            if (SPManager.getLoginState() && SPManager.getUserInfo() == null) {
                AppNetUtil.userQuery(HomeActivity.this, new AppNetUtil.userQueryCallBack() {
                    @Override
                    public void onSuccess(final UserInfo userInfo) {
                        // 设置服务器当前使用的证书
                        String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                        MKeyApi.getInstance(HomeActivity.this, null, strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
                            @Override
                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                if (result.getCode().equals("0")) {
                                    CertInfoBean bean = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                                    if (bean != null) {
                                        AppNetUtil.setServerUserCert(bean.getCertSn(), null);
                                    }
                                } else {
                                    AppNetUtil.setServerUserCert("", null);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errMsg) {
                        SPManager.setLoginState(false);
                        T.showShort(getApplicationContext(), errMsg);
                    }
                });
            }
        }
    }
  /*  @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, R.string.app_exit_tip, Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                this.startActivity(intent);
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mNetReceiver);
        mIsDestroy = true;
        ActivityManager.getInstance().closeActivity(this.getLocalClassName());
    }

    @Override
    public void onFirstClick() {
        showFragment(CONSULT);
    }

    @Override
    public void onSecondClick() {
        showFragment(CERTIFICATE);
    }

    @Override
    public void onThirdClick() {
        showFragment(SERVICE);
    }


    @Override
    public void onFourthClick() {
        showFragment(UPDATE);
    }

    @Override
    public void onFifthClick() {
        showFragment(MY);
    }

    @Override
    public void showUpdate(final String version) {
        if (mDialog == null)
            mDialog = new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("检测到有新版本")
                    .setMessage("当前版本:" + version)
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPresenter.downApk(HomeActivity.this);
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
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
    public void onBackPressed() {
        super.onBackPressed();
        if (mService != null) {
            mService = null;
            mService.onDestroy();
        }
    }
}