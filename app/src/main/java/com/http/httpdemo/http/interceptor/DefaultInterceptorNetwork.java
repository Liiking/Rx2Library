package com.http.httpdemo.http.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by qwy on 2015/12/15.
 * 网络层拦截器
 */
public class DefaultInterceptorNetwork implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        try {
            return chain.proceed(createRequest(chain.request()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    protected Request createRequest( Request request ) {
        try {
            Request.Builder builder = request.newBuilder();
            // TODO 加密处理
            return builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
