package com.http.httpdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.http.httpdemo.http.ApiManager;

import me.nereo.multi_image_selector.MultiImageSelector;

/**
 * Created by qwy on 2018/5/23.
 * 生命周期管理
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
            selectPicture(true, 1);
        }

    }

    /**
     * 选取图片
     *
     * @param showCamera    是否显示拍照
     * @param count         选取最大张数
     */
    private void selectPicture(boolean showCamera, int count) {
        if (count <= 0) {
            return ;
        }
        if (count == 1) {
            MultiImageSelector.create()
                    .showCamera(showCamera) // show camera or not. true by default
                    .single() // single mode
                    .start(BaseActivity.this, REQUEST_CODE_SELECT_IMAGE);
        } else {
            MultiImageSelector.create()
                    .showCamera(showCamera) // show camera or not. true by default
                    .count(count) // max select image size, 9 by default. used width #.multi()
                    .multi() // multi mode, default mode;
//                .origin(ArrayList<String>) // original select data set, used width #.multi()
                    .start(BaseActivity.this, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPicture(true, 1);
            } else {
                // Permission Denied
                Utility.shortToast(BaseActivity.this, "Permission Denied");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
