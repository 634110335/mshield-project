package com.cuisec.mshield.activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.cuisec.mshield.bean.ServerListBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.SuperSwipeRefreshLayout;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.activity.home.HomeActivity;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;


public class ServerConfigActivity extends BaseActivity implements ListView.OnItemClickListener, SuperSwipeRefreshLayout.OnPullRefreshListener {

    private SuperSwipeRefreshLayout mSwipeLayout;
    private ListView mListView;

    List<ServerListBean.ServerDetail> mServerList;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_server_config);
    }

    @Override
    protected void initializeViews() {
        showTitle("选择单位");
        mSwipeLayout = findViewById(R.id.server_srl);
        View child = LayoutInflater.from(mSwipeLayout.getContext())
                .inflate(R.layout.layout_refresh_head, null);
        mSwipeLayout.setHeaderView(child);
        mSwipeLayout.setOnPullRefreshListener(this);
        mListView = findViewById(R.id.server_lv);
        mListView.setOnItemClickListener(this);  // 添加列表项点击事件
    }

    @Override
    protected void initializeData() {
        // 启动加载的时候，打开下拉刷新
        mSwipeLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeLayout.setRefreshing(true);
            }
        });
        // 获取数据
        getServerList();
    }

    private void getServerList() {
        try {
            OkHttpUtils.post()
                    .url(Config.https_base_app_list)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            L.e(e.getLocalizedMessage());
                            T.showShort(ServerConfigActivity.this, e.getLocalizedMessage());
                            mSwipeLayout.setRefreshing(false);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            mSwipeLayout.setRefreshing(false);
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                ServerListBean bean = (ServerListBean) JsonUtil.toObject(response, ServerListBean.class);
                                if (bean != null) {
                                    if (bean.getRet() == 0) {
                                        // 显示列表
                                        mServerList = bean.getData();
                                        List<Map<String, Object>> itemList = new ArrayList<>();
                                        for (int i = 0; i < mServerList.size(); i++) {
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("server_name", mServerList.get(i).getName());
                                            map.put("server_url", mServerList.get(i).getName());
                                            itemList.add(map);
                                        }
                                        SimpleAdapter adapter = new SimpleAdapter(ServerConfigActivity.this, itemList,
                                                R.layout.layout_server_item,
                                                new String[]{"server_name", "server_url"},
                                                new int[]{R.id.server_name_tv, R.id.server_url_tv});
                                        mListView.setAdapter(adapter);
                                    } else {
                                        T.showShort(getApplicationContext(), bean.getMsg());
                                    }
                                } else {
                                    T.showShort(getApplicationContext(), getString(R.string.app_error));
                                }
                            } catch (Exception e) {
                                L.e(e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            mSwipeLayout.setRefreshing(false);
            L.e(e.getLocalizedMessage());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            ServerListBean.ServerDetail item = mServerList.get(position);
            // 设置单位名称和服务器地址
            SPManager.setServerCode(item.getCode());
            SPManager.setServerName(item.getName());
            SPManager.setServerUrl(item.getUrl());

            // 切换单位需要清空用户登录信息
            new Thread() {
                @Override
                public void run() {
                    try {
                        UserInfo userInfo = SPManager.getUserInfo();
                        if (userInfo != null && !userInfo.phone.equals("")) {
                            // 注销小米推送别名
                            MiPushClient.unsetAlias(getApplicationContext(), SecurityUtil.sha256(userInfo.phone + SPManager.getServerCode()), null);
                        }
                        // 调用登出接口
                        OkHttpUtils
                                .post()
                                .addHeader("token", SPManager.getUserToken())
                                .url(SPManager.getServerUrl() + Config.user_logout)
                                .build()
                                .execute();
                    } catch (Exception e) {
                        L.e(e.getLocalizedMessage());
                    }
                }
            }.start();

            // 清除登录状态
            SPManager.setLoginState(false);

            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finishActivity();
        } catch (Exception e) {
            L.e(e.getLocalizedMessage());
        }
    }

    @Override
    public void onRefresh() {
        getServerList();
    }

    @Override
    public void onPullDistance(int distance) {

    }

    @Override
    public void onPullEnable(boolean enable) {

    }
}