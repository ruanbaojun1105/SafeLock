package com.hwx.safelock.safelock.util;

/**
 * Created by Administrator on 2016/9/5.
 */
public class InterFaceUtil {
    public interface OnHttpInterFace {
        void onSuccess(String str);
        void onFail();
    }

    public interface OnclickInterFace {
        void onClick(String str);
    }

    public interface OnclickInterFaceOver {
        void onClick();
    }

    public interface OnTimerInterFace {
        void onClick(int len);
    }
    public interface OnTimerInterFaceBool {
        void onClick(boolean at);
    }
}
