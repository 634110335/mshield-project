package com.cuisec.mshield;

import com.cuisec.mshield.bean.UserLoginBean;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.config.Constants;
import com.cuisec.mshield.data.SPManager;
import com.cuisec.mshield.utils.JsonUtil;
import com.cuisec.mshield.utils.L;
import com.cuisec.mshield.utils.SecurityUtil;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Created by YangXun on 2017/5/9.
 * 自动刷新TOKEN
 */

public class TokenAuthenticator implements Authenticator {
    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // 读取刷新TOKEN
        String newToken = null;
        String refreshToken = SPManager.getRefreshToken();
        String sign = SecurityUtil.signature(Constants.APP_PRIVATE_KEY, refreshToken);
        // 同步调用token刷新接口
        Response res = OkHttpUtils
                .post()
                .url(SPManager.getServerUrl() + Config.user_refresh)
                .addParams("refreshToken", refreshToken)
                .addParams("sign", URLEncoder.encode(sign, Config.UTF_8))
                .build()
                .execute();
        String result = res.body().string();
        result = URLDecoder.decode(result, Config.UTF_8);
        UserLoginBean bean = (UserLoginBean) JsonUtil.toObject(result, UserLoginBean.class);
        if (bean != null) {
            if (bean.getRet() == 0 && bean.getData() != null) {
                SPManager.setUserToken(bean.getData().getToken());
                SPManager.setRefreshToken(bean.getData().getRefreshToken());
                newToken = bean.getData().getToken();
                L.i(newToken);
            } else {
                // 清除登录状态
                SPManager.setLoginState(false);
            }
        }

        if (newToken != null) {
            return response.request().newBuilder()
                    .header("token", newToken)
                    .build();
        } else {
            return null;
        }
    }

    public String getNewToken(String token){
        if (token != null){
            return token;
        }
        return null;
    }
}
