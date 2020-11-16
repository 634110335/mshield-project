package com.cuisec.mshield.activity.ipass;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.bean.BindIPASSBean;
import com.cuisec.mshield.bean.IPASSBindBean;
import com.cuisec.mshield.bean.IPASSCodeBean;
import com.cuisec.mshield.bean.IpassSuccessBean;
import com.cuisec.mshield.bean.SMSiPASSBean;
import com.cuisec.mshield.bean.SmsSuccessBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.PhoneUtils;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.ValidateUtil;
import com.cuisec.mshield.widget.LoadDialog;
import com.cuisec.mshield.widget.MenuDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BindiPASSActivity extends BaseActivity {
    @BindView(R.id.et_bind_phone)
    EditText mEtBindPhone;
    @BindView(R.id.et_bind_type)
    EditText mEtBindType;
    @BindView(R.id.cert_find_code_iv)
    ImageView mCertFindCodeIv;
    @BindView(R.id.btn_code)
    Button mBtnCode;
    @BindView(R.id.et_code)
    EditText mEtCode;
    @BindView(R.id.btn_submit)
    Button mBtnSubmit;
    @BindView(R.id.et_serial)
    EditText mEtSerial;
    @BindView(R.id.layout_login_rl)
    LinearLayout mLayoutLoginRl;
    @BindView(R.id.et_bind_platform)
    EditText mEtBindPlatform;
    private String mIdType = ID_TYPE_NONE;
    private static final String ID_TYPE_NONE = "0";      // 未选择
    private static final String ID_TYPE_LINK_LT = "1";//联通采购平台
    private static final String ID_TYPE_HD = "2";//华电采购平台
    private ArrayList<IPASSBindBean> bindList;
    private TimeCount mTime;
    private String mBindId;
    private SmsSuccessBean mSmsSuccessBean = null;
    private ArrayList<SmsSuccessBean> mSuccessBeans = new ArrayList<>();
    private LoadDialog mLoadDlg = null;
    private String mEtPlatform;
    private String mIpassSerial;
    private IPASSCodeBean mIpassCodeBean;
    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_bindi_pass);
        ButterKnife.bind(this);
        mEtBindPhone.setText((String) SPUtils.get(BindiPASSActivity.this, Constants.SAVE_USER_PHONE, ""));
    }

    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }

    private void initBindChannel() {
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(null
                , "");
        final Request request = new Request.Builder()
                .post(requestBody)
                .url(Config.https_base_service_url + Config.ipass_channel)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                L.i(string);
                fromToJson(string, new TypeToken<List<IPASSBindBean>>() {
                }.getType());
                ArrayList<IPASSBindBean> data = fromToJson(string, new TypeToken<List<IPASSBindBean>>() {
                }.getType());
                bindList.addAll(data);
            }
        });
    }
    //根据泛型返回解析制定的类型
    public <T> T fromToJson(String json, Type listType) {
        Gson gson = new Gson();
        T t = null;
        t = gson.fromJson(json, listType);
        return t;
    }
    @Override
    protected void initializeViews() {
        showTitle("iPASS用户绑定");
    }
    @Override
    protected void initializeData() {
        bindList = new ArrayList<>();
        initBindChannel();
    }
    @OnClick({R.id.et_bind_type, R.id.btn_code, R.id.btn_submit, R.id.layout_login_rl})
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.et_bind_type:
                final String[] list = new String[bindList.size()];
                for (int i = 0; i < bindList.size(); i++) {
                    list[i] = bindList.get(i).getDictName();
                }
                new MenuDialog(BindiPASSActivity.this, getString(R.string.app_cancel), list, true).setOnMyPopClickListener(new MenuDialog.MenuClickListener() {
                    @Override
                    public void onItemClick(int index, String content) {
                        switch (index) {
                            case 0: {
                                mIdType = ID_TYPE_LINK_LT;
                                mEtBindType.setText(list[0]);
                                break;
                            }
                            case 1: {
                                mIdType = ID_TYPE_HD;
                                mEtBindType.setText(list[1]);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelClick(String content) {
                    }
                });
                break;
            case R.id.btn_code:
                String accountStr = mEtBindPhone.getText().toString();
                mEtPlatform = mEtBindPlatform.getText().toString();
                mIpassSerial = mEtSerial.getText().toString();
                if (accountStr.length() == 0) {
                    T.showShort(this, getString(R.string.mobile_tip));
                    return;
                }

                if (!ValidateUtil.validateMobile(accountStr)) {
                    T.showShort(this, getString(R.string.mobile_error));
                    return;
                }
                if (mEtPlatform.length() == 0 || mIpassSerial.length() == 0) {
                    T.showShort(this, "请输入平台账号");
                    return;
                }
                if (mIdType.equals(ID_TYPE_NONE)){
                    T.showShort(this,"获取渠道不能为空");
                    return;
                }
                getMsgCode();
                break;
            case R.id.btn_submit:
                String smsCode = mEtCode.getText().toString();
                String bindType = mEtBindType.getText().toString();
                String phone = mEtBindPhone.getText().toString();
                if (smsCode.length() == 0 ) {
                    T.showShort("请输入验证码");
                    return;
                }
                if (mIdType.equals(ID_TYPE_NONE)) {
                    T.showShort("请选择绑定渠道");
                    return;
                }
                if (phone.length() == 0) {
                    T.showShort("请输入手机号");
                    return;
                }
                bindIpass();
                break;
            case R.id.layout_login_rl:
                hideKeyBoard();
                break;
        }
    }
    private void bindIpass() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(BindiPASSActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        final String phone = mEtBindPhone.getText().toString();
        String smsCode = mEtCode.getText().toString();

        final String indatastr = Constants.APP_ID_IPASS + "#" + phone + "#" + mBindId + "#" + smsCode;
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indatastr);
        BindIPASSBean bindIPASSBean = new BindIPASSBean();
        bindIPASSBean.setAppid(Constants.APP_ID_IPASS);
        if (mBindId != null ) {
            bindIPASSBean.setBindId(mBindId);
            L.i(mBindId);
        }
        if (mEtCode.length() > 0) {
            bindIPASSBean.setCode(smsCode);
        }
        bindIPASSBean.setPhone(phone);
        bindIPASSBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(bindIPASSBean);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url + Config.ipass_user_bind)
                .post(requestBody)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                try {
                    final String string = response.body().string();
                    final SMSiPASSBean smSiPASSBean = gson.fromJson(string, SMSiPASSBean.class);
                    final IpassSuccessBean ipassSuccessBean = gson.fromJson(string, IpassSuccessBean.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ipassSuccessBean != null && ipassSuccessBean.getResultdesc().equals("邦定成功！")){
                                T.showShort(ipassSuccessBean.getResultdesc());
                                SPUtils.put(BindiPASSActivity.this, Constants.SAVE_PASS_BIND_PHONE,phone);
                                if (mEtBindType.getText().toString().equals("联通采购平台")){
                                    SPUtils.put(BindiPASSActivity.this,Constants.SAVE_PASS_BIND_TYPE,"LT");
                                }else SPUtils.put(BindiPASSActivity.this,Constants.SAVE_PASS_BIND_TYPE,"HD");
                                Intent intent = new Intent(BindiPASSActivity.this, IPassServiceActivity.class);
                                startActivity(intent);
                            }else {
                                //T.showShort(ipassSuccessBean.getResultdesc());
                                if (ipassSuccessBean.getResultdesc().equals("bindId不能为空")){
                                    T.showShort("请先获取短信验证码");
                                }else T.showShort(ipassSuccessBean.getResultdesc());
                                L.i(string);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getMsgCode() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(BindiPASSActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        mTime = new TimeCount(60000, 1000);
        mTime.start();//开始计时
        String phone = mEtBindPhone.getText().toString();
        //"CUS32100148263" + "#" + "zhangxiaowei"
        //由应用私钥对appid#phone#ipass序列号#平台管理账号 Base64编码签名值
        String indata = Constants.APP_ID_IPASS + "#" + phone + "#" + mEtBindPlatform + "#" + mEtSerial;
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        mIpassCodeBean = new IPASSCodeBean();
        mIpassCodeBean.setAppId(Constants.APP_ID_IPASS);
        if (mEtBindType.getText().toString().equals("联通采购平台")) {
            mIpassCodeBean.setChannel(bindList.get(0).getDictKey());
        } else {
            mIpassCodeBean.setChannel(bindList.get(1).getDictKey());
        }
        //平台账号
        mIpassCodeBean.setIpassAccount(mEtPlatform);
        //ipass序列号
        mIpassCodeBean.setIpassno(mIpassSerial);
        mIpassCodeBean.setPhone(phone);
        mIpassCodeBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(mIpassCodeBean);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url + Config.ipass_sms_code)
                .post(requestBody)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        T.showShort(getApplicationContext(), e.getLocalizedMessage());
                        mTime.cancel();
                        mTime = null;
                        mBtnCode.setClickable(true);
                        mBtnCode.setText(getString(R.string.code_get));
                        mBtnCode.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                try {
                    final String string = response.body().string();
                    final SMSiPASSBean smSiPASSBean = gson.fromJson(string, SMSiPASSBean.class);
                    if (smSiPASSBean != null && smSiPASSBean.getData() != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                T.showShort(smSiPASSBean.getMsg());
                                mTime.cancel();
                                mTime = null;
                                mBtnCode.setClickable(true);
                                mBtnCode.setText(getString(R.string.code_get));
                                mBtnCode.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                            }
                        });
                        return;
                    }
                    mSmsSuccessBean = gson.fromJson(string, SmsSuccessBean.class);
                    mBindId = mSmsSuccessBean.getBindId();
                    mSuccessBeans.add(mSmsSuccessBean);
                    L.i(string);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            T.showShort(mSmsSuccessBean.getResultdesc());
                           /* if (!(mSmsSuccessBean.getResultcode().equals("发送成功，请验证短信验证码"))){
                                T.showShort(mSmsSuccessBean.getResultdesc());
                                mTime.cancel();
                                mTime = null;
                                mBtnCode.setClickable(true);
                                mBtnCode.setText(getString(R.string.code_re_get));
                                mBtnCode.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                            }*/
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }
                }

            }
        });
    }
    //短信倒计时
    private class TimeCount extends CountDownTimer {
        private TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);   // 参数依次为总时长,和计时的时间间隔
        }
        @Override
        public void onFinish() {    // 计时完毕时触发
            mBtnCode.setClickable(true);
            mBtnCode.setText(getString(R.string.code_re_get));
            mBtnCode.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            mTime.cancel();
            mTime = null;
        }

        @Override
        public void onTick(long millisUntilFinished) {  // 计时过程显示
            mBtnCode.setClickable(false);
            mBtnCode.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.font_gray));
            mBtnCode.setText(getString(R.string.code_re_get) + "(" + millisUntilFinished / 1000 + ")");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTime != null) {
            mTime.cancel();
            mTime = null;
        }
    }
    public void hideKeyBoard() {
        try {
            if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                if (getCurrentFocus() != null)
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus()
                                            .getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            if (BindiPASSActivity.this.getCurrentFocus() != null) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(BindiPASSActivity.this
                                        .getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
}
