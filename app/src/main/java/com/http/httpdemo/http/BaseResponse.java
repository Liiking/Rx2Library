package com.http.httpdemo.http;

import com.google.gson.annotations.SerializedName;

/**
 * Created by qwy on 2017/7/10.
 * 返回的基本数据类型
 */
public class BaseResponse<T> {
    // 与后台约定好的固定返回格式
    private String message;
    private int code;
    @SerializedName("result") // 测试用
    public T data;

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
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "message='" + message + '\'' +
                ", code=" + code +
                '}';
    }
}
