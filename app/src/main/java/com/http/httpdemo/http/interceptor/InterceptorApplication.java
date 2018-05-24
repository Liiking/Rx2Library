package com.http.httpdemo.http.interceptor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.Gson;
import com.http.httpdemo.util.Utility;
import com.http.httpdemo.http.BaseResponse;

import java.io.IOException;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by qwy on 2017/7/26.
 * 拦截器
 */
public class InterceptorApplication extends DefaultInterceptorNetwork {

    private final Handler mHander = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
        }
    };

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        try {
            String url = "";
            RequestBody params = null;
            String method = request.method().toUpperCase();
            if ("GET".equals(method)) {
                url = request.url().toString();
            } else {
                params = request.body();
            }

            Response response = chain.proceed(chain.request());
            String content = response.body().string();
            int type = handlerResponse(content);
            try {
                switch (type) {
                    default:
                        response = response.newBuilder().body(ResponseBody.create(response.body().contentType(), content)).build();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Utility.LogTooLongE("intercept", request.toString() + "\n" + Utility.formatJson(content));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        } catch (Exception e) {
            Utility.LogTooLongE("intercept", request.toString() + "\nException:" + e.toString());
            Utility.log("intercept", "intercept:request=" + request.toString() + "; \n response:Exception" + e.toString());
            e.printStackTrace();
        }
        return getNullResponse(request);

    }

    /***
     * 解密数据
     *
     * @param response
     * @return
     */
    private Response createResponse(Request request, Response response, String content) {
        Response.Builder builder = response.newBuilder();
        // 其他处理
        return builder.body(ResponseBody.create(response.body().contentType(), content)).build();
    }

    public Response getNullResponse(Request request) {
        Response.Builder builder = new Response.Builder();
        builder.request(request);
        builder.protocol(Protocol.HTTP_1_1);
        builder.code(500);
        return builder.build();
    }


    private int handlerResponse(String content) {
        try {
            BaseResponse response = null;
            try {
                response = new Gson().fromJson(content, BaseResponse.class);
            } catch (Exception e) {
            }
            if (response != null) {
                return response.getCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}