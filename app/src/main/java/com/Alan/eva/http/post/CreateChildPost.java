package com.Alan.eva.http.post;

import com.Alan.eva.http.core.AbsHttp;
import com.Alan.eva.http.core.ReqParam;

import java.io.File;

/**
 * Created by CW on 2017/3/14.
 * 创建孩子提交
 */
public class CreateChildPost extends AbsHttp {
    private String pid;
    private String name;
    private String gender;
    private String age;
    private String height;
    private String weight;
    private String portrait;

    @Override
    protected String domain() {
        return "child/create";
    }

    @Override
    protected ReqParam setParams(ReqParam builder) {
        builder.put("pid", pid);
        builder.put("name", name);
        builder.put("gender", gender);
        builder.put("age", age);
        builder.put("height", height);
        builder.put("weight", weight);
        return builder;
    }

    @Override
    protected boolean addFile(ReqParam params) {
        params.put("portrait", new File(portrait));
        return true;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

}
