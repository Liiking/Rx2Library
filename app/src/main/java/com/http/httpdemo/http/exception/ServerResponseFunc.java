package com.http.httpdemo.http.exception;

import com.http.httpdemo.http.BaseResponse;
import io.reactivex.functions.Function;

/**
 * 拦截固定格式的公共数据类型Response<T>,判断里面的状态码
 *
 */
public class ServerResponseFunc<T> implements Function<BaseResponse, BaseResponse> {

    @Override
    public BaseResponse apply(BaseResponse response) {
        // 对返回码进行判断，如果不是 OK，则证明服务器端返回错误信息了，便根据跟服务器约定好的错误码去解析异常
        if (ERROR.REQUEST_OK == response.getCode()) {
            // 服务器请求数据成功，返回里面的数据实体
            return response;
        }
        // 如果服务器端有错误信息返回，那么抛出异常，让下面的方法去捕获异常做统一处理
        throw new ServerException(response.getCode(), response.getMessage());
    }

}
