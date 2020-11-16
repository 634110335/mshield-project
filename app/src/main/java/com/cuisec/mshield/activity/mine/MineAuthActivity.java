package com.cuisec.mshield.activity.mine;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.cuisec.mshield.activity.home.HomeActivity;
import com.cuisec.mshield.bean.BaseBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.data.UserInfo;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.widget.LoadDialog;
import com.cuisec.mshield.widget.MenuDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLDecoder;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class MineAuthActivity extends BaseActivity {

    private static final String ID_TYPE_NONE = "0";      // 未选择
    private static final String ID_TYPE_SFZ = "1";       // 身份证
    private static final String ID_TYPE_GATZ = "2";      // 港澳台证
    private static final String ID_TYPE_HZ = "3";        // 护照
    private static final String ID_TYPE_JGZ = "4";       // 军官证
    private static final String ID_TYPE_HXZ = "5";       // 回乡证

    @BindView(R.id.mine_auth_name_et)
    EditText mNameEt;
    @BindView(R.id.mine_auth_id_type_et)
    EditText mIdTypeET;
    @BindView(R.id.mine_auth_id_et)
    EditText mIdEt;

    private String mIdType = ID_TYPE_NONE;

    private LoadDialog mLoadDlg = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_mine_auth);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("实名认证");
    }

    @Override
    protected void initializeData() {
    }


    @OnClick({R.id.mine_auth_submit_btn, R.id.mine_auth_id_type_et})
    public void onViewClicked(View view) {
        hideKeyBoard();
        switch (view.getId()) {
            case R.id.mine_auth_submit_btn: {
                String nameStr = mNameEt.getText().toString();
                String idStr = mIdEt.getText().toString();
                if (nameStr.length() == 0) {
                    T.showShort(getApplicationContext(), getString(R.string.auth_name_hint));
                    return;
                }
                if (mIdType.equals(ID_TYPE_NONE)) {
                    T.showShort(getApplicationContext(), getString(R.string.auth_id_type_hint));
                    return;
                }
                if (idStr.length() == 0) {
                    T.showShort(getApplicationContext(), getString(R.string.auth_id_hint));
                    return;
                }
                authId(nameStr, idStr);
                break;
            }
            case R.id.mine_auth_id_type_et: {
                String[] list = new String[]{"身份证", "港澳台证", "护照", "军官证", "回乡证"};
                new MenuDialog(MineAuthActivity.this, getString(R.string.app_cancel), list, true).setOnMyPopClickListener(new MenuDialog.MenuClickListener() {
                    @Override
                    public void onItemClick(int index, String content) {
                        switch (index) {
                            case 0: {
                                mIdType = ID_TYPE_SFZ;
                                mIdTypeET.setText("身份证");
                                break;
                            }
                            case 1: {
                                mIdType = ID_TYPE_GATZ;
                                mIdTypeET.setText("港澳台证");
                                break;
                            }
                            case 2: {
                                mIdType = ID_TYPE_HZ;
                                mIdTypeET.setText("护照");
                                break;
                            }
                            case 3: {
                                mIdType = ID_TYPE_JGZ;
                                mIdTypeET.setText("军官证");
                                break;
                            }
                            case 4: {
                                mIdType = ID_TYPE_HXZ;
                                mIdTypeET.setText("回乡证");
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelClick(String content) {
                    }
                });
                break;
            }
        }
    }

    // 实名认证
    private void authId(final String name, final String idNO) {

        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(MineAuthActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }
        try {
            String urlName = URLEncoder.encode(name, Config.UTF_8);
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.auth_id)
                    .addParams("userName", urlName)
                    .addParams("idNo", idNO)
                    .addParams("idType", mIdType)
                    .addHeader("token", SPManager.getUserToken())
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(MineAuthActivity.this, getString(R.string.app_network_error));

                        }
                        @Override
                        public void onResponse(String response, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            try {
                                response = URLDecoder.decode(response, Config.UTF_8);
                                BaseBean bean = (BaseBean) JsonUtil.toObject(response, BaseBean.class);
                                if (bean != null) {
                                    if (bean.getRet() == 0) {
                                        // 发送我的广播
                                        Intent intent = new Intent(Constants.MINE_UPDATA_BROADCAST);
                                        intent.putExtra("type", "TYPE_AUTH");
                                        intent.putExtra("value", Constants.AUTH_STATUS_STRONG);
                                        sendBroadcast(intent);
                                        final UserInfo userInfo = SPManager.getUserInfo();
                                        userInfo.userName = name;
                                        Log.i("sss", "onResponse: " + name);
                                        userInfo.idNo = idNO;
                                        userInfo.authStatus = Constants.AUTH_STATUS_STRONG;
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                SPManager.setUserInfo(userInfo);
                                            }
                                        }.start();
                                        T.showShort(getApplicationContext(), "实名认证成功");
                                        startActivity(new Intent(MineAuthActivity.this, HomeActivity.class));
                                        //finishActivity();
                                    } else {
                                        String errMsg = URLDecoder.decode(bean.getMsg(), Config.UTF_8);
                                        T.showShort(getApplicationContext(), errMsg);
                                    }
                                }
                            } catch (Exception e) {
                                T.showShort(getApplicationContext(), e.getLocalizedMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            if (mLoadDlg != null) {
                mLoadDlg.dismiss();
                mLoadDlg = null;
            }
            T.showShort(getApplicationContext(), e.getLocalizedMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO:OnCreate Method has been created, run ButterKnife again to generate code
        setContentView(R.layout.activity_mine_auth);
        ButterKnife.bind(this);
    }
}
