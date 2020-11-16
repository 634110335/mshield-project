package com.cuisec.mshield.utils;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenInterceptor implements Authenticator {

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        return null;
    }
}
