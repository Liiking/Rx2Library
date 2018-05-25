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
import retrofit2.http.Url;

public interface IAPINetService {

    @FormUrlEncoded
    @POST
    Observable<BaseResponse> requestPost(@Url String url, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST
    Observable<BaseResponse> requestPost(@Url String url);

    @GET
    Observable<BaseResponse> requestGet(@Url String url);

    @GET
    Observable<BaseResponse> requestGet(@Url String url, @QueryMap Map<String, String> params);

    @Multipart
    @POST
    Observable<BaseResponse> uploadFile(@Url String url, @PartMap Map<String, RequestBody> files);

    // 删除 restful
    @DELETE("{path}/{id}")
    Observable<BaseResponse> delete(@Path("path") String path, @Path("id") String id);

    // 删除
    @DELETE
    Observable<BaseResponse> delete(@Url String url, @QueryMap Map<String, String> params);

    // 修改 restful
    @PUT("{path}/{id}")
    Observable<BaseResponse> put(@Path("path") String path, @Path("id") String id);

    // 修改
    @PUT
    Observable<BaseResponse> put(@Url String url, @QueryMap Map<String, String> params);

}
