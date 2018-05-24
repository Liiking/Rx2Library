package com.http.httpdemo.http;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface IAPINetService {

    @FormUrlEncoded
    @POST("{url}")
    Observable<BaseResponse> requestPost(@Path("url") String url, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("{url}")
    Observable<BaseResponse> requestPost(@Path("url") String url);

    @GET("{url}")
    Observable<BaseResponse> requestGet(@Path("url") String url);

    @GET("{url}")
    Observable<BaseResponse> requestGet(@Path("url") String url, @QueryMap Map<String, String> params);

    @Multipart
    @POST("{path}")
    Observable<BaseResponse> uploadFile(@Path("path") String path, @PartMap Map<String, RequestBody> files);

    // 删除 restful
    @DELETE("{path}/{id}")
    Observable<BaseResponse> delete(@Path("path") String path, @Path("id") String id);

    // 删除
    @DELETE("{path}")
    Observable<BaseResponse> delete(@Path("path") String path, @QueryMap Map<String, String> params);

    // 修改 restful
    @PUT("{path}/{id}")
    Observable<BaseResponse> put(@Path("path") String path, @Path("id") String id);

    // 修改
    @PUT("{path}")
    Observable<BaseResponse> put(@Path("path") String path, @QueryMap Map<String, String> params);

}
