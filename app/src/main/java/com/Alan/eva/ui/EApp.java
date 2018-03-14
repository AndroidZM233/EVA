package com.Alan.eva.ui;


import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;

import com.Alan.eva.BuildConfig;
import com.Alan.eva.model.UserInfo;
import com.Alan.eva.tools.LogUtil;
import com.Alan.eva.tools.SpTools;
import com.Alan.eva.tools.Tools;
import com.umeng.analytics.MobclickAgent;

import org.xutils.x;

public class EApp extends Application {
    private static EApp app;
    private UserInfo userInfo;
    private int screenWidth;
    private int screenHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG_MODE);
        MobclickAgent.setDebugMode(BuildConfig.DEBUG_MODE);
        MobclickAgent.setCatchUncaughtExceptions(true);
    }


    public static EApp getApp() {
        return app;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtil.info("应用挂了~~~~");
    }

    /**
     * 获取用户信息内容
     *
     * @param activity 上下文
     * @return 用户信息
     */
    public UserInfo getUserInfo(Activity activity) {
        if (userInfo == null) {
            String userStr = SpTools.getInstance(activity).getUserInfo();
            if (TextUtils.isEmpty(userStr)) {
                return null;
            }
            userInfo = Tools.json2Bean(userStr, UserInfo.class);
        }
        return userInfo;
    }

    /**
     * 保存用户信息
     *
     * @param userInfo 用户信息
     */
    public void setUserInfo(UserInfo userInfo, Activity activity) {
        this.userInfo = userInfo;
        if (userInfo != null) {
            String userStr = Tools.bean2Json(userInfo);
            SpTools.getInstance(activity).setUserInfo(userStr);
        } else {
            SpTools.getInstance(activity).setUserInfo("");
        }
    }

    /**
     * 获取全局屏幕宽度
     *
     * @return 屏幕宽度
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * 在app进入主界面的时候调用一次set方法， 将屏幕宽高保存到application里面，这样全局都可以使用
     */
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    /**
     * 获取全局屏幕高度
     *
     * @return 屏幕高度
     */
    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * 保存高度，在app首页中调用一次即可，和set宽一样
     *
     * @param screenHeight 屏幕高度
     */
    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }
}
