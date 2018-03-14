package com.Alan.eva.ui.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.Alan.eva.tools.LogUtil;
import com.Alan.eva.tools.Tools;
import com.Alan.eva.ui.dialog.LoadingDialog;
import com.umeng.analytics.MobclickAgent;

/**
 * 抽象Activity
 *
 * @author wei19
 */
public abstract class AbsActivity extends AppCompatActivity {
    /**
     * 获取实现类的上下文环境
     *
     * @return 当前页面
     */
    public abstract Activity getCurrActivity();

    private LayoutInflater mInflater;
    private View rootView;

    public abstract int getRootViewId();

    public abstract void findView(View rootView);

    private Intent mIntent;
    private boolean inActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        super.onCreate(savedInstanceState);
        mInflater = LayoutInflater.from(getCurrActivity());
        rootView = mInflater.inflate(getRootViewId(), null);
        setContentView(rootView);
        findView(rootView);
    }

    @Override
    protected void onResume() {
        LogUtil.info(getCurrActivity().getLocalClassName() + ".onResume()");
        super.onResume();
        inActive = true;
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onStart() {
        LogUtil.info(getCurrActivity().getLocalClassName() + ".onStart()");
        super.onStart();
    }

    @Override
    protected void onPause() {
        LogUtil.info(getCurrActivity().getLocalClassName() + ".onPause()");
        super.onPause();
        inActive = false;
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        LogUtil.info(getCurrActivity().getLocalClassName() + ".onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LogUtil.info(getCurrActivity().getLocalClassName() + ".onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        LogUtil.info(getCurrActivity().getLocalClassName() + ".onRestart()");
        super.onRestart();
    }

    /**
     * 获取目标意图实例，并且已经添加好对象跳转
     *
     * @param clazz 类
     * @return 意图
     */
    public <T> Intent getIntent(Class<T> clazz) {
        if (mIntent == null) {
            mIntent = new Intent(getCurrActivity(), clazz);
        } else {
            mIntent.setClass(getCurrActivity(), clazz);
        }
        return mIntent;
    }

    protected <T> void gotoActivity(Class<T> clazz) {
        if (mIntent == null) {
            mIntent = new Intent();
        }
        mIntent.setClass(getCurrActivity(), clazz);
        getCurrActivity().startActivity(mIntent);
    }

    /**
     * @return 获取跟布局的实例对象
     */
    public View getRootView() {
        return rootView;
    }

    /**
     * 结束当前activity
     */
    public void currFinish() {
        getCurrActivity().finish();
    }

    /**
     * 获取具体视图
     *
     * @param id 布局id
     * @return 视图布局
     */
    protected View getView(int id) {
        return getRootView().findViewById(id);
    }

    public void showTips(String str) {
        if (inActive) {
            Tools.showTips(getCurrActivity(), str);
        }
    }

    protected LayoutInflater getMInflater() {
        return mInflater;
    }

    private LoadingDialog loadingDialog;

    protected void loading() {
        loading("加载中...");
    }

    protected void loading(String tips) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getCurrActivity());
            loadingDialog.tips(tips);
            loadingDialog.create();
        }
        loadingDialog.show();
    }

    protected void hide() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
