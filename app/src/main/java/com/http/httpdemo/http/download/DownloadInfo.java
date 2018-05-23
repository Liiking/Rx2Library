package com.http.httpdemo.http.download;

/**
 * Created by qwy on 2017/2/2.
 * 下载信息
 */
public class DownloadInfo {
    public static final long TOTAL_ERROR = -1;// 获取进度失败
    private String url;// 要下载的文件地址
    private long total;// 文件总长度
    private long progress;// 当前下载文件长度
    private String fileName;// 保存文件名

    public DownloadInfo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }
}
