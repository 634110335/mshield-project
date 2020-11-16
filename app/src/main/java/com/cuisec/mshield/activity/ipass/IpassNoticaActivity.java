package com.cuisec.mshield.activity.ipass;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.bean.IpassNoticeInfo;
import com.cuisec.mshield.bean.IpassOrderBean;
import com.cuisec.mshield.bean.IpassReadeaInfo;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.design.WrapContentLinearLayoutManager;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.widget.AlertDialog;
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
import static com.scwang.smartrefresh.layout.util.DensityUtil.px2dp;
public class IpassNoticaActivity extends BaseActivity {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    private ArrayList<IpassNoticeInfo.DataBean.ListBean> mDataBeans;
    private int pageNum = 1;
    private String mSavePhone;
    private String mSaveBindPhone;
    private String mSaveType;
    private CommonAdapter mAdapter;
    private LoadDialog mLoadDlg = null;
    private IpassNoticeInfo mListBean;
    private IpassNoticeInfo mInitListBean;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_ipass_notica);
        ButterKnife.bind(this);
        mSaveBindPhone = (String) SPUtils.get(this, Constants.SAVE_PASS_BIND_PHONE, "");
        mSaveType = (String) SPUtils.get(this, Constants.SAVE_PASS_BIND_TYPE, "");
        mSavePhone = (String) SPUtils.get(this, Constants.SAVE_USER_PHONE, "");
        L.i(mSaveType + "  " + mSaveBindPhone);
    }
    @Override
    protected void initializeViews() {
        showTitle("iPASS列表信息");
    }
    @Override
    protected void initializeData() {
        if (mDataBeans == null){
            mDataBeans = new ArrayList<>();
        }
        initRecycleView(mRecyclerView,mRefreshLayout);
        initData();
        selectCountiess();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void initRecycleView(RecyclerView recyclerView, RefreshLayout refreshLayout) {
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));
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
        pageNum++;
        if (mListBean != null){
            checkBindIpass();
        }else if (mInitListBean != null){
            initData();
        }
        mRefreshLayout.finishLoadMore();
    }
    private void refresh() {
        mDataBeans.clear();
        pageNum = 1;
        if (mListBean != null ){
            checkBindIpass();
        }else if (mInitListBean != null ){
            initData();
        }
        mRefreshLayout.finishRefresh();
    }
    public void checkBindIpass(){
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(IpassNoticaActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        String indata = Constants.APP_ID + "#" + ("".equals(mSaveBindPhone) ? mSavePhone : mSaveBindPhone) + "#" + ("".equals(mSaveType) ? "HD" : mSaveType);
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        final IpassOrderBean ipassOrderBean = new IpassOrderBean();
        ipassOrderBean.setAppid(Constants.APP_ID_IPASS);
        if (mSaveType != null && !("".equals(mSaveType))) {
            ipassOrderBean.setChannel(mSaveType);
        } else ipassOrderBean.setChannel("LT");
        if (mSaveBindPhone != null && !("".equals(mSaveBindPhone))) {
            ipassOrderBean.setPhone(mSavePhone);
        } else ipassOrderBean.setPhone(mSavePhone);
        ipassOrderBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassOrderBean);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url+"ipassBus/ipassNoticeInfo/list?pageNum="+pageNum+"&pageSize=10")
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
                    L.i(string);
                    mListBean = gson.fromJson(string, IpassNoticeInfo.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mListBean != null && mListBean.getMsg().equals("未绑定用户！")){
                                bindIpassPop();
                                return;
                            }
                            if (mListBean.getData().getList() == null){
                                nullMsgPop();
                                return;
                            }
                            if (mListBean.getData().getList() != null && mListBean.getData().getList().size() > 0) {
                                mDataBeans.addAll(mListBean.getData().getList());
                            }else  mRefreshLayout.finishLoadMore(true);
                            mAdapter.notifyDataSetChanged();
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
    }
    private void initData() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(IpassNoticaActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        String indata = Constants.APP_ID + "#" + ("".equals(mSaveBindPhone) ? mSavePhone : mSaveBindPhone) + "#" + ("".equals(mSaveType) ? "HD" : mSaveType);
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        final IpassOrderBean ipassOrderBean = new IpassOrderBean();
        ipassOrderBean.setAppid(Constants.APP_ID_IPASS);
        if (mSaveType != null && !("".equals(mSaveType))) {
            ipassOrderBean.setChannel(mSaveType);
        } else ipassOrderBean.setChannel("HD");
        if (mSaveBindPhone != null && !("".equals(mSaveBindPhone))) {
            ipassOrderBean.setPhone(mSavePhone);
        } else ipassOrderBean.setPhone(mSavePhone);
        ipassOrderBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassOrderBean);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url+"ipassBus/ipassNoticeInfo/list?pageNum="+pageNum+"&pageSize=10")
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
                    L.i(string);
                    mInitListBean = gson.fromJson(string, IpassNoticeInfo.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mInitListBean != null && mInitListBean.getMsg().equals("未绑定用户！")){
                                checkBindIpass();
                               /* if (mListBean != null && mListBean.getMsg().equals("未绑定用户！")){
                                    bindIpassPop();
                                }*/
                                return;
                            }
                            if (mInitListBean.getData().getList()==null){
                                nullMsgPop();
                                return;
                            }
                            if (mInitListBean.getData().getList() != null && mInitListBean.getData().getList().size() > 0) {
                                mDataBeans.addAll(mInitListBean.getData().getList());
                            }else  mRefreshLayout.finishLoadMore(true);
                            mAdapter.notifyDataSetChanged();
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
    }
    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }
    private void selectCountiess() {
        //适配渲染ipass通知列表
        mAdapter = new CommonAdapter<IpassNoticeInfo.DataBean.ListBean>(IpassNoticaActivity.this, R.layout.item_ipass_notica, mDataBeans) {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void convert(ViewHolder holder, IpassNoticeInfo.DataBean.ListBean listBean, int position) {
                holder.setText(R.id.ipass_content, mDataBeans.get(position).getContent());
                String readed = mDataBeans.get(position).getReaded();
                if (readed.equals("2")) {
                    holder.setImageResource(R.id.ipass_read_image, R.drawable.ipass_not_read);
                } else {
                    holder.setImageResource(R.id.ipass_read_image, R.drawable.ipass_read);
                }
            }
        };
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                initReaded(mDataBeans.get(position).getId() + "");
            }
            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }
    private void initReaded(String readed) {
        //解析ipass通知代码
        if (readed == null) {
            return;
        }
        String indata = Constants.APP_ID + "#" + ("".equals(mSaveBindPhone) ? mSavePhone : mSaveBindPhone) + "#" + readed;
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        IpassReadeaInfo ipassReadeaInfo = new IpassReadeaInfo();
        ipassReadeaInfo.setAppid(Constants.APP_ID_IPASS);
        ipassReadeaInfo.setNoticeid(readed);
        if (mSaveBindPhone != null && !("".equals(mSaveBindPhone))) {
            ipassReadeaInfo.setPhone(mSaveBindPhone);
        } else ipassReadeaInfo.setPhone(mSavePhone);
        ipassReadeaInfo.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassReadeaInfo);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url+Config.ipass_readed)
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
                    startActivity(new Intent(IpassNoticaActivity.this, IPassServiceActivity.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void bindIpassPop() {
        new AlertDialog(IpassNoticaActivity.this)
                .builder()
                .setTitle("未绑定iPASS")
                .setMessage("请绑定iPASS服务后查询")
                .setNegativeButton(getString(R.string.ipass_bind), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(IpassNoticaActivity.this, BindiPASSActivity.class));
                    }
                })
                .setPositiveButton(getString(R.string.ipass_back), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }).show();
    }
    public void nullMsgPop() {
        new AlertDialog(IpassNoticaActivity.this)
                .builder()
                .setTitle("提示")
                .setMessage("暂无iPASS通知")
                .setPositiveButton(getString(R.string.ipass_back), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }).show();
    }
}
