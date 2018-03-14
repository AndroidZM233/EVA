package com.Alan.eva.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.Alan.eva.R;
import com.Alan.eva.http.core.IResultHandler;
import com.Alan.eva.http.get.MonitorGet;
import com.Alan.eva.result.MonitorRes;
import com.Alan.eva.tools.LogUtil;
import com.Alan.eva.tools.Tools;
import com.Alan.eva.ui.core.AbsActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by CW on 2017/3/20.
 * 监测页面
 */
public class MonitorActivity extends AbsActivity implements IResultHandler, Callback {
    private AppCompatTextView tv_monitor_temp_data;
    private AppCompatTextView tv_monitor_temp_tips;
    /*循环请求机*/
    private ScheduledExecutorService scheduledExecutor;
    private final int LOOPER_CODE = 0x0099;
    private String monitorId;

    @Override
    public Activity getCurrActivity() {
        return this;
    }

    @Override
    public int getRootViewId() {
        return R.layout.ac_monitor_temp;
    }

    @Override
    public void findView(View rootView) {
        Toolbar tool_bar_home_title = (Toolbar) getView(R.id.tool_bar_monitor_title);
        tool_bar_home_title.setTitleTextColor(Tools.getColor(getCurrActivity(), R.color.white));
        tool_bar_home_title.setTitle(R.string.monitor);
        setSupportActionBar(tool_bar_home_title);
        tool_bar_home_title.setNavigationIcon(R.mipmap.ic_flag_back);
        tool_bar_home_title.setNavigationOnClickListener((View v) -> currFinish());
        tv_monitor_temp_data = (AppCompatTextView) getView(R.id.tv_monitor_temp_data);
        tv_monitor_temp_tips = (AppCompatTextView) getView(R.id.tv_monitor_temp_tips);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        monitorId = intent.getStringExtra("cid");
        startMonitor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMonitor();
    }

    /**
     * 启动监护
     */
    private void startMonitor() {
        if (scheduledExecutor == null) {
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        Handler monitorHandler = new Handler(this);
        scheduledExecutor.scheduleWithFixedDelay(() ->
                monitorHandler.sendEmptyMessage(LOOPER_CODE), 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 停止监护
     */
    private void stopMonitor() {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        scheduledExecutor = null;
    }

    private MonitorGet monitorGet;
    private final int MONITOR_CODE = 0x0088;

    @Override
    public void handleStart(int code) {
        if (code == MONITOR_CODE) {
            LogUtil.info("异地监测开始了");
        }
    }

    @Override
    public void handleResult(String result, int code) {
        if (code == MONITOR_CODE) {
            LogUtil.info("异地监测开始了");
            MonitorRes res = Tools.json2Bean(result, MonitorRes.class);
            if (res.isOk()) {
                String temp = res.getData();
                tv_monitor_temp_data.setText(temp);
                tv_monitor_temp_tips.setText("体温监测中");
            } else {
                String msg = res.msg();
                tv_monitor_temp_data.setText("--");
                tv_monitor_temp_tips.setText(msg);
            }
        }
    }

    @Override
    public void handleFinish(int code) {
    }

    @Override
    public void handleError(int code) {
        if (code == MONITOR_CODE) {
            showTips("监测失败，请检查网络配置");
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (monitorGet == null) {
            monitorGet = new MonitorGet();
            monitorGet.setCid(monitorId);
            monitorGet.code(MONITOR_CODE);
            monitorGet.handler(this);
        }
        monitorGet.get();
        return false;
    }
}
