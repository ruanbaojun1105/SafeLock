package com.hwx.safelock.safelock.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.hwx.safelock.safelock.Constants;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.util.LogUtils;

import java.io.File;

/**
 * Created by Administrator on 2016/9/1.
 */

public class VideoActivity extends Activity implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    public static final String TAG = "VideoPlayer";
    private VideoView mVideoView;
    private Uri mUri;
    private int mPositionWhenPaused = -1;
    private File file;

    private MediaController mMediaController;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setKeepScreenOn(true);
        if (TextUtils.isEmpty(Constants.VIDEO_PATH)){
            Toast.makeText(VideoActivity.this, "视频文件异常", Toast.LENGTH_SHORT).show();
            return;
        }

        file=new File(Constants.VIDEO_PATH);
        mMediaController = new MediaController(this);
        if(file.exists()){
            //VideoView与MediaController进行关联
            mVideoView.setMediaController(mMediaController);
            mMediaController.setMediaPlayer(mVideoView);
            //让VideiView获取焦点
            mVideoView.requestFocus();
            mVideoView.setVideoPath(file.getAbsolutePath());
            mVideoView.start();
        }
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        /*mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                VideoActivity.this.finish();
                return true;
            }
        });
        findViewById(R.id.video_lin).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                VideoActivity.this.finish();
                return true;
            }
        });*/
    }


//监听MediaPlayer上报的错误信息

    @Override
    public boolean onError(MediaPlayer mp, final int what, int extra) {
        // TODO Auto-generated method stub
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String err = "未知错误";
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        err = "媒体服务终止";
                        break;
                    default:
                        break;
                }
                try {
                    Toast.makeText(VideoActivity.this, err+"，2秒后退出。", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 2000);
        return true;
    }


    //Video播完的时候得到通知
    @Override
    public void onCompletion(final MediaPlayer mp) {
        LogUtils.e("播放完成，开始重播");
        new Handler(VideoActivity.this.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mp.release();
                    mMediaController.destroyDrawingCache();
                    mVideoView.destroyDrawingCache();
                    mVideoView.setVideoPath(file.getAbsolutePath());
                    mVideoView.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },3000);

    }


    //暂停
    @Override
    public void onPause() {
        // Stop video when the activity is pause.
        mPositionWhenPaused = mVideoView.getCurrentPosition();
        mVideoView.stopPlayback();
        Log.d(TAG, "OnStop: mPositionWhenPaused = " + mPositionWhenPaused);
        Log.d(TAG, "OnStop: getDuration  = " + mVideoView.getDuration());

        super.onPause();
    }

    @Override
    public void onResume() {
        // Resume video player
        if (mPositionWhenPaused >= 0) {
            mVideoView.seekTo(mPositionWhenPaused);
            mPositionWhenPaused = -1;
        }
        super.onResume();
    }


}
