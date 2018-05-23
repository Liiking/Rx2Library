package com.http.httpdemo.http.exception;


import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * Created by qwy on 17/8/29.
 * 全局异常处理
 */
public class HttpResponseFunc<T> implements Function<Throwable, Observable<T>> {

    @Override
    public Observable<T> apply(Throwable throwable) {
        // ExceptionEngine为处理异常的驱动器
        ApiException ae = ExceptionEngine.handleException(throwable);
        return Observable.error(ae);
    }
}