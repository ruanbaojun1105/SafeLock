package com.hwx.safelock.safelock.activity.broadcast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.hwx.safelock.safelock.Application;
import com.hwx.safelock.safelock.Constants;
import com.hwx.safelock.safelock.util.LogUtils;

import android_serialport_api.SerialPortServer;

/**
 * Created by bj 2016.10.27
 */
public abstract class ScanReceiver extends BroadcastReceiver {

    private boolean isEnd=false;
    public abstract void onDataReceived(final String qrcode);
    public ScanReceiver regiest() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.SERIAL_PORT_COMMAND_SCAN);
        LocalBroadcastManager.getInstance(Application.getContext()).registerReceiver(this, filter);
        return this;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isEnd)
            return;
        Bundle bundle = intent.getExtras();
        if (intent.getAction().equals(Constants.SERIAL_PORT_COMMAND_SCAN)) {
            onDataReceived(bundle.getString("qrcode"));
        }
    }
}
