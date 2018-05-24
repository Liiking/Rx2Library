package com.http.httpdemo.model;

import java.io.Serializable;

/**
 * Created by qwy on 17/8/29.
 * 上传图片实体
 */
public class UploadPhoto implements Serializable {

    /**
     * size : 757475
     * width : 720
     * height : 1280
     * mime : image/jpeg
     * url : http://oobum40ca.bkt.clouddn.com/20170829/fbdc66ba205bed70342149f16ababa19.
     */
    private long size;
    private int w;
    private int h;
    private String filetype;
    private String path;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
