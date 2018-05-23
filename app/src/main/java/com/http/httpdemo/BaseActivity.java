package com.http.httpdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.http.httpdemo.http.ApiManager;

/**
 * Created by qwy on 2018/5/23.
 * 生命周期管理
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected String disposableFlag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposableFlag = this.getLocalClassName() + System.currentTimeMillis();
    }

    protected String getDisposableFlag() {
        return disposableFlag;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApiManager.cancelSubscribeByFlag(getDisposableFlag());
    }
}
