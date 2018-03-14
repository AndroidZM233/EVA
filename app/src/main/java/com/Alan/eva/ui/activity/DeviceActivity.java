package com.Alan.eva.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.Alan.eva.R;
import com.Alan.eva.config.BLEConfig;
import com.Alan.eva.config.BleEvent;
import com.Alan.eva.tools.SpTools;
import com.Alan.eva.tools.Tools;
import com.Alan.eva.ui.core.AbsActivity;
import com.Alan.eva.ui.dialog.OperateDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.Alan.eva.config.BLEConfig.CMD_EXTRA;

/**
 * Created by CW on 2017/3/8.
 * 体温计详情界面
 */
public class DeviceActivity extends AbsActivity implements View.OnClickListener {
    private AppCompatTextView tv_device_detail_name;
    private AppCompatTextView tv_device_detail_mac;
    private AppCompatTextView tv_device_detail_power;
    private AppCompatTextView tv_device_detail_temp;
    private BleEvent bleEvent;
    private String name;

    @Override
    public Activity getCurrActivity() {
        return this;
    }

    @Override
    public int getRootViewId() {
        return R.layout.ac_device_detail;
    }

    @Override
    public void findView(View rootView) {
        Toolbar tool_bar_home_title = (Toolbar) getView(R.id.tool_bar_device_detail);
        tool_bar_home_title.setTitleTextColor(Tools.getColor(getCurrActivity(), R.color.white));
        tool_bar_home_title.setTitle(R.string.device_detail);
        setSupportActionBar(tool_bar_home_title);
        tool_bar_home_title.setNavigationIcon(R.mipmap.ic_flag_back);
        tool_bar_home_title.setNavigationOnClickListener((View v) -> currFinish());
        tv_device_detail_name = (AppCompatTextView) getView(R.id.tv_device_detail_name);
        tv_device_detail_mac = (AppCompatTextView) getView(R.id.tv_device_detail_mac);
        tv_device_detail_power = (AppCompatTextView) getView(R.id.tv_device_detail_power);
        tv_device_detail_temp = (AppCompatTextView) getView(R.id.tv_device_detail_temp);
        AppCompatButton btn_device_detail_unbind = (AppCompatButton) getView(R.id.btn_device_detail_unbind);
        btn_device_detail_unbind.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        bleEvent = new BleEvent();
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        String mac = intent.getStringExtra("mac");
        tv_device_detail_name.setText(name);
        tv_device_detail_mac.setText(mac);
        tv_device_detail_power.setText(R.string.getting_data);
        tv_device_detail_temp.setText(R.string.getting_data);
        sendCmd(BLEConfig.READ_DEVISE_POWER_CMD, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_device_detail_unbind:
                showUnbindDialog();
                break;
        }
    }

    /**
     * 解除
     */
    private void showUnbindDialog() {
        OperateDialog dialog = new OperateDialog(getCurrActivity());
        dialog.setContent("解除对" + name + "绑定，若解除绑定下次需要重新扫描体温计才能进行监测。确定要解除吗？");
        dialog.setOk("确定");
        dialog.setOnOk(v -> {
            dialog.dismiss();
            SpTools.getInstance(getCurrActivity()).saveMac("", "");
            Intent intent = new Intent();
            intent.putExtra("unbind", true);
            setResult(RESULT_OK, intent);
            currFinish();
        });
        int wid = getCurrActivity().getResources().getDimensionPixelOffset(R.dimen.size_300);
        dialog.create(wid, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEventMainThread(BleEvent bleEvent) {
        int code = bleEvent.getCode();
        Bundle bundle = bleEvent.getExtra();
        String extra = bundle.getString(BLEConfig.MSG_KEY);
        if (code == BLEConfig.BLE_LOOP_STOP) {  //轮询已结束
            Bundle b = bleEvent.getExtra();
            String msg = b.getString(BLEConfig.MSG_KEY);
            showTips(msg);
        } else if (code == BLEConfig.BLE_TEMP_GET) {  //体温计温度
            tv_device_detail_temp.setText(extra);
        } else if (code == BLEConfig.BLE_BATTERY_POWER) {  //体温计电量
            tv_device_detail_power.setText(extra);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 发送命令
     */
    private void sendCmd(int code, String extra) {
        bleEvent.setCode(code);
        if (!TextUtils.isEmpty(extra)) {
            Bundle bundle = new Bundle();
            bundle.putString(CMD_EXTRA, extra);
            bleEvent.setExtra(bundle);
        }
        EventBus.getDefault().post(bleEvent);
    }
}

