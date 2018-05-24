package com.http.httpdemo.http;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.http.httpdemo.Utility;
import com.http.httpdemo.http.exception.HttpResponseFunc;
import com.http.httpdemo.http.exception.ServerResponseFunc;
import com.http.httpdemo.http.interceptor.DefaultInterceptorApplication;
import com.http.httpdemo.http.interceptor.DefaultInterceptorNetwork;
import com.http.httpdemo.http.subscribers.ProgressSubscriber;
import com.http.httpdemo.http.subscribers.SubscriberListener;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by qwy on 2017/7/14.
 * 网络请求管理类
 */
public class ApiManager {

    private static ApiManager apiManager = null;
    private static IAPINetService apiNetService = null;

    private static long READ_TIME = 30; // 读取时间超时 秒级别
    private static long CONNECT_TIME = 10; // 连接时间超时 秒级别
    private static Map<String, Disposable> disposableMap = new LinkedHashMap<>();

    private ApiManager(Context context, String baseUrl) {
        apiNetService = createServiceAPI(context, baseUrl, RxJava2CallAdapterFactory.create(), IAPINetService.class, null, null, null);
    }

    public static void initApiManger(Application mContext, String baseUrl) {
        apiManager = new ApiManager(mContext, baseUrl);
    }

    public static ApiManager getInstance() {
        return apiManager;
    }

    public static IAPINetService getNetAPIInstance() {
        return apiNetService;
    }

    /**
     * 设置系统参数
     *
     * @param params 原请求参数map
     */
    public void putSystemParams(@NonNull Map<String, String> params) {
    }

    /**
     * 发送请求
     *
     * @param disposableFlag    取消订阅的flag
     * @param hideLoading       是否隐藏加载框  false：显示  true：隐藏
     * @param hideMsg           是否隐藏无网络toast提示  false：显示  true：隐藏
     * @param path              请求路径，在UrlConfig中定义
     * @param isGet             是否是Get请求 true：get 请求
     * @param tClass            对应的data数据类型
     * @param params            请求参数
     * @param listener          请求回调
     */
    public <T> void request(Context context, String disposableFlag, final boolean hideLoading, final boolean hideMsg, final String path, final boolean isGet, final Class<T> tClass, Map<String, String> params, final SubscriberListener<T> listener) {
        Observable<BaseResponse> observable;
        if (params == null) {
            params = new HashMap<>();
        }
        putSystemParams(params);
        if (isGet) {
            observable = getNetAPIInstance().requestGet(path, params);
        } else {
            observable = getNetAPIInstance().requestPost(path, params);
        }
        Utility.log("params:" + new Gson().toJson(params));
        doSubscribe(context, disposableFlag, hideLoading, hideMsg, observable, tClass, listener);
    }

    public <T> void doSubscribe(Context context, String disposableFlag, boolean hideLoading, Observable<BaseResponse> observable, final Class<T> tClass, final SubscriberListener<T> listener) {
        doSubscribe(context, disposableFlag, hideLoading, false, observable, tClass, listener);
    }

    /**
     * 统一订阅入口 请求开始
     *
     * @param context           上下文
     * @param disposableFlag    取消订阅的flag
     * @param hideLoading       是否隐藏加载框  false：显示  true：隐藏
     * @param hideMsg           是否隐藏无网络toast提示  false：显示  true：隐藏
     * @param observable        被观察者
     * @param tClass            对应的data数据类型
     * @param listener          请求回调
     */
    public <T> void doSubscribe(Context context, final String disposableFlag, boolean hideLoading, boolean hideMsg, Observable<BaseResponse> observable, final Class<T> tClass, final SubscriberListener<T> listener) {
        ProgressSubscriber<T> subscriber = new ProgressSubscriber<T>(context, hideLoading, hideMsg, listener);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        Log.e("RxHttpUtils",
                                "accept(RxHttpUtils.java:72)" + "添加到绑定订阅集合");
                        addDisposable(disposableFlag, disposable);
                    }
                })
                .map(new ServerResponseFunc<T>())
                .map(new Function<BaseResponse, T>() {
                    @Override
                    public T apply(BaseResponse response) {
                        try {
                            Utility.LogTooLongE("doSubscribe:", "response:" + Utility.formatJson(response.getData().toString()));
                            // 服务器请求数据成功，返回里面的数据实体
                            if (tClass != null && response.getData() != null) {
                                try {
                                    Utility.log("======response :" + new Gson().toJson(response));
                                    if (response.getData() instanceof Map) {
                                        T data = Utility.getBeanFromMap((Map<String, Object>) response.getData(), tClass);
                                        if (listener != null) {
                                            listener.onNext(response.getCode(), data);
                                        }
                                        return data;
                                    } else {
                                        Gson gson = new Gson();
                                        String json = gson.toJson(response.getData());
                                        T data = gson.fromJson(json, tClass);
                                        if (listener != null) {
                                            listener.onNext(response.getCode(), data);
                                        }
                                        return data;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .onErrorResumeNext(new HttpResponseFunc<T>())
                .unsubscribeOn(Schedulers.newThread())
                .subscribe(subscriber);
    }

    /**
     * 保存Disposable订阅对象
     *
     * @param disposableFlag    disposable标志
     * @param disposable        disposable
     */
    public static void addDisposable(String disposableFlag, Disposable disposable) {
        Utility.log("======addDisposable disposableFlag :" + disposableFlag);
        if (disposableMap != null) {
            disposableMap.put(disposableFlag, disposable);
        }
    }

    /**
     * 根据disposableFlag取消对应的订阅
     *
     * @param disposableFlag disposable标志
     */
    public static void cancelSubscribeByFlag(String disposableFlag) {
        Utility.log("======cancelSubscribeByFlag disposableFlag :" + disposableFlag);
        Iterator<Map.Entry<String, Disposable>> it = disposableMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Disposable> entry = it.next();
            Disposable disposable = entry.getValue();
            if (TextUtils.equals(disposableFlag, entry.getKey())) {
                if (disposable.isDisposed()) {
                    disposable.dispose();
                }
                it.remove();
            }
        }
    }

    public <T> void requestPost(Context context, String disposableFlag, @NonNull String path, @NonNull Class<T> tClass, Map<String, String> params, SubscriberListener listener) {
        requestPost(context, disposableFlag, false, path, tClass, params, listener);
    }

    public <T> void requestPost(Context context, String disposableFlag, boolean hideLoading, @NonNull String path, @NonNull Class<T> tClass, Map<String, String> params, SubscriberListener listener) {
        requestPost(context, disposableFlag, hideLoading, false, path, tClass, params, listener);
    }

    public <T> void requestPost(Context context, String disposableFlag, boolean hideLoading, boolean hideMsg, @NonNull String path, @NonNull Class<T> tClass, Map<String, String> params, SubscriberListener listener) {
        request(context, disposableFlag, hideLoading, hideMsg, path, false, tClass, params, listener);
    }

    public <T> void requestGet(Context context, String disposableFlag, @NonNull String path, @NonNull Class<T> tClass, Map<String, String> params, SubscriberListener<T> listener) {
        requestGet(context, disposableFlag, false, path, tClass, params, listener);
    }

    public <T> void requestGet(Context context, String disposableFlag, boolean hideLoading, @NonNull String path, @NonNull Class<T> tClass, Map<String, String> params, SubscriberListener<T> listener) {
        requestGet(context, disposableFlag, hideLoading, false, path, tClass, params, listener);
    }

    public <T> void requestGet(Context context, String disposableFlag, boolean hideLoading, boolean hideMsg, @NonNull String path, @NonNull Class<T> tClass, Map<String, String> params, SubscriberListener<T> listener) {
        request(context, disposableFlag, hideLoading, hideMsg, path, true, tClass, params, listener);
    }

    public <T> void request(Context context, String disposableFlag, final boolean hideLoading, final String path, final boolean isGet, final Class<T> tClass, Map<String, String> params, SubscriberListener<T> listener) {
        request(context, disposableFlag, hideLoading, false, path, isGet, tClass, params, listener);
    }

    /**
     * 修改
     */
    public <T> void put(Context context, String disposableFlag, final boolean hideLoading, String path, String id, final Class<T> tClass, SubscriberListener<T> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        put(context, disposableFlag, hideLoading, path, params, tClass, listener);
    }

    /**
     * 修改
     */
    public <T> void put(Context context, String disposableFlag, final boolean hideLoading, String path, Map<String, String> params, final Class<T> tClass, SubscriberListener<T> listener) {
        Observable<BaseResponse> observable;
        observable = getNetAPIInstance().put(path, params);
        doSubscribe(context, disposableFlag, hideLoading, observable, tClass, listener);
    }

    /**
     * restful风格删除
     */
    public <T> void deleteRestful(Context context, String disposableFlag, final boolean hideLoading, String path, String id, final Class<T> tClass, SubscriberListener<T> listener) {
        Observable<BaseResponse> observable;
        observable = getNetAPIInstance().delete(path, id);
        doSubscribe(context, disposableFlag, hideLoading, observable, tClass, listener);
    }

    /**
     * 删除
     */
    public <T> void delete(Context context, String disposableFlag, final boolean hideLoading, String path, Map<String, String> params, final Class<T> tClass, SubscriberListener<T> listener) {
        Observable<BaseResponse> observable;
        observable = getNetAPIInstance().delete(path, params);
        doSubscribe(context, disposableFlag, hideLoading, observable, tClass, listener);
    }

    /**
     * 上传文件
     *
     * @param disposableFlag    取消订阅flag
     * @param hideLoading       是否隐藏加载框
     * @param url               上传文件接口地址
     * @param tClass            返回data数据类型
     * @param params            请求参数
     * @param listener          请求回调
     */
    public <T> void uploadFile(Context context, String disposableFlag, final boolean hideLoading, String url, final Class<T> tClass, final Map<String, RequestBody> params, SubscriberListener<T> listener) {
        Observable<BaseResponse> observable;
        observable = getNetAPIInstance().uploadFile(url, params);
        doSubscribe(context, disposableFlag, hideLoading, observable, tClass, listener);
    }

    public static <T> T createServiceAPI(Context context, String baseUrl, CallAdapter.Factory factory, Class<T> serviceClass) {
        return createServiceAPI(context, baseUrl, factory, serviceClass, null, null, null);
    }

    /**
     * 创建ServiceApi对象
     *
     * @param context                   上下文
     * @param baseUrl                   请求的baseUrl
     * @param factory                   适配器工厂类
     * @param serviceClass              service类
     * @param applicationInterceptor    应用层拦截器
     * @param netWorkInterceptor        网络层拦截器
     * @param rawResources              HTTPS证书资源
     */
    private static <T> T createServiceAPI(Context context, String baseUrl, CallAdapter.Factory factory, Class<T> serviceClass, Interceptor applicationInterceptor, Interceptor[] netWorkInterceptor, int... rawResources) {
        try {
            Interceptor temApplicationInterceptor = applicationInterceptor;
            if (temApplicationInterceptor == null) {
                temApplicationInterceptor = new DefaultInterceptorApplication();
            }
            Interceptor[] temNetsInterceptor = netWorkInterceptor;
            if (temNetsInterceptor == null || temNetsInterceptor.length <= 0) {
                temNetsInterceptor = new Interceptor[1];
                temNetsInterceptor[0] = new DefaultInterceptorNetwork();
            }

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .readTimeout(READ_TIME, TimeUnit.SECONDS)
                    .connectTimeout(CONNECT_TIME, TimeUnit.SECONDS)
                    .addInterceptor(temApplicationInterceptor);
            for (Interceptor tem : temNetsInterceptor) {
                builder.addNetworkInterceptor(tem);
            }
            builder.retryOnConnectionFailure(true);
            try {
                initHttpsConfig(context, builder, rawResources);
            } catch (Exception e) {
                e.printStackTrace();
            }
            OkHttpClient client = builder.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    // 添加公共头信息
                    Request request = chain.request()
                            .newBuilder()
//                            .addHeader("Content-Type", "application/json")//  x-www-form-urlencoded
                            .build();
                    Utility.log(request.headers().toString());
                    return chain.proceed(request);
                }
            }).build();
            Retrofit.Builder builder1 = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(getGson()));
            if (factory != null) {
                builder1.addCallAdapterFactory(factory);
            }
            Retrofit retrofit = builder1.callFactory(client)
                    .build();
            return retrofit.create(serviceClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * https证书
     */
    private static void initHttpsConfig(Context context, OkHttpClient.Builder builder, int... rawResources) {
        try {
            if (context != null && rawResources != null && rawResources.length > 0) {
                final String KEY_STORE_TYPE_P12 = "PKCS12";//证书类型
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_P12);
                keyStore.load(null);
                for (int index = 0; index < rawResources.length; index++) {
                    try {
                        InputStream is = context.getResources().openRawResource(rawResources[index]);
                        keyStore.setCertificateEntry("" + index, certificateFactory.generateCertificate(is));
                        if (is != null) {
                            is.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
                if (sslContext != null) {
                    builder.sslSocketFactory(sslContext.getSocketFactory());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Gson getGson() {
        Gson gson = new Gson();
        try {
            GsonBuilder builder = new GsonBuilder();
            gson = builder.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gson;
    }

}
