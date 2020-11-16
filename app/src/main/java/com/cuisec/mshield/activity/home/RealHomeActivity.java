package com.cuisec.mshield.activity.home;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.cuisec.mshield.MainActivityPermissionsDispatcher;
import com.cuisec.mshield.MyApplication;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.mine.MineSafeActivity;
import com.cuisec.mshield.bean.CertInfoBean;
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
import com.cuisec.mshield.receiver.HomeBroadcastReceiver;
import com.cuisec.mshield.receiver.NetworkStatusReceiver;
import com.cuisec.mshield.update.UpdateManager;
import com.cuisec.mshield.updateapk.IndexContract;
import com.cuisec.mshield.updateapk.IndexPresenter;
import com.cuisec.mshield.utils.ActivityManager;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.NetUtil;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.AlertDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Date;

import io.reactivex.functions.Consumer;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.PermissionRequest;

import static android.os.Process.killProcess;

public class RealHomeActivity extends AppCompatActivity implements MyBottomView.OnBottomClick, NetworkStatusReceiver.NetworkEventHandler, IndexContract.View {

    private FrameLayout mFrameLayout;
    private MyBottomView mBottomView;
    private FragmentManager mManager;
    private final int CONSULT = 1, CERTIFICATE = 2, SERVICE = 3,MY = 4;
    private Fragment mCertiCate,mConsult,mFragment,mService;
    private BroadcastReceiver mBroadcastReceiver;
    private RxPermissions rxPermissions;
    private NetworkStatusReceiver mNetReceiver;
    private Dialog mDialog;
    private IndexPresenter mPresenter;
    String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final int RC_LOCATION_CONTACTS_PERM = 124;
    private LocationClient mLocationClient;
    private MyLocationListener mMyLocationListener;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_home);
        mPresenter = new IndexPresenter(this,this);
        rxPermissions = new RxPermissions(this);
       // initSetting();
        //initView();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initSetting() {
        ActivityManager.getInstance().putActivity(this.getLocalClassName(), this);
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
                AppNetUtil.userQuery(RealHomeActivity.this, new AppNetUtil.userQueryCallBack() {
                    @Override
                    public void onSuccess(final UserInfo userInfo) {
                        AppNetUtil.getAppCode(null);

                        // 设置服务器当前使用的证书
                        String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                        MKeyApi.getInstance(RealHomeActivity.this, null, strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
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
            /*rxPermissions
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                            //Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean){
                                PackageInfo pi = null;
                                try {
                                    pi = getPackageManager().getPackageInfo(getPackageName(),0);
                                    String local = pi.versionName;
                                    int code = pi.versionCode;
                                    mPresenter.checkUpdate(local);
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                               *//* InitLocation();
                                mLocationClient.start();*//*
                            }
                        }
                    });
*/
            // 提醒设置指纹和手势，若未设置，则间隔时间进行提醒
            if (SPManager.getLoginState()) {
                // 指纹状态
                Boolean bFingerStatus = SPManager.getFingerLockStatus();
                // 手势状态
                Boolean bGestureStatus = MyApplication.getInstance().getLockPatternUtils().savedPatternExists();
                if (!bFingerStatus && !bGestureStatus) {
                    long dTime = (long) SPUtils.get(RealHomeActivity.this, Constants.LOCK_TIP_TIMES_SP, (long)0); // 获取上次弹框时间
                    Long currentTime = new Date().getTime(); // 获取当前时间
                    if (currentTime - dTime >= Constants.YYQ_LOCK_TIP_DAYS * 24 * 60 * 60 * 1000) {
                        SPUtils.put(RealHomeActivity.this, Constants.LOCK_TIP_TIMES_SP, currentTime);   // 设置当前弹框时间
                        new AlertDialog(RealHomeActivity.this)
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
                                        Intent intent = new Intent(RealHomeActivity.this, MineSafeActivity.class);
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
    // 注册广播
    private void registerHomeBroadcast() {
        mBroadcastReceiver = new HomeBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.HOME_SDK_CODE_BROADCAST);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }
    private void initView() {
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
        if(ContextCompat.checkSelfPermission(RealHomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){//未开启定位权限
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions(RealHomeActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }else{
            InitLocation();
            mLocationClient.start();
        }
    }
    private int currentIndex = 5;
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
                    new AlertDialog(RealHomeActivity.this)
                            .builder()
                            .setTitle(getString(R.string.app_name))
                            .setMessage("功能正在完善中，敬请期待")
                            .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                } else {
                    mCertiCate = CertiiCateFragment.getInstance();
                    fragmentTransaction.add(R.id.frame_layout, mCertiCate);
                    new AlertDialog(RealHomeActivity.this)
                            .builder()
                            .setTitle(getString(R.string.app_name))
                            .setMessage("功能正在完善中，敬请期待")
                            .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                }
                break;
            case SERVICE:
                /*if (mService != null) fragmentTransaction.show(mService);
                else {
                    mService = ServiceFragment.getInstance();
                    fragmentTransaction.add(R.id.frame_layout, mService);
                }*/
                break;
            case MY:

                if (mFragment != null){
                    fragmentTransaction.show(mFragment);
                }else {
                    mFragment = MyFragment.getInstance();
                    fragmentTransaction.add(R.id.frame_layout,mFragment);
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
        showFragment(MY);
    }

    @Override
    public void onFifthClick() {

    }
    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        if (mPresenter != null){
            mPresenter.unbind(this);
            mPresenter = null;
        }

        ActivityManager.getInstance().closeActivity(this.getLocalClassName());
        if (mPresenter != null){
            mPresenter = null;
        }
    }

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
                AppNetUtil.userQuery(RealHomeActivity.this, new AppNetUtil.userQueryCallBack() {
                    @Override
                    public void onSuccess(final UserInfo userInfo) {
                        // 设置服务器当前使用的证书
                        String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                        MKeyApi.getInstance(RealHomeActivity.this, null, strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
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

    @Override
    public void showUpdate(final String version) {
        if (mDialog == null)
          mDialog = new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("检测到有新版本")
                    .setMessage("当前版本:"+version)
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPresenter.downApk(RealHomeActivity.this);
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
   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, "这位爷，在下载呢，待会再退出吧", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    /*public void needStorage() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(),0);
            String local = pi.versionName;
            int code = pi.versionCode;
            mPresenter.checkUpdate(local);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }*/

    public void deniedStorage() {

    }


    public void showRational(final PermissionRequest request) {

        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage("赋予此应用读写文件权限用于版本更新，是否同意?")
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();
    }
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
    public class MyLocationListener implements BDLocationListener {
        private String mCity = null;
        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            if (location.getCity() != null) {
                mCity = location.getCity();
                Log.i("sss", "onReceiveLocation: "+location.getCity());
            }
        }
    }
       @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200://刚才的识别码
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意权限,执行我们的操作
                    InitLocation();
                    mLocationClient.start();
                }else{//用户拒绝之后,当然我们也可以弹出一个窗口,直接跳转到系统设置页面
                    Toast.makeText(RealHomeActivity.this,"未开启定位权限,请手动到设置去开启权限",Toast.LENGTH_LONG).show();
                }
                break;
            default:break;
        }
    }


}
