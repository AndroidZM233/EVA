package com.Alan.eva.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.Alan.eva.config.BLEConfig;
import com.Alan.eva.config.BleEvent;
import com.Alan.eva.config.DownloadConfig;
import com.Alan.eva.http.core.ReqParam;
import com.Alan.eva.service.callback.DataBleCallBackEx;
import com.Alan.eva.service.receiver.BleConnectReceiver;
import com.Alan.eva.tools.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.x;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.Alan.eva.config.BLEConfig.BLE_RELEASE_DEVICE;

/**
 * Created by CW on 2017/2/21.
 * 蓝牙服务
 * //开始顺序是打开蓝牙->查找体温计->连接体温计->搜索服务->读取数据
 * //结束顺序应该是先结束轮询->断开服务->断开蓝牙
 */
@SuppressLint("NewApi")
public class BleService extends Service {
    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetooth;
    private BleEvent bleEvent;
    /*蓝牙状态变化接收器*/
    private BleConnectReceiver connectReceiver;
    /*循环请求机*/
    private ScheduledExecutorService scheduledExecutor;
    private final int LOOPER_CODE = 0x00100;
    /**
     * 电池电量，如果有值了，就不要去获取电量信息了
     */
    private String batteryPower;
    private String cid;
    private DataBleCallBackEx callBack;

    /**
     * ble搜索
     */
    private BluetoothLeScanner mBluetoothLeScanner;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bleEvent = new BleEvent();
        EventBus.getDefault().register(this);

        IntentFilter bleFilter = new IntentFilter();
//        bleFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//        bleFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        bleFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bleFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        connectReceiver = new BleConnectReceiver(this);
        registerReceiver(connectReceiver, bleFilter);
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        // 检查设备上是否支持蓝牙
        if (bluetooth == null) {
            Toast.makeText(this, "你的手机不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        openBle();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(connectReceiver);
        connectReceiver = null;
        stopLoop(); //停止轮询
        stopScan(); //停止扫描
        disconnect(); //断开连接
        closeBle(); //关闭蓝牙
        super.onDestroy();
    }

    /**
     * 打开蓝牙
     */
    private void openBle() {
        bluetooth.enable();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothLeScanner = bluetooth.getBluetoothLeScanner();
        }

    }

    /**
     * 关闭蓝牙
     */
    private void closeBle() {
        bluetooth.disable();
    }

    /**
     * 获取主线线程发来的命令
     *
     * @param bleEvent 事件体
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventMainThread(BleEvent bleEvent) {
        int code = bleEvent.getCode();
        switch (code) {
            case BLEConfig.BLE_OPEN: //打开手机蓝牙
                openBle();
                break;
            case BLEConfig.BLE_CLOSE: //关闭手机蓝牙
                closeBle();
                break;
            case BLEConfig.BLE_SCAN_CMD:  //扫描体温计
                startScan();
                break;
            case BLEConfig.BLE_CONNECT_CMD:  //连接体温计
                Bundle macExtra = bleEvent.getExtra();
                if (macExtra != null && macExtra.containsKey(BLEConfig.CMD_EXTRA)) {
                    String macAddress = macExtra.getString(BLEConfig.CMD_EXTRA);
                    if (!TextUtils.isEmpty(macAddress)) {
                        connect(macAddress);
                    } else {
                        resetBleStatus("体温计已解除，请重新扫描");
                    }
                } else {
                    resetBleStatus("体温计已解除，请重新扫描");
                }
                break;
            case BLEConfig.CHILD_ID_CMD:  //  切换孩子id
                Bundle cidExtra = bleEvent.getExtra();
                if (cidExtra != null) {
                    this.cid = cidExtra.getString(BLEConfig.CMD_EXTRA);
                }
                break;
            case BLEConfig.READ_DEVISE_POWER_CMD: //读取电池电量
                readPower();
                break;
            case BLEConfig.STOP_SERVICE_CMD:  //结束连接
                cancelDownload();
                disconnect();
                stopSelf();
                break;
            case DownloadConfig.DOWN_LOAD_START_CMD: //开始下载命令
                Bundle pathExtra = bleEvent.getExtra();
                if (pathExtra != null && pathExtra.containsKey(BLEConfig.CMD_EXTRA)) {
                    String path = pathExtra.getString(BLEConfig.CMD_EXTRA);
                    startDownLoad(path, DownloadConfig.FILE_PATH);
                }
                break;
        }
    }

    private void resetBleStatus(String tips) {
        stopLoop();
        stopScan();
        disconnect();
        sendMsg(BLE_RELEASE_DEVICE, tips);
        LogUtil.info("体温计解除执行完毕了");
    }

    /**
     * 扫描体温计
     */
    private void startScan() {
//        if (bluetooth.isDiscovering()) { //正在扫描不能重复扫描
//            bluetooth.cancelDiscovery();
//            LogUtil.info("正在扫描中");
//            return;
//        }
//        boolean isScanning = bluetooth.startDiscovery();
//        LogUtil.info("是否开始扫描?" + isScanning);

        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.startScan(mScanCallback);
            sendMsg(BLEConfig.BLE_IS_SCANNING, "正在扫描，请提前打开体温计");
        }

    }

    /**
     * 停止扫描
     */
    public void stopScan() {
//        if (bluetooth.isDiscovering()) {
//            bluetooth.cancelDiscovery();
//        }

        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            sendMsg(BLEConfig.BLE_SCAN_FINISH, "扫描结束");
        }
    }


    // 5.0+.返蓝牙信息更新到界面
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            int rssi = result.getRssi();
            BluetoothDevice device = result.getDevice();
            String address = device.getAddress();
            if (BluetoothAdapter.checkBluetoothAddress(address)) {  //mac地址是否符合要求
                String name = device.getName();
                if (!TextUtils.isEmpty(name) && name.length() > 3) { //名称是否不为空且长度大于3
                    if (name.contains("EVE")) { //体温计是否包含EVE字符
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(BLEConfig.DEVICE_KEY, device);
                        sendMsg(BLEConfig.BLE_NEW_DEVICE, bundle);
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };


    /**
     * 尝试连接
     *
     */
    private String mBluetoothDeviceAddress;

    private boolean connect(String address) {
        stopScan();  //停止扫描
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && bluetoothGatt != null) {
            LogUtil.info("Trying to use an existing mBluetoothGatt for connection.");
            if (bluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        BluetoothDevice device = bluetooth.getRemoteDevice(address);
        if (device == null) {  //蓝牙体温计无法连接，被其他体温计占用
            sendMsg(BLEConfig.BLE_DEVICE_NOT_FOUND, "体温计连接失败，请尝试重启体温计，并重新连接");
            return false;
        }
        callBack = new DataBleCallBackEx(this);
        bluetoothGatt = device.connectGatt(this, false, callBack);
        mBluetoothDeviceAddress = address;
        LogUtil.info("连接代码执行完毕");
        return true;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            callBack = null;
        }
    }

    /**
     * 循环获取温度数据
     */
    public void startLoopTemp() {
        if (scheduledExecutor == null) {
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        scheduledExecutor.scheduleWithFixedDelay(() ->
                tempHandler.sendEmptyMessage(LOOPER_CODE), 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 关闭循环遍历数据
     */
    public void stopLoop() {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        scheduledExecutor = null;
    }


    public void scanDeviceService() {
        if (bluetoothGatt != null) {
            bluetoothGatt.discoverServices();
        }
    }

    public void setBatteryPower(String batteryPower) {
        this.batteryPower = batteryPower;
        sendMsg(BLEConfig.BLE_BATTERY_POWER, String.valueOf(this.batteryPower + "%"));
    }

    public String getCid() {
        return cid;
    }

    /**
     * 向页面发消息
     *
     * @param code  code
     * @param extra 消息
     */
    public void sendMsg(int code, String extra) {
        Bundle bundle = new Bundle();
        bundle.putString(BLEConfig.MSG_KEY, extra);
        sendMsg(code, bundle);
    }

    /**
     * 向页面发消息
     *
     * @param code  code
     * @param extra 消息
     */
    public void sendMsg(int code, Bundle extra) {
        bleEvent.setCode(code);
        bleEvent.setExtra(extra);
        EventBus.getDefault().post(bleEvent);
    }

    /***
     * 这是一个内部成员
     */
    private Handler tempHandler = new Handler(msg -> {
        if (msg.what == LOOPER_CODE) {
            readTemp();
        }
        return false;
    });

    private BluetoothGattCharacteristic tempCharacteristic;

    /**
     * 读取温度数据
     */
    private void readTemp() {
        LogUtil.info("获取一次温度数据");
        if (tempCharacteristic != null) {
            bluetoothGatt.readCharacteristic(tempCharacteristic);
            return;
        }
        List<BluetoothGattService> serviceList = bluetoothGatt.getServices();
        if (serviceList == null || serviceList.size() <= 0) {
            LogUtil.info("服务特征值列表为空了");
            return;
        }
        for (BluetoothGattService gattService : serviceList) { //遍历 体温计的所有特征值
            String parentUuid = gattService.getUuid().toString();
            if (TextUtils.equals(parentUuid, BLEConfig.TEMPERATURE_SERVICES)) { //体温计包含所需的服务列表  根据特征值查找服务
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {  //遍历特征值服务中存在的数据存储点
                    String childUuid = gattCharacteristic.getUuid().toString();
                    if (TextUtils.equals(childUuid, BLEConfig.TEMPERATURE_CHARACTERISTICS)) { //服务列表中有所需的特征值数据
                        int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            tempCharacteristic = gattCharacteristic;
                            bluetoothGatt.readCharacteristic(gattCharacteristic);
                        }
                    }
                }
            }
        }
    }

    /**
     * 读取电量信息
     */
    private void readPower() {
        if (!TextUtils.isEmpty(batteryPower)) {
            sendMsg(BLEConfig.BLE_BATTERY_POWER, String.valueOf(batteryPower + "%"));
            return;
        }
        List<BluetoothGattService> serviceList = bluetoothGatt.getServices();
        if (serviceList == null || serviceList.size() <= 0) {
            return;
        }
        for (BluetoothGattService gattService : serviceList) { //遍历 体温计的所有特征值
            String parentUuid = gattService.getUuid().toString();
            if (TextUtils.equals(parentUuid, BLEConfig.TEMPERATURE_SERVICES_POWER)) { //电量服务
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {  //遍历特征值服务中存在的数据存储点
                    String childUuid = gattCharacteristic.getUuid().toString();
                    if (TextUtils.equals(childUuid, BLEConfig.TEMPERATURE_CHARACTERISTICS_POWER)) {
                        int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            bluetoothGatt.readCharacteristic(gattCharacteristic);
                        }
                    }
                }
            }
        }
    }

    private Callback.Cancelable cancelable;

    /**
     * 开始下载
     *
     * @param url      外网路径
     * @param localUri 本地路径
     */
    private void startDownLoad(String url, String localUri) {
        Callback.ProgressCallback<File> callback = new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                sendMsg(DownloadConfig.DOWN_LOAD_STARTED, "开始下载...");
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                if (isDownloading) {
                    String process = "下载:" + (int) (current * 100 / total) + "%";
                    LogUtil.info(process);
                }
            }

            @Override
            public void onSuccess(File result) {
                LogUtil.info("下载完成了");
                String path = result.getPath();
                LogUtil.info("path=" + path);
                sendMsg(DownloadConfig.DOWN_LOAD_SUCCESS, path);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.info("下载失败了");
                sendMsg(DownloadConfig.DOWN_LOAD_FAILED, "下载失败");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtil.info("下载被取消了");
            }

            @Override
            public void onFinished() {
                LogUtil.info("下载结束了");
            }
        };
        LogUtil.info("url=" + url);
        ReqParam params = new ReqParam(url);
        params.setAutoResume(true);
        params.setAutoRename(true);
        params.setSaveFilePath(localUri);
        Executor executor = new PriorityExecutor(2);
        params.setExecutor(executor);
        params.setCancelFast(true);
        cancelable = x.http().get(params, callback);
    }

    private void cancelDownload() {
        if (cancelable != null) {
            cancelable.cancel();
        }
    }
}
