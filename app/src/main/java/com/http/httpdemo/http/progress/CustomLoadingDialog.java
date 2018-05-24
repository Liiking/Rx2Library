package com.http.httpdemo.http.progress;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import com.http.httpdemo.R;

/**
 * Created by qwy on 17/8/8.
 * 自定义加载框
 */
public class CustomLoadingDialog extends Dialog {

    public CustomLoadingDialog(Context context){
            super(context, R.style.CustomDialogStyle);
        }

    /**
     * 自定义加载框主题
     *
     * @param context   上下文
     * @param theme     主题资源
     */
    public CustomLoadingDialog(Context context, int theme){
            super(context, theme);
        }

        protected void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_custom_loading);
        }

}