package com.http.httpdemo.http.exception;

/**
 * Created by qwy on 17/8/29.
 * 约定异常
 */
public class ERROR {
    /**
     * 约定请求成功的返回码
     */
    public static final int REQUEST_OK = 0;

    /**
     * token为空或已过期，刷新token（跳转登录）
     */
    public static final int TOKEN_NEED_REFRESH = 5;

    /**
     * 需要展示错误信息的错误
     */
    public static final int SHOW_MESSAGE_ERROR = 1;

    /**
     * 未知错误
     */
    public static final int UNKNOWN = 1000;

    /**
     * 解析错误
     */
    public static final int PARSE_ERROR = 1001;

    /**
     * 网络错误
     */
    public static final int NETWORK_ERROR = 1002;

    /**
     * 协议出错
     */
    public static final int HTTP_ERROR = 1003;

}