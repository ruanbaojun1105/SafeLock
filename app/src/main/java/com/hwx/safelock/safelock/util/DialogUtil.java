package com.hwx.safelock.safelock.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.bigkoo.svprogresshud.listener.OnDismissListener;
import com.hwx.safelock.safelock.AppConfig;
import com.hwx.safelock.safelock.R;

/**
 * Created by baojun on 2016/9/1.
 */

public class DialogUtil {
    /*private static DialogUtil params;
    public static DialogUtil getInstance() {
        if (params == null) {
            params = new DialogUtil();
        }
        return params;
    }*/
    public static void showErrorWithStatus(final SVProgressHUD mSVProgressHUD, final String str){
        if (mSVProgressHUD==null)
            return;
        mSVProgressHUD.getView().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mSVProgressHUD.showErrorWithStatus(str, SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public static void showInfoWithStatus(final SVProgressHUD mSVProgressHUD, final String str){
        if (mSVProgressHUD==null)
            return;
        mSVProgressHUD.getView().post(new Runnable() {
            @Override
            public void run() {
            try {
                mSVProgressHUD.showInfoWithStatus(str, SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }});
    }
    public static void showSVProgressHUD(final SVProgressHUD mSVProgressHUD, final String str, final InterFaceUtil.OnclickInterFaceOver listener){
        /*mSVProgressHUD.showInfoWithStatus("这是提示", SVProgressHUD.SVProgressHUDMaskType.None);
        mSVProgressHUD.showWithStatus("加载中...");
        mSVProgressHUD.showErrorWithStatus("不约，叔叔我们不约～", SVProgressHUD.SVProgressHUDMaskType.GradientCancel);*/
        if (mSVProgressHUD==null)
            return;
        mSVProgressHUD.getView().post(new Runnable() {
            @Override
            public void run() {
                try {
                    mSVProgressHUD.showSuccessWithStatus(str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        if (listener==null)
            return;
        mSVProgressHUD.getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.onClick();
            }
        }, 1100);
    }
    public static void showInfoDialog(final Activity activity,String text,final InterFaceUtil.OnTimerInterFaceBool onclickInterFace){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        //dialog.setCanceledOnTouchOutside(true);
        Display d = activity.getWindowManager().getDefaultDisplay(); // 为获取屏幕宽、高
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes(); // 获取对话框当前的参数值
        p.width =(int)( d.getWidth()*0.4);
        //p.height =(int)( d.getHeight()*0.6);
        dialog.getWindow().setAttributes(p);
        TextView tip_text= (TextView) dialog.findViewById(R.id.tip_text);
        tip_text.setText(text);
        dialog.findViewById(R.id.ok_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (activity != null)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onclickInterFace.onClick(true);
                        }
                    });
            }
        });
        dialog.findViewById(R.id.cancle_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (activity!=null)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() { onclickInterFace.onClick(false);}
                    });
            }
        });
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_UP) {//不响应按键抬起时的动作
                    final int KeyCode = event.getKeyCode();
                    final int kayvalue = KeyCode;
                    try {
                        if (KeyEvent.KEYCODE_ENTER == KeyCode || KeyCode == KeyEvent.KEYCODE_F1) {
                            dialogInterface.dismiss();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onclickInterFace.onClick(true);
                                }
                            });
                            return true;
                        } else if (KeyEvent.KEYCODE_BACK == KeyCode || KeyCode == KeyEvent.KEYCODE_F2 || KeyCode == KeyEvent.KEYCODE_ESCAPE) {
                            dialogInterface.dismiss();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onclickInterFace.onClick(false);
                                }
                            });
                            return true;
                        } else {
                            dialogInterface.dismiss();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onclickInterFace.onClick(false);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                }
                return false;
            }
        });
        dialog.show();
    }
    public static void showEditDialog(final Activity activity, String title, final String arg, final InterFaceUtil.OnclickInterFace onclickInterFace){
        final EditText et = new EditText(activity);
        //et.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(activity).setTitle(title)
                .setIcon(android.R.drawable.ic_menu_send)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (activity == null)
                            return;
                        final String input = et.getText().toString();
                        if (TextUtils.isEmpty(input))
                            return;
                        input.trim();
                        if (!TextUtils.isEmpty(arg))
                            AppConfig.getInstance().putString(arg, input);
                        if (onclickInterFace != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onclickInterFace.onClick(input);
                                }
                            });
                        }

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
    public static void showDialog(final Activity activity, String title, final InterFaceUtil.OnTimerInterFaceBool onclickInterFace) {

        new AlertDialog.Builder(activity).setTitle(title)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        dialog.dismiss();
                        final int KeyCode = event.getKeyCode();
                        final int kayvalue = KeyCode;
                        try {
                            if (KeyEvent.KEYCODE_ENTER == KeyCode) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onclickInterFace.onClick(true);
                                    }
                                });
                                return true;
                            }
                            if (KeyEvent.KEYCODE_BACK == KeyCode) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onclickInterFace.onClick(false);
                                    }
                                });
                                return true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (activity == null)
                                return;
                            if (onclickInterFace != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onclickInterFace.onClick(true);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (activity == null)
                                return;
                            if (onclickInterFace != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onclickInterFace.onClick(false);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

}
