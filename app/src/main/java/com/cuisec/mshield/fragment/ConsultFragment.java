package com.cuisec.mshield.fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cuca.security.PKIUtil;
import com.cuca.security.bean.CertInfo;
import com.cuisec.mshield.R;
import com.cuisec.mshield.SeachActivity;
import com.cuisec.mshield.activity.DetailsActivity;
import com.cuisec.mshield.bean.CitrsBean;
import com.cuisec.mshield.bean.IpassOrderBean;
import com.cuisec.mshield.bean.IpassTheadNoticeBean;
import com.cuisec.mshield.bean.ParameterBean;
import com.cuisec.mshield.bean.PushBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.design.NetTrustManager;
import com.cuisec.mshield.design.WrapContentLinearLayoutManager;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.AddressPopupWindow;
import com.cuisec.mshield.widget.LoadDialog;
import com.cuisec.mshield.widget.MenuDialog;
import com.cuisec.mshield.widget.MessageSourcePopupwindow;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
public class ConsultFragment extends Fragment {
    private static ConsultFragment sConsultFragment;
    @BindView(R.id.tv_located_city)
    TextView mTvLocatedCity;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.rl_seach)
    RelativeLayout mRlSeach;
    @BindView(R.id.tv_located_web)
    TextView mTvLocatedWeb;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;
    @BindView(R.id.image_city)
    ImageView mImageCity;
    @BindView(R.id.image_web)
    ImageView mImageWeb;
    @BindView(R.id.search_iamge)
    ImageView mSearchIamge;
    @BindView(R.id.activity_nac)
    LinearLayout mActivityNac;
    @BindView(R.id.btn_link)
    TextView mBtnLink;
    @BindView(R.id.btn_huadian)
    TextView mBtnHuadian;
    @BindView(R.id.image_null)
    ImageView mImageNull;
    @BindView(R.id.tv_located_details)
    TextView mTvLocatedDetails;
    @BindView(R.id.image_details)
    ImageView mImageDetails;
    private CommonAdapter mAdapter;
    private ArrayList<CitrsBean.DomainListBean> mDomainListBeans;
    private AddressPopupWindow mAddressPopupWindow;
    private int page = 1;
    private LoadDialog mLoadDlg = null;
    private String mSid = "";
    private String mSaveCity;
    private String mSourceName;
    private MessageSourcePopupwindow mMessagePop;
    private CitrsBean mCitrsBean;
    private String mId;
    String[] list = new String[]{"全部","招标公告","中标公告","采购"};
    String bidType;
    Thread mThread = null;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private static final String CHANNEL = "update";
    private Intent updateIntent;
    private PendingIntent updatePendingIntent;
    private ArrayList<PushBean.DataBean> mDataBeans;
    //调用定时刷新函数
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
                setNotification();
        }
    };
    private static PushBean mPushBean;
    private Date mParse;
    private volatile boolean mIsDestroy = false;
    private int countNum = 0;
    public PushBean mPushBean1;
    private static int mChaZhi;
    private PushBean mPushBean2;
    private String mNextUpdateDate;
    private String mSavePhone;
    private String mSaveBindPhone;
    private String mSaveType;
    private int mCount;
    private Thread mIpassThread;
    private IpassTheadNoticeBean mIpassTheadNoticeBean;
    private IpassTheadNoticeBean mCheckBind;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_consult, null);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this, inflate);
        mSaveCity = (String) SPUtils.get(getActivity(), Constants.GET_CITY, "全国");
        mSourceName = (String) SPUtils.get(getActivity(), Constants.SOURCE_INFO, "全部");
        mSid = (String) SPUtils.get(getActivity(), Constants.GET_CITY_SID, "");
        mId = (String) SPUtils.get(getActivity(), "data", "ALL");
        bidType = (String) SPUtils.get(getActivity(),"bidtype","全部");
        mSaveBindPhone = (String) SPUtils.get(getActivity(), Constants.SAVE_PASS_BIND_PHONE, "");
        mSaveType = (String) SPUtils.get(getActivity(), Constants.SAVE_PASS_BIND_TYPE, "");
        mSavePhone = (String) SPUtils.get(getActivity(), Constants.SAVE_USER_PHONE, "");
        mTvLocatedWeb.setText(mSourceName);
        mTvLocatedCity.setText(mSaveCity);
        mTvLocatedDetails.setText(bidType);
        initView(inflate);
        initData3();
        checkBindIpass();
        initCount();
        mThread = new Thread(mRunnable);
        mThread.start();
        if (!(mIpassTheadNoticeBean != null && mIpassTheadNoticeBean.getMsg().equals("未绑定用户！"))){
            startThread();
        }else if (!(mCheckBind != null && mCheckBind.getMsg().equals("未绑定用户！"))){
            startThreadLT();
        }
        initData();
        return inflate;
    }
    public void checkBindIpass(){
        String indata = Constants.APP_ID + "#" + ("".equals(mSaveBindPhone) ? mSavePhone : mSaveBindPhone) + "#" + ("".equals(mSaveType) ? "HD" : mSaveType);
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        final IpassOrderBean ipassOrderBean = new IpassOrderBean();
        ipassOrderBean.setAppid(Constants.APP_ID_IPASS);
        if (mSaveType != null && !("".equals(mSaveType))) {
            ipassOrderBean.setChannel(mSaveType);
        } else ipassOrderBean.setChannel("LT");
        if (mSaveBindPhone != null && !("".equals(mSaveBindPhone))) {
            ipassOrderBean.setPhone(mSaveBindPhone);
        } else ipassOrderBean.setPhone(mSavePhone);
        ipassOrderBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassOrderBean);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url+"ipassBus/ipassNoticeInfo/count?pageNum=1&pageSize=1")
                .post(requestBody)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response)  {
                try {
                    String string = response.body().string();
                    L.i(string);
                    Gson gson1 = new Gson();
                    mCheckBind = gson1.fromJson(string, IpassTheadNoticeBean.class);
                    if (mCheckBind.getData() != null && mCheckBind.getMsg().equals("未绑定用户！")){
                        return;
                    }
                    /*if (mIpassTheadNoticeBean != null && mIpassTheadNoticeBean.getMsg().equals("未绑定用户！")){
                        return;
                    }*/
                    if (mCheckBind.getData() != null){
                        mCount = mCheckBind.getData().getCount();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //检查用户是否有新的ipass订单
    private void initCount() {
        String indata = Constants.APP_ID + "#" + ("".equals(mSaveBindPhone) ? mSavePhone : mSaveBindPhone) + "#" + ("".equals(mSaveType) ? "HD" : mSaveType);
        final String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        final IpassOrderBean ipassOrderBean = new IpassOrderBean();
        ipassOrderBean.setAppid(Constants.APP_ID_IPASS);
        if (mSaveType != null && !("".equals(mSaveType))) {
            ipassOrderBean.setChannel(mSaveType);
        } else ipassOrderBean.setChannel("HD");
        if (mSaveBindPhone != null && !("".equals(mSaveBindPhone))) {
            ipassOrderBean.setPhone(mSaveBindPhone);
        } else ipassOrderBean.setPhone(mSavePhone);
        ipassOrderBean.setSign(sign);
        final Gson gson = new Gson();
        String json = gson.toJson(ipassOrderBean);
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        final Request request = new Request.Builder()
                .url(Config.https_base_service_url+"ipassBus/ipassNoticeInfo/count?pageNum=1&pageSize=1")
                .post(requestBody)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response)  {
                try {
                    String string = response.body().string();
                    Gson gson1 = new Gson();
                    mIpassTheadNoticeBean = gson1.fromJson(string, IpassTheadNoticeBean.class);
                        if (mIpassTheadNoticeBean.getData() != null && mIpassTheadNoticeBean.getMsg().equals("未绑定用户！")){
                            checkBindIpass();
                            if (mIpassTheadNoticeBean.getData() != null && mIpassTheadNoticeBean.getMsg().equals("未绑定用户！")){
                                return;
                            }
                        }
                        if (mIpassTheadNoticeBean.getData() != null){
                            mCount = mIpassTheadNoticeBean.getData().getCount();
                        }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public  void startThreadLT(){
        mThread = new Thread(mRunnableLT);
        mThread.start();
    }
    public void startThread() {
        mThread = new Thread(mIpassRunnable);
        mThread.start();
    }
    private Runnable mRunnableLT = new Runnable() {
        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(60 * 1000);
                    if (mIsDestroy){
                        mIpassThread.interrupt();
                        break;
                    }
                    checkBindIpass();
                    if (mCheckBind != null && mCheckBind.getMsg().equals("未绑定用户！")){
                        break;
                    }
                    if (mCheckBind != null) {
                        if (mCheckBind.getData() != null && mCheckBind.getData().getCount() != 0) {
                            EventBus.getDefault().post(new ReturnPayResult(mCheckBind.getData().getCount() + "", 3));
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    };
    private Runnable mIpassRunnable = new Runnable() {

        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(60 * 1000);
                    if (mIsDestroy){
                        mIpassThread.interrupt();
                        break;
                    }
                    initCount();
                    if (mIpassTheadNoticeBean != null && mIpassTheadNoticeBean.getMsg().equals("未绑定用户！")){
                        break;
                    }

                    if (mIpassTheadNoticeBean != null) {
                        if (mIpassTheadNoticeBean.getData() != null && mIpassTheadNoticeBean.getData().getCount() != 0) {
                            EventBus.getDefault().post(new ReturnPayResult(mIpassTheadNoticeBean.getData().getCount() + "", 3));
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    };
    private void initData3() {
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(null
                , "");
        final Request request = new Request.Builder()
                .post(requestBody)
                //.url(Config.https_base_service_url+"bidinfo/checkcount")
                .url(Config.https_base_service_url+Config.query_checkcount)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Gson gson = new Gson();
                mPushBean = gson.fromJson(string, PushBean.class);
            }
        });
    }
    @Subscribe
    public void onEvent(ReturnPayResult result) {
        //接收以及处理数据
        if (result.getFlag() == 1){
            mTvLocatedCity.setText(result.getStatus());
            mDomainListBeans.clear();
            initData();
        }else mTvLocatedCity.setText("全部");

    }
    /**
     * 创建通知栏
     */
    private void setNotification() {
        if (mNotificationManager == null )
            mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    getContext().getApplicationContext().getPackageName(),
                    "通知",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder = new NotificationCompat.Builder(getActivity(),CHANNEL);
        mBuilder
                .setSmallIcon(R.drawable.app_lable2)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setOngoing(true)
                .setAutoCancel(true)
                .setChannelId(getActivity().getApplication().getPackageName())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setTicker("您有一条新消息!")
                .setContentTitle("您有"+mChaZhi+"条新的招标讯息")
                //.setContentText(mPushBean.getData().getNextUpdateDate().substring(0, 11)+"点击查看更多详情信息")
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        updateIntent = new Intent(getActivity(), SeachActivity.class);
        //updateIntent.putExtra("title",mCitrsBean.getDomain_list().get(0).getTitle());
        updatePendingIntent = PendingIntent.getActivity(getActivity(),0, updateIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(updatePendingIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }

    //实现定时刷新
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while(true){
                try {
                    Thread.sleep(60 * 1000);
                    if (mIsDestroy){
                        mThread.interrupt();
                        break;
                    }
                    //线程休眠十分钟检查一次更新
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date curdate =new Date();
                    try {
                        mParse = dateFormat.parse(mPushBean.getData().getNextUpdateDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    L.i("当前时间"+curdate);
                    L.i("获取时间"+mParse);
                    String format = dateFormat.format(curdate);
                    String format2 = null;
                    if (mParse != null){
                        format2 = dateFormat.format(mParse);
                    }
                if (mParse != null && curdate.getTime() < mParse.getTime()){
                    L.i("获取时间"+(curdate.getTime() < mParse.getTime()));
                    continue;

                }
                    countNum = (int) SPUtils.get(getActivity(),"num",countNum);
                    initData3();
                    int count1 = mPushBean.getData().getCount();
                    mNextUpdateDate = mPushBean.getData().getNextUpdateDate();
                    SPUtils.put(getActivity(),"num",count1);
                    L.i("count1 "+countNum + ""+"mNextUpdateDate"+mNextUpdateDate);
                    if (countNum >= count1 /*|| countNum == 0*/){
                         continue;
                    }
                    mChaZhi = count1 - countNum;
                    EventBus.getDefault().post(new ReturnPayResult(mChaZhi+"",2));
                    mHandler.sendMessage(mHandler.obtainMessage());
                    //sleep2秒，可根据需求更换为响应的时间
                    //SystemClock.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private void selectCountiess() {
        mAdapter = new CommonAdapter<CitrsBean.DomainListBean>(getActivity(), R.layout.item_address_textview, mDomainListBeans) {
            @Override
            protected void convert(ViewHolder holder, CitrsBean.DomainListBean domainListBean, int position) {
                holder.setText(R.id.tv_name, domainListBean.getTitle());
                //holder.setText(R.id.tv1, domainListBean.getSource());
                holder.setText(R.id.tv3, domainListBean.getReleasetime().substring(0, 11));
                if (domainListBean.getBidType() != null){
                    holder.setText(R.id.call_details,domainListBean.getBidType());
                }
            }
        };
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("data", mDomainListBeans.get(position));
                //intent.putParcelableArrayListExtra("data",mDomainListBeans);
                getActivity().startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initData() {
        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(getContext(), R.style.CustomDialog);
            mLoadDlg.show();
        }
        String phone = (String) SPUtils.get(getActivity(), Constants.SAVE_USER_PHONE, "");
//        String indata=  Base64Utils.encodeToString(Constants.APP_ID + "#" + phone);
        String indata=Constants.APP_ID + "#" + phone;
        String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, indata);
        ParameterBean parameterBean = new ParameterBean();
        String cert= "MIIDAzCCAeugAwIBAgIOA/5febRcb/PCj3IWyQIwDQYJKoZIhvcNAQELBQAwITELMAkGA1UEBhMCQ04xEjAQBgNVBAMMCUFQUElOU0lERTAeFw0xNzA1MTgwMjEyMDZaFw0yNTEyMzExNjAwMDBaMCExCzAJBgNVBAYTAkNOMRIwEAYDVQQDDAlBUFBJTlNJREUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCILj7EvlvL81LnteSCUUhx5X8HKjlkI8iFJrAUpYXtfO7RRRry7svxvzS1d7UqXFCUCg8WtJKMCzTGtqWA9B4AzUt8d2SdptNvt/CfJO/rLBUkNQRrNzKRT4NRV+vkIHNdmY2aAw4yqpdtENsT7alKuV1Pd+072Mp09Cnp3Po8vgR4+/7/wOvR+t8sGi9vQgU1e3ANN2bnvbg5xDefJWYd1wEmWnR3uBRGx7fMIkYPtZooZP4cQ3OuS+KfVSujKRF61q7prkIRaALQqm+8WjYkhVP1u3xJh8H27tr9XBpHMnz/8dEUfWB6GduNAXfLFctYy4Tg6Ip3uaszQ6rZ09TRAgMBAAGjOTA3MAkGA1UdEwQCMAAwCwYDVR0PBAQDAgTQMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjANBgkqhkiG9w0BAQsFAAOCAQEAZU+UGkqGuhE2tTtNZ0KNOuQs1qHJD2aWHhQjx/M2GUEKssIBbyOK1YBml5RiM78lLuU5UTy4UK1oot6afP8qUZ8IaSn6P7mcQs2dZRIBbmbK3jXmgFM5DBvXpTqiOaVPqmzXnV0XPsiehJ9/sKLS5XZNj+yQ4UDYkoJa/HNbSO6W5wVRK5B0m9UbbStLTyjWC8Vqz2yZs7N2zVPoNqWFtFEkYrbED1OhPgZFyJ8LI/vQGt3u/jD2LZb5z9WdKDzAIiBkmY7dSCtnNxg7ROBUMecR/BM6YGOXIrzpOG9PgT+UCsFe6GotFOSfzg8u9xPVeE4zM9pCRD8qiR8AmlAh7A==";
        //验证签名
        CertInfo certinfo = null;
        try {
            certinfo = PKIUtil.getCertInfo(cert.getBytes());
//            PKIUtil.getCertInfo(cert.getBytes());
            //boolean ret= PKIUtil.verify(SecurityUtil.SIGN_ALGORITHMS, certinfo.getPublicKey(), indata.getBytes(), Base64.(sign.getBytes(),0), "SOFT", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            parameterBean.setSign(sign);
            parameterBean.setAppid(Constants.APP_ID);
            parameterBean.setPhone(phone);
            parameterBean.setProvince("ALL");
            /*if (mTvLocatedCity.getText().toString().equals("全国")){
            }else {
                parameterBean.setProvince(mSid);
                T.showShort(mSid);
            }*/
            if (mTvLocatedDetails.getText().toString().equals(list[1])){
                parameterBean.setBidType("1");
            }else if (mTvLocatedDetails.getText().toString().equals(list[2])){
                parameterBean.setBidType("2");
            }else if (mTvLocatedDetails.getText().toString().equals(list[3])){
                parameterBean.setBidType("3");
            }else {
                parameterBean.setBidType("ALL");
            }
            if (mTvLocatedWeb.getText().toString().equals("全部") /*|| mTvLocatedWeb.getText().toString().equals("中国华电电子商务平台")*/) {
                parameterBean.setSourceId("ALL");
            } else if (mTvLocatedWeb.getText().toString().equals("中国联通采购")) {
                parameterBean.setSourceId("1");
            } else if (mTvLocatedWeb.getText().toString().equals("华电集团采购")) {
                parameterBean.setSourceId("2");
            } else parameterBean.setSourceId(mId);
                parameterBean.setCity("");
            if (mTvLocatedCity.getText().equals("全国")) {
                parameterBean.setTitle("");
            } else parameterBean.setTitle(mTvLocatedCity.getText().toString());
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
                    final String result = response.body().string();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson json = new Gson();
                            mCitrsBean = json.fromJson(result, CitrsBean.class);
                            if (mCitrsBean.getDomain_list() != null && mCitrsBean.getDomain_list().size() > 0) {
                                mRefreshLayout.setVisibility(View.VISIBLE);
                                mImageNull.setVisibility(View.GONE);
                                mDomainListBeans.addAll(mCitrsBean.getDomain_list());
                            } else {
                                if (mDomainListBeans != null && mDomainListBeans.size() > 0) {
                                    mRefreshLayout.finishLoadMore(true);
                                } else {
                                    mRefreshLayout.setVisibility(View.GONE);
                                    mImageNull.setVisibility(View.VISIBLE);
                                }
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

    public static ConsultFragment getInstance() {
        if (sConsultFragment == null) {
            sConsultFragment = new ConsultFragment();
        }
        return sConsultFragment;
    }
    public void initRecycleView(RecyclerView recyclerView, RefreshLayout refreshLayout) {
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity()));
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
        if (mDomainListBeans != null && mDomainListBeans.size() > 0) {
            initData();
        } else {
            mRefreshLayout.finishLoadMore(true);
        }
        mRefreshLayout.finishLoadMore();
    }
    private void refresh() {
        mDomainListBeans.clear();
        page = 1;
        initData();
        mRefreshLayout.finishRefresh();
        mAdapter.notifyDataSetChanged();
    }
    private void initView(View inflate) {
        mRefreshLayout.setVisibility(View.VISIBLE);
        mDomainListBeans = new ArrayList<>();
        if (mDataBeans == null){
            mDataBeans = new ArrayList<>();
        }
      /*  //从asset 读取字体
        //得到AssetManager
        AssetManager mgr = getContext().getAssets();
        //根据路径得到Typeface
        Typeface tf = Typeface.createFromAsset(mgr, "fonts/SourceHanSansCN-Medium.otf");
        mTvHuangdian.setTypeface(tf);*/
        //mTvLocatedCity.setText(mCity);
        // mTvHuangdian.getPaint().setFakeBoldText(true);//加粗
        //mTvLink.getPaint().setFakeBoldText(true);
        // mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        initRecycleView(mRecyclerView, mRefreshLayout);
        selectCountiess();

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @OnClick({R.id.tv_located_city, R.id.rl_seach, R.id.tv_located_web, R.id.image_city, R.id.image_web, R.id.btn_link, R.id.btn_huadian, R.id.recyclerView, R.id.refreshLayout, R.id.activity_nac, R.id.tv_located_details, R.id.image_details, R.id.image_null})
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.tv_located_city:
                mAddressPopupWindow = new AddressPopupWindow(getActivity());
                mAddressPopupWindow.showAtLocation(mTvLocatedCity, Gravity.BOTTOM, 0, 0);
                mAddressPopupWindow.setCallBack(new AddressPopupWindow.IcallBack() {
                    @Override
                    public void callBack(final String data) {
                        mDomainListBeans.clear();
                        mTvLocatedCity.setText(data);
                        SPUtils.put(getActivity(), Constants.GET_CITY, data);
                        initData();
                    }
                    @Override
                    public void callNationwide(final String data, final String sid) {
                        mDomainListBeans.clear();
                        mAdapter.notifyDataSetChanged();
                        //mAdapter.notifyDataSetChanged();
                        mTvLocatedCity.setText(data);
                        mSid = sid;
                        initData();
                        SPUtils.put(getActivity(), Constants.GET_CITY, data);
                        SPUtils.put(getActivity(), Constants.GET_CITY_SID, sid);
                    }
                });
                break;
            case R.id.rl_seach:
                Intent intent = new Intent(getActivity(), SeachActivity.class);
                getActivity().startActivity(intent);
                break;
            case R.id.tv_located_web:
                mMessagePop = new MessageSourcePopupwindow(getContext());
                mMessagePop.showAsDropDown(mTvLocatedWeb);
                mMessagePop.setCallBack(new MessageSourcePopupwindow.IcallBack() {
                    @Override
                    public void callBack(String address, String id) {
                        mDomainListBeans.clear();
                        mAdapter.notifyDataSetChanged();
                        mTvLocatedWeb.setText(address);
                        mId = id;
                        SPUtils.put(getActivity(), Constants.SOURCE_INFO, address);
                        SPUtils.put(getActivity(), "data", id);
                        initData();
                    }
                    @Override
                    public void callALL(String data) {
                        mDomainListBeans.clear();
                        mAdapter.notifyDataSetChanged();
                        mTvLocatedWeb.setText(data);
                        SPUtils.put(getActivity(), Constants.SOURCE_INFO, data);
                        initData();
                    }
                });
                break;
            case R.id.image_city:
                AddressPopupWindow addressPop = new AddressPopupWindow(getActivity());
                addressPop.showAtLocation(mImageCity, Gravity.BOTTOM, 0, 0);
                addressPop.setCallBack(new AddressPopupWindow.IcallBack() {
                    @Override
                    public void callBack(final String data) {
                        mDomainListBeans.clear();
                        mAdapter.notifyDataSetChanged();
                        mTvLocatedCity.setText(data);
                        initData();
                        SPUtils.put(getActivity(), Constants.GET_CITY, data);
                    }

                    @Override
                    public void callNationwide(final String data, final String sid) {
                        mDomainListBeans.clear();
                        mAdapter.notifyDataSetChanged();
                        //mAdapter.notifyDataSetChanged();
                        mSid = sid;
                        mTvLocatedCity.setText(data);
                        initData();
                        SPUtils.put(getActivity(), Constants.GET_CITY, data);
                        SPUtils.put(getActivity(), Constants.GET_CITY_SID, sid);
                    }
                });
                break;
            case R.id.image_web:
                MessageSourcePopupwindow sourcePopupwindow = new MessageSourcePopupwindow(getActivity());
                sourcePopupwindow.showAsDropDown(mImageWeb);
                sourcePopupwindow.setCallBack(new MessageSourcePopupwindow.IcallBack() {
                    @Override
                    public void callBack(String address, String id) {
                        mDomainListBeans.clear();
                        mAdapter.notifyDataSetChanged();
                        mTvLocatedWeb.setText(address);
                        mId = id;
                        SPUtils.put(getActivity(), Constants.SOURCE_INFO, address);
                        SPUtils.put(getActivity(), "data", id);
                        initData();
                    }
                    @Override
                    public void callALL(String data) {
                        mDomainListBeans.clear();
                        mAdapter.notifyDataSetChanged();
                        mTvLocatedWeb.setText(data);
                        SPUtils.put(getActivity(), Constants.SOURCE_INFO, data);
                        initData();
                    }
                });
                break;
            case R.id.btn_link:
                mDomainListBeans.clear();
                mAdapter.notifyDataSetChanged();
                mTvLocatedWeb.setText(mBtnLink.getText().toString());
                initData();
                break;
            case R.id.btn_huadian:
                mDomainListBeans.clear();
                mAdapter.notifyDataSetChanged();
                mTvLocatedWeb.setText(mBtnHuadian.getText().toString());
                initData();
                break;
            case R.id.tv_located_details:
                /*CallTypePopupwindow callTypePopupwindow = new CallTypePopupwindow(getActivity());
                callTypePopupwindow.showAsDropDown(mTvLocatedDetails);*/
                new MenuDialog(getActivity(),getString(R.string.app_cancel),list,true).setOnMyPopClickListener(new MenuDialog.MenuClickListener() {
                    @Override
                    public void onItemClick(int index, String content) {
                        mDomainListBeans.clear();
                        mTvLocatedDetails.setText(content);
                        bidType = content;
                        SPUtils.put(getActivity(),"bidtype",content);
                        initData();
                        //T.showShort(content);
                    }
                    @Override
                    public void onCancelClick(String content) {

                    }
                });
                break;
            case R.id.image_details:
                /*CallTypePopupwindow callTypePopupwindow1 = new CallTypePopupwindow(getActivity());
                callTypePopupwindow1.showAsDropDown(mTvLocatedDetails);*/
              new MenuDialog(getActivity(),getString(R.string.app_cancel),list,true).setOnMyPopClickListener(new MenuDialog.MenuClickListener() {
                    @Override
                    public void onItemClick(int index, String content) {
                        mDomainListBeans.clear();
                        mTvLocatedDetails.setText(content);
                        bidType = content;
                        SPUtils.put(getActivity(),"bidtype",content);
                        initData();
                    }
                    @Override
                    public void onCancelClick(String content) {
                    }
                });
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
       /* String saveCity = (String) SPUtils.get(getActivity(), Constants.GET_CITY, mTvLocatedCity.getText().toString());
        mTvLocatedCity.setText(saveCity);*/
        //拿到InputMethodManager
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        //如果window上view获取焦点 && view不为空
        if (imm.isActive() && getActivity().getCurrentFocus() != null) {
            //拿到view的token 不为空
            if (getActivity().getCurrentFocus().getWindowToken() != null) {
                //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsDestroy = true;
        if (mLoadDlg != null && mLoadDlg.isShowing()) {
            mLoadDlg = null;
            mLoadDlg.dismiss();
        }
        if (mAddressPopupWindow != null){
            mAddressPopupWindow = null;
            mAddressPopupWindow.dismiss();
        }
        EventBus.getDefault().unregister(this);
        if (mThread != null){
            mThread = null;
        }
        if (mIpassThread != null){
            mIpassThread = null;
        }
        mHandler.removeCallbacksAndMessages(null);
    }

}
