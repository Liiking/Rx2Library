package com.http.httpdemo.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by qwy on 2016/1/20.
 * 工具类
 */
public class Utility {

    public static String SP = "shared_preference";
    public static boolean showLog = true;// 是否打印日志,true打印日志

    /**
     * 打印日志
     *
     * @param msg   要打印的内容
     */
    public static void log(String tag, String msg) {
        if (showLog) {
            Log.i(tag, msg);
        }
    }

    public static void log(String msg) {
        log("Log", msg);
    }

    public static void log(Context context, int msg) {
        if (context != null) {
            log(context.getResources().getString(msg));
        }
    }

    /**
     * 展示toast信息
     *
     * @param context   上下文
     * @param msg       要展示的信息资源
     */
    public static void shortToast(Context context, int msg) {
        if (context == null || msg == 0) {
            return ;
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void shortToast(Context context, String msg) {
        if (context == null || TextUtils.isEmpty(msg)) {
            return ;
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 在主线程展示toast信息
     *
     * @param context       上下文
     * @param msg           要展示的信息
     */
    public static void shortToastInMainThread(Context context, int msg) {
        if (context == null || msg == 0) {
            return ;
        }
        shortToastInMainThread(context, context.getResources().getString(msg));
    }

    /**
     * 主线程弹出toast
     *
     * @param context   上下文
     * @param msg       要展示的toast信息
     */
    public static void shortToastInMainThread(final Context context, final String msg) {
        if (context == null || TextUtils.isEmpty(msg)) {
            return ;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                shortToast(context, msg);
            }
        });
    }

    /**
     * 是否有网络
     *
     * @param context   上下文
     */
    public static boolean hasNet(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return mConnectivityManager != null && mConnectivityManager.getActiveNetworkInfo() != null;
    }

    /**
     * 复制指定内容到系统剪贴板
     *
     * @param context   上下文
     * @param content   要复制的内容
     */
    public static void copyContent(Context context, String content) {
        if (TextUtils.isEmpty(content)) {
            return ;
        }
        // 获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", content);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
        }
    }

    /**
     * 使用Log来显示调试信息,因为log在实现上每个message有4k字符长度限制
     *
     * @param tag   tag
     * @param str   要展示的信息
     */
    public static void LogTooLongE(String tag, String str) {
        try {
            str = str.trim();
            int index = 0;
            int maxLength = 4000;
            String sub;
            while (index < str.length()) {
                // java的字符不允许指定超过总的长度end
                if (str.length() <= index + maxLength) {
                    sub = str.substring(index);
                } else {
                    sub = str.substring(index, index + maxLength);
                }

                index += maxLength;
                log(tag, sub.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 格式化
     *
     * @param jsonStr   要格式化的json字符串
     */
    public static String formatJson(String jsonStr) {
        try {
            jsonStr = unicode2GB(jsonStr);
            if (TextUtils.isEmpty(jsonStr)) {
                return "";
            }
            jsonStr = new String(jsonStr.getBytes(), "UTF-8");
            StringBuilder sb = new StringBuilder();
            char last = '\0';
            char current = '\0';
            int indent = 0;
            for (int i = 0; i < jsonStr.length(); i++) {
                last = current;
                current = jsonStr.charAt(i);
                switch (current) {
                    case '{':
                    case '[':
                        sb.append(current);
                        sb.append('\n');
                        indent++;
                        addIndentBlank(sb, indent);
                        break;
                    case '}':
                    case ']':
                        sb.append('\n');
                        indent--;
                        addIndentBlank(sb, indent);
                        sb.append(current);
                        break;
                    case ',':
                        sb.append(current);
                        if (last != '\\') {
                            sb.append('\n');
                            addIndentBlank(sb, indent);
                        }
                        break;
                    default:
                        sb.append(current);
                }
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    /**
     * 编码转换，Unicode -> GB2312
     *
     * @param unicodeStr    要转换成GB2312的Unicode编码的内容
     * @return              转换后的字符串
     */
    public static String unicode2GB(String unicodeStr) {
        try {
            if (TextUtils.isEmpty(unicodeStr)) {
                return "";
            }
            StringBuilder retBuf = new StringBuilder();
            int maxLoop = unicodeStr.length();
            for (int i = 0; i < maxLoop; i++) {
                if (unicodeStr.charAt(i) == '\\') {
                    if ((i < maxLoop - 5) && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr.charAt(i + 1) == 'U')))
                        try {
                            retBuf.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
                            i += 5;
                        } catch (NumberFormatException localNumberFormatException) {
                            retBuf.append(unicodeStr.charAt(i));
                        }
                    else
                        retBuf.append(unicodeStr.charAt(i));
                } else {
                    retBuf.append(unicodeStr.charAt(i));
                }
            }
            return retBuf.toString();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 指定内容添加count个tab，用于格式化
     *
     * @param sb        待添加tab的内容
     * @param count     要添加的tab数
     */
    private static void addIndentBlank(StringBuilder sb, int count) {
        try {
            for (int i = 0; i < count; i++) {
                sb.append('\t');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int a=(int)(pxValue/scale+0.5f);
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2dp(Resources resources, float px) {
        final float scale = resources.getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }

    /**
     * md5加密
     *
     * @param key   加密的key
     * @return      md5加密后的字符串
     */
    public static String md5(String key) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(key.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 是否是身份证号
     *
     * @param number 身份证号
     */
    public static boolean isIdCardNumber(String number) {
        String pattern15 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";// 15位身份证号
        String pattern18 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";// 18位身份证号
        return checkPattern(number, pattern15) || checkPattern(number, pattern18);
    }

    /**
     * 是否是手机号
     *
     * @param number    手机号
     */
    public static boolean isPhoneNumber(String number) {
        String pattern = "^1\\d{10}$";
        return checkPattern(number, pattern);
    }

    /**
     * 是否是邮箱地址
     *
     * @param email     邮箱地址
     */
    public static boolean isEmailAddress(String email) {
        String pattern = "^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        return checkPattern(email, pattern);
    }

    /**
     * 根据给定pattern校验
     *
     * @param src       待校验的内容
     * @param pattern   校验规则
     */
    public static boolean checkPattern(String src, String pattern) {
        if (TextUtils.isEmpty(src)) {
            return false;
        }
        if (TextUtils.isEmpty(pattern)) {
            return true;
        }
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(src);
        return m.matches();
    }

    /**
     * 判断是否有新版
     *
     * @param serverVer 服务器端版本号
     * @param localVer  本地版本号
     */
    public static boolean hasNewVersion(String serverVer, String localVer) {
        if (!TextUtils.isEmpty(serverVer)) {
            String[] lastVer = serverVer.split("\\.");
            String[] curVer = localVer.split("\\.");
            int len = curVer.length;
            for (int i = 0; i < lastVer.length; i++) {
                if (len > i) {
                    int v1 = getDigit(lastVer[i]);
                    int v2 = getDigit(curVer[i]);
                    if (v1 > v2) {
                        return true;
                    } else if (v1 < v2) {
                        return false;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否有新版
     *
     * @param latestVer 服务器最新版本号
     */
    public static boolean hasNewVersion(Context context, String latestVer) {
        return hasNewVersion(latestVer, getCurVersionName(context));
    }

    /**
     * 获取版本名称
     *
     * @param context   上下文
     * @return          当前应用的版本名称
     */
    public static String getCurVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 从字符串取数字，非全数字的返回0
     *
     * @param src       数据源
     */
    public static int getDigit(String src) {
        if (!TextUtils.isEmpty(src) && TextUtils.isDigitsOnly(src)) {
            return Integer.parseInt(src);
        }
        return 0;
    }

    /**
     * 获取手机宽度
     *
     * @param context   上下文
     */

    public static int getWindowWidth(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getWidth();

    }

    /**
     * 获取手机高度
     *
     * @param context   上下文
     */
    public static int getWindowHeight(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getHeight();
    }

    /**
     * 设置view的margin值(像素)
     *
     * @param v         要设置margin的view
     * @param l         要设置的 margin left
     * @param t         要设置的 margin top
     * @param r         要设置的 margin right
     * @param b         要设置的 margin bottom
     */
    public static void setMargins(View v, int l, int t, int r, int b) {
        ViewGroup.MarginLayoutParams p = null;
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        }else {
            p = new ViewGroup.MarginLayoutParams(v.getLayoutParams());
        }
        p.setMargins(l, t, r, b);
        v.requestLayout();
    }

    /**
     * format时间,智能显示,用于视频播放显示时长 最大显示: 59:59:59
     *
     * @param time      要转换的时间毫秒值 不大于
     */
    @SuppressLint("SimpleDateFormat")
    public static String getSmartVideoTime(long time){
        if(time <= 0){
            return "00:00";
        }
        if(time / 1000 >= 60 * 60 * 59){
            return "59:59:59";
        }
        SimpleDateFormat f = null;
        if(time < 60 * 60 * 1000){
            f = new SimpleDateFormat("mm:ss");
        }else {
            f = new SimpleDateFormat("HH:mm:ss");
        }
        return f.format(time);
    }

    /**
     * 描述：获取表示当前日期时间的字符串.
     *
     * @param format    格式化字符串，如："yyyy-MM-dd HH:mm:ss"
     * @return          String类型的当前日期时间
     */
    public static String getCurrentDate(String format) {
        String curDateTime = null;
        try {
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            Calendar c = new GregorianCalendar();
            curDateTime = mSimpleDateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curDateTime;
    }

    public static String getFormatDate(long millis) {
        return getFormatDate(millis, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 描述：获取表示当前日期时间的字符串.
     *
     * @param millis    要转换的时间戳，单位 毫秒
     * @param format    转换格式
     * @return          String类型的当前日期时间
     */
    public static String getFormatDate(long millis, String format) {
        try {
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            return mSimpleDateFormat.format(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static long getDateFromStr(String date) {
        return getDateFromStr(date, "yyyy-MM-dd");
    }

    /**
     * 根据String类型的日期获取对应的时间戳
     *
     * @param date      日期，String类型
     * @param format    对应date的格式 如：2018-05-24 对应 yyyy-MM-dd
     * @return          返回转换后的时间戳
     */
    public static long getDateFromStr(String date, String format) {
        try {
            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format);
            return mSimpleDateFormat.parse(date).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取手机上安装的应用商店package name列表
     */
    public static ArrayList<String> getInstalledMarketPackages(Context context) {
        ArrayList<String> packages = new ArrayList<String>();
        if (context == null)
            return packages;
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.APP_MARKET");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infoList = pm.queryIntentActivities(intent, 0);
        if (infoList == null || infoList.size() == 0) {
            return packages;
        }
        int size = infoList.size();
        for (int i = 0; i < size; i++) {
            String pkgName = "";
            try {
                ActivityInfo activityInfo = infoList.get(i).activityInfo;
                pkgName = activityInfo.packageName;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(pkgName)) {
                packages.add(pkgName);
            }
        }
        return packages;
    }

    /**
     * 启动到app详情界面
     *
     * @param appPkg    App的包名
     * @param marketPkg 应用商店包名 ,如果为""则由系统弹出应用商店列表供用户选择,否则调转到目标市场的应用详情界面，某些应用商店可能会失败
     */
    public static void launchAppDetail(Context context, String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg))
                return;
            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg))
                intent.setPackage(marketPkg);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动应用市场并转到app详情界面
     *
     * @param appPkg    App的包名
     */
    public static void launchAppDetail(Context context, String appPkg) {
        try {
            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开系统分享对话框
     *
     * @param context   上下文
     * @param content   要分享的内容
     */
    public static void openSystemShare(Context context, String content){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        context.startActivity(Intent.createChooser(sendIntent, "分享给好友"));
    }

    /**
     * 打开默认浏览器到指定地址
     *
     * @param context   上下文
     * @param url       要打开的地址
     */
    public static void openExplore(Context context, String url){
        if(TextUtils.isEmpty(url)){
            return ;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        context.startActivity(intent);
    }

    /**
     * 展示消息对话框
     *
     * @param context           上下文
     * @param message           提示消息
     * @param okText            positive按钮文案
     * @param okListener        positive按钮点击监听
     */
    public static void showMessageDialog(Context context, int message, String okText, final View.OnClickListener okListener) {
        showMessageDialog(context, "提示", context.getResources().getString(message), okText, okListener);
    }

    public static void showMessageDialog(Context context, String title, String message, String okText, final View.OnClickListener okListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(true)
                .setNegativeButton("取消", null)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (okListener != null) {
                            okListener.onClick(null);
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    public static void showMessageDialog(Context context, int message) {
        showMessageDialog(context, message, "确定", null);
    }

    public static String getSmartTime(Date time) {
        return getSmartTime(time, "yyyy-MM-dd HH:mm");
    }

    /**
     * 显示智能的日期提示（例如：用于消息列表）
     * 五分钟内显示刚刚
     * 一小时内显示 x分钟前
     * 12小时内显示 x小时前
     * 超过12小时显示今天 HH:mm
     * 昨天的显示昨天 HH:mm
     * 一周内显示 x天前
     * 再之前的显示 根据format转换 如 MM/dd/yyyy HH:mm
     *
     * @param time      要转换的日期 Date类型
     * @param format    超过一周的日期转换格式
     * @return          转换后的日期 String类型
     */
    public static String getSmartTime(Date time, String format) {
        if (time == null) {
            return null;
        }
        long minutes = (System.currentTimeMillis() - time.getTime()) / 60000;
        if (minutes <= 59) {
            if (minutes < 5) {
                return "刚刚";
            } else {
                return minutes + "分钟前";
            }
        } else {
            long hours = (System.currentTimeMillis() - time.getTime()) / 3600000;
            if (hours <= 12) {
                return hours + "小时前";
            } else {
                Date now = new Date();
                Date today = parseDate(formatDateTime(now, "yyyy-MM-dd 00:00:00"));
                if (time.getTime() > today.getTime()) {
                    // 今天
                    return formatDateTime(time, "今天 HH:mm");
                } else {
                    Date yesterday = parseDate(formatDateTime(new Date(now.getTime() - 86400000), "yyyy-MM-dd 00:00:00"));
                    if (time.getTime() > yesterday.getTime()) {
                        // 昨天
                        return formatDateTime(time, "昨天 HH:mm");
                    } else {
                        long days = (System.currentTimeMillis() - time.getTime()) / 3600000 / 24;
                        if (days < 7) {
                            return days + "天前";
                        } else {
                            // 之前的了
                            return formatDateTime(time, format);
                        }
                    }
                }
            }
        }
    }

    /**
     * 格式化日期
     *
     * @param date      要格式化的日期
     * @param format    转换格式
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatDateTime(Date date, String format) {
        if(date == null){
            return null;
        }else{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            return simpleDateFormat.format(date);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 将字符串转化为Date
     *
     * @param val 格式为yyyy-MM-dd HH:mm:ss
     */
    public static Date parseDate(String val){
        if(val == null){
            return null;
        }else{
            Date ret = null;
            try {
                ret = dateFormat.parse(val.toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return ret;
        }
    }

    /**
     * 将json转化成对应的JavaBean
     *
     * @param json      要转换的json内容
     * @param clazz     要转换的class文件
     */
    public static <T> T getBeanFromJson(String json, Class<T> clazz) throws Exception {
        if (TextUtils.isEmpty(json) || clazz == null)
            return null;
        Gson gson = new Gson();
        return (T) gson.fromJson(json, clazz);
    }

    /**
     * 将json转化成对应的JavaBean
     */
    public static <T> T getBeanFromMap(Map<String, Object> map, Class<T> cl) {
        try {
            return getBeanFromJson(getJsonFromMap(map), cl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将Map<String, String>对象转换为JSON字符串
     *
     * @param params        参数
     * @return              Json字符串
     */
    public static String getJsonFromMap(Map<String, Object> params) {
        if (params == null || params.size() <= 0) {
            return "";
        }
        return getJSONObjectFromMap(params).toString();
    }

    /**
     * 将Map<String, String>对象转换为JSONObject字符串
     *
     * @param params        要转换的map
     */
    @SuppressWarnings("unchecked")
    private static JSONObject getJSONObjectFromMap(Map<String, Object> params) {
        Set<Map.Entry<String, Object>> entrySet = params.entrySet();
        JSONObject object = new JSONObject();
        for (Map.Entry<String, Object> entry : entrySet) {
            try {
                Object ob = entry.getValue();
                if (ob instanceof List) {
                    List list = (List) ob;
                    if (list.size() == 0) {
                        continue;
                    }
                    JSONArray array = new JSONArray();
                    for (Object o : list) {
                        if (o instanceof Map) {
                            array.put(getJSONObjectFromMap((Map<String, Object>) o));
                        } else {
                            array.put(o);
                        }
                    }
                    object.put(entry.getKey(), array);
                } else if (ob instanceof Map) {
                    object.put(entry.getKey(), getJSONObjectFromMap((Map<String, Object>) ob));
                } else {
                    object.put(entry.getKey(), entry.getValue());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    /**
     * 将对象传换成Json串
     *
     * @param t     要转换的对象
     * @return      转换后的json字符串
     */
    public static <T> String bean2Json(T t) {
        try {
            if (t == null)
                return "";
            Gson gson = new Gson();
            return gson.toJson(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
