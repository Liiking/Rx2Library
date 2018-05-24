package com.http.httpdemo;

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
import com.http.httpdemo.util.FileUtil;
import com.http.httpdemo.util.PictureUtil;
import com.http.httpdemo.util.Utility;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import io.reactivex.disposables.Disposable;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ImageView imageView;
    private TextView textView;
    private TextView tv_progress;
    private TextView tv_upload_path;

    private static final String URL_UPLOAD_FILE = "file/upload";     //   post    上传文件接口
    private static final String URL_COMMON_LOGIN = "user/login";     //   post    登录
    private static final String URL_COMMON_LIST = "api/list";        //   get     获取列表

    private String token;
    private boolean isDownloading = false;// 是否正在下载中

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        tv_upload_path = (TextView) findViewById(R.id.tv_upload_path);
        tv_upload_path.setOnClickListener(this);
        findViewById(R.id.get).setOnClickListener(this);
        findViewById(R.id.post).setOnClickListener(this);
        findViewById(R.id.download).setOnClickListener(this);
        findViewById(R.id.upload).setOnClickListener(this);
    }

    /**
     * 测试get请求
     */
    private void testGet() {
        Map<String, String> p = new HashMap<>();
        p.put("type", "" + (new Random().nextInt(8) + 1));
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
        if (isDownloading) {
            Utility.shortToast(MainActivity.this, "下载中...");
            return ;
        }
        isDownloading = true;
        String downloadFileUrl = "http://shop.axwith.com/apk/pinjungongshe.apk";
        File file = DownloadManager.getDownloadFile(downloadFileUrl);
        if (file != null && file.exists()) {
            FileUtil.deleteFileWithPath(file.getAbsolutePath());
        }
        DownloadManager.getInstance().download(downloadFileUrl, new DownLoadObserver() {
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
                    tv_progress.setText("下载进度：" + progress + "%");
                }
                if (progress == 100) {
                    File file = DownloadManager.getDownloadFile(downloadInfo.getUrl());
                    if (file != null) {
                        tv_progress.setText(file.getAbsolutePath());
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                isDownloading = false;
                tv_progress.setText("下载失败");
                Utility.shortToast(MainActivity.this, "下载失败");
            }

            @Override
            public void onComplete() {
                super.onComplete();
                isDownloading = false;
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
        params.put("imageFiles\";filename=\"" + System.currentTimeMillis() + ".png", requestBody);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.get:// 测试get请求
                testGet();
                break;
            case R.id.post:// 测试post请求
                testPost();
                break;
            case R.id.download:// 测试文件下载
                testDownload();
                break;
            case R.id.upload:// 测试文件上传
                if (TextUtils.isEmpty(token)) {
                    Utility.shortToast(MainActivity.this, "请先请求post接口");
                    return ;
                }
                selectSinglePicture();
                break;
            case R.id.tv_upload_path:// 复制文件下载路径
                String content = tv_upload_path.getText().toString().trim();
                if (!TextUtils.isEmpty(content)) {
                    Utility.copyContent(MainActivity.this, content);
                    Utility.shortToast(MainActivity.this, "复制成功");
                }
                break;
        }
    }
}
