package com.http.httpdemo.http.subscribers;

import android.content.Context;
import com.http.httpdemo.R;
import com.http.httpdemo.Utility;
import com.http.httpdemo.http.exception.ApiException;
import com.http.httpdemo.http.exception.ERROR;
import com.http.httpdemo.http.progress.ProgressCancelListener;
import com.http.httpdemo.http.progress.ProgressDialogHandler;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by qwy on 16/3/10.
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束是，关闭ProgressDialog
 * 调用者自己对请求数据进行处理
 */
public class ProgressSubscriber<T> implements Observer<T>, ProgressCancelListener {

    private SubscriberListener mSubscriberListener;
    private ProgressDialogHandler mProgressDialogHandler;
    private boolean hideLoading = false;// 是否隐藏加载框，默认展示
    private boolean hideMsg = false;// 是否隐藏无网络提示，默认展示
    private Context context;
    private Disposable disposable;

    public ProgressSubscriber(Context context, SubscriberListener<T> mSubscriberListener) {
        this(context, false, false, mSubscriberListener);
    }

    public ProgressSubscriber(Context context, boolean hideLoading, SubscriberListener<T> mSubscriberListener) {
        this(context, hideLoading, false, mSubscriberListener);
    }

    public ProgressSubscriber(Context context, boolean hideLoading, boolean hideMsg, SubscriberListener<T> mSubscriberListener) {
        this.context = context;
        this.hideMsg = hideMsg;
        this.hideLoading = hideLoading;
        this.mSubscriberListener = mSubscriberListener;
        if (!hideLoading) {
            mProgressDialogHandler = new ProgressDialogHandler(context, this, false);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialogHandler != null && !hideLoading) {
            mProgressDialogHandler.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialogHandler != null) {
            mProgressDialogHandler.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
            mProgressDialogHandler = null;
        }
    }

    /**
     * 订阅开始时调用
     * 显示ProgressDialog
     */
    @Override
    public void onSubscribe(Disposable d) {
        this.disposable = d;
        // 接下来可以检查网络连接等操作
        if (!Utility.hasNet(context)) {
            if (!hideMsg) {
                Utility.shortToast(context, R.string.no_net_error);
            }
            if (context != null) {
                onError(new ApiException(new Throwable(context.getResources().getString(R.string.no_net_error)), ERROR.NETWORK_ERROR));
            }
            // 取消本次Subscriber订阅
            if (d != null && !d.isDisposed()) {
                d.dispose();
            }
        } else {
            onRequestStart();
        }
    }

    /**
     * 真正请求开始，展示对话框
     */
    public void onRequestStart() {
        if (mSubscriberListener != null) {
            mSubscriberListener.onStart();
        }
        showProgressDialog();
    }

    /**
     * 请求完成，隐藏ProgressDialog
     */
    @Override
    public void onComplete() {
        if (mSubscriberListener != null) {
            mSubscriberListener.onCompleted();
        }
        dismissProgressDialog();
    }

    /**
     * 对错误进行统一处理
     * 隐藏ProgressDialog
     *
     * @param e
     */
    public void onError(ApiException e) {
        if (mSubscriberListener != null) {
            mSubscriberListener.onError(e);
        }
        dismissProgressDialog();
    }

    /**
     * 将onNext方法中的返回结果交给Activity或Fragment自己处理
     * @param t 创建Subscriber时的泛型类型
     */
    @Override
    public void onNext(T t) {
        if (mSubscriberListener != null) {
            mSubscriberListener.onNext(t);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof ApiException){
            // 访问获得对应的Exception
            ApiException ae = (ApiException) e;
            if (ae.code == ERROR.SHOW_MESSAGE_ERROR) {
                // 参数错误，展示错误信息
                Utility.shortToastInMainThread(context, ae.message);
            } else if (ae.code == ERROR.TOKEN_NEED_REFRESH) {
                // TODO token过期,清除本地用户信息，跳登录等 可通过接口传入
            }
            onError(ae);
        } else {
            // 将Throwable 和 未知错误的status code返回
            onError(new ApiException(e, ERROR.UNKNOWN));
        }
    }

    /**
     * 取消ProgressDialog的时候，取消对observable的订阅，同时也取消了http请求
     */
    @Override
    public void onCancelProgress() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

}