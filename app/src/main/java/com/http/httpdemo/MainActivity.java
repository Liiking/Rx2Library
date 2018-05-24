package com.http.httpdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.http.httpdemo.http.ApiManager;
import com.http.httpdemo.http.download.DownLoadObserver;
import com.http.httpdemo.http.download.DownloadInfo;
import com.http.httpdemo.http.download.DownloadManager;
import com.http.httpdemo.http.exception.ApiException;
import com.http.httpdemo.http.subscribers.SubscriberListener;
import com.http.httpdemo.model.UploadPhoto;
import com.http.httpdemo.util.PictureUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MainActivity extends BaseActivity {

    private ImageView imageView;
    private TextView textView;
    private TextView tv_progress;
    private TextView tv_upload_path;

    private static final String URL_UPLOAD_FILE = "file/upload";     //   post    上传文件接口
    private static final String URL_COMMON_LOGIN = "user/login";     //   post    登录
    private static final String URL_COMMON_LIST = "api/list";        //   get     获取列表

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        tv_upload_path = (TextView) findViewById(R.id.tv_upload_path);
        findViewById(R.id.get).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testGet();
            }
        });
        findViewById(R.id.post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testPost();
            }
        });
        findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testDownload();
            }
        });
        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(token)) {
                    Utility.shortToast(MainActivity.this, "请先请求post接口");
                    return ;
                }
                selectSinglePicture();
            }
        });
    }

    /**
     * 测试get请求
     */
    private void testGet() {
        Map<String, String> p = new HashMap<>();
        p.put("type", "6");
        ApiManager.getInstance()
                .requestGet(MainActivity.this, getDisposableFlag(), URL_COMMON_LIST, Object.class, p, new SubscriberListener<Object>() {
                    @Override
                    public void onNext(Object obj) {
                        textView.setText(new Gson().toJson(obj));
                    }
                });
    }

    /**
     * 测试post请求
     */
    private void testPost() {
        Map<String, String> p = new HashMap<>();
        p.put("username", "18046551406");
        p.put("password", "qqqqqq");
        ApiManager.getInstance()
                .requestPost(MainActivity.this, getDisposableFlag(), URL_COMMON_LOGIN, HashMap.class, p, new SubscriberListener<HashMap<String, String>>() {
                    @Override
                    public void onNext(HashMap<String, String> map) {
                        if (map != null) {
                            textView.setText(map.toString());
                            token = map.get("access_token");
                        }
                    }
                });
    }

    /**
     * 测试文件下载
     */
    private void testDownload() {
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
                    tv_progress.setText(DownloadManager.getDownloadFile(downloadInfo.getUrl()).getAbsolutePath());
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

    /**
     * 上传单张图片
     *
     * @param local_path 要上传的图片的本地路径
     */
    protected void uploadSinglePicture(String local_path) {
        long cur = System.currentTimeMillis();
        String compressPath = PictureUtil.compressImageTo200KB(this, local_path);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), new File(compressPath));
        Map<String, RequestBody> params = new HashMap<>();
        putUploadParams(params, requestBody);
        ApiManager.getInstance().uploadFile(this, getDisposableFlag(), false, URL_UPLOAD_FILE, UploadPhoto.class, params, new SubscriberListener<UploadPhoto>() {

            @Override
            public void onStart() {
                super.onStart();
                Utility.shortToast(MainActivity.this, "开始上传图片");
            }

            @Override
            public void onNext(UploadPhoto obj) {
                if (obj != null) {
                    tv_upload_path.setText(obj.getPath());
                    Utility.shortToast(MainActivity.this, "上传成功");
                }
            }

            @Override
            public void onError(ApiException e) {
                super.onError(e);
                Utility.shortToast(MainActivity.this, "上传失败，请稍后重试");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_SELECT_IMAGE){
            if(resultCode == RESULT_OK){
                // Get the result list of select image paths
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                if (path != null && path.size() > 0) {
                    String url = path.get(0);
                    Glide.with(MainActivity.this).load(url).into(imageView);
                    uploadSinglePicture(url);
                }
            }
        }
    }

    /**
     * 添加上传文件参数
     *
     * @param params        参数map
     * @param requestBody   要上传的文件
     */
    private void putUploadParams(Map<String, RequestBody> params, RequestBody requestBody) {
        RequestBody bodyToken = RequestBody.create(MediaType.parse("text/plain"), token);
        RequestBody domainType = RequestBody.create(MediaType.parse("text/plain"), "4");
        RequestBody type = RequestBody.create(MediaType.parse("text/plain"), "gravatar");
        params.put("token", bodyToken);
        params.put("domain_type", domainType);
        params.put("type", type);
        params.put("imageFiles\";filename=\"" + System.currentTimeMillis(), requestBody);
    }

}
