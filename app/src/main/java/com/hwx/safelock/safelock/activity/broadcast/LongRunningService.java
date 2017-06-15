package com.hwx.safelock.safelock.activity.broadcast;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.hwx.safelock.safelock.activity.BaseMainActivity;
import com.hwx.safelock.safelock.activity.MainActivity;
import com.hwx.safelock.safelock.util.HttpUtilS;
import com.hwx.safelock.safelock.util.LogUtils;

import java.util.Date;
import java.util.List;

import android_serialport_api.SerialPortServer;

public class LongRunningService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("定时任务 沟通板子LongRunningService---后台还是前台",isBackground(LongRunningService.this)+ "executed at " + new Date().toString());
                SerialPortServer.getInstance().sendData((byte)0x10,new byte[]{(byte)0f},true);//和硬件沟通 每十秒发送一次心跳包
                MainActivity.refreshDoor(LongRunningService.this,BaseMainActivity.mSVProgressHUD);//刷门
                //HttpUtilS.refreshApplication(LongRunningService.this,BaseMainActivity.filePath, BaseMainActivity.lHost,false);//apk
                if (isBackground(LongRunningService.this)){
                    stopSelf();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }

        }).start();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        int anHour = 20* 1000;   // 毫秒数

        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);

        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);

    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    Log.i("后台", appProcess.processName);
                    return true;
                }else{
                    Log.i("前台", appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }


    public static boolean isProessRunning(Context context, String proessName) {

        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo info : lists){
            if(info.processName.equals(proessName)){
                //Log.i("Service2进程", ""+info.processName);
                isRunning = true;
            }
        }

        return isRunning;
    }

    public static boolean isServiceRunning(Context context, String serviceName) {

        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> lists = am.getRunningServices(30);

        for (ActivityManager.RunningServiceInfo info : lists) {//判断服务
            if(info.service.getClassName().equals(serviceName)){
                Log.i("Service1进程", ""+info.service.getClassName());
                isRunning = true;
            }
        }


        return isRunning;
    }

}