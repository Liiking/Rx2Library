package com.http.httpdemo;

import android.app.Application;
import android.content.Context;

import com.http.httpdemo.http.ApiManager;
import com.http.httpdemo.util.MyImageLoader;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.view.CropImageView;

/**
 * Created by qianweiying on 2018/5/16.
 * 自定义application
 */

public class MyApplication extends Application {

    private String baseUrl = "http://api.bigplayer666.com/";
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        ApiManager.initApiManger(this, baseUrl);
        initImagePicker();
    }

    /**
     * 初始化ImagePicker
     */
    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new MyImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  // 显示拍照按钮
        imagePicker.setCrop(true);        // 允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(9);    // 选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  // 裁剪框的形状
        imagePicker.setFocusWidth(800);   // 裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  // 裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);// 保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);// 保存文件的高度。单位像素
    }

}
