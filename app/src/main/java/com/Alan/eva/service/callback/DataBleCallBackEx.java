package com.Alan.eva.service.callback;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.text.TextUtils;

import com.Alan.eva.config.BLEConfig;
import com.Alan.eva.http.core.IResultHandler;
import com.Alan.eva.http.post.ChildTempPost;
import com.Alan.eva.result.Res;
import com.Alan.eva.service.BleService;
import com.Alan.eva.tools.LogUtil;
import com.Alan.eva.tools.Tools;

/**
 * Created by CW on 2017/2/23.
 * 蓝牙数据回调实现类
 */
public class DataBleCallBackEx extends BluetoothGattCallback implements IResultHandler {
    private BleService service;

    public DataBleCallBackEx(BleService service) {
        this.service = service;
    }

    private int currState = -1;

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTING) {
            service.sendMsg(BLEConfig.BLE_CONNECTING, "正在连接体温计，请稍候");//正在连接
        } else if (newState == BluetoothProfile.STATE_CONNECTED) {
            service.sendMsg(BLEConfig.BLE_CONNECTED, "连接成功，正在读取体温计信息");//连接成功
            service.scanDeviceService();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            if (currState != newState) {
                currState = newState;
                service.stopLoop();
                service.disconnect();
                service.sendMsg(BLEConfig.BLE_SERVER_DISCONNECTED, "体温计连接已断开");
            }
        }
        super.onConnectionStateChange(gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {  //读取服务成功
            service.sendMsg(BLEConfig.BLE_DEVICE_DISCOVERY, "体温监测中...");
            service.startLoopTemp();
        } else if (status == BluetoothGatt.GATT_FAILURE) {  //服务不能正常使用
            service.sendMsg(BLEConfig.BLE_SERVICE_NOT_AVAILABLE, "体温计不能正常工作，请尝试连接其他体温计");
            service.stopLoop();
        }
        super.onServicesDiscovered(gatt, status);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            String receivedUUID = characteristic.getUuid().toString();
            byte[] data = characteristic.getValue();
            if (TextUtils.equals(receivedUUID, BLEConfig.TEMPERATURE_CHARACTERISTICS)) {  //温度数据
                getActualTemp(data);
            } else if (TextUtils.equals(receivedUUID, BLEConfig.TEMPERATURE_CHARACTERISTICS_POWER)) { //电池电量
                sendBatteryPower(data);
            }
        }
        super.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
    }

    /**
     * 电池电量
     */
    private void sendBatteryPower(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length);
        int index;
        for (byte aData : data) {
            index = aData;
            sb.append(index);
        }
        String power = sb.toString();
        service.setBatteryPower(power);
    }

    /**
     * 获取实时温度数据
     */
    private void getActualTemp(byte[] data) {
        if (data != null && data.length > 0) {
            String integralPart = String.format("%s", data[0]);
            String decimalPart = String.format("%s", data[1]);
            String temp = integralPart + "." + decimalPart;
            service.sendMsg(BLEConfig.BLE_TEMP_GET, String.valueOf(temp + "℃"));
            LogUtil.info("获取温度数据" + temp);
            submit(temp);
        }
    }

    private ChildTempPost tempPost;
    private final int TEMP_POST = 0x011;
    private int tempCount = 0;

    private void submit(String temp) {
        tempCount += 1;
        if (tempCount >= 6) {
            tempCount = 0;
            String cid = service.getCid();
            if (TextUtils.isEmpty(cid)) {  //如果孩子id为空了，就不要上传了
                LogUtil.info("孩子id为空了，不能上传");
                return;
            }
            if (tempPost == null) {
                tempPost = new ChildTempPost();
                tempPost.code(TEMP_POST);
                tempPost.handler(this);
            }
            tempPost.setCid(cid);
            tempPost.setTemp(temp);
            tempPost.post();
        }
    }

    @Override
    public void handleStart(int code) {
        if (code == TEMP_POST) {
            LogUtil.info("上传温度开始");
        }
    }

    @Override
    public void handleResult(String result, int code) {
        if (code == TEMP_POST) {
            Res res = Tools.json2Bean(result, Res.class);
            LogUtil.info(res.msg());
        }
    }

    @Override
    public void handleFinish(int code) {
        if (code == TEMP_POST) {
            LogUtil.info("上传温度结束了");
        }
    }

    @Override
    public void handleError(int code) {
        if (code == TEMP_POST) {
            LogUtil.info("上传温度结束了");
        }
    }
}
