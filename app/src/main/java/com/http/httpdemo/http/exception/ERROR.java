package com.http.httpdemo.http.exception;

/**
 * Created by qwy on 17/8/29.
 * 约定异常
 */
public class ERROR {
    /**
     * 约定请求成功的返回码
     */
    public static final int REQUEST_OK = 200;
    public static final int REQUEST_CREATE_OK = 201;
    public static final int REQUEST_DELETE_OK = 204;

    /**
     * token为空或已过期，刷新token（跳转登录）
     */
    public static final int TOKEN_NEED_REFRESH = 401;

    /**
     * 需要展示错误信息的错误
     */
    public static final int SHOW_MESSAGE_ERROR = 400;

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