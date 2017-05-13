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
 * Created by bj on 2016/10/29.
 * 存货输入手机号
 */

public class SaveEnityActivity extends CaptureActivity {

    private View view;
    private ViewHolder viewHolder;
    private String phone;
    private SVProgressHUD mSVProgressHUD;
    @Override
    public View getRootView() {
        view=getLayoutInflater().inflate(R.layout.activity_save_enity, null);
        return view;
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
        hasPause = false;
        if (viewHolder != null)
            viewHolder.input_phone.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!hasPause)
                        SaveSuccessActivity.close(SaveEnityActivity.this);
                }
            }, Constants.SCREEN_TIME_OUT);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() != KeyEvent.ACTION_UP) {//不响应按键抬起时的动作
            //TODO 代码
            //System.out.println("Back pressed. event.getKeyCode() => " + event.getKeyCode() + ", event.getKeyCode() => " + event.getAction());
            //return true;//注意这儿返回值为true时该事件将不会继续往下传递，false时反之。根据程序的需要调整
            final int KeyCode = event.getKeyCode();
            final int kayvalue = KeyCode;
            if (viewHolder == null)
                return true;
            if (KeyEvent.KEYCODE_ENTER == KeyCode||KeyCode==KeyEvent.KEYCODE_F1) {
                /*String phone = viewHolder.input_phone.getText().toString().trim();
                if (!TextUtils.isEmpty(phone)) {
                    detailPhone(phone);
                }*/
                return true;
            }
            if (KeyEvent.KEYCODE_BACK == KeyCode||KeyCode==KeyEvent.KEYCODE_F2) {
                finish();
                return true;
            }
            if (KeyEvent.KEYCODE_PAGE_UP == KeyCode) {
                viewHolder.input_phone.requestFocus();
                return true;
            }
            if (KeyEvent.KEYCODE_PAGE_DOWN == KeyCode) {
                viewHolder.input_pass.requestFocus();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private ScanReceiver scanReceiver;
    @Override
    public void init(Activity activity) {
        mSVProgressHUD=new SVProgressHUD(this);
        if (viewHolder==null)
            viewHolder=new ViewHolder(view);
        scanReceiver=new ScanReceiver() {
            @Override
            public void onDataReceived(String qrcode) {
                scanResult(qrcode);
            }
        }.regiest();
        viewHolder.input_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                try {
                    InputMethodManager imm = (InputMethodManager) SaveEnityActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(viewHolder.input_phone.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        viewHolder.input_pass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                try {
                    InputMethodManager imm = (InputMethodManager) SaveEnityActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(viewHolder.input_pass.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        viewHolder.input_phone.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                LogUtils.e("this touch at ++++");
                final int KeyCode = event.getKeyCode();
                final int kayvalue = KeyCode;
                if (KeyEvent.KEYCODE_ENTER == KeyCode || KeyCode == KeyEvent.KEYCODE_F1) {
                    String phone1 = viewHolder.input_phone.getText().toString().trim();
                    if (!TextUtils.isEmpty(phone1)) {
                        detailPhone(phone1);
                    } else {
                        DialogUtil.showInfoWithStatus(mSVProgressHUD, "没有输入内容");
                    }
                    return true;
                }
                if (KeyEvent.KEYCODE_BACK == KeyCode || KeyCode == KeyEvent.KEYCODE_F2) {
                    finish();
                    return true;
                }
                if (KeyEvent.KEYCODE_PAGE_UP == KeyCode) {
                    viewHolder.input_phone.requestFocus();
                    return true;
                }
                if (KeyEvent.KEYCODE_PAGE_DOWN == KeyCode) {
                    viewHolder.input_pass.requestFocus();
                    return true;
                }
                return false;
            }
        });
        viewHolder.input_pass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                final int KeyCode = event.getKeyCode();
                final int kayvalue = KeyCode;
                if (KeyEvent.KEYCODE_ENTER == KeyCode || KeyCode == KeyEvent.KEYCODE_F1) {
                    String pass = viewHolder.input_pass.getText().toString().trim();
                    if (!TextUtils.isEmpty(pass)) {
                        detailPass(pass);
                    } else {
                        DialogUtil.showInfoWithStatus(mSVProgressHUD, "没有输入内容");
                    }
                    return true;
                }
                if (KeyEvent.KEYCODE_BACK == KeyCode || KeyCode == KeyEvent.KEYCODE_F2) {
                    finish();
                    return true;
                }
                if (KeyEvent.KEYCODE_PAGE_UP == KeyCode) {
                    viewHolder.input_phone.requestFocus();
                    return true;
                }
                if (KeyEvent.KEYCODE_PAGE_DOWN == KeyCode) {
                    viewHolder.input_pass.requestFocus();
                    return true;
                }

                return false;
            }
        });
    }
    @Override
    protected void onDestroy() {
        scanReceiver.setEnd(true);
        SerialPortServer.getInstance().closeScanPort();
        super.onDestroy();
    }

    private void scanResult(String result) {
        LogUtils.e("扫描结果："+result);
        DialogUtil.showInfoWithStatus(mSVProgressHUD,"扫描结果\n"+result);
        try {
            JSONObject object=new JSONObject();
            object.put("qrCode",result);
            object.put("deviceId", AppConfig.getInstance().getString("deviceId",""));
            HttpUtilS.postJson(object.toString(), "loginByCode", new InterFaceUtil.OnHttpInterFace() {
                @Override
                public void onSuccess(final String str) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        try {
                            JSONObject obj=new JSONObject(str);
                            success(obj.getString("phoneNumber"),obj.getString("keyPwd"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            DialogUtil.showErrorWithStatus(mSVProgressHUD,"json解析失败");
                        }
                    }});
                }

                @Override
                public void onFail() {
                    DialogUtil.showErrorWithStatus(mSVProgressHUD,"验证失败");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void detailPass(String charSequence) {
        if (charSequence.length()==6/*&& TextUtils.isDigitsOnly(charSequence.toString())*/){
            try {
                final String code=charSequence.toString();
                JSONObject object=new JSONObject();
                object.put("phoneNumber",phone);
                object.put("keyPwd",code);
                object.put("deviceId", AppConfig.getInstance().getString("deviceId",""));
                HttpUtilS.postJson(object.toString(), "loginByPhone", new InterFaceUtil.OnHttpInterFace() {
                    @Override
                    public void onSuccess(String str) {
                        success(phone,code);
                    }

                    @Override
                    public void onFail() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            if (viewHolder!=null)
                                viewHolder.input_pass.setText("");
                            DialogUtil.showErrorWithStatus(mSVProgressHUD,"验证失败");
                        }});
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean canSendCode=true;
    private void detailPhone(String charSequence) {
        if (charSequence.length()==11&&DrawableUtil.isMobileNO(charSequence.toString())&&canSendCode){
            try {
                phone=charSequence.toString();
                JSONObject object=new JSONObject();
                object.put("phoneNumber",charSequence.toString());
                object.put("deviceId", AppConfig.getInstance().getString("deviceId",""));
                canSendCode=false;
                HttpUtilS.postJson(object.toString(), "getSmsCode", new InterFaceUtil.OnHttpInterFace() {
                    @Override
                    public void onSuccess(String str) {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    viewHolder.input_pass.requestFocus();
                                    DialogUtil.showSVProgressHUD(mSVProgressHUD, "验证码发送成功,请注意查收！", null);
                                    canSendCode = true;
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail() {
                        canSendCode = true;
                        viewHolder.input_pass.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DialogUtil.showErrorWithStatus(mSVProgressHUD, "验证码发送失败！");
                            }},500);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if (!canSendCode){
            DialogUtil.showInfoWithStatus(mSVProgressHUD,"请稍后再试,验证码已发送");
        }else {
            DialogUtil.showInfoWithStatus(mSVProgressHUD,"请输入正确格式的手机号");
        }
    }

    private void success(final String phone, final String code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtil.showSVProgressHUD(mSVProgressHUD, "登陆成功", null);
                Intent intent = new Intent(SaveEnityActivity.this, SaveLockRestDetailActivity.class);
                intent.putExtra("phoneNumber", phone);
                intent.putExtra("keyPwd", code);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void result(String result) {
        scanResult(result);
    }

    public static class ViewHolder {
        public View rootView;
        public ImageView background;
        public ImageView imageView2;
        public ImageView imageView3;
        public EditText input_phone;
        public EditText input_pass;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.background = (ImageView) rootView.findViewById(R.id.background);
            this.input_phone = (EditText) rootView.findViewById(R.id.input_phone);
            this.input_pass = (EditText) rootView.findViewById(R.id.input_pass);
            DrawableUtil.displayImage(rootView.getContext(),background,R.drawable.background);
        }

    }
}
