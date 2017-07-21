package com.hwx.safelock.safelock.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hwx.safelock.safelock.AppConfig;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.activity.SimpleFragment;
import com.hwx.safelock.safelock.util.LogUtils;
import com.hwx.safelock.safelock.weight.FullScreenWebView;

import butterknife.BindView;

/**
 * A placeholder fragment containing a simple view.
 */
public class VideoFragment extends SimpleFragment implements MediaPlayer.OnCompletionListener {
    public static final String TAG = "VideoPlayerFragment";
    @BindView(R.id.videoView)
    FullScreenWebView mVideoView;
    @BindView(R.id.video_lin)
    LinearLayout videoLin;
    private int mPositionWhenPaused = -1;
    private String videoUrl;

    public VideoFragment() {
    }

    public static VideoFragment newInstance(String videoUrl) {
        VideoFragment fragment = new VideoFragment();
        Bundle bundle = new Bundle();
        bundle.putString("videoUrl", videoUrl);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video;
    }

    @Override
    protected void initEventAndData() {
        mVideoView.setKeepScreenOn(true);
        videoUrl = getArguments().getString("videoUrl");
        mPositionWhenPaused = AppConfig.getInstance().getInt("position_video", -1);
        if (TextUtils.isEmpty(videoUrl)) {
            Toast.makeText(getActivity(), "视频文件异常", Toast.LENGTH_SHORT).show();
            return;
        }
        //mMediaController = new MediaController(getContext());
        //VideoView与MediaController进行关联
        mVideoView.setVideoPath(videoUrl);
        //mVideoView.setMediaController(mMediaController);
        // mMediaController.setMediaPlayer(mVideoView);
        //让VideiView获取焦点
        mVideoView.requestFocus();
        mVideoView.start();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
//            mp.setOnVideoSizeChangedListener((mp1, width, height) -> {
////FixMe 获取视频资源的宽度
//                int mVideoWidth = mp1.getVideoWidth();
////FixMe 获取视频资源的高度
//                int mVideoHeight = mp1.getVideoHeight();
////FixMe 获取屏幕的宽度
//                DisplayMetrics display = getResources().getDisplayMetrics();
////FixMe 在资源尺寸可以播放观看时处理
//                if (mVideoHeight > 0 && mVideoWidth > 0) {
////FixMe 拉伸比例
//                    float scale = (float) mVideoWidth / (float) mVideoHeight;
////FixMe 视频资源拉伸至屏幕宽度，横屏竖屏需结合传感器等特殊处理
//                    mVideoWidth = display.widthPixels;
////FixMe 拉伸VideoView高度
//                    mVideoHeight = (int) (mVideoWidth / scale);//FixMe 设置surfaceview画布大小
//                    mVideoView.getHolder().setFixedSize(mVideoWidth, mVideoHeight);
////FixMe 重绘VideoView大小，这个方法是在重写VideoView时对外抛出方法
//                    //mVideoView.setMeasure(mVideoWidth, mVideoHeight);
////FixMe 请求调整
//                    mVideoView.requestLayout();
//                }
//            });
                mp.start();
                mp.setLooping(true);
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                mPositionWhenPaused=0;
                return true;
            }
        });
        mVideoView.setOnCompletionListener(this);
        //mVideoView.setOnErrorListener(this);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        videoLin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }


    //Video播完的时候得到通知
    @Override
    public void onCompletion(final MediaPlayer mp) {
        LogUtils.e("播放完成，开始重播");
        mVideoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mp.release();
                    mVideoView.setVideoPath(videoUrl);
                    // mMediaController.destroyDrawingCache();
                    mVideoView.destroyDrawingCache();
                    mVideoView.start();
                    mVideoView.requestLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 5000);

    }

    //暂停
    @Override
    public void onPause() {
        try {
            // Stop video when the activity is pause.
            mPositionWhenPaused = mVideoView.getCurrentPosition();
            AppConfig.getInstance().putInt("position_video", mPositionWhenPaused);
            if (mVideoView.isPlaying())
                mVideoView.stopPlayback();
            Log.d(TAG, "OnStop: mPositionWhenPaused = " + mPositionWhenPaused);
            Log.d(TAG, "OnStop: getDuration  = " + mVideoView.getDuration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        try {
            // Resume video player
            if (mPositionWhenPaused >= 0) {
                mVideoView.seekTo(mPositionWhenPaused);
                mPositionWhenPaused = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }


    @Override
    public void onDestroyView() {
        mVideoView = null;
        //mMediaController=null;
        super.onDestroyView();
    }

}
