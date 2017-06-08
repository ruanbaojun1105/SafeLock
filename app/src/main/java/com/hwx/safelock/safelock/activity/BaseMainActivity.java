package com.hwx.safelock.safelock.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.hwx.safelock.safelock.AppConfig;
import com.hwx.safelock.safelock.Application;
import com.hwx.safelock.safelock.Constants;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.activity.broadcast.CommandReceiver;
import com.hwx.safelock.safelock.util.ACache;
import com.hwx.safelock.safelock.util.DialogUtil;
import com.hwx.safelock.safelock.util.FileUtil;
import com.hwx.safelock.safelock.util.HttpUtilS;
import com.hwx.safelock.safelock.util.InterFaceUtil;
import com.hwx.safelock.safelock.util.LogUtils;
import com.ldoublem.loadingviewlib.view.LVBlazeWood;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.SerialPortServer;

/**
 * Created by Administrator on 2016/10/29.
 */

public abstract class BaseMainActivity extends AppCompatActivity {
    public static String videoUrl;
    protected String filePath = "";
    private boolean isTest = false;
    public static SoundPool soundPool;
    public static int anInt1, anInt2, anInt3;

    public static long exitTime = 0;
    private int recLen = 1;
    private int newversionCode = 0;
    private Handler handler = new Handler();

    protected LVBlazeWood lHost;
    protected SVProgressHUD mSVProgressHUD;
    protected PowerManager.WakeLock mWakeLock;
    abstract void initPlayer();
    abstract void initView();
    abstract void refreshDoor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        if (!LibsChecker.checkVitamioLibs(this))
//            return;
        setContentView(R.layout.activity_video_main);
        if (getIntent().getExtras()!=null){
            newversionCode=getIntent().getExtras().getInt("version");
            if (newversionCode!=0){
                Toast.makeText(this,"安装新版本成功",Toast.LENGTH_SHORT).show();
            }
        }
        //AppConfig.getInstance().putString("deviceId", "rivertest");
        MobclickAgent. startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this,"58ae7704f43e4853d5002308","bj", MobclickAgent.EScenarioType. E_UM_NORMAL));
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        initView();
        new CommandReceiver() {
            @Override
            public void onDataReceived(byte[] buffer, byte function, byte safeCod) {

            }
            @Override
            public void onFail(String str) {
                Application.JNI_SUCCESS=false;
                DialogUtil.showErrorWithStatus(mSVProgressHUD, str + "\n设备号：" + AppConfig.getInstance().getString("deviceId", ""));
            }
        }.regiest(this);
        new InitReciver().regiest();
        initData();
    }

    private void initData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //此处先获取视频资源
                filePath = FileUtil.getSDPath() + "/居一格";
            //url="file:///android_asset/video.mp4";
            File file=new File(filePath+"/url");
            String a1= ACache.get(file).getAsString("videoUrl");
            String a2=ACache.get(file).getAsString("videoUrlTag");
            //String a1=AppConfig.getInstance().getString("videoUrl","");
            //String a2=AppConfig.getInstance().getString("videoUrlTag","");
            if (!TextUtils.isEmpty(a1)) {
                videoUrl=a1;
            }else if (!TextUtils.isEmpty(a2)) {
                videoUrl=a2;
            }

            String id = FileUtil.readTxtFile(new File(filePath + "/device.txt"));
            if (!TextUtils.isEmpty(id)) {
                AppConfig.getInstance().putString("deviceId", id.trim());
                LogUtils.e("set id for " + id);
            }
            String server = FileUtil.readTxtFile(new File(filePath + "/server.txt"));
            if (!TextUtils.isEmpty(server)) {
                if (!(server.length()<10)) {
                    HttpUtilS.server = server;
                    LogUtils.e("set server for " + server);
                }
            }
            //DialogUtil.showInfoWithStatus(mSVProgressHUD, "\n设备号：" + AppConfig.getInstance().getString("deviceId", ""));
            initPlayer();
            HttpUtilS.refreshApplication(BaseMainActivity.this,filePath, lHost,true);
        }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler!=null)
            handler=null;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        final int KeyCode = event.getKeyCode();
        final int kayvalue = KeyCode;

        /*if(KeyCode==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN && event.getCharacters()!=null){
            LogUtils.e("扫描枪数据："+event.getCharacters());
            return true;
        }*/

        if (KeyEvent.KEYCODE_ENTER == KeyCode) {
            LogUtils.e("刷枪扫描了一次");
        }
        if (KeyEvent.KEYCODE_BACK == KeyCode) {

        }
        if (KeyEvent.KEYCODE_F10 == KeyCode) {
            System.exit(0);
        }
        if (KeyEvent.KEYCODE_PAGE_UP == KeyCode) {
            lHost.post(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(BaseMainActivity.this, SaveEnityActivity.class));
                }});
        }
        if (KeyEvent.KEYCODE_PAGE_DOWN == KeyCode) {
            lHost.post(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(BaseMainActivity.this, GetEnityActivity.class));}
            });
            lHost.postDelayed(new Runnable() {
                @Override
                public void run() {
                    soundPool.play(anInt1, 1, 1, 0, 0, 1);//终于等到你
                }
            },1500);
        }

        recLen = 1;
        if (KeyEvent.KEYCODE_F4 == KeyCode) {//开门
            if (isTest)
                return true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogUtils.e(recLen + "f4");
                    if (recLen > 71) {
                        isTest = false;
                        handler.removeCallbacks(this);
                        recLen = 1;
                    } else {
                        isTest = true;
                        SerialPortServer.getInstance().sendData((byte) 0x01, new byte[]{(byte) recLen}, true);//开锁
                        recLen++;
                        handler.postDelayed(this, 500);
                    }
                }
            }, 500);
        }
        if (KeyEvent.KEYCODE_F5 == KeyCode) {//开热
            if (isTest)
                return true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogUtils.e(recLen + "f5");
                    if (recLen > 71) {
                        isTest = false;
                        handler.removeCallbacks(this);
                        recLen = 1;
                    } else {
                        isTest = true;
                        SerialPortServer.getInstance().sendData((byte) 0x02, new byte[]{(byte) recLen}, true);//开灯
                        SerialPortServer.getInstance().sendData2((byte) 0xff, new byte[]{(byte) recLen}, true);//开锁
                        recLen++;
                        handler.postDelayed(this, 500);
                    }
                }
            }, 500);
        }
        if (KeyEvent.KEYCODE_F6 == KeyCode) {//关热
            if (isTest)
                return true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogUtils.e(recLen + "f6");
                    if (recLen > 71) {
                        isTest = false;
                        handler.removeCallbacks(this);
                        recLen = 1;
                    } else {
                        isTest = true;
                        SerialPortServer.getInstance().sendData((byte) 0x03, new byte[]{(byte) recLen}, true);
                        recLen++;
                        handler.postDelayed(this, 500);
                    }
                }
            }, 500);
        }
        if (KeyEvent.KEYCODE_F8 == KeyCode) {
            lHost.post(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(BaseMainActivity.this, SaveEnityActivity.class));}});
        }
        if (KeyEvent.KEYCODE_F9 == KeyCode) {
            lHost.post(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(BaseMainActivity.this, GetEnityActivity.class));}
                });
            lHost.postDelayed(new Runnable() {
                @Override
                public void run() {
                    soundPool.play(anInt1, 1, 1, 0, 0, 1);//终于等到你
                }
            },1500);
        }
        return true;
    }

    class MyTask extends TimerTask {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message message = new Message();
            message.what = 1;
            timerHandler.sendMessage(message);
        }
    }

    protected void stop() {
        number = 0;
        isStop = true;
    }

    protected void star() {
        isStop = false;
    }

    private int number = 0;
    private int count = 1;
    private Timer timer;
    protected boolean isStop = false;
    /**
     * 处理UI操作
     */
    private Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtils.e("计时器定时处理：" + number);
            number++;
            if (count>60000)
                count=0;
            if (number>60000)
                count=0;
            if (isStop) {
                number = 0;
                return;
            }
            if (number%60==0){
                LogUtils.e("第"+count+"次强制开门----");
                count++;
                refreshDoor();
            }
            if (number%10==0){
                SerialPortServer.getInstance().sendData((byte)0x10,new byte[]{(byte)0f},true);//和硬件沟通 每十秒发送一次心跳包
            }
            if (number%1800==0){//半小时刷新一次软件升级
                HttpUtilS.refreshApplication(BaseMainActivity.this,filePath, lHost,false);
            }
        }
    };

    protected void startTimmerLoop() {
        number = 0;
        timer=null;
        timer = new Timer();
        MyTask task = new MyTask();
        timer.schedule(task, 1000, 1000);//第二个参数是等待一秒后执行schedule，第三个参数是每隔一秒重复执行一次
    }

    protected void stopTimerLoop() {
        if (timer != null)
            timer.cancel();
    }

    class InitReciver extends BroadcastReceiver {
        public void regiest() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.VIDEO_DO_INIT);
            LocalBroadcastManager.getInstance(Application.getContext())
                    .registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //TextView tv = textView.get();
            if (action.equals(Constants.VIDEO_DO_INIT)){
                initPlayer();
            }
        }
    }
    @Deprecated
    private void initFile(final Activity context, final InterFaceUtil.OnclickInterFaceOver over) {
        new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                boolean has = false;
                String[] paths = FileUtil.getExtSDCardPath(context);
                for (String path : paths) {
                    File at = new File(path + "/居一格");
                    if (at.exists()) {
                        if (path.contains(FileUtil.getInnerSDCardPath())) {
                            has=true;
                            continue;
                        }
                        DialogUtil.showInfoWithStatus(mSVProgressHUD, "开始加载资源文件...");
                        has = true;
                        try {
                            FileUtil.copyFolder(at.getAbsolutePath(), filePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    } else {
                        at = null;
                        continue;
                    }
                }
                if (!has) {
                    InputStream is = getAssets().open("video.mp4");
                    InputStream is2 = getAssets().open("device.txt");
                    FileUtil.copyFilesFassets(is, is2, filePath, "video.mp4", "device.txt", over);
                }else {
                    over.onClick();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }}).start();
    }
}
