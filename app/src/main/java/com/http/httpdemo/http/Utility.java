package com.http.httpdemo.http;

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
    private static int versionCode;
    private static String versionName;

    /**
     * 记录日志
     * @param msg
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

    public static void shortToastInMainThread(Context context, int msg) {
        if (context == null || msg == 0) {
            return ;
        }
        shortToastInMainThread(context, context.getResources().getString(msg));
    }

    public static void shortToastInMainThread(final Context context, final String msg) {
        if (context == null || TextUtils.isEmpty(msg)) {
            return ;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // 放在UI线程弹Toast
                shortToast(context, msg);
            }
        });
    }

    /**
     * 添加字段
     * @param key 字段名称
     * @param value 字段值
     */
    public static void put(Context context, String key, Object value) {
        try {
            if (key == null)
                return;
            SharedPreferences.Editor editor = context.getSharedPreferences(Utility.SP, 0).edit();// 获取编辑器
            if (value == null) {
                value = "";
            }
            String type = value.getClass().getSimpleName();
            if ("String".equals(type)) {
                String str = (String) value;
                // 加密
                str = Base64.encodeToString(str.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
                editor.putString(key, str);
            } else if ("Integer".equals(type)) {
                editor.putInt(key, (Integer) value);
            } else if ("Boolean".equals(type)) {
                editor.putBoolean(key, (Boolean) value);
            } else if ("Float".equals(type)) {
                editor.putFloat(key, (Float) value);
            } else if ("Long".equals(type)) {
                editor.putLong(key, (Long) value);
            } else {
                String str = bean2Json(value);
                str = Base64.encodeToString(str.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
                editor.putString(key, str);
            }
            editor.apply();// 提交修改
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存数据的方法
     * @param headers
     */
    public static void put(Context context, Map<String, String> headers) {
        try {
            SharedPreferences.Editor editor = context.getSharedPreferences(Utility.SP, 0).edit();// 获取编辑器
            if (headers != null) {
                Set<String> keys = headers.keySet();
                for (String key : keys) {
                    Object value = headers.get(key);
                    put(context, key, value);
                }
            }
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据字段名称删除字段
     * @param key 字段名称
     */
    public static void remove(Context context, String key) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Utility.SP, 0).edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * 清空所有字段名称
     * @throws Exception
     */
    public static void removeAll(Context context) throws Exception {
        SharedPreferences.Editor editor = context.getSharedPreferences(Utility.SP, 0).edit();
        editor.clear();
        editor.apply();
    }

    /**
     * 获取SharedPreference中保存的
     * @param context
     */
    public static <T> T get(Context context, String key, T defaultValue) {
        Class<?> clazz = String.class;
        if (defaultValue != null) {
            clazz = defaultValue.getClass();
        }
        return (T) get(context, key, clazz, defaultValue);
    }

    private static <T> Object get(Context context, String key, Class<?> valueType, T defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(Utility.SP, 0);
        String type = valueType.getSimpleName();
        if ("String".equals(type)) {
            String value = sp.getString(key, "");
            if (TextUtils.isEmpty(value)) {
                return defaultValue;
            } else {
                // 解密
                byte[] bt = Base64.decode(value, Base64.URL_SAFE | Base64.NO_WRAP);
                String strvalue = new String(bt);
                return strvalue;
            }

        } else if ("Integer".equals(type)) {
            return sp.getInt(key, (Integer) defaultValue);
        } else if ("Boolean".equals(type)) {
            return sp.getBoolean(key, (Boolean) defaultValue);
        } else if ("Float".equals(type)) {
            return sp.getFloat(key, (Float) defaultValue);
        } else if ("Long".equals(type)) {
            return sp.getLong(key, (Long) defaultValue);
        }
        return null;
    }

    /***
     * 将字符串转成对应的数字类型
     *
     * @param value
     * @param def
     * @return
     */

    public static <T> T transformNum(String value, T def) {
        T result = def;
        try {
            if (TextUtils.isEmpty(value)) {
                return result;
            }

            if (def.getClass().equals(Integer.class)) {
                try {
                    result = (T) Integer.valueOf(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (def.getClass().equals(Long.class)) {
                try {
                    result = (T) Long.valueOf(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (def.getClass().equals(Float.class)) {
                try {
                    result = (T) Float.valueOf(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (def.getClass().equals(Double.class)) {
                try {
                    result = (T) Double.valueOf(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    /***
     * 在map中取数据，处理一些默认的错误
     *
     * @param info
     * @param key
     * @return
     */
    public static <T> T getValueFromMap(Map<String, Object> info, String key, T def) {

        T result = def;
        try {
            if (info == null || info.size() <= 0) {
                return result;
            }
            Object tem = info.get(key);
            if (tem == null) {
                return result;
            }
            String value = tem.toString();
            if (def.getClass().equals(Integer.class)) {
                try {
                    result = (T) Integer.valueOf(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (def.getClass().equals(Long.class)) {
                try {
                    result = (T) Long.valueOf(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (def.getClass().equals(Float.class)) {
                try {
                    result = (T) Float.valueOf(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (def.getClass().equals(Double.class)) {
                try {
                    result = (T) Double.valueOf(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (def.getClass().equals(String.class)) {
                return (T) (tem + "");
            } else {
                return (T) tem;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * 复制指定内容到系统剪贴板
     * @param context
     * @param content
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
     * 根据应用包名判断是否已安装应用
     * @param packageName
     * @return
     */
    public static boolean hasInstallApk(Context context, String packageName) {
        PackageInfo info = getInstallApk(context, packageName);
        return info != null;
    }

    public static PackageInfo getInstallApk(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName)) {
                return pinfo.get(i);
            }
        }
        return null;
    }


    /**
     * 根据包名打开指定应用
     * @param context
     * @param packageName
     */
    public static void openByPackageName(Context context, String packageName) {
        try {
            if (hasInstallApk(context, packageName)) {
                Intent resolveIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                context.startActivity(resolveIntent);
            } else {
                shortToast(context, "找不到指定应用");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 安装应用
     * @param file
     * @param authority 例如："packagename.fileprovider"
     */
    public static void installApk(Context context, File file, String authority) {
        if(file.exists()) {
            chmod(file.getAbsolutePath());
        }
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            // 获取数据,通过provider方式兼容7.0
            Uri uri = null;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                uri = FileProvider.getUriForFile(context, authority, file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);// 给目标应用（系统安装程序）一个临时的授权
            } else {
                uri = Uri.fromFile(file);
            }
            // 设置数据和类型
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改文件或文件夹权限
     * @param path
     * @return
     */
    public static void chmod(String path) {
        String[] command = {"chmod", "777", path};
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否有网络
     * @param context
     * @return
     */
    public static boolean hasNet(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return mConnectivityManager.getActiveNetworkInfo() != null;
    }

    // 使用Log来显示调试信息,因为log在实现上每个message有4k字符长度限制
    // 所以这里使用自己分节的方式来输出足够长度的message
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
     * @param jsonStr
     * @return
     * @author lizhgb
     * @Date 2015-10-14 下午1:17:35
     */
    public static String formatJson(String jsonStr) {
        try {
            if (null == jsonStr || "".equals(jsonStr)) return "";
            jsonStr = unicode2GB(jsonStr);
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
     * @param unicodeStr
     * @return
     */
    public static String unicode2GB(String unicodeStr) {
        try {
            if (unicodeStr == null) {
                return null;
            }
            StringBuffer retBuf = new StringBuffer();
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
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return unicodeStr;
    }

    /**
     * 添加space
     * @param sb
     * @param indent
     * @author lizhgb
     * @Date 2015-10-14 上午10:38:04
     */
    private static void addIndentBlank(StringBuilder sb, int indent) {
        try {
            for (int i = 0; i < indent; i++) {
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
     * @param key
     * @return
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
     * @param number
     * @return
     */
    public static boolean isIdCardNumber(String number) {
        String pattern15 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";// 15位身份证号
        String pattern18 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";// 18位身份证号
        return checkPattern(number, pattern15) || checkPattern(number, pattern18);
    }

    /**
     * 是否是手机号
     * @param number
     * @return
     */
    public static boolean isPhoneNumber(String number) {
        String pattern = "^1\\d{10}$";
        return checkPattern(number, pattern);
    }

    /**
     * 是否是邮箱地址
     * @param email
     * @return
     */
    public static boolean isEmailAddress(String email) {
        String pattern = "^[A-Za-z0-9\\u4e00-\\u9fa5]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        return checkPattern(email, pattern);
    }

    /**
     * 根据给定pattern校验
     * @param src
     * @param pattern
     * @return
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

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem / (1024*1024);
    }

    public static void clearMemory(Context context) {
        ActivityManager activityManger = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManger.getRunningAppProcesses();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                ActivityManager.RunningAppProcessInfo apinfo = list.get(i);
                String[] pkgList = apinfo.pkgList;
                if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                    // Process.killProcess(apinfo.pid);
                    for (int j = 0; j < pkgList.length; j++) {
                        //2.2以上是过时的,请用killBackgroundProcesses代替
                        activityManger.killBackgroundProcesses(pkgList[j]);
                    }
                }
            }
        }
    }

    /**
     * 获取手机厂商
     * @return  手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取系统版本名称
     * @return
     */
    public static String getSystemVersionName(){
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取系统版本号
     * @return
     */
    public static int getSystemVersionCode(){
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取应用版本号
     * @return
     */
    public static int getVersionCode() {
        return versionCode;
    }

    public static void loadAppVersion (Context context) {
        if(versionName == null || versionCode <= 0) {
            // 获取软件版本号
            PackageInfo info;
            try {
                PackageManager pm = context.getPackageManager();
                if(pm != null) {
                    info = pm.getPackageInfo(context.getPackageName(), 0);
                    versionCode = info.versionCode;
                    versionName = info.versionName;
                    info = null;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取应用版本名
     * @return
     */
    public static String getVersionName() {
        return versionName;
    }

    /**
     * 获取Application中的<meta-data>元素值
     * @param context
     * @param name
     * @return
     */
    public static String getMetaData(Context context, String name){
        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.get(name).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 判断应用是否在前台运行
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将json串转为list数组
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    public static <T> List<T> getListFromJSON(String json, Class<T[]> type) {
        if (TextUtils.isEmpty(json) || !json.startsWith("[")) {
            return null;
        }
        T[] list = new Gson().fromJson(json, type);
        return Arrays.asList(list);
    }

    // 关闭软键盘
    public static void closeKeyboard(Activity context, EditText editText) {
        try {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(context.INPUT_METHOD_SERVICE);
            // imm.hideSoftInputFromWindow(context.getCurrentFocus().getApplicationWindowToken(),
            // InputMethodManager.HIDE_NOT_ALWAYS);
            if (editText != null) {
                if (imm.hideSoftInputFromWindow(editText.getWindowToken(), 0)) {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 打开软键盘
    public static void openKeyboard(Activity context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        // 得到InputMethodManager的实例
        if (imm.isActive()) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }

    }

    /**
     * 判断是否有新版
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
     * @param lastestVer 服务器最新版本号
     * @return
     */
    public static boolean hasNewVersion(Context context, String lastestVer) {
        return hasNewVersion(lastestVer, getCurVersionName(context));
    }

    /**
     * 获取版本名称
     * @return 当前应用的版本名称
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
     * @param src
     * @return
     */
    public static int getDigit(String src) {
        if (!TextUtils.isEmpty(src) && TextUtils.isDigitsOnly(src)) {
            return Integer.parseInt(src);
        }
        return 0;
    }

    /**
     * 获取手机宽度
     * @param context
     * @return
     */

    public static int getWindowWidth(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getWidth();

    }

    /**
     * 获取手机高度
     * @param context
     * @return
     */
    public static int getWindowHeight(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return display.getHeight();
    }

    /**
     * 设置view的margin值(像素)
     * @param v
     * @param l
     * @param t
     * @param r
     * @param b
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
     * @param time 要转换的时间毫秒值 不大于
     * @return
     */
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
     * @param format  格式化字符串，如："yyyy-MM-dd HH:mm:ss"
     * @return String String类型的当前日期时间
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

    /**
     * 描述：获取表示当前日期时间的字符串.
     * @return String String类型的当前日期时间
     */
    public static String getFormatDate(long millis) {
        return getFormatDate(millis, "yyyy-MM-dd HH:mm:ss");
    }

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
     *
     * @param date
     * @param format
     * @return millis
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
     * 获取安装的应用商店
     */
    public static ArrayList<String> getInstalledMarketPkgs(Context context) {
        ArrayList<String> pkgs = new ArrayList<String>();
        if (context == null)
            return pkgs;
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.APP_MARKET");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        if (infos == null || infos.size() == 0) {
            return pkgs;
        }
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            String pkgName = "";
            try {
                ActivityInfo activityInfo = infos.get(i).activityInfo;
                pkgName = activityInfo.packageName;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(pkgName)) {
                pkgs.add(pkgName);
            }
        }
        return pkgs;
    }

    /**
     * 启动到app详情界面
     * @param appPkg App的包名
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
     * 启动到app详情界面
     * @param appPkg App的包名
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
     * @param context
     * @param content
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
     * @param context
     * @param url
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
     * 图片按比例大小压缩方法
     * @param srcPath （根据路径获取图片并压缩）
     * @return
     */
    public static Bitmap getCompressBitmap(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    /**
     * 质量压缩方法
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;

        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public static void copyFileUsingFileChannels(File source, File dest) {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            try {
                inputChannel = new FileInputStream(source).getChannel();
                outputChannel = new FileOutputStream(dest).getChannel();
                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                inputChannel.close();
                outputChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 展示消息对话框
     *
     * @param context
     * @param message
     * @param okText
     * @param okListener
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

    /**
     * 显示智能的日期提示（例如：用于消息列表）
     * 五分钟内显示刚刚
     * 一小时内显示 x分钟前
     * 12小时内显示 x小时前
     * 超过12小时显示今天 HH:mm
     * 昨天的显示昨天 HH:mm
     * 一周内显示 x天前
     * 再之前的显示 MM/dd/yyyy HH:mm
     *
     * @param time
     * @return
     */
    public static String getSmartTime(Date time) {
        return getSmartTime(time, "yyyy-MM-dd HH:mm");
    }

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
     * 显示智能的聊天记录日期提示
     * 一分钟内显示刚刚
     * 一小时内显示 x分钟前
     * 超过一小时显示今天 HH:mm
     * 昨天的显示昨天 HH:mm
     * 一周内显示 x天前
     * 再之前的显示 MM/dd/yyyy HH:mm
     *
     * @param time
     * @return
     */
    public static String getSmartChatTime(Date time) {
        return getSmartChatTime(time, "MM/dd/yyyy HH:mm");
    }

    public static String getSmartChatTime(Date time, String format) {
        if (time == null) {
            return null;
        }
        long minutes = (System.currentTimeMillis() - time.getTime()) / 60000;
        if (minutes <= 59) {
            if (minutes < 1) {
                return "刚刚";
            } else {
                return minutes + "分钟前";
            }
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

    /**
     * 格式化日期
     * @param date
     * @param format
     * 例如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String formatDateTime(Date date, String format) {
        if(date == null){
            return null;
        }else{
            SimpleDateFormat formater = new SimpleDateFormat(format);
            return formater.format(date);
        }
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 将字符串转化为Date
     * @param val 格式为yyyy-MM-dd HH:mm:ss
     * @return
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
     * 将bitmap保存到相册并通知图库刷新
     * @param context
     * @param bmp
     */
    public static String saveBitmapToGallery(Context context, Bitmap bmp) {
        if (bmp == null) {
            return "";
        }
        // 首先保存图片
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();// 注意小米手机必须这样获得public绝对路径
        File appDir = new File(file, "新建文件夹");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File target = new File(appDir, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(target);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    target.getAbsolutePath(), fileName, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        // 通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(target.getPath()))));
        return target.getPath();
    }

    /**
     * 将json转化成对应的JavaBean
     * @param json
     * @return
     */
    public static <T> T getBeanFromJson(String json, Class<T> cl) throws Exception {
        if (TextUtils.isEmpty(json) || cl == null)
            return null;
        Gson gson = new Gson();
        return (T) gson.fromJson(json, cl);
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
     * @param params 参数
     * @return Json字符串
     */
    public static String getJsonFromMap(Map<String, Object> params) {
        if (params == null || params.size() <= 0) {
            return "";
        }
        return getJSONObjectFromMap(params).toString();
    }

    /**
     * 将Map<String, String>对象转换为JSON字符串
     * @param params 参数
     * @return Json字符串
     */
    public static <T> String getJsonFromList(ArrayList<T> params) {
        try {
            if (params == null || params.size() <= 0) {
                return "";
            }
            JSONArray array = new JSONArray();
            for (Object o : params) {
                if (o instanceof Map) {
                    array.put(getJSONObjectFromMap((Map<String, Object>) o));
                } else {
                    array.put(o);
                }
            }
            return array.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将Map<String, String>对象转换为JSONObject字符串
     * @param params
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
     * @param t
     * @return
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
