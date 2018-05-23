package com.http.httpdemo.http.exception;

/**
 * Created by qwy on 17/8/29.
 * 服务器异常
 */
public class ServerException extends RuntimeException {
    public int code;
    public String message;

    public ServerException(int code, String message) {
        this.code = code;
        this.message = message;
    }
}