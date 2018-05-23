package com.http.httpdemo;

import android.app.Application;

import com.http.httpdemo.http.ApiManager;

/**
 * Created by qianweiying on 2018/5/16.
 * 自定义application
 */

public class MyApplication extends Application {

    private String baseUrl = "http://op.juhe.cn/";

    @Override
    public void onCreate() {
        super.onCreate();
        ApiManager.initApiManger(this, baseUrl);
    }
}
