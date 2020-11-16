package com.cuisec.mshield.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.cuisec.mshield.R;
import com.cuisec.mshield.bean.Citys;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.db.DbDao;
import com.cuisec.mshield.utils.GetJsonDataUtil;
import com.cuisec.mshield.utils.SPUtils;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.StatusBarUtil;
import com.cuisec.mshield.widget.LoadDialog;
import com.google.gson.Gson;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import q.rorbin.verticaltablayout.VerticalTabLayout;
import q.rorbin.verticaltablayout.widget.ITabView;
import q.rorbin.verticaltablayout.widget.QTabView;
import q.rorbin.verticaltablayout.widget.TabView;

public class LocationActivity extends AppCompatActivity {
    private static final String TAG = "LocationActivity";
    @BindView(R.id.recy_hot_city)
    RecyclerView mRecyHotCity;
    @BindView(R.id.recy_city)
    RecyclerView mRecyCity;
    @BindView(R.id.tab_layout)
    VerticalTabLayout mTabLayout;
    @BindView(R.id.tv_popup)
    TextView mTvPopup;
    @BindView(R.id.tv_whole)
    TextView mTvWhole;
    @BindView(R.id.click_located_city)
    TextView mClickLocatedCity;
    @BindView(R.id.tv_located_city)
    TextView mTvLocatedCity;
    @BindView(R.id.iamge_back)
    ImageView mIamgeBack;
    @BindView(R.id.character)
    TextView mCharacter;
    /**
     * 最终确认的地址提交参数
     */
    private String saveProvinceCode, saveCityCode, saveCountyCode, saveZipCode;
    private String saveProvinceName, saveCityName, saveCountyName;
    /**
     * 省截取2位代码
     */
    private String provinceChargeCode;
    /**
     * 市截取2位代码
     */
    private String citiesChargeCode;
    /**
     * 当前省下显示市
     */
    private List<Citys.CityBean> currentCitiesDatas1 = new ArrayList<>();
    /**
     * 当前市下显示县
     */
    private List<String> currentCountiesDatas = new ArrayList<>();
    private Citys mCitys;
    public LocationClient mLocationClient;//定位SDK的核心类
    public MyLocationListener mMyLocationListener;//定义监听类
    private String city = null;
    String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private LoadDialog mLoadDlg = null;
    private final int RC_LOCATION_CONTACTS_PERM = 124;
    private DbDao mDbDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);
        initView();
        initData();
        Log.i(TAG, "onReceiveLocation: "+city);
        String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, "100002"+"##"+"13391826151");
        try {
            URLEncoder.encode(sign, Config.UTF_8);
            Log.e(TAG, "onCreate: "+URLEncoder.encode(sign, Config.UTF_8) );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }



    private void initView() {
        // 设置状态栏透明和文本颜色
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.StatusBarLightMode(this);
        //添加最近使用的数据
        currentCountiesDatas.add((String) SPUtils.get(this,Constants.GET_CITY,""));
        String saveCityName = (String) SPUtils.get(this, Constants.GET_CITY, mClickLocatedCity.getText().toString());
        currentCountiesDatas.add(saveCityName);
        selectCountiess();
        mLocationClient = new LocationClient(this);
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        mRecyHotCity.setLayoutManager(new LinearLayoutManager(this));
        mRecyCity.setLayoutManager(new LinearLayoutManager(this));
        InitLocation();
        mLocationClient.start();
        mTvLocatedCity.setText(city);
    }
    private void initData() {

        parseArray();
        String cityName;
        for (int i = 0; i < mCitys.getCity().size(); i++) {
            cityName = mCitys.getCity().get(i).getText();
            if (cityName.contains("省") || cityName.equals("北京市") || cityName.equals("天津市") || cityName.equals("上海市") || cityName.equals("重庆市")) {
                mTvWhole.setVisibility(View.VISIBLE);
                mBoolean = true;
                currentCitiesDatas1.add(mCitys.getCity().get(i));
            }
        }
        selectCitess();
        mTabLayout.addOnTabSelectedListener(new VerticalTabLayout.OnTabSelectedListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onTabSelected(TabView tab, int position) {
                TextView titleView = tab.getTitleView();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    titleView.setTextColor(getColor(R.color.black));
                }
                if (position == 0) {
                    mTvWhole.setVisibility(View.VISIBLE);
                    mBoolean = true;
                } else {
                    mTvWhole.setVisibility(View.GONE);
                    mBoolean = false;
                }
                currentCountiesDatas.clear();
                String countyCode;
                saveProvinceCode = mCitys.getProvince().get(position).getId();
                saveProvinceName = mCitys.getProvince().get(position).getText();
                provinceChargeCode = saveProvinceCode.substring(0, 2);

                if (currentCitiesDatas1 != null) currentCitiesDatas1.clear();
                for (int i = 0; i < mCitys.getCity().size(); i++) {
                    if (provinceChargeCode.equals(mCitys.getCity().get(i).getId().substring(0, 2))) {
                        currentCitiesDatas1.add(mCitys.getCity().get(i));
                    }

                }
                selectCitess();
            }

            @Override
            public void onTabReselected(TabView tab, int position) {

            }
        });

        for (int i = 0; i < mCitys.getProvince().size(); i++) {
            mTabLayout.addTab(new QTabView(getBaseContext()).setTitle(
                    new QTabView.TabTitle.Builder().setContent(mCitys.getProvince().get(i).getText()).build()));

        }
    }
    private void InitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
        //LocationMode.Hight_Accuracy 高精度定位模式下，会同时使用GPS、Wifi和基站定位，返回的是当前条件下精度最好的定位结果
        option.setCoorType("gcj02");//返回的定位结果是百度经纬度，默认值gcj02
        //可选项："gcj02"国策局加密经纬度坐标
        //"bd09ll"百度加密经纬度坐标
        //"bd09"百度加密墨卡托坐标
        option.setIsNeedAddress(true);//反编译获得具体位置，只有网络定位才可以
        mLocationClient.setLocOption(option);
    }

    /**
     * 解析出所有省市县
     */
    private void parseArray() {
        String JsonData = new GetJsonDataUtil().getJson(this, "json.txt");//获取assets目录下的json文件数据
        try {
            Gson gson = new Gson();
            mCitys = gson.fromJson(JsonData, Citys.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据当前省选择市
     */
    private void selectCitess() {
        CommonAdapter adapter = new CommonAdapter<Citys.CityBean>(this, R.layout.item_address_crit, currentCitiesDatas1) {
            @Override
            protected void convert(ViewHolder holder, Citys.CityBean cityBean, int position) {
                holder.setText(R.id.tv_popup_place, cityBean.getText());
            }
        };
        mRecyCity.setAdapter(adapter);
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                saveCityName = currentCitiesDatas1.get(position).getText();
                finish();
                SPUtils.put(LocationActivity.this, Constants.GET_CITY, saveCityName);
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });

    }

    private void selectCountiess() {
        CommonAdapter adapter = new CommonAdapter<String>(this, R.layout.item_address_crit, currentCountiesDatas) {

            @Override
            protected void convert(ViewHolder holder, String s, int position) {
                holder.setText(R.id.tv_popup_place, s);
            }
        };
        mRecyHotCity.setAdapter(adapter);
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                String text = mCitys.getProvince().get(position).getText();
                finish();
                SPUtils.put(LocationActivity.this, Constants.GET_CITY, currentCountiesDatas.get(position));
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
    }

    boolean mBoolean = false;

    @OnClick({R.id.tv_popup, R.id.tv_whole, R.id.click_located_city, R.id.tv_located_city, R.id.iamge_back})
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.tv_popup:
                if (mBoolean) {
                    SPUtils.put(LocationActivity.this, Constants.GET_CITY, "全国");
                } else {
                    SPUtils.put(LocationActivity.this, Constants.GET_CITY, saveProvinceName);
                }
                finish();
                break;
            case R.id.tv_whole:
                SPUtils.put(LocationActivity.this, Constants.GET_CITY, "全国");
                finish();
                break;
            case R.id.click_located_city:
                InitLocation();
                mLocationClient.start();
                break;
            case R.id.tv_located_city:
                if (city != null) {
                    SPUtils.put(this, Constants.GET_LOCATION_CITY, mTvLocatedCity.getText().toString());
                    SPUtils.put(LocationActivity.this, Constants.GET_CITY, mTvLocatedCity.getText().toString());
                }
                finish();
                break;
            case R.id.iamge_back:
                finish();
                break;
        }
    }


    /**
     * 实现实位回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location.getCity() != null) {
                city = location.getCity();
                Log.i(TAG, "onReceiveLocation: "+city);
                mTvLocatedCity.setText(city);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTvLocatedCity.setText((String) SPUtils.get(this, Constants.GET_LOCATION_CITY, ""));
    }

    @Override
    protected void onStop() {
        mLocationClient.stop();
        super.onStop();
    }
}
