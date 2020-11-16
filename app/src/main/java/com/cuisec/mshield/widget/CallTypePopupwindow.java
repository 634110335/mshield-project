package com.cuisec.mshield.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cuisec.mshield.R;
import com.cuisec.mshield.bean.SourceBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.design.NetTrustManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CallTypePopupwindow extends PopupWindow {
    private View view;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private ArrayList<SourceBean> mSourceBeans;
    private IcallBack mIcallBack;
    private TextView mTvClear;
    private CommonAdapter mCommonAdapter;
    private TextView mTvAll;
    private MyHandler mHandler = new MyHandler(this);
    private class MyHandler extends Handler {
        private WeakReference<CallTypePopupwindow> mAddressPopupWindowWeakReference;

        public MyHandler(CallTypePopupwindow popupWindow) {
            mAddressPopupWindowWeakReference = new WeakReference<CallTypePopupwindow>(popupWindow);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mCommonAdapter.notifyDataSetChanged();
            }
        }
    }
    public CallTypePopupwindow(Context context) {
        this.mContext = context;
        view = ((Activity) context).getLayoutInflater().from(context).inflate(R.layout.messag_esource_popupwindow, null);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new BitmapDrawable());
        setFocusable(true);
        setOutsideTouchable(true);
        setAnimationStyle(R.style.PopupAnimation);
        setContentView(view);
        backgroundAlpha(0.5f);
        initData();
        initView();
    }

    private void initData() {
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(null
                , "");
        final Request request = new Request.Builder()
                .post(requestBody)
                .url(Config.https_base_service_url+Config.source_list)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response)  {
                try {
                    String string = response.body().string();
                    fromToJson(string,new TypeToken<List<SourceBean>>(){}.getType());
                    ArrayList<SourceBean> data = fromToJson(string, new TypeToken<List<SourceBean>>() {
                    }.getType());
                    mSourceBeans.addAll(data);
                    mHandler.sendEmptyMessageDelayed(1, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        /*OkHttpUtils.post()
                .url(Config.https_base_service_url+Config.source_list)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        fromToJson(response,new TypeToken<List<SourceBean>>(){}.getType());
                        ArrayList<SourceBean> data = fromToJson(response, new TypeToken<List<SourceBean>>() {
                        }.getType());
                        mSourceBeans.addAll(data);
                        mCommonAdapter.notifyDataSetChanged();
                    }
                });*/
    }
    //根据泛型返回解析制定的类型
    public  <T> T fromToJson(String json, Type listType){
        Gson gson = new Gson();
        T t = null;
        t = gson.fromJson(json,listType);
        return t;
    }

    private void initView() {
        if (mSourceBeans == null) {
            mSourceBeans = new ArrayList<>();
        }
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mTvClear = view.findViewById(R.id.tv_clear);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mTvAll = view.findViewById(R.id.tv_popup_all);
        mCommonAdapter = new CommonAdapter<SourceBean>(mContext,R.layout.item_address_source, mSourceBeans) {
            @Override
            protected void convert(ViewHolder holder, SourceBean sourceBean, int position) {
                holder.setText(R.id.tv_popup_source,mSourceBeans.get(position).getTitle());
            }
        };
        mRecyclerView.setAdapter(mCommonAdapter);
        mCommonAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                mIcallBack.callBack(mSourceBeans.get(position).getTitle(),mSourceBeans.get(position).getId());
                dismiss();
                view.clearAnimation();
                backgroundAlpha(1f);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mTvAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIcallBack != null){
                    mIcallBack.callALL(mTvAll.getText().toString());
                }
                dismiss();
                mTvAll.clearAnimation();
                backgroundAlpha(1f);
            }
        });
        mTvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                view.clearAnimation();
                backgroundAlpha(1f);
            }
        });
    }

    public interface IcallBack {
        void callBack(String address, String id);
        void callALL(String data);
    }

    public void setCallBack(IcallBack callBack) {
        this.mIcallBack = callBack;
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha 透明度
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        ((Activity) mContext).getWindow().setAttributes(lp);
    }
    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }
}
