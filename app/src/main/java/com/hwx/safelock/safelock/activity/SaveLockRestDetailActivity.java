package com.hwx.safelock.safelock.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.hwx.safelock.safelock.AppConfig;
import com.hwx.safelock.safelock.Application;
import com.hwx.safelock.safelock.Constants;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.activity.broadcast.ItemClickAdapter;
import com.hwx.safelock.safelock.util.DialogUtil;
import com.hwx.safelock.safelock.util.DrawableUtil;
import com.hwx.safelock.safelock.util.HttpUtilS;
import com.hwx.safelock.safelock.util.InterFaceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/29.
 */

public class SaveLockRestDetailActivity extends AppCompatActivity {

    private ImageView background;
    private RecyclerView detail_list;
    private EditText input_phone;

    private boolean canBack=false;
    boolean hasPause;
    private SVProgressHUD mSVProgressHUD;
    private ItemClickAdapter mAdapter;
    private GridLayoutManager gridLayoutManager;
    @Override
    protected void onPause() {
        super.onPause();
        hasPause=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasPause=false;
        detail_list.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!hasPause)
                    SaveSuccessActivity.close(SaveLockRestDetailActivity.this);
            }
        }, Constants.SCREEN_TIME_OUT);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() != KeyEvent.ACTION_UP) {//不响应按键抬起时的动作
            final int KeyCode = event.getKeyCode();
            final int kayvalue = KeyCode;
            if (KeyEvent.KEYCODE_ENTER == KeyCode||KeyCode==KeyEvent.KEYCODE_F1) {
                String charSequence = input_phone.getText().toString().trim();
                if (TextUtils.isEmpty(charSequence))
                    return true;
                if (TextUtils.isDigitsOnly(charSequence.toString())) {
                    Intent intent = new Intent(SaveLockRestDetailActivity.this, SavePhoneOrderActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.putExtra("phoneNumber", getIntent().getExtras().getString("phoneNumber"));
                    intent.putExtra("keyPwd", getIntent().getExtras().getString("keyPwd"));
                    intent.putExtra("position", Integer.parseInt(charSequence.toString()));
                    startActivity(intent);
                    finish();
                }
                return true;
            }
            if (KeyEvent.KEYCODE_BACK == KeyCode||KeyCode==KeyEvent.KEYCODE_F2) {
                //此处对话框提示
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtil.showInfoDialog(SaveLockRestDetailActivity.this, "是否结束本次存货？", new InterFaceUtil.OnTimerInterFaceBool() {
                            @Override
                            public void onClick(boolean at) {
                                if (at) {
                                    SaveSuccessActivity.close(SaveLockRestDetailActivity.this);
                                }
                            }
                        });
                    }
                });
                return true;
            }
            if (KeyEvent.KEYCODE_PAGE_UP == KeyCode) {
                return true;
            }
            if (KeyEvent.KEYCODE_PAGE_DOWN == KeyCode) {
                return true;
            }
            if (KeyEvent.KEYCODE_F7 == KeyCode) {
                List<String> strings=new ArrayList<String>();
                for (int i = 0; i < 23; i++) {
                    strings.add(i+"\t号");
                }
                int co=6;
                if (strings.size()>24&&strings.size()<52)
                    co=9;
                if (strings.size()>51)
                    co=12;
                gridLayoutManager.setSpanCount(co);
                mAdapter.setNewData(strings);
                return true;
            }
            if (KeyEvent.KEYCODE_F8 == KeyCode) {
                List<String> strings=new ArrayList<String>();
                for (int i = 0; i < 50; i++) {
                    strings.add(i+"\t号");
                }
                int co=6;
                if (strings.size()>24&&strings.size()<52)
                    co=9;
                if (strings.size()>51)
                    co=12;
                gridLayoutManager.setSpanCount(co);
                mAdapter.setNewData(strings);
                return true;
            }
            if (KeyEvent.KEYCODE_F9 == KeyCode) {
                List<String> strings=new ArrayList<String>();
                for (int i = 0; i < 71; i++) {
                    strings.add(i+" 号");
                }
                int co=6;
                if (strings.size()>24&&strings.size()<52)
                    co=9;
                if (strings.size()>51)
                    co=12;
                gridLayoutManager.setSpanCount(co);
                mAdapter.setNewData(strings);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_rest_detail);
        mSVProgressHUD=new SVProgressHUD(this);
        initView();
        mAdapter = new ItemClickAdapter(new ArrayList<String>());
        LinearLayout layout=new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        TextView textView=new TextView(this);
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(Application.dip2px(50),Application.dip2px(10));
        textView.setLayoutParams(layoutParams);
        textView.setBackgroundColor(getResources().getColor(R.color.colormain3));
        TextView textView2=new TextView(this);
        textView2.setText("表示可用柜门");
        //layout.setGravity(Gravity.CENTER);
        layout.addView(textView);
        layout.addView(textView2);
        mAdapter.addHeaderView(layout);
        gridLayoutManager=new GridLayoutManager(this,5);
        detail_list.setLayoutManager(gridLayoutManager);
        detail_list.setAdapter(mAdapter);
        try {
            JSONObject object=new JSONObject();
            object.put("deviceId", AppConfig.getInstance().getString("deviceId",""));
            object.put("phoneNumber",getIntent().getExtras().getString("phoneNumber"));
            object.put("keyPwd",getIntent().getExtras().getString("keyPwd"));
            HttpUtilS.postJson(object.toString(), "getDoorUseDetail", new InterFaceUtil.OnHttpInterFace() {
                @Override
                public void onSuccess(String str) {
                    if (TextUtils.isEmpty(str)){
                        error();
                        return;
                    }
                    str.trim();
                    String[] arr=str.split(",");
                    if (arr==null){
                        error();
                        return;
                    }
                    if (arr.length==0){
                        error();
                        return;
                    }
                    final List<String> strings=new ArrayList<String>();
                    for (int i = 0; i < arr.length; i++) {
                        strings.add(arr[i]+"\t号");
                    }
                    detail_list.post(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtil.showInfoWithStatus(mSVProgressHUD, "查询柜门使用详情成功");
                            int co = 4;
                            if (strings.size() > 12 && strings.size() < 25)
                                co = 6;
                            if (strings.size() > 24 && strings.size() < 52)
                                co = 9;
                            if (strings.size() > 51)
                                co = 12;
                            gridLayoutManager.setSpanCount(co);
                            mAdapter.setNewData(strings);
                            mAdapter.removeAllFooterView();
                            input_phone.requestFocus();
                        }
                    });
                }

                @Override
                public void onFail() {
                    detail_list.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DialogUtil.showErrorWithStatus(mSVProgressHUD, "柜门查询失败");
                        }
                    },300);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        input_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                try {
                    InputMethodManager imm = (InputMethodManager) SaveLockRestDetailActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(input_phone.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /*input_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequencea, int i, int i1, int i2) {
                LogUtils.e(charSequencea.toString());
                String charSequence = input_phone.getText().toString().trim();
                if (TextUtils.isEmpty(charSequence))
                    return ;
                if (TextUtils.isDigitsOnly(charSequence.toString())) {
                    Intent intent = new Intent(SaveLockRestDetailActivity.this, SavePhoneOrderActivity.class);
                    intent.putExtra("phoneNumber", getIntent().getExtras().getString("phoneNumber"));
                    intent.putExtra("keyPwd", getIntent().getExtras().getString("keyPwd"));
                    intent.putExtra("position", charSequence.toString());
                    startActivity(intent);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });*/
    }

    private void error() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                input_phone.clearFocus();
                DialogUtil.showErrorWithStatus(mSVProgressHUD, "没有可以使用的柜门,3秒后回到播放视频！");
                detail_list.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SaveSuccessActivity.close(SaveLockRestDetailActivity.this);
                    }
                }, 3000);
            }
        });

    }

    private void initView() {
        detail_list = (RecyclerView) findViewById(R.id.detail_list);
        background = (ImageView) findViewById(R.id.background);
        input_phone = (EditText) findViewById(R.id.input_phone);
        DrawableUtil.displayImage(this,background,R.drawable.background);
    }

}
