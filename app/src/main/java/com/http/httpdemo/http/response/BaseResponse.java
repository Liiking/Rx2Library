package com.http.httpdemo.http.response;

/**
 * Created by qwy on 2017/7/10.
 * 返回的基本数据类型
 */
public class BaseResponse<T> {
    private String message;
    private int code;
    public T result;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return result;
    }

    public void setData(T data) {
        this.result = data;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "message='" + message + '\'' +
                ", code=" + code +
                '}';
    }
}
