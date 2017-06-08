package com.hwx.camera_doul.install_tool;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Debug;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.hwx.camera_doul.install_tool.util.LongRunningService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_F10 == keyCode) {
            System.exit(0);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //启动定时任务服务
        Intent intent = new Intent(this, LongRunningService.class);
        startService(intent);

        TextView text= (TextView) findViewById(R.id.text);
        if (getIntent().getExtras()==null) {
            Toast.makeText(this,"no url get",Toast.LENGTH_SHORT).show();
            finish();
            text=null;
            return;
        }
        final String path=getIntent().getExtras().getString("path");
        final int version=getIntent().getExtras().getInt("version");
        final String packageTag=getIntent().getExtras().getString("packageTag");
        LogUtils.e("-----path:"+path+"-----version:"+version+"-----packageTag:"+packageTag);
        boolean isInstall = FileUtil.isAppInstalled(this, "com.hwx.camera_doul.install_tool");
        if (isInstall) {
            ApkController.uninstall(packageTag, this);//先卸载
        }
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                ApkController.install(path, Application.getContext());//静默安装
            }
        },25000);
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtils.e("30秒开始打开软件");
                todo(packageTag, version);
            }
        },55000);
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isTopActivity(packageTag)) {
                    todo(packageTag, version);
                } else LogUtils.e("已在运行");
            }
        },75000);
    }
    private void todo(String packageTag,int version){
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageTag);
        // 这里如果intent为空，就说名没有安装要跳转的应用嘛
        if (intent != null) {
            // 这里跟Activity传递参数一样的嘛，不要担心怎么传递参数，还有接收参数也是跟Activity和Activity传参数一样
            intent.putExtra("version",version);
            startActivity(intent);
        } else {
            // 没有安装要跳转的app应用，提醒一下
            Toast.makeText(MainActivity.this, "哟，赶紧下载安装这个APP吧，逗我呢~~", Toast.LENGTH_LONG).show();
        }
    }
//================================================
        //Android系统内部状态信息的相关api：
        //得到ActivityManager ：
//        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE)
        //这个位查到底是什么信息：
//        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        //获取进程内存状态的信息：
//        Debug.MemoryInfo[] processMemoryInfo = activityManager.getProcessMemoryInfo(processIds);

        //获取当前运行的service信息：
//        List<ActivityManager.RunningServiceInfo> runningServiceInfos = activityManager.getRunningServices(MaxValue);
        //获取当前运行的任务信息：
//        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(MaxValue);
        //其中runningTaskInfos 的 topActivity就是当前Task的活跃Activity
        //在getRunningTasks()所返回的Task队列中系统会根据这些Task的活跃度有一个排序，越活跃越是靠前。第一个就是当前活动的Task

    //最后在应用中添加所需的权限：
//<uses-permission android:name="android.permission.GET_TASKS"/>
    /**get the launcher status */
    private  boolean isLauncherRunnig(Context context) {
        boolean result = false ;
        List<String> names = getAllTheLauncher();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE) ;
        List<ActivityManager.RunningAppProcessInfo> appList = mActivityManager.getRunningAppProcesses() ;
        for (ActivityManager.RunningAppProcessInfo running : appList) {
            if (running.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (int i = 0; i < names.size(); i++) {
                    if (names.get(i).equals(running.processName)) {
                        result = true ;
                        break;
                    }
                }
            }
        }
        return result ;
    }
    private List<String> getAllTheLauncher(){
        List<String> names = null;
        PackageManager pkgMgt = this.getPackageManager();
        Intent it = new Intent(Intent.ACTION_MAIN);
        it.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> ra =pkgMgt.queryIntentActivities(it,0);
        if(ra.size() != 0){
            names = new ArrayList<String>();
        }
        for(int i=0;i< ra.size();i++)
        {
            String packageName =  ra.get(i).activityInfo.packageName;
            names.add(packageName);
        }
        return names;
    }
    //android 如何判断程序是否在前台运行
    private boolean isTopActivity(String packageName){
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo>  tasksInfo = activityManager.getRunningTasks(1);
        if(tasksInfo.size() > 0){
            //应用程序位于堆栈的顶层
            if(packageName.equals(tasksInfo.get(0).topActivity.getPackageName())){
                return true;
            }
        }
        return false;
    }
}
