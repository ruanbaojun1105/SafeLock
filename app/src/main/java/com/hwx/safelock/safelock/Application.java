/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.hwx.safelock.safelock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.hwx.safelock.safelock.activity.broadcast.MyLifecycleHandler;
import com.hwx.safelock.safelock.db.DaoMaster;
import com.hwx.safelock.safelock.db.DaoSession;
import com.hwx.safelock.safelock.db.GreenDaoManager;
import com.hwx.safelock.safelock.util.LogUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

import android_serialport_api.SerialPortServer;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class Application extends android.app.Application {

    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    public static Context mContext;
    public static OkHttpClient mOkHttpClient;
    private static Application _instance;
    public static String versionCode;
    public static int versionCodeNumber;

    public static boolean JNI_SUCCESS = false;

    public SerialPort mSerialPort = null;
    public SerialPort mSerialPortScan = null;

    public SerialPort getmSerialPort() {
        return mSerialPort;
    }

    public SerialPort getmSerialPortScan() {
        return mSerialPortScan;
    }

    public static Application getInstance() {
        return _instance;
    }
    public static Context getContext() {
        return mContext;
    }
    public static OkHttpClient getmOkHttpClient() {
        if (mOkHttpClient==null){
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS);
            mOkHttpClient=builder.connectTimeout(60, TimeUnit.SECONDS).build();
        }
        return mOkHttpClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //if (!BuildConfig.DEBUG) {
            Cockroach.install(new Cockroach.ExceptionHandler() {
                @Override
                public void handlerException(final Thread thread, final Throwable throwable) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MobclickAgent.reportError(Application.this, throwable);
                                Log.d("Cockroach", thread + "\n" + throwable.toString());
                                throwable.printStackTrace();
                                //Toast.makeText(Application.this, "Exception Happend\n" + thread + "\n" + throwable.toString(), Toast.LENGTH_SHORT).show();
                            } catch (Throwable e) {
                            }
                        }
                    });
                }
            });
        //}

        registerActivityLifecycleCallbacks(new MyLifecycleHandler());
        _instance = this;
        mContext = getApplicationContext();
        versionCode=getAppVersion();
        versionCodeNumber=getAppVersionNumber();
        initOkHttpClient();
        //GreenDaoManager.getInstance();
    }

    private int getAppVersionNumber() {
        try {
            PackageInfo packageInfo = Application.getContext().getPackageManager()
                    .getPackageInfo(Application.getContext().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            Log.v("hwx","getAppVersion: Could not get package name: " + e);
            return 0;
        }
    }

    private void initOkHttpClient() {
        _instance = this;
        mContext = getApplicationContext();
        versionCode= getAppVersion();
        File sdcache = getExternalCacheDir();
        int cacheSize = 10 * 1024 * 1024;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize));
        mOkHttpClient = builder.build();
        OkHttpUtils.initClient(mOkHttpClient);
    }
    public SerialPort getConnSerialPort()throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort==null) {
            mSerialPort = getSerialPort("/dev/ttyS1");
            LogUtils.e("-------","开启通信板OK");
        }
        return mSerialPort;
    }
    public SerialPort getScanSerialPort()throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPortScan==null) {
            mSerialPortScan = getSerialPort("/dev/ttyS4");
            LogUtils.e("-------","开启扫描口OK");
        }
        return mSerialPortScan;
    }
    public SerialPort getSerialPort(String path) throws SecurityException, IOException, InvalidParameterException {
        //if (mSerialPort == null) {
            /* Read serial port parameters */
        //SharedPreferences sp = getSharedPreferences("com.hwx.safelock.safelock_preferences", MODE_PRIVATE);
        //String path = sp.getString("DEVICE", "/dev/ttyS0");
        //int baudrate = Integer.decode(sp.getString("BAUDRATE", "115200"));
        int baudrate =9600;

			/* Check parameters */
        if ((path.length() == 0) || (baudrate == -1)) {
            throw new InvalidParameterException();
        }

			/* Open the serial port */
        SerialPort mSerialPort = new SerialPort(new File(path), baudrate, 0);
        //}
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
    public void closeSerialPort2() {
        if (mSerialPortScan != null) {
            mSerialPortScan.close();
            mSerialPortScan = null;
        }
    }
    public static int dip2px( float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    public static int px2dip(float pxValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    public static void sendLocalBroadCast(String action) {
        sendLocalBroadCast(action, null);
    }
    public static void sendLocalBroadCast(String action, Bundle bundle) {
        Intent bi = new Intent();
        bi.setAction(action);
        if (bundle != null)
            bi.putExtras(bundle);
        LocalBroadcastManager.getInstance(getContext())
                .sendBroadcast(bi);
    }
    public static String getAppVersion() {
        try {
            PackageInfo packageInfo = Application.getContext().getPackageManager()
                    .getPackageInfo(Application.getContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            Log.v("hwx","getAppVersion: Could not get package name: " + e);
            return "";
        }
    }
}
