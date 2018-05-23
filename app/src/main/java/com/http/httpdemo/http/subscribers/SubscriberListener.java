package com.http.httpdemo.http.subscribers;

import com.http.httpdemo.http.exception.ApiException;

/**
 * Created by qwy on 16/3/10.
 * 监听器
 */
public abstract class SubscriberListener<T> {
    public abstract void onNext(T t);
    public void onNext(int code, T t) {

    }

    public void onCompleted() {
    }


    public void onStart() {
    }


    public void onError(ApiException e) {
    }

}
