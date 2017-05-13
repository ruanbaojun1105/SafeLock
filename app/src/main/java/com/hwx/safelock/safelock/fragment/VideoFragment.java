package com.hwx.safelock.safelock.fragment;

/**
 * Created by Administrator on 2016/8/22.
 */

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.hwx.safelock.safelock.AppConfig;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.util.LogUtils;
import com.hwx.safelock.safelock.weight.FullScreenWebView;
import com.ldoublem.loadingviewlib.view.LVBlazeWood;

/**
 * A placeholder fragment containing a simple view.
 */
public class VideoFragment extends Fragment implements MediaPlayer.OnCompletionListener{
    public static final String TAG = "VideoPlayerFragment";
    private FullScreenWebView mVideoView;
    private Uri mUri;
    private int mPositionWhenPaused = -1;
    //private MediaController mMediaController;
    private ImageView detail_image;
    private String videoUrl;

    public VideoFragment() {
    }

    public static VideoFragment newInstance(String videoUrl) {
        VideoFragment fragment = new VideoFragment();
        Bundle bundle=new Bundle();
        bundle.putString("videoUrl",videoUrl);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_video, container, false);
        mVideoView = (FullScreenWebView) rootView.findViewById(R.id.videoView);
        mVideoView.setKeepScreenOn(true);
        videoUrl=getArguments().getString("videoUrl");
        if (TextUtils.isEmpty(videoUrl)){
            Toast.makeText(getActivity(), "视频文件异常", Toast.LENGTH_SHORT).show();
            return rootView;
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
        mVideoView.setOnCompletionListener(this);
        //mVideoView.setOnErrorListener(this);
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        rootView.findViewById(R.id.video_lin).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mPositionWhenPaused=AppConfig.getInstance().getInt("position_video",-1);
        return rootView;
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
        },5000);

    }


    //暂停
    @Override
    public void onPause() {
        try {
            // Stop video when the activity is pause.
            mPositionWhenPaused = mVideoView.getCurrentPosition();
            mVideoView.stopPlayback();
            AppConfig.getInstance().putInt("position_video",mPositionWhenPaused);
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
        mVideoView=null;
        //mMediaController=null;
        super.onDestroyView();
    }
}
