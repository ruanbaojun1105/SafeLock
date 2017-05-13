package com.hwx.safelock.safelock.activity;
/*
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.znn.ijkplayerdemo.R;
import com.znn.ijkplayerdemo.common.PlayerManager;

public class PlayVideoMainActivity2 extends Activity implements PlayerManager.PlayerStateListener{

    private PlayerManager player;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view=getLayoutInflater().inflate(R.layout.activity_video_,null);
        setContentView(view);
        if (getIntent().getExtras()!=null)
            url=getIntent().getExtras().getString("url");
        //url="http://zv.3gv.ifeng.com/live/zhongwen800k.m3u8";
        url="/storage/sdcard0/相机/video_20161107_170808.mp4";
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }
        initPlayer();
    }

    private void initPlayer() {
        player = new PlayerManager(this);
        player.setFullScreenOnly(true);
        player.setScaleType(PlayerManager.SCALETYPE_FILLPARENT);
        player.playInFullScreen(true);

        player.setPlayerStateListener(this);

        player.setDefaultRetryTime(5000);
        //player.play("http://zv.3gv.ifeng.com/live/zhongwen800k.m3u8");
        player.play(url);
        player.setOnTouch(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                finish();
                return true;
            }
        });
    }


    @Override
    protected void onPause() {
        if (player!=null)
            player.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (player!=null)
            player.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (player!=null)
            player.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onComplete() {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player!=null)
                    player.play(url);//http://tb-video.bdstatic.com/tieba-smallvideo/1252235_1ba154c669cc6c14cff45f9b7281379c.mp4
            }
        },2000);
    }

    @Override
    public void onError() {
        Toast.makeText(this,"视频播放错误！",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onPlay() {

    }
}*/
