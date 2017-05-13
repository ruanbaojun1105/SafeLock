package com.hwx.safelock.safelock.activity.broadcast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.hwx.safelock.safelock.Application;
import com.hwx.safelock.safelock.Constants;
import android_serialport_api.SerialPortServer;
import com.hwx.safelock.safelock.util.LogUtils;

/**
 * Created by bj 2016.10.27
 */
public abstract class CommandReceiver extends BroadcastReceiver {

    private Activity activity;
    public abstract void onDataReceived(final byte[] buffer, final byte function, byte safeCod);
    public abstract void onFail(String str);
    public void regiest(Activity activity) {
        this.activity=activity;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.SERIAL_PORT_COMMAND);
        filter.addAction(Constants.SERIAL_PORT_CONNECT_FAIL);
        LocalBroadcastManager.getInstance(Application.getContext()).registerReceiver(this, filter);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle=intent.getExtras();
        if (intent.getAction().equals(Constants.SERIAL_PORT_COMMAND)) {
            onDataReceived(bundle.getByteArray("buffer"),bundle.getByte("function"),bundle.getByte("safeCode"));
        }else if (intent.getAction().equals(Constants.SERIAL_PORT_CONNECT_FAIL)) {
            String str=bundle.getString("error");
            onFail(str);
            LogUtils.e("error",str);
        }
    }
}
