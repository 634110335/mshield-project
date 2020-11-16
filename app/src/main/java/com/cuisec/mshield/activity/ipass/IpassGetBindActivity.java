package com.cuisec.mshield.activity.ipass;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.bean.IpassGetBinInfo;
import com.cuisec.mshield.bean.IpassGetBindBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.design.KylinSearchView;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.seach.OnSearchListener;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.PhoneUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.SensitiveInfoUtils;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.ValidateUtil;
import com.cuisec.mshield.widget.AlertDialog;
import com.google.gson.Gson;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IpassGetBindActivity extends BaseActivity  {


    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private String mBindMsg;
    private ArrayList<IpassGetBinInfo.DataBean> mSearchList;
    private CommonAdapter mAdapter;
    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_ipass_get_bind);
    }

    @Override
    protected void initializeViews() {
        showTitle("iPASS用户绑定信息");
        if (getIntent() != null){
            mBindMsg = getIntent().getStringExtra("ipass");
        }
    }

    @Override
    protected void initializeData() {
        if (mSearchList == null){
            mSearchList = new ArrayList<>();
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            searchAdapter();
            initGetBind();
    }

    private void searchAdapter() {
        mAdapter = new CommonAdapter<IpassGetBinInfo.DataBean>(IpassGetBindActivity.this, R.layout.item_ipass_get_bind, mSearchList) {
            @Override
            protected void convert(ViewHolder holder, IpassGetBinInfo.DataBean dataBean, int position) {

                holder.setText(R.id.ipass_account, dataBean.getIpassAccount());
                holder.setText(R.id.ipass_ipassno, SensitiveInfoUtils.mobile(dataBean.getIpassno()));
                holder.setText(R.id.ipass_phone, PhoneUtils.phoneEncrypt(dataBean.getIpassphone()));
                holder.setText(R.id.ipass_channel, dataBean.getChannel().equals("LT") ? "联通采购平台" : "华电采购平台");
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }
    private void initGetBind() {
        OkHttpClient clientWithCache = getClientWithCache();
        String indata = Constants.APP_ID_IPASS + "#" + mBindMsg;
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        IpassGetBindBean ipassGetBindBean = new IpassGetBindBean();
        ipassGetBindBean.setAppid(Constants.APP_ID_IPASS);
        ipassGetBindBean.setPhone(mBindMsg);
        ipassGetBindBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassGetBindBean);
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url+Config.ipass_get_bind)
                .post(requestBody)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String string = response.body().string();
                    L.i(string);
                    final IpassGetBinInfo ipassGetBinInfo = gson.fromJson(string, IpassGetBinInfo.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ipassGetBinInfo != null ) {
                                if (ipassGetBinInfo.getData() != null &&  ipassGetBinInfo.getData().size() > 0){
                                    mSearchList.addAll(ipassGetBinInfo.getData());
                                    mAdapter.notifyDataSetChanged();
                                }else {
                                    bindIpassPop();
                                }
                            }
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }
    public void bindIpassPop() {
        new AlertDialog(this)
                .builder()
                .setTitle("提示")
                .setMessage("暂无用户信息")
                .setNegativeButton(getString(R.string.ipass_back), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       finish();
                    }
                }).show();
    }
}
