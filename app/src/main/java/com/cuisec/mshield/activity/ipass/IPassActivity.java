package com.cuisec.mshield.activity.ipass;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.bean.IpassOrderBean;
import com.cuisec.mshield.bean.IpassOrderInfo;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SecurityUtil;
import com.google.gson.Gson;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import java.io.IOException;
import java.util.ArrayList;
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
public class IPassActivity extends BaseActivity {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private ArrayList<IpassOrderInfo.DataBean> orderList;
    private CommonAdapter mAdapter;
    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_ipass);
        ButterKnife.bind(this);
    }
    @Override
    protected void initializeViews() {
        showTitle("iPASS自助服务");
    }
    @Override
    protected void initializeData() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (orderList == null) {
            orderList = new ArrayList<>();
        }
        initIpassList();
        selectCountiess();
    }
    private void selectCountiess() {
        mAdapter = new CommonAdapter<IpassOrderInfo.DataBean>(IPassActivity.this, R.layout.item_ipass_order, orderList) {
            @Override
            protected void convert(ViewHolder holder, IpassOrderInfo.DataBean dataBean, int position) {
                holder.setText(R.id.ipass_acceptno, dataBean.getAcceptno());
                holder.setText(R.id.ipass_addTime, dataBean.getAddTime());
                holder.setText(R.id.ipass_businesstype, dataBean.getBusinesstype());
                holder.setText(R.id.ipass_status, dataBean.getStatus());
            }
        };
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                String acceptno = orderList.get(position).getAcceptno();
                Intent intent = new Intent(IPassActivity.this,IPassDetailsActivity.class);
                intent.putExtra("ipass",acceptno);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initIpassList() {
        String indata = Constants.APP_ID_IPASS + "#" + "13391826150" + "#" + "LT";
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        final IpassOrderBean ipassOrderBean = new IpassOrderBean();
        ipassOrderBean.setAppid(Constants.APP_ID_IPASS);
        ipassOrderBean.setChannel("LT");
        ipassOrderBean.setPhone("13391826150");
        ipassOrderBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassOrderBean);
        L.i(json);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url("http://192.168.13.88:8082/ipassBus/getIpassOrder")
                .post(requestBody)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.i(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String string = response.body().string();
                    L.i(string);
                    IpassOrderInfo ipassOrderInfo = gson.fromJson(string, IpassOrderInfo.class);
                    if (ipassOrderInfo.getMsg().equals("请先绑定IPASS平台账号！")){

                    }
                    if (ipassOrderInfo.getData() != null){
                        orderList.addAll(ipassOrderInfo.getData());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
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
}
