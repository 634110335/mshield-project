package com.cuisec.mshield.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cuisec.mshield.R;
import com.cuisec.mshield.SeachActivity;
import com.cuisec.mshield.activity.ipass.IPassServiceActivity;
import com.cuisec.mshield.adapter.ServiceAdapter;
import com.cuisec.mshield.bean.ServiceBean;
import com.cuisec.mshield.bean.ServiceInfo;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.design.BadgeView;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.LoadDialog;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

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

public class ServiceFragment extends Fragment {
    private static ServiceFragment sServiceFragment;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.service_info)
    LinearLayout mServiceInfo;
    @BindView(R.id.service_ipass)
    LinearLayout mServiceIpass;
    @BindView(R.id.ipass_image)
    ImageView mIpassImage;
    private ArrayList<ServiceInfo.DomainListBean> mDomainListBeans;
    private ServiceAdapter mServiceAdapter;
    private LoadDialog mLoadDlg = null;
    private View view;
    private BadgeView mBadgeView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_service, null);
        ButterKnife.bind(this, inflate);
        initView();
        initData();
        return inflate;
    }

    private void initData() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(getActivity(), R.style.CustomDialog);
            mLoadDlg.show();
        }
        String userPhone = (String) SPUtils.get(getActivity(), Constants.SAVE_USER_PHONE, "");
        String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, Constants.APP_ID + "#" + userPhone);
        final Gson gson = new Gson();
        ServiceBean serviceBean = new ServiceBean();
        try {
            serviceBean.setSign(sign);
            serviceBean.setAppid(Constants.APP_ID);
            serviceBean.setPhone(userPhone);
            String json = gson.toJson(serviceBean);
            OkHttpClient okHttpClient = getClientWithCache();
            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , json);
            final Request request = new Request.Builder()
                    .url(Config.https_base_service_url + Config.service_app_list)
                    .post(requestBody)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String message = e.getMessage();
                                String substring = message.substring(0, 17);
                                T.showShort(getActivity(),substring);
                            }catch (Exception e){
                                e.getMessage();
                            }
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (mLoadDlg != null) {
                        mLoadDlg.dismiss();
                        mLoadDlg = null;
                    }
                    try {
                        String string = response.body().string();
                        ServiceInfo domainListBean = gson.fromJson(string, ServiceInfo.class);
                        if (domainListBean.getDomain_list() != null && domainListBean.getDomain_list().size() > 0){
                            mDomainListBeans.addAll(domainListBean.getDomain_list());
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mServiceAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (IOException e) {
                        if (mLoadDlg != null) {
                            mLoadDlg.dismiss();
                            mLoadDlg = null;
                        }
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initView() {
        if (mDomainListBeans == null) {
            mDomainListBeans = new ArrayList<>();
        }
        /*mBadgeView = new BadgeView(getActivity());
        mBadgeView.setTargetView(mIpassImage);
        mBadgeView.setText("9", TextView.BufferType.EDITABLE);*/
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mServiceAdapter = new ServiceAdapter(getActivity(), mDomainListBeans);
        mRecyclerView.setAdapter(mServiceAdapter);
    }


    public static ServiceFragment getInstance() {
        if (sServiceFragment == null) {
            sServiceFragment = new ServiceFragment();
        }
        return sServiceFragment;
    }

    @OnClick({R.id.service_info, R.id.service_ipass})
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.service_info:
                getActivity().startActivity(new Intent(getActivity(), SeachActivity.class));
                break;
            case R.id.service_ipass:
                //mBadgeView.setText("0");
                getActivity().startActivity(new Intent(getActivity(), IPassServiceActivity.class));
                break;
        }
    }

    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }
}
