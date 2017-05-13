package com.hwx.safelock.safelock.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.hwx.safelock.safelock.AppConfig;
import com.hwx.safelock.safelock.Constants;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.activity.broadcast.ScanReceiver;
import com.hwx.safelock.safelock.util.DialogUtil;
import com.hwx.safelock.safelock.util.DrawableUtil;
import com.hwx.safelock.safelock.util.HttpUtilS;
import com.hwx.safelock.safelock.util.InterFaceUtil;
import com.hwx.safelock.safelock.util.LogUtils;
import com.zbar.lib.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import android_serialport_api.SerialPortServer;

/**
 * Created by Administrator on 2016/10/29.
 */

public class GetEnityActivity extends CaptureActivity {

    private ImageView imageView1;
    private EditText input_phone;
    boolean hasPause;
    private SVProgressHUD mSVProgressHUD;
    @Override
    protected void onPause() {
        super.onPause();
        hasPause=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasPause=false;
        try {
            input_phone.setText("");
            input_phone.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        input_phone.postDelayed(new Runnable() {
            @Override
            public void run() {
            if (!hasPause)
                SaveSuccessActivity.close(GetEnityActivity.this);
        }}, Constants.SCREEN_TIME_OUT);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LogUtils.e("this touch at ++++");
        if(event.getAction() != KeyEvent.ACTION_UP) {//不响应按键抬起时的动作
            final int KeyCode = event.getKeyCode();
            final int kayvalue = KeyCode;
            if (KeyEvent.KEYCODE_ENTER == KeyCode||KeyCode==KeyEvent.KEYCODE_F1) {
                String phone1 = input_phone.getText().toString().trim();
                if (!TextUtils.isEmpty(phone1)) {
                    detail(phone1);
                } else {
                    DialogUtil.showInfoWithStatus(mSVProgressHUD, "没有输入内容");
                    input_phone.requestFocus();
                }
                return true;
            }
            if (KeyEvent.KEYCODE_BACK == KeyCode||KeyCode==KeyEvent.KEYCODE_F2) {
                finish();
                return true;
            }
            if (KeyEvent.KEYCODE_PAGE_UP == KeyCode) {
                return true;
            }
            if (KeyEvent.KEYCODE_PAGE_DOWN == KeyCode) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    public View getRootView() {
        return getLayoutInflater().inflate(R.layout.activity_get_enity,null);
    }

    private ScanReceiver scanReceiver;
    @Override
    public void init(Activity activity) {
        mSVProgressHUD=new SVProgressHUD(this);
        initView();
        input_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                try {
                    InputMethodManager imm = (InputMethodManager)GetEnityActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input_phone.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        scanReceiver=new ScanReceiver() {
            @Override
            public void onDataReceived(String qrcode) {
                scanResult(qrcode);
            }
        }.regiest();
//        input_phone.setOnKeyListener((view13, i, event) -> {
//            LogUtils.e("this touch at ++++");
//            final int KeyCode = event.getKeyCode();
//            final int kayvalue = KeyCode;
//            if (KeyEvent.KEYCODE_ENTER == KeyCode || KeyCode == KeyEvent.KEYCODE_F1) {
//                String phone1 = input_phone.getText().toString().trim();
//                if (!TextUtils.isEmpty(phone1)) {
//                    detail(phone1);
//                } else {
//                    DialogUtil.showInfoWithStatus(mSVProgressHUD, "没有输入内容");
//                }
//                return true;
//            }
//            if (KeyEvent.KEYCODE_BACK == KeyCode || KeyCode == KeyEvent.KEYCODE_F2) {
//                finish();
//                return true;
//            }
//            return false;
//        });
//        input_phone.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                LogUtils.e(charSequence.toString());
//                detail(charSequence.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
    }

    private void detail(String charSequence){
        if (charSequence.length()==6&&TextUtils.isDigitsOnly(charSequence.toString())){
            try {
                String phone=charSequence.toString();
                JSONObject object=new JSONObject();
                object.put("qrCode",phone);
                object.put("deviceId", AppConfig.getInstance().getString("deviceId",""));
                HttpUtilS.postJson(object.toString(), "getCommodity_two", new InterFaceUtil.OnHttpInterFace() {
                    @Override
                    public void onSuccess(String str) {
                        success(str);
                    }

                    @Override
                    public void onFail() {
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               DialogUtil.showInfoWithStatus(mSVProgressHUD, "没找到你的包裹");
                           }
                       });
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            DialogUtil.showInfoWithStatus(mSVProgressHUD,"请输入正确验证码");
        }
    }

    @Override
    public void result(String result) {
        scanResult(result);
    }

    @Override
    protected void onDestroy() {
        scanReceiver.setEnd(true);
        SerialPortServer.getInstance().closeScanPort();
        super.onDestroy();
    }

    private void scanResult(String result){
        LogUtils.e("扫描结果："+result);
        DialogUtil.showInfoWithStatus(mSVProgressHUD,"扫描结果\n"+result);
        try {
            JSONObject object=new JSONObject();
            object.put("qrCode",result);
            object.put("deviceId", AppConfig.getInstance().getString("deviceId",""));
            HttpUtilS.postJson(object.toString(), "getCommodity_one", new InterFaceUtil.OnHttpInterFace() {
                @Override
                public void onSuccess(String str) {
                    success(str);
                }

                @Override
                public void onFail() {
                    input_phone.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtil.showInfoWithStatus(mSVProgressHUD, "没找到您的包裹");
                        }
                    }, 1800);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void success(final String position) {
        if (!TextUtils.isDigitsOnly(position))
            return;
        int a=Integer.parseInt(position);
        try {
            SerialPortServer.getInstance().sendData((byte)0x01,new byte[]{(byte)a},true);//开锁
            SerialPortServer.getInstance().sendData((byte)0x03,new byte[]{(byte)a},true);//关加热
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                DialogUtil.showSVProgressHUD(mSVProgressHUD, "找到了您的包裹,柜门已打开", new InterFaceUtil.OnclickInterFaceOver() {
                    @Override
                    public void onClick() {
                        input_phone.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.soundPool .play(MainActivity.anInt2,1,1, 0, 0, 1);//好好吃饭
                            }},800);
                        Intent intent=new Intent(GetEnityActivity.this, GetSuccessActivity.class);
                        intent.putExtra("position",position);
                        startActivity(intent);
                    }
                });

            }});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initView() {
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        input_phone = (EditText) findViewById(R.id.input_phone);
        ImageView background = (ImageView) findViewById(R.id.background);
        DrawableUtil.displayImage(this,background,R.drawable.background);
        DrawableUtil.displayImage(this,imageView1,R.drawable.kawayi_4);
    }

}
