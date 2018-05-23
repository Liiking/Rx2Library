package com.http.httpdemo.http.download;

import io.reactivex.Observer;

/**
 * Created by qwy on 2017/2/2.
 * 下载观察者
 */
public abstract class DownLoadObserver implements Observer<DownloadInfo> {
    protected DownloadInfo downloadInfo;

    @Override
    public void onNext(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onComplete() {
    }
}
