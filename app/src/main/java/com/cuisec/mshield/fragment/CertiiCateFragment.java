package com.cuisec.mshield.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.icu.text.UnicodeSetSpanner;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cuisec.mshield.R;
import com.cuisec.mshield.TokenAuthenticator;
import com.cuisec.mshield.activity.common.WebViewActivity;
import com.cuisec.mshield.activity.home.CertApplyActivity;
import com.cuisec.mshield.activity.home.CertDetailActivity;
import com.cuisec.mshield.activity.home.CertRevokeActivity;
import com.cuisec.mshield.activity.home.CertSetActivity;
import com.cuisec.mshield.activity.home.CertUpdateActivity;
import com.cuisec.mshield.activity.login.LoginActivity;
import com.cuisec.mshield.activity.mine.MineActivity;
import com.cuisec.mshield.activity.mine.seal.MineSealActivity;
import com.cuisec.mshield.bean.CertInfoBean;
import com.cuisec.mshield.bean.CodeLoginBean;
import com.cuisec.mshield.bean.MadeQrBean;
import com.cuisec.mshield.bean.QRCodeBean;
import com.cuisec.mshield.bean.QRMsgBean;
import com.cuisec.mshield.bean.QrMsgInfo;
import com.cuisec.mshield.bean.QrResultInfo;
import com.cuisec.mshield.bean.QrScanBean;
import com.cuisec.mshield.bean.UserCodeBean;
import com.cuisec.mshield.bean.UserLoginBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.data.YAppManager;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.utils.AppNetUtil;
import com.cuisec.mshield.utils.AppUtil;
import com.cuisec.mshield.utils.Base64Utils;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.AlertDialog;
import com.cuisec.mshield.widget.LoadDialog;
import com.custle.ksmkey.MKeyApi;
import com.custle.ksmkey.MKeyApiCallback;
import com.custle.ksmkey.MKeyApiResult;
import com.custle.ksmkey.MKeyMacro;
import com.custle.qrcode.zxing.activity.CaptureActivity;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class CertiiCateFragment extends Fragment {
    private static CertiiCateFragment mCertiiCateFragment;
    @BindView(R.id.home_scan_rl)
    LinearLayout mHomeScanRl;
    @BindView(R.id.home_mine_btn)
    Button mHomeMineBtn;
    @BindView(R.id.home_cert_apply_rl)
    RelativeLayout mHomeCertApplyRl;
    @BindView(R.id.home_cert_detail_rl)
    RelativeLayout mHomeCertDetailRl;
    @BindView(R.id.home_cert_revoke_rl)
    RelativeLayout mHomeCertRevokeRl;
    @BindView(R.id.home_cert_update_rl)
    RelativeLayout mHomeCertUpdateRl;
    @BindView(R.id.home_cert_log_rl)
    RelativeLayout mHomeCertLogRl;
    @BindView(R.id.home_cert_set_rl)
    RelativeLayout mHomeCertSetRl;
    @BindView(R.id.cert_bg)
    TextView mCertBg;
    private LoadDialog mLoadDlg = null;
    private RxPermissions rxPermissions;
    private String mCert;
    private String mCertSn;
    private View view;
    private String mToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.activity_home, null);
        ButterKnife.bind(this, inflate);
        initView(inflate);
        rxPermissions = new RxPermissions(this);
        return inflate;
    }

    private void initQrscan(final String reuslt) {
        if (reuslt == null) {
            return;
        }
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(getActivity(), R.style.CustomDialog);
            mLoadDlg.show();
        }
        //获取证书和证书序列号
        //getCertInfo(SPManager.getSDKAuthCode(), uuid, singData);
        /*if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(getActivity(), R.style.CustomDialog);
            mLoadDlg.show();
        }*/
        QrScanBean qrScanBean = new QrScanBean();
        if (mCert != null) {
            qrScanBean.setCert(mCert);
        }
        if (mCertSn != null) {
            qrScanBean.setCertSn(mCertSn);
        }
        qrScanBean.setUuid(reuslt);
        final Gson gson = new Gson();
        String json = gson.toJson(qrScanBean);
        OkHttpUtils.postString()
                .url("http://192.168.13.88:9097/bidding/checkQr")
                .content(json)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                        CodeLoginBean codeLoginBean = gson.fromJson(response, CodeLoginBean.class);
                        if (codeLoginBean.getMsg().equals("操作成功！")) {
                            confirmLoginCode(reuslt);
                        } else {
                            T.showShort("登录失败");
                        }
                    }
                });
    }

    private void getCertInfo(String sdkAuthCode) {
        UserInfo userInfo = SPManager.getUserInfo();
        String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(getActivity(), sdkAuthCode, strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult mKeyApiResult) {
                if (mKeyApiResult.getCode().equals("0")) {
                    CertInfoBean certInfo = (CertInfoBean) JsonUtil.toObject(mKeyApiResult.getData(), CertInfoBean.class);
                    if (certInfo != null) {
                        mCert = certInfo.getCert();
                        mCertSn = certInfo.getCertSn();
                    }
                }
            }
        });
    }

    public void getNewToken() {
        try {
            String refreshToken = SPManager.getRefreshToken();
            String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, refreshToken);
            OkHttpUtils.post()
                    .url(SPManager.getServerUrl() + Config.user_refresh)
                    .addParams("refreshToken", refreshToken)
                    .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
                    .build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {

                }

                @Override
                public void onResponse(String response, int id) {
                    String result = null;
                    try {
                        result = URLDecoder.decode(response, Config.UTF_8);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    final UserLoginBean bean = (UserLoginBean) JsonUtil.toObject(result, UserLoginBean.class);
                    if (bean != null) {
                        if (bean.getRet() == 0 && bean.getData() != null) {
                            SPManager.setUserToken(bean.getData().getToken());
                            SPManager.setRefreshToken(bean.getData().getRefreshToken());
                            mToken = bean.getData().getToken();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

               /* try {
                    String newToken = null;
                    String refreshToken = SPManager.getRefreshToken();
                    String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, refreshToken);
                    // 同步调用token刷新接口
                    Response res = OkHttpUtils
                            .post()
                            .url(SPManager.getServerUrl() + Config.user_refresh)
                            .addParams("refreshToken", refreshToken)
                            .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
                            .build()
                            .execute();
                    String result = res.body().string();
                    result = URLDecoder.decode(result, Config.UTF_8);
                    final UserLoginBean bean = (UserLoginBean) JsonUtil.toObject(result, UserLoginBean.class);
                    if (bean != null) {
                        if (bean.getRet() == 0 && bean.getData() != null) {
                            SPManager.setUserToken(bean.getData().getToken());
                            SPManager.setRefreshToken(bean.getData().getRefreshToken());

                                    mToken = bean.getData().getToken();
                                    L.i(mToken);


                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }*/

    }

    private void codeSingTrue(String uuid, String singData, String certSn, String cert) {
        QrScanBean qrScanBean = new QrScanBean();
        //签名值
        qrScanBean.setSigndata(singData);
        //签名原文
        qrScanBean.setSignsrc(uuid);
        L.i(certSn);
        qrScanBean.setUserId(certSn);
        final Gson gson = new Gson();
        String json = gson.toJson(qrScanBean);
        OkHttpUtils.postString()
                .url("http://192.168.13.88:9097/bidding/checkSign")
                .content(json)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                        CodeLoginBean codeLoginBean = gson.fromJson(response, CodeLoginBean.class);
                        if (codeLoginBean.getMsg().equals("操作成功！")) {

                        } else {
                            T.showShort("登录失败");
                        }
                        L.i(response);
                    }
                });
    }

    private void initView(View inflate) {
        //mCertBg.setText("CA证书"+"\n"+"开启网络真时代");
    }

    public static CertiiCateFragment getInstance() {
        if (mCertiiCateFragment == null) {
            mCertiiCateFragment = new CertiiCateFragment();
        }
        return mCertiiCateFragment;
    }

    private void showNoCertAlert(String title, String msg, final Class<?> cls) {
        new AlertDialog(getActivity()).builder()
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), cls);
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
            mLoadDlg = new LoadDialog(getActivity(), R.style.CustomDialog);
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
                            T.showShort(getActivity(), getString(R.string.app_network_error));
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
                                        Intent intent = new Intent(getActivity(), WebViewActivity.class);
                                        intent.putExtra("title", getString(R.string.cert_rz));
                                        intent.putExtra("url", url);
                                        startActivity(intent);
                                    } else {
                                        T.showShort(getActivity(), bean.getMsg());
                                    }
                                }
                            } catch (Exception e) {
                                T.showShort(getActivity(), e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(getActivity(), e.getLocalizedMessage());
        }
    }

    // 二维码扫码
    private void qrscanMethod() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(getActivity(), R.style.CustomDialog);
            mLoadDlg.show();
        }
        UserInfo userInfo = SPManager.getUserInfo();
        String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
        MKeyApi.getInstance(getActivity(), SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
            @Override
            public void onMKeyApiCallBack(MKeyApiResult result) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                if (result.getCode().equals("0")) {
                    // 打开二维码扫描
                    getNewToken();
                    Intent intent = new Intent(getActivity(), CaptureActivity.class);
                    startActivityForResult(intent, 0);
                } else {

                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                        getActivity().sendBroadcast(intent);
                    }

                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                        showNoCertAlert(getString(R.string.app_scan), getString(R.string.cert_no_tip), CertApplyActivity.class);
                    } else {
                        T.showShort(getActivity(), result.getMsg());
                    }
                }
            }
        });
    }

    // 扫码签名和扫码登录的时候，调用此接口通知后台扫码完成
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
                T.showShort(getActivity(), "获取数据失败");
                return;
            }
            try {
                String substring = result.substring(result.lastIndexOf("/") + 1, result.length());
                executeResult(substring);
                /*QrResultInfo qrResultInfo = (QrResultInfo) JsonUtil.toObject(result, QrResultInfo.class);
                if (qrResultInfo != null){
                    getCertInfo(SPManager.getSDKAuthCode());
                    if ("".equals(qrResultInfo.getSaveid()) || "".equals(qrResultInfo.getOrgdata())){
                        qrLogin(qrResultInfo);
                    }else{
                        qrSingTure(qrResultInfo);
                    }
                }else{
                    T.showShort("格式错误");
                }*/

                /*result = URLDecoder.decode(result, Config.UTF_8);
                String s = Base64Utils.decodeToString(result);
                L.i(s);
                QRCodeBean QRCodeBean = (QRCodeBean) JsonUtil.toObject(result, QRCodeBean.class);
                if (QRCodeBean != null) {
                    qrcodeConfirm(QRCodeBean);
                    if (Constants.QRCODE_SIGN.equals(QRCodeBean.getAction())) {
                        Intent intent = new Intent(getActivity(), CertSignActivity.class);
                        Bundle signBundle = new Bundle();
                        signBundle.putString("data", result);
                        intent.putExtras(signBundle);
                        startActivity(intent);
                    } else if (Constants.QRCODE_LOGIN.equals(QRCodeBean.getAction())) {
                        Intent intent = new Intent(getActivity(), CertLoginActivity.class);
                        Bundle signBundle = new Bundle();
                        signBundle.putString("data", result);
                        intent.putExtras(signBundle);
                        startActivity(intent);
                    } else {
                        T.showShort(getActivity(), "敬请期待");
                    }
                } else {
                    T.showShort(getActivity(), "数据格式错误");
                }*/
            } catch (Exception e) {
                T.showShort("数据格式错误");
            }
        }
    }

    private void executeResult(String substring) {
        if (mToken != null) {
            if (mLoadDlg == null) {
                mLoadDlg = new LoadDialog(getActivity(), R.style.CustomDialog);
                mLoadDlg.show();
            }
            String userToken = SPManager.getUserToken();
            OkHttpUtils.post()
                    .url(SPManager.getServerUrl() + Config.QR_CODE_LOGIN)
                    .addHeader("token", mToken)
                    .addParams("random", substring)
                    .build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }
                }

                @Override
                public void onResponse(String response, int id) {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }
                    try {
                        QrMsgInfo qrMsgInfo = (QrMsgInfo) JsonUtil.toObject(response, QrMsgInfo.class);
                        if (qrMsgInfo != null) {
                            //获取证书
                            getCertInfo(SPManager.getSDKAuthCode());
                            if (qrMsgInfo.getRet() == 0) {
                                if (qrMsgInfo.getData().getAction().equals(Constants.QRCODE_LOGIN)) {
                                    //扫码登录
                                    codeLogin(qrMsgInfo);
                                } else if (qrMsgInfo.getData().getAction().equals(Constants.QRCODE_SIGN)) {
                                    //扫码标书制作
                                    codeSing(qrMsgInfo);
                                } else {
                                    T.showShort("敬请期待");
                                }
                            } else {
                                String decode = URLDecoder.decode(qrMsgInfo.getMsg(), Config.UTF_8);
                                T.showShort(decode);
                                if (mLoadDlg != null) {
                                    mLoadDlg.dismiss();
                                    mLoadDlg = null;
                                }
                            }
                        } else {
                            T.showShort("格式错误");
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                        }
                    } catch (Exception e) {
                        L.i(e.getMessage());
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                    }
                }
            });
        } else startActivity(new Intent(getActivity(), LoginActivity.class));
    }

    private void codeSing(final QrMsgInfo qrMsgInfo) {
        if (mLoadDlg != null) {
            mLoadDlg.dismiss();
            mLoadDlg = null;
        }
        final QRMsgBean qrMsgBean = new QRMsgBean();

        String msg = qrMsgInfo.getData().getMsg();
        String decode = null;
        try {
            decode = URLDecoder.decode(msg, Config.UTF_8);
            UserInfo userInfo = SPManager.getUserInfo();
            final String sdkAuthCode = SPManager.getSDKAuthCode();
            String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
            String finalDecode = decode;
            final String[] split = finalDecode.split(":");
            // String inData = new String(Base64.decode(split[1],Base64.DEFAULT));
            MKeyApi.getInstance(getActivity(), sdkAuthCode, strUserInfo, SPManager.getUserInfo().algVersion).signature(split[1], new MKeyApiCallback() {
                @Override
                public void onMKeyApiCallBack(MKeyApiResult mKeyApiResult) {
                    if (mKeyApiResult.getCode().equals("0")) {
                        //获取签名原文‘
                        qrMsgBean.setSaveid(split[0]);
                        qrMsgBean.setAction(qrMsgInfo.getData().getAction());
                        qrMsgBean.setBizSn(qrMsgInfo.getData().getBizSn());
                        qrMsgBean.setCert(mCert);
                        qrMsgBean.setSignAlg("SM3withSM2");
                        qrMsgBean.setSignValue(mKeyApiResult.getData());
                        qrMsgBean.setId(mCertSn);
                        netWorkUrl(qrMsgInfo.getData().getUrl(),qrMsgBean);
                    }else {
                        T.showShort("操作失败");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

}

    private void codeLogin(final QrMsgInfo qrMsgInfo) {
        if (mLoadDlg != null) {
            mLoadDlg.dismiss();
            mLoadDlg = null;
        }
        final QRMsgBean qrMsgBean = new QRMsgBean();
                UserInfo userInfo = SPManager.getUserInfo();
                final String sdkAuthCode = SPManager.getSDKAuthCode();
                String[] split = Base64Utils.decodeToString(qrMsgInfo.getData().getMsg()).split("::");
                String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                MKeyApi.getInstance(getActivity(), sdkAuthCode, strUserInfo, SPManager.getUserInfo().algVersion).signature(split[1], new MKeyApiCallback() {
                    @Override
                    public void onMKeyApiCallBack(MKeyApiResult mKeyApiResult) {
                        if (mKeyApiResult.getCode().equals("0")) {
                            //获取签名原文
                            qrMsgBean.setAction(qrMsgInfo.getData().getAction());
                            qrMsgBean.setBizSn(qrMsgInfo.getData().getBizSn());
                            qrMsgBean.setCert(mCert);
                            qrMsgBean.setSignAlg("SM3withSM2");
                            qrMsgBean.setSignValue(mKeyApiResult.getData());
                            qrMsgBean.setId(mCertSn);
                            netWorkUrl(qrMsgInfo.getData().getUrl(),qrMsgBean);
                        }else {
                            T.showShort("操作失败");
                        }
                    }
                });

    }

    private void netWorkUrl(String url,QRMsgBean qrMsgBean) {
        String json = JsonUtil.toJson(qrMsgBean);
        OkHttpUtils.postString()
                .url(url)
                .content(json)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                        T.showShort(e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                        L.i(response);
                        CodeLoginBean codeLoginBean = (CodeLoginBean) JsonUtil.toObject(response, CodeLoginBean.class);
                        if (codeLoginBean != null) {
                            if (codeLoginBean.getMsg().equals("操作成功！")) {
                                T.showShort("操作成功");
                            } else {
                                T.showShort("操作失败");
                            }
                        }
                        L.i(response);
                    }
                });


    }

    private void qrLogin(QrResultInfo qrResultInfo) {
        QrScanBean qrScanBean = new QrScanBean();
        qrScanBean.setUuid(qrResultInfo.getUuid());
        qrScanBean.setCertSn(mCertSn);
        qrScanBean.setCert(mCert);
        final Gson gson = new Gson();
        String json = gson.toJson(qrScanBean);
        OkHttpUtils.postString()
                .url("http://192.168.13.88:9097/bidding/checkQr")
                .content(json)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                        CodeLoginBean codeLoginBean = gson.fromJson(response, CodeLoginBean.class);
                        if (codeLoginBean != null && codeLoginBean.getMsg().equals("操作成功！")) {
                            T.showShort("登录成功");
                        } else {
                            T.showShort("登录失败");
                        }
                    }
                });
    }

    private void qrSing(QrResultInfo qrResultInfo, String singData) {
        MadeQrBean madeQrBean = new MadeQrBean();
        madeQrBean.setSaveid(qrResultInfo.getSaveid());
        madeQrBean.setSigndata(singData);
        madeQrBean.setUuid(qrResultInfo.getUuid());
        madeQrBean.setCertSn(mCertSn);
        final Gson gson = new Gson();
        String json = gson.toJson(madeQrBean);
        OkHttpUtils.postString()
                .url("http://192.168.13.88:9097/bidding/checkBiddingMadeQr")
                .content(json)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                        L.i(response);
                    }
                });

    }

    private void confirmLoginCode(final String uuid) {
        new AlertDialog(getActivity())
                .builder()
                .setTitle(getString(R.string.app_name))
                .setMessage("扫码登录")
                .setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                })
                .setPositiveButton(getString(R.string.app_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //codeSingTrue(uuid);
                    }
                }).show();
    }

    private void qrSingTure(final QrResultInfo orgdata) {

        AppUtil.signVerifyPin(getActivity(), new AppUtil.VeifyCallBack() {
            @Override
            public void onSuccess(String pin) {
                UserInfo userInfo = SPManager.getUserInfo();
                final String sdkAuthCode = SPManager.getSDKAuthCode();
                String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                MKeyApi.getInstance(getActivity(), sdkAuthCode, strUserInfo, SPManager.getUserInfo().algVersion).signature(orgdata.getOrgdata(), new MKeyApiCallback() {
                    @Override
                    public void onMKeyApiCallBack(MKeyApiResult mKeyApiResult) {
                        if (mKeyApiResult.getCode().equals("0")) {
                            //获取签名原文
                            String singData = mKeyApiResult.getData();
                            qrSing(orgdata, singData);
                            //获取证书
                            //getCertInfo(sdkAuthCode,uuid,singData);
                        }
                    }
                });
            }

            @Override
            public void onFailure(String errMsg) {
                T.showShort(errMsg);
            }
        });

    }

    private void userSetSeal() {
        new AlertDialog(getActivity())
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
                        Intent intent = new Intent(getActivity(), MineSealActivity.class);
                        startActivity(intent);
                    }
                }).show();
    }

    @OnClick({R.id.home_scan_rl, R.id.home_mine_btn, R.id.home_cert_apply_rl, R.id.home_cert_detail_rl, R.id.home_cert_revoke_rl, R.id.home_cert_update_rl, R.id.home_cert_log_rl, R.id.home_cert_set_rl})
    public void onViewClicked(final View view) {
        if (view.getId() == R.id.home_scan_rl) {
            //T.showShort("功能正在完善中，敬请期待");
            rxPermissions
                    .request(Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                // 权限开启
                                if (!AppUtil.isLoginWithAuth(getActivity(), true, getString(R.string.app_scan), getString(R.string.auth_no_tip))) {
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
                            } else {
                                //权限未开启
                                Toast.makeText(getActivity(), "请在设置->应用管理中打开拍照或录像、读写手机存储权限", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else if (view.getId() == R.id.home_mine_btn) {
            //T.showShort("功能正在完善中，敬请期待");
            /*if (!AppUtil.isLogin(getActivity())) {
                return;
            }

            Intent intent = new Intent(getActivity(), MineActivity.class);
            startActivity(intent);*/
        } else {
            //T.showShort("功能正在完善中，敬请期待");
            rxPermissions
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean){
                                //权限开启
                                if (!AppUtil.isLoginWithAuth(getActivity(), true, "证书管理", getString(R.string.auth_no_tip))) {
                                    return;
                                }

                                if (mLoadDlg == null) {
                                    mLoadDlg = new LoadDialog(getActivity(), R.style.CustomDialog);
                                    mLoadDlg.show();
                                }

                                UserInfo userInfo = SPManager.getUserInfo();
                                String strUserInfo = "{\"name\":\"" + userInfo.userName + "\",\"idNo\":\"" + userInfo.idNo + "\",\"mobile\":\"" + userInfo.phone + "\"}";
                                switch (view.getId()) {
                                    case R.id.home_cert_apply_rl: {
                                        MKeyApi.getInstance(getActivity(), SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
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
                                                        getActivity().sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        Intent intent = new Intent(getActivity(), CertApplyActivity.class);
                                                        startActivity(intent);
                                                    } else {
                                                        T.showShort(getActivity(), result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    case R.id.home_cert_detail_rl: {
                                        //T.showShort("功能正在完善中，敬请期待");
                                        if (mLoadDlg != null) {
                                            mLoadDlg.dismiss();
                                            mLoadDlg = null;
                                        }
                                        MKeyApi.getInstance(getActivity(), SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
                                            @Override
                                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                                if (mLoadDlg != null) {
                                                    mLoadDlg.dismiss();
                                                    mLoadDlg = null;
                                                }
                                                if (result.getCode().equals("0")) {
                                                    Intent intent = new Intent(getActivity(), CertDetailActivity.class);
                                                    startActivity(intent);
                                                } else {

                                                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                                        getActivity().sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        showNoCertAlert(getString(R.string.cert_ck), getString(R.string.cert_no_tip), CertApplyActivity.class);
                                                    } else {
                                                        T.showShort(getActivity(), result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    /*case R.id.home_cert_update_rl: {
                                        //T.showShort("功能正在完善中，敬请期待");
                                        if (mLoadDlg != null) {
                                            mLoadDlg.dismiss();
                                            mLoadDlg = null;
                                        }
                                        MKeyApi.getInstance(getActivity(), SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCertInfo(new MKeyApiCallback() {
                                            @Override
                                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                                if (mLoadDlg != null) {
                                                    mLoadDlg.dismiss();
                                                    mLoadDlg = null;
                                                }
                                                if (result.getCode().equals("0")) {
                                                    CertInfoBean bean = (CertInfoBean) JsonUtil.toObject(result.getData(), CertInfoBean.class);
                                                    Intent intent = new Intent(getActivity(), CertUpdateActivity.class);
                                                    intent.putExtra("cert_end_date", bean.getEndDate());
                                                    startActivity(intent);
                                                } else {

                                                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                                        getActivity().sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        showNoCertAlert(getString(R.string.cert_gx), getString(R.string.cert_no_tip), CertApplyActivity.class);
                                                    } else {
                                                        T.showShort(getActivity(), result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    case R.id.home_cert_revoke_rl: {
                                       // T.showShort("功能正在完善中，敬请期待");
                                        if (mLoadDlg != null) {
                                            mLoadDlg.dismiss();
                                            mLoadDlg = null;
                                        }
                                        MKeyApi.getInstance(getActivity(), SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
                                            @Override
                                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                                if (mLoadDlg != null) {
                                                    mLoadDlg.dismiss();
                                                    mLoadDlg = null;
                                                }
                                                if (result.getCode().equals("0")) {
                                                    Intent intent = new Intent(getActivity(), CertRevokeActivity.class);
                                                    startActivity(intent);
                                                } else {

                                                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                                        getActivity().sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        showNoCertAlert(getString(R.string.cert_zx), getString(R.string.cert_no_tip), CertApplyActivity.class);
                                                    } else {
                                                        T.showShort(getActivity(), result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    case R.id.home_cert_set_rl: {
                                        //T.showShort("功能正在完善中，敬请期待");
                                        if (mLoadDlg != null) {
                                            mLoadDlg.dismiss();
                                            mLoadDlg = null;
                                        }
                                        MKeyApi.getInstance(getActivity(), SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
                                            @Override
                                            public void onMKeyApiCallBack(MKeyApiResult result) {
                                                if (mLoadDlg != null) {
                                                    mLoadDlg.dismiss();
                                                    mLoadDlg = null;
                                                }
                                                if (result.getCode().equals("0")) {
                                                    Intent intent = new Intent(getActivity(), CertSetActivity.class);
                                                    startActivity(intent);
                                                } else {

                                                    if (result.getCode().equals(MKeyMacro.ERR_APP_AUTH)) {
                                                        Intent intent = new Intent(Constants.HOME_SDK_CODE_BROADCAST);
                                                        getActivity().sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        showNoCertAlert(getString(R.string.cert_sz), getString(R.string.cert_no_tip), CertApplyActivity.class);
                                                    } else {
                                                        T.showShort(getActivity(), result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }
                                    case R.id.home_cert_log_rl: {
                                        //T.showShort("功能正在完善中，敬请期待");
                                        if (mLoadDlg != null) {
                                            mLoadDlg.dismiss();
                                            mLoadDlg = null;
                                        }
                                        MKeyApi.getInstance(getActivity(), SPManager.getSDKAuthCode(), strUserInfo, SPManager.getUserInfo().algVersion).getCert(new MKeyApiCallback() {
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
                                                        getActivity().sendBroadcast(intent);
                                                    }

                                                    if (result.getCode().equals(MKeyMacro.ERR_CERT_GET)) {
                                                        showNoCertAlert(getString(R.string.cert_rz), getString(R.string.cert_no_tip), CertApplyActivity.class);
                                                    } else {
                                                        T.showShort(getActivity(), result.getMsg());
                                                    }
                                                }
                                            }
                                        });
                                        break;
                                    }*/
                                    default:
                                        T.showShort("功能正在完善中，敬请期待");
                                        if (mLoadDlg != null) {
                                            mLoadDlg.dismiss();
                                            mLoadDlg = null;
                                        }
                                        break;
                                }
                            }else{
                                //权限未开启
                                Toast.makeText(getActivity(), "请在设置->应用管理中打开读写手机存储权限", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
