package com.hwx.safelock.safelock.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;

@Entity
public class ConfigBean implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    @Property(nameInDb = "videoUrl")//name
    private String videoUrl;
    @Property(nameInDb = "videoUrlTag")//iamge
    private String videoUrlTag;
    @Property(nameInDb = "userId")//辅料
    private String userId;

    public ConfigBean() {
    }

    public ConfigBean(String videoUrl, String videoUrlTag, String userId) {
        this.videoUrl = videoUrl;
        this.videoUrlTag = videoUrlTag;
        this.userId = userId;
    }

    @Generated(hash = 787906453)
    public ConfigBean(Long id, String videoUrl, String videoUrlTag, String userId) {
        this.id = id;
        this.videoUrl = videoUrl;
        this.videoUrlTag = videoUrlTag;
        this.userId = userId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoUrlTag() {
        return videoUrlTag;
    }

    public void setVideoUrlTag(String videoUrlTag) {
        this.videoUrlTag = videoUrlTag;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}