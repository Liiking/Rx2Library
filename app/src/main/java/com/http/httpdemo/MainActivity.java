package com.http.httpdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.gson.Gson;
import com.http.httpdemo.http.ApiManager;
import com.http.httpdemo.http.download.DownLoadObserver;
import com.http.httpdemo.http.download.DownloadInfo;
import com.http.httpdemo.http.download.DownloadManager;
import com.http.httpdemo.http.subscribers.SubscriberListener;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseActivity {

    String GET_CONTENT_LIST = "onebox/exchange/query";
    private TextView textView;
    private TextView tv_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiTest();
            }
        });
    }

    /**
     * 测试get请求
     */
    private void apiTest() {
        Map<String, String> p = new HashMap<>();
        p.put("type", "shishang");
        p.put("key", "095e3c7a2288d20bb664fd12c6c57a57");
        ApiManager.getInstance()
                .requestGet(MainActivity.this, getDisposableFlag(), GET_CONTENT_LIST, Object.class, p, new SubscriberListener<Object>() {
                    @Override
                    public void onNext(Object obj) {
                        textView.setText(new Gson().toJson(obj));
                    }
                });
    }

    /**
     * 测试文件下载
     */
    private void download() {
        Map<String, String> p = new HashMap<>();
        p.put("type", "shishang");
        p.put("key", "095e3c7a2288d20bb664fd12c6c57a57");
        DownloadManager.getInstance().download(MainActivity.this, "http://shop.axwith.com/apk/pinjungongshe.apk", new DownLoadObserver() {
            @Override
            public void onSubscribe(Disposable d) {
                Utility.shortToast(MainActivity.this, "开始下载");
                Utility.log("===========download onSubscribe");
            }

            @Override
            public void onNext(DownloadInfo downloadInfo) {
                super.onNext(downloadInfo);
                int progress = 0;
                if (downloadInfo.getTotal() > 0) {
                    progress = (int) (downloadInfo.getProgress() * 100 / downloadInfo.getTotal());
                    tv_progress.setText("下载进度：" + downloadInfo.getProgress() * 100 / downloadInfo.getTotal() + "%");
                }
                if (progress == 100) {
                    textView.setText("文件下载至：" + DownloadManager.getDownloadFile(downloadInfo.getUrl()).getAbsolutePath());
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                Utility.shortToast(MainActivity.this, "下载失败");
            }

            @Override
            public void onComplete() {
                super.onComplete();
                Utility.shortToast(MainActivity.this, "下载完成");
            }
        });
    }

}
