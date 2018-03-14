package com.Alan.eva.model;

/**
 * Created by CW on 2017/3/9.
 * 我的孩子数据模型
 */
public class ChildModel {
    private String cid;
    private String pid;
    private String name;
    private String weight;
    private String height;
    private String age;
    private String gender;
    private String highestTemp;
    private String lowestTemp;
    private String kickTime;
    private String feverTime;
    private String createTime;
    private String portrait;
    private boolean owner;
    private boolean isDefault;

    public String getCid() {
        return cid;
    }

    public String getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public String getWeight() {
        return weight;
    }

    public String getHeight() {
        return height;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getHighestTemp() {
        return highestTemp;
    }

    public String getLowestTemp() {
        return lowestTemp;
    }

    public String getKickTime() {
        return kickTime;
    }

    public String getFeverTime() {
        return feverTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getPortrait() {
        return portrait;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
