package com.cuisec.mshield;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cuisec.mshield.activity.DetailsActivity;
import com.cuisec.mshield.bean.CitrsBean;
import com.cuisec.mshield.bean.ParameterBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.db.DbDao;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.seach.KylinSearchView;
import com.cuisec.mshield.seach.OnSearchListener;
import com.cuisec.mshield.searchhistory.FlowLayout;
import com.cuisec.mshield.searchhistory.TagAdapter;
import com.cuisec.mshield.searchhistory.TagFlowLayout;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.StatusBarUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.LoadDialog;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.IOException;
import java.lang.ref.WeakReference;
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

import static com.scwang.smartrefresh.layout.util.DensityUtil.px2dp;

public class SeachActivity extends AppCompatActivity implements OnSearchListener {


    private static final String TAG = "SeachActivity";
    @BindView(R.id.tv_call)
    TextView mTvCall;
    @BindView(R.id.ll_back)
    LinearLayout mLlBack;
    @BindView(R.id.sv_default)
    KylinSearchView mSvDefault;
    @BindView(R.id.rl_search)
    RelativeLayout mRlSearch;
    @BindView(R.id.ll_search)
    LinearLayout mLlSearch;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.blank_page)
    ImageView mBlankPage;
    @BindView(R.id.tag_flowlayout)
    TagFlowLayout mTagFlowlayout;
    @BindView(R.id.tv_clear)
    TextView mTvClear;
    @BindView(R.id.rl_history)
    RelativeLayout mRlHistory;
    private LayoutInflater mInflater;
    //流式布局的子布局
    private TextView tv;
    private DbDao mDbDao;
    private CommonAdapter mAdapter;
    private int page = 1;
    private LoadDialog mLoadDlg = null;
    private ArrayList<CitrsBean.DomainListBean> mDomainListBeans = null;
    private String seachContent = null;
    private ArrayList<String> mStrings = null;
    private CitrsBean mCitrsBean;

    private class MyHandler extends Handler {
        private WeakReference<SeachActivity> mActivityWeakReference;

        public MyHandler(SeachActivity activity) {
            mActivityWeakReference = new WeakReference<SeachActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mTagFlowlayout.setAdapter(new TagAdapter<String>(mDbDao.queryData("")) {
                    @Override
                    public View getView(FlowLayout parent, int position, String s) {
                        tv = (TextView) mInflater.inflate(R.layout.tv,
                                mTagFlowlayout, false);
                        tv.setText(s);
                        return tv;
                    }
                });
            } else if (msg.what == 2) {
                mCitrsBean = (CitrsBean) msg.obj;
                if (mCitrsBean.getDomain_list() != null && mCitrsBean.getDomain_list().size() > 0) {
                    mDomainListBeans.addAll(mCitrsBean.getDomain_list());
                    mRefreshLayout.setVisibility(View.VISIBLE);
                    mBlankPage.setVisibility(View.GONE);
                } else {
                    if (mDomainListBeans != null && mDomainListBeans.size() > 0) {
                        mRefreshLayout.finishLoadMore(true);
                    } else {
                        mRefreshLayout.setVisibility(View.GONE);
                        mBlankPage.setVisibility(View.VISIBLE);
                    }
                }
            }
            mAdapter.notifyDataSetChanged();
            //mAdapter.notifyItemRangeRemoved(0, mDomainListBeans.size());
        }
    }

    private MyHandler mMyHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);
        initView();
        // 设置状态栏透明和文本颜色
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.StatusBarLightMode(this);
        initData();
    }

    private void initView() {
        if (mStrings == null) {
            mStrings = new ArrayList<>();
        }
        if (mDomainListBeans == null) {
            mDomainListBeans = new ArrayList<>();
        }
        initRecycleView(mRecyclerView, mRefreshLayout);
        mInflater = LayoutInflater.from(this);

        mSvDefault.setOnSearchListener(this);
        mDbDao = new DbDao(this);
        //FontUtil.replaceFont(mTvCall, "server_protocol.html");
        selectCountiess();
        mTagFlowlayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                List<String> strings = mDbDao.queryData("");
                mDomainListBeans.clear();
                seachContent = strings.get(position);
                initData();
                return true;
            }
        });
    }

    private void initData() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(SeachActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        String phone = (String) SPUtils.get(SeachActivity.this, Constants.SAVE_USER_PHONE, "13391826151");
        String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, Constants.APP_ID + "#" + phone);
        ParameterBean parameterBean = new ParameterBean();
        try {
            parameterBean.setSign(sign);
            parameterBean.setAppid(Constants.APP_ID);
            parameterBean.setPhone(phone);
            parameterBean.setProvince("ALL");
            parameterBean.setSourceId("ALL");
            parameterBean.setCity("");
            if (seachContent != null) {
                parameterBean.setTitle(seachContent);
            } else {
                parameterBean.setTitle("");
            }
            parameterBean.setBidType("ALL");
            //T.showShort(seachContent);
            parameterBean.setPageNum(Integer.parseInt(String.valueOf(page)));
        } catch (Exception e) {
            e.printStackTrace();
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
        }
        Gson gson = new Gson();
        String json = gson.toJson(parameterBean);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url + Config.query_list)
                .post(requestBody)
                .build();
        Call call = clientWithCache.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (mLoadDlg != null) {
                    mLoadDlg.dismiss();
                    mLoadDlg = null;
                }
                try {
                    Gson json = new Gson();
                    try {
                        CitrsBean citrsBean = json.fromJson(response.body().string(), CitrsBean.class);
                        Message message = new Message();
                        message.obj = citrsBean;
                        message.what = 2;
                        mMyHandler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.getMessage();
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

    public void initRecycleView(RecyclerView recyclerView, RefreshLayout refreshLayout) {
        recyclerView.setLayoutManager(new LinearLayoutManager(SeachActivity.this));
        if (refreshLayout != null) {
            refreshLayout.setHeaderHeight(px2dp(120));
            refreshLayout.setFooterHeight(px2dp(100));
            refreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                    refresh();

                }
            });
            refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                    loadMore();
                }
            });
        }
    }

    private void loadMore() {
        page++;
        initData();
        mRefreshLayout.finishLoadMore();
    }

    private void refresh() {
        mDomainListBeans.clear();
        page = 1;
        initData();
        mRefreshLayout.finishRefresh();
    }

    private void selectCountiess() {
        mAdapter = new CommonAdapter<CitrsBean.DomainListBean>(this, R.layout.item_address_textview, mDomainListBeans) {
            @Override
            protected void convert(ViewHolder holder, CitrsBean.DomainListBean domainListBean, int position) {
                holder.setText(R.id.tv_name, mDomainListBeans.get(position).getTitle());
                // holder.setText(R.id.tv1, mDomainListBeans.get(position).getSource());
                holder.setText(R.id.tv3, mDomainListBeans.get(position).getReleasetime().substring(0, 11));
                holder.setText(R.id.call_details, mDomainListBeans.get(position).getBidType());
            }
        };
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                Intent intent = new Intent(SeachActivity.this, DetailsActivity.class);
                intent.putExtra("data", mDomainListBeans.get(position));
                //intent.putParcelableArrayListExtra("data",mDomainListBeans);
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
    }

    @Override
    public void search(String content) {
        mDomainListBeans.clear();
        if ("".equals(content)) {
            T.showShort("搜索内容不能为空");
            return;
        }
        mRlHistory.setVisibility(View.GONE);
        mTagFlowlayout.setVisibility(View.GONE);
        seachContent = content;
        initData();
        boolean b = mDbDao.hasData(content);
        if (!b) {
            mDbDao.insertData(content);
            //seachContent = content;
            mMyHandler.sendEmptyMessageDelayed(1, 0);
        }
    }

    @OnClick({R.id.ll_back, R.id.tv_clear})
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.ll_back:
                finish();
                break;
            case R.id.tv_clear:
                mDbDao.deleteData();
                mTagFlowlayout.removeAllViews();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<String> strings = mDbDao.queryData("");
        if (strings.size() <= 0){
            mRlHistory.setVisibility(View.GONE);
            mTagFlowlayout.setVisibility(View.GONE);
        }
        mTagFlowlayout.setAdapter(new TagAdapter<String>(mDbDao.queryData("")) {
            @Override
            public View getView(FlowLayout parent, int position, String s) {
                tv = (TextView) mInflater.inflate(R.layout.tv,
                        mTagFlowlayout, false);
                tv.setText(s);
                return tv;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadDlg != null) {
            mLoadDlg = null;
            mLoadDlg.dismiss();
        }
    }
}
