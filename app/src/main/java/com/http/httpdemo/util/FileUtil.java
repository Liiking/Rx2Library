package com.http.httpdemo.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.http.httpdemo.MyApplication;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class FileUtil {

    private static String root = null;
    // 应用的总文件夹名称
    public static final String APP_PATH = "apps";

    /**
     * 程序包数据存储路径
     * <p>
     * eg: storage/emulated/0/Android/data/packagename
     */
    public static String getPackagePath() {
        File appFile = MyApplication.context.getExternalFilesDir(null);
        if (appFile != null)
            return appFile.getParent();

        return getSDCardRootPath()
                + "Android/data/"
                + MyApplication.context.getPackageName();
    }

    public static String getDownloadPath(Context context) {
        if (root == null) {
            if (context != null) {
                File file = getCacheDirectory(context, true);
                if (file != null) {
                    root = file.getAbsolutePath() + File.separatorChar + APP_PATH + File.separatorChar;
                    // 目录/data/data/package/cache/
                    requestPermission(root);
                }
            }
        }

        if (root != null) {
            try {
                createDirectory(root);
                File file = new File(root, ".nomedia");
                if (!file.exists())
                    file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return root;
    }

    /***
     * 获取根文件路径
     * @param context
     * @param preferExternal 是否使用外部SDcard,true：使用外部SDcard,false:使用Android下包路径的cache路径
     * @return
     */
    private static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (Exception e) {
            externalStorageState = "";
        }
        if (preferExternal && Environment.MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
            ///storage/emulated/0/Android/data/package/cache/
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
            ///data/data/package/cache/apis/
        }
        return appCacheDir;
    }

    /***
     * 获取SDcard外部文件路径
     *../android/data/data/....
     * @param context
     * @return
     */
    public static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {

                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {

            }
        }
        return appCacheDir;
    }

    /***
     * 判断是否有写外部文件的权限
     * @param context
     * @return
     */
    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 修改文件或文件夹权限
     * @param path
     * @return
     */
    public static void requestPermission(String path) {
        String[] command = {"chmod", "777", path};
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取程序文件数据存储路径 <br/>
     * <p>
     * eg: storage/emulated/0/Android/data/com.goopal.grb.xkey/files
     */
    public static String getExternalFilesPath() {
        //storage/emulated/0/Android/data/com.goopal.grb.xkey/files
        File appFile = MyApplication.context.getExternalFilesDir(null);
        if (appFile != null)
            return appFile.getAbsolutePath();

        return getSDCardRootPath()
                + "Android/data/"
                + MyApplication.context.getPackageName()
                + File.separator
                + "files";
    }

    /**
     * sd卡根目录： SDCard/
     */
    public static String getSDCardRootPath() {
        return Environment.getExternalStorageDirectory().toString() + "/";
    }

    /**
     * 创建保存下载文件的目录
     */
    public static void createFilePath(String userName) {
        createDirectory(getFilePath(userName));
    }

    /**
     * 检查路径是否存在
     */
    public static boolean checkFilePathExists(String path) {
        return path != null && new File(path).exists();
    }

    /**
     * 删除文件
     */
    public static boolean deleteFileWithPath(String filePath) {
        if (!checkFilePathExists(filePath)) {
            return false;
        }
        SecurityManager checker = new SecurityManager();
        File f = new File(filePath);
        checker.checkDelete(filePath);
        if (f.isFile()) {
            return f.delete();
        }
        return false;
    }

    /**
     * 新建目录
     */
    public static boolean createDirectory(String directory) {
        if (checkFilePathExists(directory)) {
            return true;
        }
        if (TextUtils.isEmpty(directory))
            return false;
        return new File(directory).mkdirs();
    }

    /**
     * 文件存储路径
     * <p>
     * eg: SDCard/Android/data/packageName/files/张三/2018-03-14/
     */
    public static String getFilePath(String userName) {
        return getExternalFilesPath()
                + File.separator
                + userName
                + File.separator
                + Utility.formatDateTime(new Date(), "yyyy-MM-dd")
                + File.separator;
    }

}
