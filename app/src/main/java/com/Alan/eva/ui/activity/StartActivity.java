package com.Alan.eva.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import com.Alan.eva.R;
import com.Alan.eva.service.BleService;
import com.Alan.eva.tools.SpTools;
import com.Alan.eva.ui.EApp;
import com.Alan.eva.ui.core.AbsActivity;
import com.Alan.eva.ui.dialog.OperateDialog;

public class StartActivity extends AbsActivity {
    private final int GOTO_GUIDE = 0x0001;
    private final int GOTO_MAIN = 0x0002;

    @Override
    public Activity getCurrActivity() {
        return this;
    }

    @Override
    public int getRootViewId() {
        return R.layout.ac_start;
    }

    @Override
    public void findView(View rootView) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*获取屏幕宽度 开始*/
        DisplayMetrics dm = new DisplayMetrics();
        Display display = getCurrActivity().getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        EApp.getApp().setScreenWidth(dm.widthPixels);
        EApp.getApp().setScreenHeight(dm.heightPixels);
        /*获取屏幕宽度 结束*/
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth == null) {
            showBleErrorDialog();
            return;
        }
        Intent startCmd = new Intent(getCurrActivity(), BleService.class);
        startService(startCmd);
        checkIsFirst();
    }

    private void showBleErrorDialog() {
        OperateDialog dialog = new OperateDialog(getCurrActivity());
        dialog.setCancel("");
        dialog.setOk("退出应用");
        dialog.setOnOk(v -> {
            dialog.dismiss();
            currFinish();
        });
        dialog.setContent("本机不支持蓝牙通信，不能使用。");
        int wid = getCurrActivity().getResources().getDimensionPixelOffset(R.dimen.size_300);
        dialog.create(wid, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    /**
     * 判断是否是首次进入app
     */
    private void checkIsFirst() {
        Handler handler = new Handler(msg -> {
            int what = msg.what;
            if (what == GOTO_GUIDE) {
                guide();
            } else if (what == GOTO_MAIN) {
                gotoActivity(HomeActivity.class);
            }
            currFinish();
            return false;
        });
        handler.postDelayed(() -> {
            boolean isFirst = SpTools.getInstance(getCurrActivity()).isFirstStart();
            handler.sendEmptyMessage(isFirst ? GOTO_GUIDE : GOTO_MAIN);
            SpTools.getInstance(getCurrActivity()).putFirstStart(false);
        }, 3000);
    }

    private void guide() {
        gotoActivity(SplashActivity.class);
    }
}
