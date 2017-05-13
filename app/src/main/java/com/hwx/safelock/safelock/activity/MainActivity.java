package com.hwx.safelock.safelock.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.hwx.safelock.safelock.AppConfig;
import com.hwx.safelock.safelock.Application;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.fragment.VideoFragment;
import com.hwx.safelock.safelock.util.DialogUtil;
import com.hwx.safelock.safelock.util.FileUtil;
import com.hwx.safelock.safelock.util.HttpUtilS;
import com.hwx.safelock.safelock.util.InterFaceUtil;
import com.hwx.safelock.safelock.util.LogUtils;
import com.ldoublem.loadingviewlib.view.LVBlazeWood;
import com.umeng.analytics.MobclickAgent;
import com.zbar.lib.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import android_serialport_api.SerialPortServer;
import android_serialport_api.sample.ConsoleActivity;
import android_serialport_api.sample.HwxTestActivity;
import android_serialport_api.sample.LoopbackActivity;
import android_serialport_api.sample.SendingActivity;
import android_serialport_api.sample.SerialPortPreferences;

public class MainActivity extends BaseMainActivity implements View.OnClickListener{


    private void replaceVideofragment(final String videoUrl) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.blank_video, VideoFragment.newInstance(videoUrl)).commitAllowingStateLoss();
            }});
        System.gc();
    }

    @Override
    void initPlayer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(videoUrl)) {
                    if (!lHost.isAnimatorRunning())
                        lHost.startAnim();
                    if (lHost.getVisibility() == View.GONE)
                        lHost.setVisibility(View.VISIBLE);
                    return;
                }
                if (lHost == null)
                    return;
                if (lHost.isAnimatorRunning())
                    lHost.stopAnim();
                if (lHost.getVisibility() == View.VISIBLE)
                    lHost.setVisibility(View.GONE);
                replaceVideofragment(videoUrl);
            }
        });
    }

    @Override
    void initView() {
        mSVProgressHUD = new SVProgressHUD(this);
        soundPool = new SoundPool(15, AudioManager.STREAM_MUSIC, 5);
        anInt1 = soundPool.load(this, R.raw.dengdaoni, 1);
        anInt2 = soundPool.load(this, R.raw.haohaochi, 1);
        anInt3 = soundPool.load(this, R.raw.jideguanmen, 1);
        lHost=(LVBlazeWood) findViewById(R.id.lv_LVGhost);
        lHost.startAnim();
        startTimmerLoop();

        TextView textView= (TextView) findViewById(R.id.textView);
        //textView.setText(ScreenParamsUtil.getInstance(this).toString());
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((System.currentTimeMillis() - exitTime) > 500) {
                    exitTime = System.currentTimeMillis();
                } else {
//                try {//重启
//                    Log.v("ttt-->fuck", "root Runtime->reboot");
//                    Process proc =Runtime.getRuntime().exec(new String[]{"su","-c","reboot "});  //关机
//                    proc.waitFor();
//                }catch (Exception ex){
//                    ex.printStackTrace();
//                }
                    DialogUtil.showEditDialog(MainActivity.this, "输入设备号,当前设备号：" + AppConfig.getInstance().getString("deviceId", ""), "deviceId", new InterFaceUtil.OnclickInterFace() {
                        @Override
                        public void onClick(String str) {
                            boolean a = false;
                            try {
                                a = FileUtil.writeTxtFile(str, new File(filePath), "/device.txt");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (a) {
                                HttpUtilS.refreshApplication(MainActivity.this, filePath, lHost, true);
                                AppConfig.getInstance().putString("deviceId", str.trim());
                            }
                            final boolean finalA = a;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DialogUtil.showSVProgressHUD(mSVProgressHUD, finalA ? "OK\n设备号修改成功" : "设备号修改失败", null);
                                }
                            });
                        }
                    });
                }
            }
        });
    }
    /*强制刷新开门*/
    @Override
    void refreshDoor() {
        try {
            JSONObject object = new JSONObject();
            object.put("deviceId", AppConfig.getInstance().getString("deviceId", ""));
            HttpUtilS.postJson(object.toString(), "getTimerComm", new InterFaceUtil.OnHttpInterFace() {
                @Override
                public void onSuccess(String str) {
                    try {
                        if (TextUtils.isEmpty(str))
                            return;
                        final JSONObject obj = new JSONObject(str);
                        //runOnUiThread(() -> DialogUtil.showSVProgressHUD(mSVProgressHUD, obj.toString(), null));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000 * (detailData(obj.getString("openDoor"), (byte) 0x01)));
                                    Thread.sleep(1000 * (detailData(obj.getString("openHeat"), (byte) 0x02)));
                                    Thread.sleep(1000 * (detailData(obj.getString("closeHeat"), (byte) 0x03)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail() {
                    DialogUtil.showErrorWithStatus(mSVProgressHUD, "强制开门刷新失败");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPause() {
        MobclickAgent.onPageEnd("main"); // （仅有Activity的应用中SDK自动调用，不需要单独写）保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息。"SplashScreen"为页面名称，可自定义
        MobclickAgent.onPause(this);
        try {
            stop();
            mWakeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        MobclickAgent.onPageStart("main"); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);          //统计时长
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!Application.JNI_SUCCESS) {
                    Application.JNI_SUCCESS = SerialPortServer.getInstance().initPort();
                }
            }
        }).start();
        star();
        mWakeLock.acquire();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        stopTimerLoop();
        //SerialPortServer.getInstance().close();//不可关闭
        super.onDestroy();
    }

    private synchronized int detailData(String str, final byte code) {
        if (TextUtils.isEmpty(str)){
            return 0;
        }
        str.trim();
        String[] arr=str.split(",");
        if (arr==null){
            return 0;
        }
        if (arr.length==0){
            return 0;
        }
        for (int i = 0; i < arr.length; i++) {
            if (TextUtils.isDigitsOnly(arr[i])){
                try {
                    final int position=Integer.parseInt(arr[i]);
                    new Handler(getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SerialPortServer.getInstance().sendData(code, new byte[]{(byte) position}, true);//开锁
                            LogUtils.e("---" + code + "---" + position);
                        }
                    },1000*i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return arr.length;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.console:
                startActivity(new Intent(this, ConsoleActivity.class));
                break;
            case R.id.test:
                startActivity(new Intent(this, HwxTestActivity.class));
                break;
            case R.id.loopback:
                startActivity(new Intent(this, LoopbackActivity.class));
                break;
            case R.id.sending:
                startActivity(new Intent(this, SendingActivity.class));
                break;
            case R.id.serialport:
                startActivity(new Intent(this, SerialPortPreferences.class));
                break;
            case R.id.save:
                startActivity(new Intent(this, SaveEnityActivity.class));
                break;
            case R.id.get:
                startActivity(new Intent(this, GetEnityActivity.class));
                soundPool.play(anInt1, 1, 1, 0, 0, 1);//终于等到你
                break;
            case R.id.set:
                startActivity(new Intent(this, SerialPortPreferences.class));
                break;
            case R.id.ercode:
                // 跳转到扫描画面
                Intent intent = new Intent();
                intent.setClass(this, CaptureActivity.class);
                startActivityForResult(intent, 1);
                break;
        }
    }

}
