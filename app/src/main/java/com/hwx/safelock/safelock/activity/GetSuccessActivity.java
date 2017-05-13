package com.hwx.safelock.safelock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.hwx.safelock.safelock.Application;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.util.DrawableUtil;

/**
 * Created by Administrator on 2016/10/29.
 */

public class GetSuccessActivity extends AppCompatActivity {

    private ImageView imageView1;
    private ImageView background;
    private TextView position;
    boolean pause=false;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() != KeyEvent.ACTION_UP) {//不响应按键抬起时的动作
            final int KeyCode = event.getKeyCode();
            final int kayvalue = KeyCode;
            if (KeyEvent.KEYCODE_ENTER == KeyCode) {
                return true;
            }
            if (KeyEvent.KEYCODE_BACK == KeyCode||KeyEvent.KEYCODE_PAGE_UP == KeyCode) {
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
    protected void onPause() {
        super.onPause();
        pause=true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_success);
        initView();
        try {
            position.setText(getIntent().getExtras().getString("position"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        position.postDelayed(new Runnable() {
            @Override
            public void run() {
        if (!GetSuccessActivity.this.isDestroyed() || !GetSuccessActivity.this.isFinishing())
                MainActivity.soundPool.play(MainActivity.anInt3, 1, 1, 0, 0, 1);//五秒后播放记得关门
        }},5000);
        position.postDelayed(new Runnable() {
            @Override
            public void run() {
            if (!pause)
                SaveSuccessActivity.close(GetSuccessActivity.this);
        }}, 10000);
    }

    private void initView() {
        position = (TextView) findViewById(R.id.position);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        background = (ImageView) findViewById(R.id.background);
        DrawableUtil.displayImage(this,background,R.drawable.background);
        DrawableUtil.displayImage(this,imageView1,R.drawable.kawayi_5);
    }
}
