package com.hwx.camera_doul.install_tool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by Administrator on 2016/9/12.
 */
public class HttpUtil {
    public static MediaType JSON=MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");


    //下载APK文件
    public static void downAPK(final Context context, String apkUrl){
       String itemPath=getSDPath()+"/居一格/";
        String TAG="apk_down";
        try {
            InputStream is = context.getAssets().open("test.apk");
            FileUtil.copyFile(is,new File(itemPath, "test.apk"));
            ApkController.install(itemPath+"test.apk", Application.getContext());//静默安装
            new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    //60秒后开始打开软件
                    LogUtils.e("30秒开始打开软件");
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.hwx.safelock.safelock");
                    // 这里如果intent为空，就说名没有安装要跳转的应用嘛
                    if (intent != null) {
                        // 这里跟Activity传递参数一样的嘛，不要担心怎么传递参数，还有接收参数也是跟Activity和Activity传参数一样
                        intent.putExtra("tag", "talk you as new version");
                        context.startActivity(intent);
                    } else {
                        // 没有安装要跳转的app应用，提醒一下
                        Toast.makeText(context, "哟，赶紧下载安装这个APP吧", Toast.LENGTH_LONG).show();
                    }
                }
            },30000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*OkHttpUtils//
                .get()//
                .url(apkUrl)//
                .build()//
                .execute(new FileCallBack(*//*Environment.getExternalStorageDirectory().getAbsolutePath()*//*itemPath, "newapk.apk")//
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(File response, int id) {
                        ApkController.install(response.getAbsolutePath(), Application.getContext());//静默安装
                        new Handler(context.getMainLooper()).postDelayed(() -> {
                            //60秒后开始打开软件
                            LogUtils.e("60秒开始打开软件");
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.putExtra("tag","talk you as new version");
                            ComponentName cn = new ComponentName("com.hwx.safelock.safelock.activity", "MainActivity");
                            intent.setComponent(cn);
                            context.startActivity(intent);
                        },60000);
                    }
                });*/
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        return sdDir.toString();
    }


    public static File getFilePath(String filePath,
                                   String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
    }
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {

        }
    }


}
