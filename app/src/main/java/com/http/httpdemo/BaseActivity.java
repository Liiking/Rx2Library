package com.http.httpdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.http.httpdemo.http.ApiManager;
import com.http.httpdemo.util.Utility;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ui.ImageGridActivity;

/**
 * Created by qwy on 2018/5/23.
 * base activity 管理请求的订阅和取消，提供公共方法等
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected String disposableFlag;
    protected static final int REQUEST_CODE_SELECT_IMAGE = 1000;
    protected static final int PERMISSIONS_REQUEST_STORAGE = 2000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposableFlag = this.getLocalClassName() + System.currentTimeMillis();
    }

    protected String getDisposableFlag() {
        return disposableFlag;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApiManager.cancelSubscribeByFlag(getDisposableFlag());
    }

    /**
     * 选取单张图片
     */
    protected void selectSinglePicture() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 没有外部存储权限，动态获取
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PERMISSIONS_REQUEST_STORAGE);
        } else {
            selectPicture(false, 1);
        }

    }

    /**
     * 选取图片
     *
     * @param openCamera    是否直接打开拍照
     * @param count         选取最大张数
     */
    private void selectPicture(boolean openCamera, int count) {
        if (count <= 0) {
            return ;
        }
        ImagePicker.getInstance().setSelectLimit(count);
        Intent intent = new Intent(this, ImageGridActivity.class);
        if (openCamera) {
            intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS,true); // 是否是直接打开相机
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPicture(false, 1);
            } else {
                // Permission Denied
                Utility.shortToast(BaseActivity.this, "Permission Denied");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
