package com.cuisec.mshield.utils;

import com.cuca.bouncycastle.asn1.cryptopro.GOST3410NamedParameters;
import com.cuisec.mshield.config.Config;
import com.cuisec.mshield.design.NetTrustManager;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetWorkUtils {
    public static String getjson(Object o){
        Gson gson = new Gson();
        String json = "";
            if (o != null){
                 json = gson.toJson(o);
            }
        return json;
    }
    public static void getNetWork(String url,Object o){
        OkHttpClient clientWithCache = getClientWithCache();
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , getjson(o));
        final Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        clientWithCache.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
    public static OkHttpClient getClientWithCache() {
        return new OkHttpClient.Builder()
                .sslSocketFactory(NetTrustManager.getNetTrustManager().createSSLSocketFactory()).hostnameVerifier(new NetTrustManager.TrustAllHostnameVerifier())
                .build();
    }
}
