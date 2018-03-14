package com.Alan.eva.result;

import com.Alan.eva.tools.Tools;

/**
 * Created by CW on 2017/3/2.
 * 抽象数据回调
 */
public class Res {
    private int code;
    private String msg;

    public boolean isOk() {
        return code == Tools.MSG_OK;
    }

    public String msg() {
        return msg;
    }

    public int code() {
        return code;
    }
}
