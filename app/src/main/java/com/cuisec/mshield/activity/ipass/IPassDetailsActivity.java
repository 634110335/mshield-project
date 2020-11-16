package com.cuisec.mshield.activity.ipass;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.bean.Datas;
import com.cuisec.mshield.bean.IpassDetailInfo;
import com.cuisec.mshield.bean.IpassDetailsInfo;
import com.cuisec.mshield.bean.IpassTrackInfo;
import com.cuisec.mshield.bean.UserCodeBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.widget.AlertDialog;
import com.cuisec.mshield.widget.LoadDialog;
import com.google.gson.Gson;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IPassDetailsActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private String mAcceptno;
    private ArrayList<IpassDetailsInfo.DataBean> mDataBeans;
    private ArrayList<IpassDetailsInfo.DataBean.ChildDataBean> mChildDataBeans;
    private CommonAdapter mAdapter;
    private CommonAdapter mAdapterRec;
    private LoadDialog mLoadDlg = null;
    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_ipass_details);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("订单详情");
    }

    @Override
    protected void initializeData() {
        if (mChildDataBeans == null){
            mChildDataBeans = new ArrayList<>();
        }
        if (getIntent() != null) {
            mAcceptno = getIntent().getStringExtra("ipass");
        }
        if (mDataBeans == null) {
            mDataBeans = new ArrayList<>();
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        initTrack();
        selectCountiess();
    }
    private void setRecyclerView(){
        mAdapterRec = new CommonAdapter<IpassDetailsInfo.DataBean.ChildDataBean>(IPassDetailsActivity.this, R.layout.item_ipass_dateils_boy, mChildDataBeans) {
            @Override
            protected void convert(ViewHolder holder, IpassDetailsInfo.DataBean.ChildDataBean dataBean, int position) {
                    holder.setText(R.id.ipass_opDate,dataBean.getOpDate());
                    holder.setText(R.id.ipass_opDesc,dataBean.getOpDesc());
            }
        };
        mAdapterRec.notifyDataSetChanged();
    }
    private void selectCountiess() {
        mAdapter = new CommonAdapter<IpassDetailsInfo.DataBean>(IPassDetailsActivity.this, R.layout.item_ipass_dateils, mDataBeans) {
            @Override
            protected void convert(ViewHolder holder, IpassDetailsInfo.DataBean dataBean, int position) {
                holder.setText(R.id.ipass_acceptId,mDataBeans.get(position).getAcceptId());
                        RecyclerView view = holder.getView(R.id.recyclerView);
                        view.setLayoutManager(new LinearLayoutManager(IPassDetailsActivity.this));
                        setRecyclerView();
                        view.setAdapter(mAdapterRec);
            }
        };
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initTrack() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(IPassDetailsActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        String saveBindPhone = (String) SPUtils.get(this, Constants.SAVE_PASS_BIND_PHONE, "");
        String savePhone = (String) SPUtils.get(this, Constants.SAVE_USER_PHONE, "");
        String indata = Constants.APP_ID_IPASS + "#" + ("".equals(saveBindPhone) ? savePhone : saveBindPhone) + "#" + mAcceptno;
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        OkHttpClient clientWithCache = getClientWithCache();
        IpassTrackInfo ipassTrackInfo = new IpassTrackInfo();
        ipassTrackInfo.setAcceptno(mAcceptno);
        ipassTrackInfo.setAppid(Constants.APP_ID_IPASS);
        if (saveBindPhone != null && !("".equals(saveBindPhone))) {
            ipassTrackInfo.setPhone(saveBindPhone);
        } else ipassTrackInfo.setPhone(savePhone);
        ipassTrackInfo.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassTrackInfo);
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url+Config.ipass_details)
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
                    String string = response.body().string();
                    final IpassDetailsInfo ipassDetailsInfo = gson.fromJson(string, IpassDetailsInfo.class);
                    L.i(string);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ipassDetailsInfo.getData() != null && ipassDetailsInfo.getData().size() > 0) {
                                mDataBeans.addAll(ipassDetailsInfo.getData());
                                for (int i = 0; i <ipassDetailsInfo.getData().size(); i++) {
                                    if (ipassDetailsInfo.getData().get(i).getChildData().size() > 0){
                                        mChildDataBeans.addAll(ipassDetailsInfo.getData().get(i).getChildData());
                                    }else {
                                        bindIpassPop();
                                    }
                                }
                            }else {
                                bindIpassPop();
                            }
                            mAdapter.notifyDataSetChanged();
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

    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }
    public void bindIpassPop() {
        new AlertDialog(IPassDetailsActivity.this)
                .builder()
                .setTitle("订单状况")
                .setMessage("暂无订单跟踪信息")
                .setPositiveButton(getString(R.string.ipass_back), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }).show();
    }
}
