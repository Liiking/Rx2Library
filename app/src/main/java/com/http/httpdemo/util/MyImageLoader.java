package com.http.httpdemo.util;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lzy.imagepicker.loader.ImageLoader;

import java.io.File;

/**
 * 自定义image loader，用于ImagePicker
 */
public class MyImageLoader implements ImageLoader {

    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
        Glide.with(activity)
                .load(Uri.fromFile(new File(path)))
//                .placeholder(R.mipmap.default_image)
//                .error(R.mipmap.default_image)
                .into(imageView);
    }

    @Override
    public void displayImagePreview(Activity activity, String s, ImageView imageView, int i, int i1) {
        Glide.with(activity)                                // 配置上下文
                .load(Uri.fromFile(new File(s)))            // 设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .diskCacheStrategy(DiskCacheStrategy.ALL)   // 缓存全尺寸
                .into(imageView);
    }

    @Override
    public void clearMemoryCache() {
        // 这里是清除缓存的方法,根据需要自己实现
    }
}
