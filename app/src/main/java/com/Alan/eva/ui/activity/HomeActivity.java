package com.Alan.eva.ui.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.Alan.eva.BuildConfig;
import com.Alan.eva.R;
import com.Alan.eva.config.BLEConfig;
import com.Alan.eva.config.BleEvent;
import com.Alan.eva.config.DownloadConfig;
import com.Alan.eva.config.URlConfig;
import com.Alan.eva.http.core.IResultHandler;
import com.Alan.eva.http.get.CheckVersionGet;
import com.Alan.eva.http.get.ChildSummaryGet;
import com.Alan.eva.model.ChildSummary;
import com.Alan.eva.model.UserInfo;
import com.Alan.eva.model.VersionData;
import com.Alan.eva.result.ChildSummaryRes;
import com.Alan.eva.result.VersionRes;
import com.Alan.eva.tools.LogUtil;
import com.Alan.eva.tools.Tools;
import com.Alan.eva.ui.EApp;
import com.Alan.eva.ui.core.AbsActivity;
import com.Alan.eva.ui.dialog.OperateDialog;
import com.Alan.eva.ui.widget.CircleImageView;
import com.Alan.eva.ui.widget.TempCircleView;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

import static com.Alan.eva.config.BLEConfig.CMD_EXTRA;
import static com.Alan.eva.ui.EApp.getApp;

/**
 * Created by CW on 2017/2/21.
 * 新首页
 */
public class HomeActivity extends AbsActivity implements View.OnClickListener, IResultHandler {
    private AppCompatTextView tv_home_check_new_version;
    private AppCompatTextView tv_home_log_out;
    private DrawerLayout drawer_home_holder;
    private AppCompatTextView tv_home_temp_operator;
    private AppCompatImageView iv_home_indicator_bg;
    private TempCircleView circle_view_home_temp_indicator;
    private AppCompatTextView tv_home_temp_tips_shower;

    private CircleImageView circle_home_child_portrait;
    private AppCompatTextView tv_home_child_name;
    private AppCompatTextView tv_home_child_height;
    private AppCompatTextView tv_home_child_weight;
    private AppCompatTextView tv_home_child_age;
    private AppCompatTextView tv_home_child_gender;

    private AppCompatTextView tv_home_child_max;
    private AppCompatTextView tv_home_child_min;
    private AppCompatTextView tv_home_child_count;
    private AppCompatTextView tv_home_child_tips;

    private BleEvent bleEvent;

    private BluetoothDevice device;


    @Override
    public Activity getCurrActivity() {
        return this;
    }

    @Override
    public int getRootViewId() {
        return R.layout.ac_home;
    }

    @Override
    public void findView(View rootView) {
        initTitleBar();
        tv_home_temp_operator = (AppCompatTextView) getView(R.id.tv_home_temp_operator);
        iv_home_indicator_bg = (AppCompatImageView) getView(R.id.iv_home_indicator_bg);
        circle_view_home_temp_indicator = (TempCircleView) getView(R.id.circle_view_home_temp_indicator);
        tv_home_temp_tips_shower = (AppCompatTextView) getView(R.id.tv_home_temp_tips_shower);

        AppCompatTextView tv_home_about_us = (AppCompatTextView) getView(R.id.tv_home_about_us);
        AppCompatTextView tv_home_user_helper = (AppCompatTextView) getView(R.id.tv_home_user_helper);
        AppCompatTextView tv_home_medicine_remind = (AppCompatTextView) getView(R.id.tv_home_medicine_remind);
        AppCompatTextView tv_home_seggestion = (AppCompatTextView) getView(R.id.tv_home_seggestion);
        AppCompatTextView tv_home_version_name = (AppCompatTextView) getView(R.id.tv_home_version_name);
        tv_home_version_name.setText(String.valueOf("当前版本：V" + BuildConfig.VERSION_NAME));

        tv_home_check_new_version = (AppCompatTextView) getView(R.id.tv_home_check_new_version);
        tv_home_log_out = (AppCompatTextView) getView(R.id.tv_home_log_out);

        circle_home_child_portrait = (CircleImageView) getView(R.id.circle_home_child_portrait);
        tv_home_child_name = (AppCompatTextView) getView(R.id.tv_home_child_name);
        tv_home_child_height = (AppCompatTextView) getView(R.id.tv_home_child_height);
        tv_home_child_weight = (AppCompatTextView) getView(R.id.tv_home_child_weight);
        tv_home_child_age = (AppCompatTextView) getView(R.id.tv_home_child_age);
        tv_home_child_gender = (AppCompatTextView) getView(R.id.tv_home_child_gender);

        tv_home_child_max = (AppCompatTextView) getView(R.id.tv_home_child_max);
        tv_home_child_min = (AppCompatTextView) getView(R.id.tv_home_child_min);
        tv_home_child_count = (AppCompatTextView) getView(R.id.tv_home_child_count);
        tv_home_child_tips = (AppCompatTextView) getView(R.id.tv_home_child_tips);

        resetOperate("扫描体温计", "请开始扫描体温计");
        tv_home_about_us.setOnClickListener(this);
        tv_home_user_helper.setOnClickListener(this);
        tv_home_medicine_remind.setOnClickListener(this);
        tv_home_seggestion.setOnClickListener(this);
        tv_home_check_new_version.setOnClickListener(this);
        tv_home_log_out.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_home_about_us:  //关于我们
                Intent about = getIntent(CommonWebActivity.class);
                about.putExtra(URlConfig.URL_KEY_TITLE, URlConfig.ABOUT_US);
                about.putExtra(URlConfig.URL_KEY_URL, URlConfig.ABOUT_US_URL);
                startActivity(about);
                break;
            case R.id.tv_home_user_helper:  //使用说明
                Intent helper = getIntent(CommonWebActivity.class);
                helper.putExtra(URlConfig.URL_KEY_TITLE, URlConfig.ABOUT_PRODUCT);
                helper.putExtra(URlConfig.URL_KEY_URL, URlConfig.ABOUT_PRODUCT_URL);
                startActivity(helper);
                break;
            case R.id.tv_home_medicine_remind:  //吃药提醒
                gotoActivity(AlarmListActivity.class);
                break;
            case R.id.tv_home_seggestion:  //反馈建议
                gotoActivity(SuggestActivity.class);
                break;
            case R.id.tv_home_check_new_version:  //检查更新
                checkVersion();
                break;
            case R.id.tv_home_log_out:  //退出登录
                showLogout();
                break;
        }
    }


    private final int CHECK_VERSION = 0x0016;

    private void checkVersion() {
        int versionCode = BuildConfig.VERSION_CODE;
        CheckVersionGet get = new CheckVersionGet();
        get.code(CHECK_VERSION);
        get.handler(this);
        get.setCode(String.valueOf(versionCode));
        get.get();
    }

    /**
     * 显示退出登录对话框
     */
    private void showLogout() {
        OperateDialog dialog = new OperateDialog(getCurrActivity());
        dialog.setContent("是否要退出登录");
        dialog.setOk("退出");
        dialog.setOnOk(v -> {
            dialog.dismiss();
            getApp().setUserInfo(null, getCurrActivity());
            MobclickAgent.onProfileSignOff();
            tv_home_log_out.setVisibility(View.GONE);
            showTips("已退出");
        });
        int wid = getCurrActivity().getResources().getDimensionPixelOffset(R.dimen.size_300);
        dialog.create(wid, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        bleEvent = new BleEvent();
        startAperture();
        UserInfo userInfo = getApp().getUserInfo(getCurrActivity());
        if (userInfo != null) {
            String cid = userInfo.getCid();
            if (!TextUtils.isEmpty(cid)) {
                tv_home_log_out.setVisibility(View.VISIBLE);
                sendCmd(BLEConfig.CHILD_ID_CMD, cid);//添加监听对象id
                summaryGet(cid);
            } else {
                tv_home_log_out.setVisibility(View.GONE);
                showAddChildDialog();
            }
        }
    }


    /**
     * 根据蓝牙地址进行连接
     *
     * @param address mac 地址
     */
    public void connectBle(String address) {
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            sendCmd(BLEConfig.BLE_CONNECT_CMD, address);//连接
        } else {
            resetOperate("重新扫描", "体温计校验错误，请尝试扫描其他体温计");
        }
    }

    /**
     * 重置界面并添加提示信息
     *
     * @param operate 操作提示内容
     */
    private void resetOperate(String operate, String tips) {
        tv_home_temp_operator.setClickable(true);
        tv_home_temp_operator.setText(operate);
        tv_home_temp_operator.setOnClickListener(v -> startScan());
        tv_home_temp_tips_shower.setText(tips);
    }

    /**
     * 有东西输出的时候
     */
    private void onTips(String operate, String tips) {
        tv_home_temp_operator.setClickable(false);
        tv_home_temp_operator.setText(operate);
        tv_home_temp_tips_shower.setText(tips);
    }

    /**
     * 扫描体温计
     */
    private void startScan() {
        sendCmd(BLEConfig.BLE_SCAN_CMD, null);//扫描体温计
        tv_home_temp_operator.setText("扫描中...");
        tv_home_temp_operator.setClickable(false);
        startRotateAnim();
    }

    /**
     * 蓝牙服务事件回调函数
     *
     * @param bleEvent 事件体
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onMessageEventMainThread(BleEvent bleEvent) {
        int code = bleEvent.getCode();
        Bundle bundle = bleEvent.getExtra();
        String extra = "";
        if (bundle != null) {
            extra = bundle.getString(BLEConfig.MSG_KEY);
        }
        switch (code) {
            case BLEConfig.BLE_IS_SCANNING: //正在扫描
                onTips("扫描中", extra);
                break;
            case BLEConfig.BLE_NEW_DEVICE:  //发现体温计
                if (bundle != null) {
                    device = bundle.getParcelable(BLEConfig.DEVICE_KEY);
                }
                if (device != null) {
                    String address = device.getAddress();
                    connectBle(address);
                }
                break;
            case BLEConfig.BLE_SCAN_FINISH: //扫描结束
                stopRotateAnim();
                if (device == null) {
                    resetOperate("重新扫描", "没有发现可用的体温计，请打开体温计重试");
                } else {
                    onTips("扫描结束", extra);
                }
                break;
            case BLEConfig.BLE_CONNECTING: //正在连接
                onTips("连接中...", extra);
                break;
            case BLEConfig.BLE_CONNECTED://蓝牙服务正常连接了
                onTips("读取中...", extra);
                break;
            case BLEConfig.BLE_DEVICE_DISCOVERY:   //体温计服务被发现了..数据准备就绪开始监测体温
                onTips("读取中", extra);
                break;
            case BLEConfig.BLE_SERVER_DISCONNECTED:  //蓝牙服务断开连接了
                if (device == null) {
                    resetOperate("重新扫描", extra);
                } else {
                    tv_home_temp_operator.setClickable(true);
                    tv_home_temp_operator.setText("重新连接");
                    tv_home_temp_tips_shower.setText(extra);
                    tv_home_temp_operator.setOnClickListener(v -> {
                        String macAddress = device.getAddress();
                        connectBle(macAddress);
                    });
                }
                MediaPlayer mp = new MediaPlayer();
                try {
                    mp.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    mp.prepare();
                    mp.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case BLEConfig.BLE_TEMP_GET:  //得到温度
                onTips(extra, "体温监测中");
                break;
            case BLEConfig.BLE_DEVICE_NOT_FOUND: //体温计不可用，需要重新扫描
                resetOperate("重新扫描", extra);
                break;
            case BLEConfig.BLE_OFF_LINE:
                showBleClosedDialog(extra);
                break;
            case BLEConfig.BLE_ON_LINE:
                showTips(extra);
                if (device != null) {
                    String address = device.getAddress();
                    onTips("重连中", "蓝牙已重新打开正常尝试重连");
                    connectBle(address);
                } else {
                    resetOperate("重新扫描", "蓝牙已重新打开，请重新扫描体温计");
                }
                break;
            case BLEConfig.BLE_RELEASE_DEVICE: //体温计已解除
                resetOperate("扫描体温计", extra);
                break;
            case DownloadConfig.DOWN_LOAD_STARTED:
                showTips(extra);
                break;
            case DownloadConfig.DOWN_LOAD_FAILED:
                showTips(extra);
                break;
            case DownloadConfig.DOWN_LOAD_SUCCESS:
                showTips("下载成功，请安装");
                if (!TextUtils.isEmpty(extra)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(extra)), "application/vnd.android.package-archive");
                    getCurrActivity().startActivity(intent);
                }
                break;
        }
    }

    /**
     * 显示蓝牙断开连接对话框
     *
     * @param tips 内容
     */
    private void showBleClosedDialog(String tips) {
        OperateDialog dialog = new OperateDialog(getCurrActivity());
        dialog.setContent(tips);
        dialog.setOk("打开蓝牙");
        dialog.setCancel("退出使用");
        dialog.setOnCancel(v -> {
            dialog.dismiss();
            currFinish();
        });
        dialog.setOnOk(v -> {
            dialog.dismiss();
            sendCmd(BLEConfig.BLE_OPEN, "");
        });
        int wid = getCurrActivity().getResources().getDimensionPixelOffset(R.dimen.size_300);
        dialog.create(wid, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    /**
     * 发送命令
     */
    private void sendCmd(int code, String extra) {
        bleEvent.setCode(code);
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(extra)) {
            bundle.putString(CMD_EXTRA, extra);
        }
        bleEvent.setExtra(bundle);
        EventBus.getDefault().post(bleEvent);
    }

    /**
     * 初始化标题栏信息
     */
    private void initTitleBar() {
        drawer_home_holder = (DrawerLayout) getView(R.id.drawer_home_holder);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getCurrActivity(), drawer_home_holder, R.string.open, R.string.close);
        actionBarDrawerToggle.syncState();
        drawer_home_holder.addDrawerListener(actionBarDrawerToggle);
        Toolbar tool_bar_home_title = (Toolbar) getView(R.id.tool_bar_home_title);
        tool_bar_home_title.setTitleTextColor(Tools.getColor(getCurrActivity(), R.color.white));
        tool_bar_home_title.setTitle(R.string.app_name);
        setSupportActionBar(tool_bar_home_title);
        tool_bar_home_title.setNavigationIcon(R.mipmap.ic_home_menu);
        tool_bar_home_title.setNavigationOnClickListener((View v) -> drawer_home_holder.openDrawer(GravityCompat.START));
        tool_bar_home_title.setOnMenuItemClickListener((MenuItem item) -> {
            switch (item.getItemId()) {
                case R.id.title_bar_home_user:
                    UserInfo userInfo = getApp().getUserInfo(getCurrActivity());
                    if (userInfo != null) {
                        String uid = userInfo.getUid();
                        if (TextUtils.isEmpty(uid)) {
                            showTips("请先登录");
                            login();
                        } else {
                            Intent intent = getIntent(ChildListActivity.class);
                            intent.putExtra("uid", uid);
                            startActivity(intent);
                        }
                    } else {
                        showTips("请先登录");
                        login();
                    }
                    break;
                case R.id.title_bar_home_device:
                    gotoDeviceDetail();
                    break;
            }
            return true;
        });
    }

    private final int LOGIN_CODE = 0x0099;

    /**
     * 登录
     */
    private void login() {
        Intent intent = getIntent(LoginActivity.class);
        startActivityForResult(intent, LOGIN_CODE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            String cid = intent.getStringExtra("cid");
            if (!TextUtils.isEmpty(cid)) {
                summaryGet(cid);
                sendCmd(BLEConfig.CHILD_ID_CMD, cid);//切换孩子
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == DEVICE_DETAIL) { //解除了体温计
                boolean unbind = data.getBooleanExtra("unbind", false);
                if (unbind) {
                    device = null;
                    LogUtil.info("体温计被解除了");
                    sendCmd(BLEConfig.BLE_CONNECT_CMD, null);//连接
                }
            } else if (requestCode == LOGIN_CODE) {
                UserInfo info = EApp.getApp().getUserInfo(getCurrActivity());
                if (info != null) {
                    String uid = info.getUid();
                    if (!TextUtils.isEmpty(uid)) {
                        tv_home_log_out.setVisibility(View.VISIBLE);
                    } else {
                        tv_home_log_out.setVisibility(View.GONE);
                    }
                } else {
                    tv_home_log_out.setVisibility(View.GONE);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Animation connectRotaAni = null;

    /**
     * 开始 连接蓝牙旋转动画
     */
    private void startRotateAnim() {
        if (connectRotaAni == null) {
            connectRotaAni = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            connectRotaAni.setDuration(2000); //周期2秒
            connectRotaAni.setInterpolator(new LinearInterpolator());// 匀速
            connectRotaAni.setRepeatCount(-1);
        }
        circle_view_home_temp_indicator.setAnimation(connectRotaAni);
        connectRotaAni.start();
    }

    /**
     * 结束  连接蓝牙旋转动画
     */
    private void stopRotateAnim() {
        circle_view_home_temp_indicator.clearAnimation();
    }

    /**
     * 开启光圈动画
     */
    private void startAperture() {
        final ObjectAnimator animatorAlp = ObjectAnimator.ofFloat(iv_home_indicator_bg, "alpha", 1f, 0.2f, 1f);
        animatorAlp.setDuration(3000);
        animatorAlp.setRepeatCount(-1);
        animatorAlp.setInterpolator(new LinearInterpolator());
        animatorAlp.setRepeatMode(ValueAnimator.RESTART);
        animatorAlp.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_toolbar_menu, menu);
        return true;
    }

    /**
     * 显示添加孩子对话框
     */
    private void showAddChildDialog() {
        OperateDialog dialog = new OperateDialog(getCurrActivity());
        dialog.setContent("未添加默认孩子，只能做实时监测，无法进行异地监测和健康分析。现在去设置默认绑定？");
        dialog.setOk("去绑定");
        dialog.setCancel("本地监测");
        dialog.setOnOk(v -> {
            dialog.dismiss();
            gotoActivity(ChildListActivity.class);
        });
        dialog.setCancelable(false);
        int wid = getResources().getDimensionPixelOffset(R.dimen.size_300);
        dialog.create(wid, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        sendCmd(BLEConfig.STOP_SERVICE_CMD, null);//结束服务
        super.onDestroy();
    }

    private void gotoDeviceDetail() {
        if (device == null) {
            showTips("请先选择体温计进行连接");
            return;
        }
        String bleName = device.getName();
        String macAddress = device.getAddress();
        Intent intent = getIntent(DeviceActivity.class);
        intent.putExtra("name", bleName);
        intent.putExtra("mac", macAddress);
        startActivityForResult(intent, DEVICE_DETAIL);
    }

    private final int DEVICE_DETAIL = 0x00081;

    private final int SUMMARY_GET = 0x0018;

    private void summaryGet(String cid) {
        ChildSummaryGet get = new ChildSummaryGet();
        get.code(SUMMARY_GET);
        get.handler(this);
        get.setCid(cid);
        get.get();
    }

    @Override
    public void handleStart(int code) {
        if (code == SUMMARY_GET) {
            LogUtil.info("获取孩子概况开始");
        } else if (code == CHECK_VERSION) {
            tv_home_check_new_version.setClickable(false);
        }
    }

    @Override
    public void handleResult(String result, int code) {
        if (code == SUMMARY_GET) {
            ChildSummaryRes res = Tools.json2Bean(result, ChildSummaryRes.class);
            if (res.isOk()) {
                ChildSummary data = res.getData();
                String name = data.getName();
                String portrait = data.getPortrait();
                String height = data.getHeight();
                String weight = data.getWeight();
                String age = data.getAge();
                String gender = data.getGender();
                String max = data.getMax();
                String min = data.getMin();
                String count = data.getCount();
                String tips = data.getTips();
                Tools.display(circle_home_child_portrait, portrait);
                tv_home_child_name.setText(name);
                tv_home_child_height.setText(height);
                tv_home_child_weight.setText(weight);
                tv_home_child_age.setText(age);
                tv_home_child_gender.setText(gender);
                tv_home_child_max.setText(max);
                tv_home_child_min.setText(min);
                tv_home_child_count.setText(count);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_home_child_tips.setText(Html.fromHtml(tips, Html.FROM_HTML_MODE_COMPACT));
                } else {
                    //noinspection deprecation
                    tv_home_child_tips.setText(Html.fromHtml(tips));
                }
            } else {
                showTips(res.msg());
            }
        } else if (code == CHECK_VERSION) {
            VersionRes res = Tools.json2Bean(result, VersionRes.class);
            if (res.isOk()) {//有新版本
                VersionData data = res.getData();
                showUpdate(data);
            } else {
                showTips(res.msg());
            }
        }
    }

    private void showUpdate(VersionData data) {
        String vName = data.getvName();
        String path = data.getPath();
        OperateDialog dialog = new OperateDialog(getCurrActivity());
        dialog.setOk("开始下载");
        dialog.setCancel("稍候再说");
        dialog.setContent("最新版本已更新至" + vName + "，请及时更新。");
        dialog.setOnOk(v -> {
            dialog.dismiss();
            sendCmd(DownloadConfig.DOWN_LOAD_START_CMD, path);
        });
        int wid = getCurrActivity().getResources().getDimensionPixelOffset(R.dimen.size_300);
        dialog.create(wid, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    @Override
    public void handleFinish(int code) {
        if (code == SUMMARY_GET) {
            LogUtil.info("获取孩子概况结束");
        } else if (code == CHECK_VERSION) {
            tv_home_check_new_version.setClickable(true);
        }
    }

    @Override
    public void handleError(int code) {
        if (code == SUMMARY_GET) {
            LogUtil.info("获取孩子概况错误");
        } else if (code == CHECK_VERSION) {
            showTips("检查更新出错，请重试");
        }
    }
}