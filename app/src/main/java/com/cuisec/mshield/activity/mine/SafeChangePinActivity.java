package com.cuisec.mshield.activity.mine;

import android.widget.EditText;

import com.cuisec.mshield.bean.BaseBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SecurityUtil;
import com.cuisec.mshield.utils.T;
import com.cuisec.mshield.utils.ValidateUtil;
import com.cuisec.mshield.widget.LoadDialog;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.common.BaseActivity;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.net.URLDecoder;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

public class SafeChangePinActivity extends BaseActivity {

    @BindView(R.id.safe_pin_et)
    EditText mPinEt;
    @BindView(R.id.safe_pin_new_et)
    EditText mPinNewEt;
    @BindView(R.id.safe_pin_re_new_et)
    EditText mPinReNewEt;

    private LoadDialog mLoadDlg = null;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_safe_change_pin);
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeViews() {
        showTitle("修改密码");
    }

    @Override
    protected void initializeData() {

    }

    @OnClick(R.id.safe_pin_ok_btn)
    public void onViewClicked() {
        hideKeyBoard();

        String strOldPwd = mPinEt.getText().toString();
        String strNewPwd = mPinNewEt.getText().toString();
        String strReNewPwd = mPinReNewEt.getText().toString();

        if (strOldPwd.length() == 0) {
            T.showShort(SafeChangePinActivity.this, getString(R.string.pwd_old_tip));
            return;
        }
        if (strNewPwd.length() == 0) {
            T.showShort(SafeChangePinActivity.this, getString(R.string.pwd_new_tip));
            return;
        }

        if (strNewPwd.length() < 8) {
            T.showShort(getApplicationContext(), getString(R.string.pwd_low));
            return;
        }

        if (!ValidateUtil.isLetterDigit(strNewPwd)) {
            T.showShort(getApplicationContext(), getString(R.string.pwd_comb));
            return;
        }

        if (strReNewPwd.length() == 0) {
            T.showShort(SafeChangePinActivity.this, getString(R.string.pwd_re_new_tip));
            return;
        }
        if (!strNewPwd.equals(strReNewPwd)) {
            T.showShort(SafeChangePinActivity.this, getString(R.string.pwd_not_match));
            return;
        }

        userChangePassword(strOldPwd, strNewPwd);
    }

    private void userChangePassword(String oldPwd, String newPwd) {

        if (mLoadDlg == null) {
            mLoadDlg = new LoadDialog(SafeChangePinActivity.this, R.style.CustomDialog);
            mLoadDlg.show();
        }

        try {
            String strOldPwd = SecurityUtil.sha256(oldPwd);
            final String strNewPwd = SecurityUtil.sha256(newPwd);
            OkHttpUtils
                    .post()
                    .url(SPManager.getServerUrl() + Config.user_changepwd)
                    .addHeader("token", SPManager.getUserToken())
                    .addParams("oldPassword", URLEncoder.encode(strOldPwd, Config.UTF_8))
                    .addParams("newPassword", URLEncoder.encode(strNewPwd, Config.UTF_8))
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (mLoadDlg != null) {
                                mLoadDlg.dismiss();
                                mLoadDlg = null;
                            }
                            T.showShort(SafeChangePinActivity.this, getString(R.string.app_network_error));
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
                                        T.showShort(SafeChangePinActivity.this, getString(R.string.pwd_change_success));
                                        finishActivity();
                                    } else {
                                        T.showShort(getApplicationContext(), bean.getMsg());
                                    }
                                }
                            } catch (Exception e) {
                                L.e(e.getLocalizedMessage());
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
}
