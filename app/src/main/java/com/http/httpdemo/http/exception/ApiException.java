package com.http.httpdemo.http.exception;

/**
 * Created by qwy on 17/8/29.
 * Api异常
 */
public class ApiException extends Exception {
    public int code;
    public String message;

    public ApiException(Throwable throwable, int code) {
        super(throwable);
        this.code = code;
    }
}