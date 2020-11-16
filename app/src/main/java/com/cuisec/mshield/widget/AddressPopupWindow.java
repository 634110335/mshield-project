package com.cuisec.mshield.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cuisec.mshield.R;
import com.cuisec.mshield.SeachActivity;
import com.cuisec.mshield.bean.ProvinceBean;
import com.cuisec.mshield.bean.ProvinceDataBean;
import com.cuisec.mshield.bean.ProvinceEntity;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.searchhistory.FlowLayout;
import com.cuisec.mshield.searchhistory.TagAdapter;
import com.cuisec.mshield.utils.GetJsonDataUtil;
import com.google.gson.Gson;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFileBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by libo on 2017/4/27.
 * <p>
 * 地址弹出框选择
 */
public class AddressPopupWindow extends PopupWindow {
    private View view;
    private Context context;
    private List<ProvinceDataBean.DataBean> mProvinceBeans = null;
    private IcallBack callBack;
    private ArrayList<ProvinceDataBean.DataBean> list = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private CommonAdapter mCommonAdapter;
    private MyHandler mMyHandler = new MyHandler(this);
    private class MyHandler extends Handler {
        private WeakReference<AddressPopupWindow> mAddressPopupWindowWeakReference;

        public MyHandler(AddressPopupWindow popupWindow) {
            mAddressPopupWindowWeakReference = new WeakReference<AddressPopupWindow>(popupWindow);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
               mCommonAdapter.notifyDataSetChanged();
            }
        }
    }
    public AddressPopupWindow(Context context) {
        this.context = context;
        view = ((Activity) context).getLayoutInflater().from(context).inflate(R.layout.address_popupwindow, null);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new BitmapDrawable());
        setFocusable(true);
        setOutsideTouchable(true);
        setAnimationStyle(R.style.PopupAnimation);
        setContentView(view);
        backgroundAlpha(0.5f);
        initView();
        initData();
    }

    private void initData() {
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(null
                , "");
        final Request request = new Request.Builder()
                .post(requestBody)
                .url(Config.https_base_service_url+Config.system_list)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response)  {
                Gson gson = new Gson();
                try {
                    ProvinceDataBean dataBean = gson.fromJson(response.body().string(), ProvinceDataBean.class);
                    mProvinceBeans.addAll(dataBean.getData());
                    mMyHandler.sendEmptyMessageDelayed(1, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
      /*  OkHttpUtils
                .post()
                .url("https://61.138.142.30:38081/provinceData/list")

                //.url("http://10.192.10.21:28082/system/provinceData/list")
               // .url(Config.https_base_service_url+"provinceData/list")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("eeee", "onError: "+e.getMessage() );
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Gson gson = new Gson();
                        ProvinceDataBean dataBean = gson.fromJson(response, ProvinceDataBean.class);
                        mProvinceBeans.addAll(dataBean.getData());
                        mCommonAdapter.notifyDataSetChanged();
                    }
                });*/
    }

    public void paseArray(){
        try {
            String JsonData = new GetJsonDataUtil().getJson(context, "province.txt");
            Gson gson = new Gson();
            ProvinceBean provinceBean = gson.fromJson(JsonData, ProvinceBean.class);
            //mProvinceBeans =provinceBean.getData();
            for (int i = 0; i <mProvinceBeans.size() ; i++) {
                list.add(mProvinceBeans.get(i));
            }
            mCommonAdapter.notifyDataSetChanged();
        }catch (Exception e){
            e.getMessage();
        }
    }
    private void initView() {
        if (mProvinceBeans == null){
            mProvinceBeans = new ArrayList<>();
        }
        mRecyclerView = view.findViewById(R.id.rv_popup_address);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mCommonAdapter = new CommonAdapter<ProvinceDataBean.DataBean>(context,R.layout.item_address_crit,mProvinceBeans) {

            @Override
            protected void convert(ViewHolder holder, ProvinceDataBean.DataBean dataBean, int position) {
                holder.setText(R.id.tv_popup_place,mProvinceBeans.get(position).getProvince());
            }
        };
        mRecyclerView.setAdapter(mCommonAdapter);
        mCommonAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                String province = mProvinceBeans.get(position).getProvince();
                int sid = mProvinceBeans.get(position).getSid();
                String provinceId = mProvinceBeans.get(position).getProvinceId();
                if (callBack != null){
                    callBack.callNationwide(province,String.valueOf(provinceId));
                }
                dismiss();
                view.clearAnimation();
                backgroundAlpha(1f);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }

        });

        view.findViewById(R.id.tv_popup_nationwide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView nationwide = view.findViewById(R.id.tv_popup_nationwide);
                if (callBack != null){
                   callBack.callBack(nationwide.getText().toString());
                }
                dismiss();
                nationwide.clearAnimation();
                backgroundAlpha(1f);
            }
        });
        view.findViewById(R.id.tv_popup_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                view.clearAnimation();
                backgroundAlpha(1f);
            }
        });
    }



    public interface IcallBack {
        void callBack(String data);
        void callNationwide(String data,String sid);
    }

    public void setCallBack(IcallBack callBack) {
        this.callBack = callBack;
    }
    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha 透明度
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        ((Activity) context).getWindow().setAttributes(lp);
    }
    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }
}
