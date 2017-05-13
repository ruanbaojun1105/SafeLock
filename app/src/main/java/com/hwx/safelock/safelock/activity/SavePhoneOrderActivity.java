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

import android_serialport_api.SerialPortServer;

/**
 * Created by Administrator on 2016/10/29.
 */

public class SavePhoneOrderActivity extends CaptureActivity {

    private EditText input_phone;
    private SVProgressHUD mSVProgressHUD;
    private int position;

    @Override
    public View getRootView() {
        position=getIntent().getIntExtra("position",0);
        return getLayoutInflater().inflate(R.layout.activity_save_phone_order,null);
    }
    boolean hasPause;
    @Override
    protected void onPause() {
        super.onPause();
        hasPause=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasPause=false;
        input_phone.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!hasPause)
                    SaveSuccessActivity.close(SavePhoneOrderActivity.this);
            }
        }, Constants.SCREEN_TIME_OUT);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() != KeyEvent.ACTION_UP) {//不响应按键抬起时的动作
            final int KeyCode = event.getKeyCode();
            final int kayvalue = KeyCode;
            if (KeyEvent.KEYCODE_ENTER == KeyCode||KeyCode==KeyEvent.KEYCODE_F1) {
                String phone = input_phone.getText().toString().trim();
                if (!TextUtils.isEmpty(phone)) {
                    detailPhone(phone);
                }
                return true;
            }
            if (KeyEvent.KEYCODE_BACK == KeyCode||KeyCode==KeyEvent.KEYCODE_F2) {
                //此处对话框提示
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.showInfoDialog(SavePhoneOrderActivity.this, "是否结束本次存货？", new InterFaceUtil.OnTimerInterFaceBool() {
                            @Override
                            public void onClick(boolean at) {
                                if (at) {
                                    SaveSuccessActivity.close(SavePhoneOrderActivity.this);
                                }
                            }
                        });
                    }});
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
    private void detailPhone(final String charSequence) {
        if (charSequence.length()==11&&DrawableUtil.isMobileNO(charSequence.toString())){
                check(new InterFaceUtil.OnTimerInterFaceBool() {
                    @Override
                    public void onClick(final boolean at) {
                        try {
                            String phone = charSequence.toString();
                            JSONObject object = new JSONObject();
                            object.put("addresseeNumber", phone);
                            object.put("deviceId", AppConfig.getInstance().getString("deviceId", ""));
                            object.put("phoneNumber", getIntent().getExtras().getString("phoneNumber"));
                            object.put("keyPwd", getIntent().getExtras().getString("keyPwd"));
                            object.put("position", String.valueOf(position));
                            HttpUtilS.postJson(object.toString(), "saveCommodityInfo_two", new InterFaceUtil.OnHttpInterFace() {
                                @Override
                                public void onSuccess(String str) {
                                    openIntent(at, null);
                                }

                                @Override
                                public void onFail() {
                                    input_phone.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            DialogUtil.showErrorWithStatus(mSVProgressHUD, "信息保存失败");
                                        }
                                    }, 200);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        }else {
            DialogUtil.showInfoWithStatus(mSVProgressHUD,"请输入正确格式的手机号");
        }
    }
    private ScanReceiver scanReceiver;
    @Override
    public void init(Activity activity) {
        mSVProgressHUD = new SVProgressHUD(this);
        initView();
        input_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                try {
                    InputMethodManager imm = (InputMethodManager) SavePhoneOrderActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input_phone.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }});
        scanReceiver= new ScanReceiver() {
            @Override
            public void onDataReceived(String qrcode) {
                scanResult(qrcode);
            }
        }.regiest();
//        input_phone.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                LogUtils.e(charSequence.toString());
//                detailPhone(charSequence.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
    }
    @Override
    protected void onDestroy() {
        scanReceiver.setEnd(true);
        SerialPortServer.getInstance().closeScanPort();
        super.onDestroy();
    }
    private void scanResult(final String result) {
        LogUtils.e("扫描结果："+result);
        DialogUtil.showInfoWithStatus(mSVProgressHUD,"扫描结果\n"+result);
        check(new InterFaceUtil.OnTimerInterFaceBool() {
            @Override
            public void onClick(final boolean at) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("orderNumer", result);
                    object.put("deviceId", AppConfig.getInstance().getString("deviceId", ""));
                    object.put("phoneNumber", getIntent().getExtras().getString("phoneNumber"));
                    object.put("keyPwd", getIntent().getExtras().getString("keyPwd"));
                    object.put("position", String.valueOf(position));
                    HttpUtilS.postJson(object.toString(), "saveCommodityInfo_one", new InterFaceUtil.OnHttpInterFace() {
                        @Override
                        public void onSuccess(String str) {
                            openIntent(at, str);
                        }

                        @Override
                        public void onFail() {
                            input_phone.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    DialogUtil.showErrorWithStatus(mSVProgressHUD, "未找到扫描的订单信息");
                                }
                            }, 1520);

                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void check(final InterFaceUtil.OnTimerInterFaceBool listener){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtil.showInfoDialog(SavePhoneOrderActivity.this, "是否需要开启加热？", new InterFaceUtil.OnTimerInterFaceBool() {
                    @Override
                    public void onClick(final boolean at) {
                        try {
                            DialogUtil.showSVProgressHUD(mSVProgressHUD, (at ? "已开启加热" : "不开启加热") + "\n即将打开柜门", new InterFaceUtil.OnclickInterFaceOver() {
                                @Override
                                public void onClick() {
                                    listener.onClick(at);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onClick(at);
                        }
                    }});
            }
        });
    }

    private void openIntent(final boolean openHot, final String info){
        try {
            JSONObject object=new JSONObject();
            object.put("deviceId", AppConfig.getInstance().getString("deviceId",""));
            object.put("isHeat",openHot?"0":"1");
            object.put("position",String.valueOf(position));
            HttpUtilS.postJson(object.toString(),"sendHeat",null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (openHot) {
                    SerialPortServer.getInstance().sendData((byte) 0x02, new byte[]{(byte) position}, true);//开灯
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SerialPortServer.getInstance().sendData((byte) 0x01, new byte[]{(byte) position}, true);//开锁
            }
        }).start();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SavePhoneOrderActivity.this, SaveSuccessActivity.class);
                intent.putExtra("position", String.valueOf(position));
                intent.putExtra("phoneNumber", getIntent().getExtras().getString("phoneNumber"));
                intent.putExtra("keyPwd", getIntent().getExtras().getString("keyPwd"));
                if (!TextUtils.isEmpty(info))
                    intent.putExtra("info", info);
                startActivity(intent);
                finish();
            }});
    }

    @Override
    public void result(String result) {
        scanResult(result);
    }

    private void initView() {
        input_phone = (EditText) findViewById(R.id.input_phone);

        ImageView background = (ImageView) findViewById(R.id.background);
        DrawableUtil.displayImage(this,background,R.drawable.background);
    }
}
