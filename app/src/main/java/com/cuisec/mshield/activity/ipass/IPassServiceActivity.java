package com.cuisec.mshield.activity.ipass;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import com.cuca.security.PKIUtil;
import com.cuca.security.bean.CertInfo;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.bean.IpassGetBinInfo;
import com.cuisec.mshield.bean.IpassOrderBean;
import com.cuisec.mshield.bean.IpassOrderInfo;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.utils.Base64Utils;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.AlertDialog;
import com.cuisec.mshield.widget.LoadDialog;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class IPassServiceActivity extends BaseActivity {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private ArrayList<IpassOrderInfo.DataBean> orderList;
    private CommonAdapter mAdapter;
    private String mSaveType;
    private LoadDialog mLoadDlg = null;
    private RxPermissions rxPermissions;
    private String mSavePhone;
    private String mSaveBindPhone;
    private ArrayList<IpassGetBinInfo.DataBean> mSearchList;
    private IpassOrderInfo mCheckBind;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_ipass_service);
        ButterKnife.bind(this);
        rxPermissions = new RxPermissions(this);
        mSaveBindPhone = (String) SPUtils.get(IPassServiceActivity.this, Constants.SAVE_PASS_BIND_PHONE, "");
        mSaveType = (String) SPUtils.get(IPassServiceActivity.this, Constants.SAVE_PASS_BIND_TYPE, "");
        mSavePhone = (String) SPUtils.get(IPassServiceActivity.this, Constants.SAVE_USER_PHONE, "");
        L.i(mSaveType + "  " + mSaveBindPhone);
        initNotice();
    }

    private void initNotice() {
        String indata = Constants.APP_ID + "#" + ("".equals(mSaveBindPhone) ? mSavePhone : mSaveBindPhone) + "#" + ("".equals(mSaveType) ? "HD" : mSaveType);
        L.i(indata);
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        String cert = "MIIDAzCCAeugAwIBAgIOA/5febRcb/PCj3IWyQIwDQYJKoZIhvcNAQELBQAwITELMAkGA1UEBhMCQ04xEjAQBgNVBAMMCUFQUElOU0lERTAeFw0xNzA1MTgwMjEyMDZaFw0yNTEyMzExNjAwMDBaMCExCzAJBgNVBAYTAkNOMRIwEAYDVQQDDAlBUFBJTlNJREUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCILj7EvlvL81LnteSCUUhx5X8HKjlkI8iFJrAUpYXtfO7RRRry7svxvzS1d7UqXFCUCg8WtJKMCzTGtqWA9B4AzUt8d2SdptNvt/CfJO/rLBUkNQRrNzKRT4NRV+vkIHNdmY2aAw4yqpdtENsT7alKuV1Pd+072Mp09Cnp3Po8vgR4+/7/wOvR+t8sGi9vQgU1e3ANN2bnvbg5xDefJWYd1wEmWnR3uBRGx7fMIkYPtZooZP4cQ3OuS+KfVSujKRF61q7prkIRaALQqm+8WjYkhVP1u3xJh8H27tr9XBpHMnz/8dEUfWB6GduNAXfLFctYy4Tg6Ip3uaszQ6rZ09TRAgMBAAGjOTA3MAkGA1UdEwQCMAAwCwYDVR0PBAQDAgTQMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAQEAZU+UGkqGuhE2tTtNZ0KNOuQs1qHJD2aWHhQjx/M2GUEKssIBbyOK1YBml5RiM78lLuU5UTy4UK1oot6afP8qUZ8IaSn6P7mcQs2dZRIBbmbK3jXmgFM5DBvXpTqiOaVPqmzXnV0XPsiehJ9/sKLS5XZNj+yQ4UDYkoJa/HNbSO6W5wVRK5B0m9UbbStLTyjWC8Vqz2yZs7N2zVPoNqWFtFEkYrbED1OhPgZFyJ8LI/vQGt3u/jD2LZb5z9WdKDzAIiBkmY7dSCtnNxg7ROBUMecR/BM6YGOXIrzpOG9PgT+UCsFe6GotFOSfzg8u9xPVeE4zM9pCRD8qiR8AmlAh7A==";
        //验证签名
        CertInfo certinfo = null;
        try {
            certinfo = PKIUtil.getCertInfo(cert.getBytes());
            PKIUtil.getCertInfo(cert.getBytes());
            byte[] decode1 = Base64.decode(sign.getBytes(), Base64.DEFAULT);
            boolean ret;
            ret = PKIUtil.verify(SecurityUtil.SIGN_ALGORITHMS, certinfo.getPublicKey(), indata.getBytes(),
                    decode1, "SOFT", 1);
            L.i(String.valueOf(ret));
        } catch (Exception e) {
            e.printStackTrace();
        }
        final IpassOrderBean ipassOrderBean = new IpassOrderBean();
        ipassOrderBean.setAppid(Constants.APP_ID_IPASS);
        if (mSaveType != null && !("".equals(mSaveType))) {
            ipassOrderBean.setChannel(mSaveType);
        } else ipassOrderBean.setChannel("HD");
        //ipassOrderBean.setPhone("13391826150");
        if ("".equals(mSaveBindPhone)){
            ipassOrderBean.setPhone(mSavePhone);
        }else ipassOrderBean.setPhone(mSaveBindPhone);
       /* if (mSaveBindPhone != null && !("".equals(mSaveBindPhone))) {
            ipassOrderBean.setPhone(mSaveBindPhone);
        } else ipassOrderBean.setPhone(mSavePhone);*/
        ipassOrderBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassOrderBean);
        L.i(json);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url+"ipassBus/ipassNoticeInfo/list?pageNum=2&pageSize=1")
                .post(requestBody)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.i(e.getMessage());
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
                    String string = response.body().string();
                    L.i(string);
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

    @Override
    protected void initializeViews() {
        showTitle("iPASS自助服务");
        ipassBindActivity(true,"".equals(mSaveBindPhone) ? mSavePhone : mSaveBindPhone);
    }

    @Override
    protected void initializeData() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (orderList == null) {
            orderList = new ArrayList<>();
        }
        if (mSearchList == null) {
            mSearchList = new ArrayList<>();
        }
        initIpassList();
        selectCountiess();
    }

    private void selectCountiess() {
        mAdapter = new CommonAdapter<IpassOrderInfo.DataBean>(IPassServiceActivity.this, R.layout.item_ipass_order, orderList) {
            @Override
            protected void convert(ViewHolder holder, IpassOrderInfo.DataBean dataBean, int position) {
                holder.setText(R.id.ipass_acceptno, dataBean.getAcceptno());
                holder.setText(R.id.ipass_businesstype, dataBean.getBusinesstype());
                holder.setText(R.id.ipass_status, dataBean.getStatus());
            }
        };
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                String acceptno = orderList.get(position).getAcceptno();
                Intent intent = new Intent(IPassServiceActivity.this, IPassDetailsActivity.class);
                intent.putExtra("ipass", acceptno);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }
    public void checkBindIpass(){
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(IPassServiceActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        String indata = Constants.APP_ID + "#" + ("".equals(mSaveBindPhone) ? mSavePhone : mSaveBindPhone) + "#" + ("".equals(mSaveType) ? "HD" : mSaveType);
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        String cert = "MIIDAzCCAeugAwIBAgIOA/5febRcb/PCj3IWyQIwDQYJKoZIhvcNAQELBQAwITELMAkGA1UEBhMCQ04xEjAQBgNVBAMMCUFQUElOU0lERTAeFw0xNzA1MTgwMjEyMDZaFw0yNTEyMzExNjAwMDBaMCExCzAJBgNVBAYTAkNOMRIwEAYDVQQDDAlBUFBJTlNJREUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCILj7EvlvL81LnteSCUUhx5X8HKjlkI8iFJrAUpYXtfO7RRRry7svxvzS1d7UqXFCUCg8WtJKMCzTGtqWA9B4AzUt8d2SdptNvt/CfJO/rLBUkNQRrNzKRT4NRV+vkIHNdmY2aAw4yqpdtENsT7alKuV1Pd+072Mp09Cnp3Po8vgR4+/7/wOvR+t8sGi9vQgU1e3ANN2bnvbg5xDefJWYd1wEmWnR3uBRGx7fMIkYPtZooZP4cQ3OuS+KfVSujKRF61q7prkIRaALQqm+8WjYkhVP1u3xJh8H27tr9XBpHMnz/8dEUfWB6GduNAXfLFctYy4Tg6Ip3uaszQ6rZ09TRAgMBAAGjOTA3MAkGA1UdEwQCMAAwCwYDVR0PBAQDAgTQMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAQEAZU+UGkqGuhE2tTtNZ0KNOuQs1qHJD2aWHhQjx/M2GUEKssIBbyOK1YBml5RiM78lLuU5UTy4UK1oot6afP8qUZ8IaSn6P7mcQs2dZRIBbmbK3jXmgFM5DBvXpTqiOaVPqmzXnV0XPsiehJ9/sKLS5XZNj+yQ4UDYkoJa/HNbSO6W5wVRK5B0m9UbbStLTyjWC8Vqz2yZs7N2zVPoNqWFtFEkYrbED1OhPgZFyJ8LI/vQGt3u/jD2LZb5z9WdKDzAIiBkmY7dSCtnNxg7ROBUMecR/BM6YGOXIrzpOG9PgT+UCsFe6GotFOSfzg8u9xPVeE4zM9pCRD8qiR8AmlAh7A==";
        //验证签名
        CertInfo certinfo = null;
        try {
            certinfo = PKIUtil.getCertInfo(cert.getBytes());
            PKIUtil.getCertInfo(cert.getBytes());
            //String decode1 = Base64.encodeToString(sign.getBytes(), Base64.DEFAULT);
            String s = Base64Utils.decodeToString(sign);
            boolean ret;
            ret = PKIUtil.verify(SecurityUtil.SIGN_ALGORITHMS, certinfo.getPublicKey(), indata.getBytes(),
                    s.getBytes(), "SOFT", 1);
            L.i(String.valueOf(ret));
        } catch (Exception e) {
            e.printStackTrace();
        }
        final IpassOrderBean ipassOrderBean = new IpassOrderBean();
        ipassOrderBean.setAppid(Constants.APP_ID_IPASS);
        if (mSaveType != null && !("".equals(mSaveType))) {
            ipassOrderBean.setChannel(mSaveType);
        } else ipassOrderBean.setChannel("LT");
        //ipassOrderBean.setPhone("13391826150");
        if ("".equals(mSaveBindPhone)){
            ipassOrderBean.setPhone(mSavePhone);
        }else  ipassOrderBean.setPhone(mSaveBindPhone);
        /*if (mSaveBindPhone != null && !("".equals(mSaveBindPhone))) {
            ipassOrderBean.setPhone(mSaveBindPhone);
        } else ipassOrderBean.setPhone(mSavePhone);*/
        ipassOrderBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassOrderBean);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url+Config.ipass_get_order)
                .post(requestBody)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
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
                    if (response.isSuccessful()) {

                        String string = response.body().string();
                        mCheckBind = gson.fromJson(string, IpassOrderInfo.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mCheckBind != null && mCheckBind.getMsg().equals("请先绑定IPASS平台账号！")) {
                                    bindIpassPop();
                                    return;
                                }
                                if (mCheckBind != null && mCheckBind.getMsg().equals("IPASS平台调用失败！")) {
                                    T.showShort("平台信息调用失败");
                                    finish();
                                    return;
                                }
                                if (mCheckBind.getData() != null && mCheckBind.getData().size() > 0) {
                                    orderList.addAll(mCheckBind.getData());
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
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
    private void initIpassList() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(IPassServiceActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        String indata = Constants.APP_ID + "#" + ("".equals(mSaveBindPhone) ? mSavePhone : mSaveBindPhone) + "#" + ("".equals(mSaveType) ? "HD" : mSaveType);
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        String cert = "MIIDAzCCAeugAwIBAgIOA/5febRcb/PCj3IWyQIwDQYJKoZIhvcNAQELBQAwITELMAkGA1UEBhMCQ04xEjAQBgNVBAMMCUFQUElOU0lERTAeFw0xNzA1MTgwMjEyMDZaFw0yNTEyMzExNjAwMDBaMCExCzAJBgNVBAYTAkNOMRIwEAYDVQQDDAlBUFBJTlNJREUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCILj7EvlvL81LnteSCUUhx5X8HKjlkI8iFJrAUpYXtfO7RRRry7svxvzS1d7UqXFCUCg8WtJKMCzTGtqWA9B4AzUt8d2SdptNvt/CfJO/rLBUkNQRrNzKRT4NRV+vkIHNdmY2aAw4yqpdtENsT7alKuV1Pd+072Mp09Cnp3Po8vgR4+/7/wOvR+t8sGi9vQgU1e3ANN2bnvbg5xDefJWYd1wEmWnR3uBRGx7fMIkYPtZooZP4cQ3OuS+KfVSujKRF61q7prkIRaALQqm+8WjYkhVP1u3xJh8H27tr9XBpHMnz/8dEUfWB6GduNAXfLFctYy4Tg6Ip3uaszQ6rZ09TRAgMBAAGjOTA3MAkGA1UdEwQCMAAwCwYDVR0PBAQDAgTQMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAQEAZU+UGkqGuhE2tTtNZ0KNOuQs1qHJD2aWHhQjx/M2GUEKssIBbyOK1YBml5RiM78lLuU5UTy4UK1oot6afP8qUZ8IaSn6P7mcQs2dZRIBbmbK3jXmgFM5DBvXpTqiOaVPqmzXnV0XPsiehJ9/sKLS5XZNj+yQ4UDYkoJa/HNbSO6W5wVRK5B0m9UbbStLTyjWC8Vqz2yZs7N2zVPoNqWFtFEkYrbED1OhPgZFyJ8LI/vQGt3u/jD2LZb5z9WdKDzAIiBkmY7dSCtnNxg7ROBUMecR/BM6YGOXIrzpOG9PgT+UCsFe6GotFOSfzg8u9xPVeE4zM9pCRD8qiR8AmlAh7A==";
        //验证签名
        CertInfo certinfo = null;
        try {
            certinfo = PKIUtil.getCertInfo(cert.getBytes());
            PKIUtil.getCertInfo(cert.getBytes());
            byte[] decode1 = Base64.decode(sign.getBytes(), Base64.DEFAULT);
            boolean ret;
            ret = PKIUtil.verify(SecurityUtil.SIGN_ALGORITHMS, certinfo.getPublicKey(), indata.getBytes(),
                    decode1, "SOFT", 0);
            L.i(String.valueOf(ret));
        } catch (Exception e) {
            e.printStackTrace();
            L.i(e.getMessage());
        }
        final IpassOrderBean ipassOrderBean = new IpassOrderBean();
        ipassOrderBean.setAppid(Constants.APP_ID_IPASS);
        if (mSaveType != null && !("".equals(mSaveType))) {
            ipassOrderBean.setChannel(mSaveType);
        } else ipassOrderBean.setChannel("HD");
        //ipassOrderBean.setPhone("13391826150");
        if ("".equals(mSaveBindPhone)){
            ipassOrderBean.setPhone(mSavePhone);
        }else  ipassOrderBean.setPhone(mSaveBindPhone);
        /*if (mSaveBindPhone != null && !("".equals(mSaveBindPhone))) {
            ipassOrderBean.setPhone(mSaveBindPhone);
        } else ipassOrderBean.setPhone(mSavePhone);*/
        ipassOrderBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassOrderBean);
        OkHttpClient clientWithCache = getClientWithCache();

        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url+Config.ipass_get_order)
                .post(requestBody)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
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
                    if (response.isSuccessful()){
                        String string = response.body().string();
                        final IpassOrderInfo ipassOrderInfo = gson.fromJson(string, IpassOrderInfo.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (ipassOrderInfo!= null && ipassOrderInfo.getMsg().equals("请先绑定IPASS平台账号！")) {
                                    checkBindIpass();
                                    if (mCheckBind != null && mCheckBind.getMsg().equals("请先绑定IPASS平台账号！")){
                                        bindIpassPop();
                                    }
                                    return;
                                }
                                if (ipassOrderInfo != null && ipassOrderInfo.getMsg().equals("IPASS平台调用失败！")){
                                    T.showShort("平台信息调用失败");
                                    //平台信息调用
                                    finish();
                                    return;
                                }
                                if (ipassOrderInfo.getData() != null && ipassOrderInfo.getData().size() > 0){
                                    orderList.addAll(ipassOrderInfo.getData());
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }

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

    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .cache(new Cache(new File(this.getExternalCacheDir(), "okhttpcache"), 10 * 1024 * 1024))
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }

    public void bindIpassPop() {
        new AlertDialog(IPassServiceActivity.this)
                .builder()
                .setTitle("未绑定iPASS")
                .setMessage("请绑定iPASS服务后查询")
                .setNegativeButton(getString(R.string.ipass_bind), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(IPassServiceActivity.this, BindiPASSActivity.class));
                    }
                })
                .setPositiveButton(getString(R.string.ipass_back), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }).show();
    }
}
