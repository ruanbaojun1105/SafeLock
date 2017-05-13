package com.hwx.safelock.safelock.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hwx.safelock.safelock.Application;
import com.hwx.safelock.safelock.Constants;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.util.DrawableUtil;

/**
 * Created by Administrator on 2016/10/29.
 */

public class SaveSuccessActivity extends AppCompatActivity {

    private TextView position;

    public static void close(Activity activity){
        try {
            Intent intent=new Intent(Application.getContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Application.getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            if (activity==null)
                return;
            Intent intent=new Intent(activity, MainActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() != KeyEvent.ACTION_UP) {//不响应按键抬起时的动作
            final int KeyCode = event.getKeyCode();
            final int kayvalue = KeyCode;
            if (KeyEvent.KEYCODE_ENTER == KeyCode||KeyCode==KeyEvent.KEYCODE_F1) {
                return true;
            }
            if (KeyEvent.KEYCODE_BACK == KeyCode||KeyEvent.KEYCODE_PAGE_UP == KeyCode||KeyCode==KeyEvent.KEYCODE_F2) {
                close(SaveSuccessActivity.this);
                return true;
            }
            if (KeyEvent.KEYCODE_PAGE_DOWN == KeyCode) {
                Intent intent=new Intent(SaveSuccessActivity.this, SaveLockRestDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("phoneNumber", getIntent().getExtras().getString("phoneNumber"));
                intent.putExtra("keyPwd",getIntent().getExtras().getString("keyPwd"));
                startActivity(intent);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
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
        position.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!hasPause)
                    close(SaveSuccessActivity.this);
            }
        },  Constants.SCREEN_TIME_OUT);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_success);
        initView();
        position.setText(getIntent().getExtras().getString("position"));
        position.postDelayed(new Runnable() {
            @Override
            public void run() {
            if (!SaveSuccessActivity.this.isDestroyed() || !SaveSuccessActivity.this.isFinishing())
                MainActivity.soundPool.play(MainActivity.anInt3, 1, 1, 0, 0, 1);//五秒后播放记得关门
        }},5000);
    }

    private void initView() {
        position = (TextView) findViewById(R.id.position);
        ImageView background = (ImageView) findViewById(R.id.background);
        DrawableUtil.displayImage(this,background,R.drawable.background);
    }
}
