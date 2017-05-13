package com.hwx.safelock.safelock.util;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.hwx.safelock.safelock.AppConfig;
import com.hwx.safelock.safelock.Application;
import com.hwx.safelock.safelock.Constants;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.activity.BaseMainActivity;
import com.ldoublem.loadingviewlib.view.base.LVBase;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/9/12.
 */
public class HttpUtilS {
    public static MediaType JSON=MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");

    public static String server="http://47.92.6.33:8080/LockerSys/";
    //public static String server="http://122.193.26.170:8888/LockerSys/";
    /**
     * 根据数据获取请求链接
     * @param api
     * @return
     */
    public static String getHttpRequestUrl(String api){
        //return "http://122.193.26.170:8888/LockerSys/"+api;//http://47.92.6.33:8080/LockerSys/
        return server+api;
    }
    public static void refreshApplication(final Context context, final String itemPath, final LVBase lvGhost, boolean isFirst) {
        JSONObject object = new JSONObject();
        try {
            object.put("deviceId", AppConfig.getInstance().getString("deviceId", ""));
            if (isFirst)
                object.put("version",Application.versionCodeNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpUtilS.postJson(object.toString(), "getResource", new InterFaceUtil.OnHttpInterFace() {
            @Override
            public void onSuccess(String str) {
                if (TextUtils.isEmpty(str))
                    return;
                try {
                    JSONObject obj = new JSONObject(str);
                    String apkUrl=server+obj.getString("apkUrl");
                    final int apkVersion=obj.getInt("apkVersion");
                    final String videoUrl=server+obj.getString("videoUrl");
                    if (apkVersion!=Application.versionCodeNumber&&apkVersion>0){
                        OkHttpUtils.get().url(apkUrl).build().execute(new FileCallBack(itemPath, "newVersion" + apkVersion + ".apk") {
                            @Override
                            public void inProgress(float progress, long total, int id) {
                                LogUtils.e("apkUrl当前下载进度："+(int) (100 * progress));
                                new Handler(context.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (lvGhost.getVisibility() == View.GONE) {
                                            lvGhost.setVisibility(View.VISIBLE);
                                            lvGhost.startAnim(13000);
                                        }
                                    }
                                });
                                super.inProgress(progress, total, id);
                            }
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                new Handler(context.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        lvGhost.stopAnim();
                                        lvGhost.setVisibility(View.GONE);
                                        Toast.makeText(context,"下载升级软件出错！请检查服务器连接和主板网络连接！",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(File response, int id) {
                                //处理版本升级
                                final String downFileStr=response.getAbsolutePath();
                                LogUtils.e("apkUrl,文件下载成功,开始打开安装辅助程序");
                                boolean isInstall = FileUtil.isAppInstalled(context, "com.hwx.camera_doul.install_tool");
                                if (isInstall) {
                                    ApkController.uninstall("com.hwx.camera_doul.install_tool", context);//先卸载
                                }
                                try {
                                    InputStream is = context.getAssets().open("install.apk");
                                    FileUtil.copyFile(is,new File(itemPath, "install.apk"));
                                    new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            ApkController.install(itemPath + "/install.apk", Application.getContext());//静默安装
                                        }
                                    },25000);
                                    new Handler(context.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.hwx.camera_doul.install_tool");
                                            if (intent != null) {
                                                // 这里跟Activity传递参数一样的嘛，不要担心怎么传递参数，还有接收参数也是跟Activity和Activity传参数一样
                                                intent.putExtra("path", downFileStr);
                                                intent.putExtra("version", apkVersion);
                                                intent.putExtra("packageTag", "com.hwx.safelock.safelock");
                                                context.startActivity(intent);
                                            } else {
                                                Toast.makeText(context, "未安装辅助安装程序\n哟，赶紧下载安装这个APP吧", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    },55000);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    String tag=ACache.get(new File(itemPath+"/url")).getAsString("videoUrlTag");
                    if (!/*AppConfig.getInstance().getString("videoUrlTag","")*/ (TextUtils.isEmpty(tag)?"":tag).equals(videoUrl)) {
                        OkHttpUtils.get().url(videoUrl).build().execute(new FileCallBack(itemPath, "newVideo" + apkVersion + ".mp4") {
                            @Override
                            public void inProgress(float progress, long total, int id) {
                                LogUtils.e("videoUrl当前下载进度："+(int) (100 * progress));
                                new Handler(context.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (lvGhost.getVisibility() == View.GONE) {
                                            lvGhost.setVisibility(View.VISIBLE);
                                            lvGhost.startAnim(13000);
                                        }
                                    }
                                });
                                super.inProgress(progress, total, id);
                            }

                            @Override
                            public void onError(Call call, Exception e, int id) {
                                new Handler(context.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        lvGhost.stopAnim();
                                        lvGhost.setVisibility(View.GONE);
                                        Toast.makeText(context,"下载更新视频出错！请检查服务器、主板的通信是否正常！",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(File response, int id) {
                                new Handler(context.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        lvGhost.stopAnim();
                                        lvGhost.setVisibility(View.GONE);
                                    }
                                });
                                ACache aCache= ACache.get(new File(itemPath+"/url"));
                                aCache.put("videoUrlTag",videoUrl);
                                aCache.put("videoUrl",response.getAbsolutePath());
                                //AppConfig.getInstance().putString("videoUrlTag",videoUrl);
                                //AppConfig.getInstance().putString("videoUrl",response.getAbsolutePath());
                                Application.sendLocalBroadCast(Constants.VIDEO_DO_INIT);
                                BaseMainActivity.videoUrl=response.getAbsolutePath();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFail() {
            }
        });
    }


    //get请求
    public static void getAsynHttp(String url, final InterFaceUtil.OnHttpInterFace todo) {
        //OkHttpClient mOkHttpClient=new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url(url);
        //可以省略，默认是GET请求
        //requestBuilder.method("GET",null);
        Request request = requestBuilder.build();
        Call mcall= Application.getmOkHttpClient().newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (todo!=null)
                    todo.onFail();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response.cacheResponse()) {
                    String str = response.cacheResponse().toString();
                    Log.i("wangshu", "cache---" + str);
                    if (todo != null) {
                        if (response.code() == 200) {
                            todo.onSuccess(str);
                        } else if (response.code() == 500) {
                            todo.onFail();
                        }
                    }
                } else {
                    response.body().string();
                    String str = response.networkResponse().toString();
                    Log.i("wangshu", "network---" + str);
                    if (todo != null) {
                        if (response.code() == 200) {
                            todo.onSuccess(str);
                        } else if (response.code() == 500) {
                            todo.onFail();
                        }
                    }
                }
            }
        });
    }

    public static String postExecuteJson(String json,String url) {
        if (TextUtils.isEmpty(AppConfig.getInstance().getString("deviceId",""))){
            new Handler(Application.getContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Application.getContext(), "请先进入后台管理设置设备ID", Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }
        RequestBody formBody = new FormBody.Builder()
                .add("json",AES.jiami(json) )
                .build();
        Request request = new Request.Builder().url(url).post(formBody).build();
        try {
            Response response = Application.getmOkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                String str=response.body().string();
                LogUtils.e(str);
                JSONObject object=new JSONObject(str);
                if (object.getInt("status")==200) {
                    return object.get("data").toString();
                }else if (object.getInt("status")==500) {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void postJson(String json,final String api, final InterFaceUtil.OnHttpInterFace callback) {

        if (TextUtils.isEmpty(AppConfig.getInstance().getString("deviceId",""))){
            new Handler(Application.getContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Application.getContext(), R.string.dfdas, Toast.LENGTH_SHORT).show();
                }
            });
            if (callback!=null)
                callback.onFail();
            return;
        }
        RequestBody body = new FormBody.Builder()
                .add("json",AES.jiami(json) )
                .build();
        //RequestBody body = RequestBody.create(JSON, AES.jiami(json));
        Request request = new Request.Builder()
                .url(getHttpRequestUrl(api))
                .post(body)
                .build();
        Application.getmOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    try {
                        callback.onFail();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        try {
                            String str=AES.jiemi(response.body().string());
                            LogUtils.e("api---"+api+"-----"+str);
                            JSONObject object=new JSONObject(str);
                            if (object.getInt("status")==200) {
                                if (callback != null)
                                    callback.onSuccess(object.get("data").toString());
                            }else {
                                if (callback != null)
                                    callback.onFail();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (callback != null)
                            callback.onFail();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //post请求
    public static void postAsynHttpforjson(String url, String json, final InterFaceUtil.OnHttpInterFace face) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = Application.getmOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (face!=null)
                    face.onFail();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();
                Log.i("wangshu", str);
            }
        });
    }
    //上传完文件
    private void postAsynFile() {
        //OkHttpClient mOkHttpClient=new OkHttpClient();
        File file = new File("/sdcard/wangshu.txt");
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
                .build();
		/*try {
			//同步上传
			mOkHttpClient.newCall(request).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
        Application.getmOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("wangshu",response.body().string());
            }
        });
    }
    //异步下载文件
    public static void downAsynFile(String url, final File path, final InterFaceUtil.OnHttpInterFace face) {
        if (TextUtils.isEmpty(Constants.Deviceid)){
            new Handler(Application.getContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Application.getContext(), "请先进入后台管理设置设备ID和密码", Toast.LENGTH_SHORT).show();
                }
            });
            if (face!=null)
                face.onFail();
            return;
        }
        //OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Application.getmOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (face!=null)
                    face.onFail();
            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(path);
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Log.i("wangshu", "IOException");
                    e.printStackTrace();
                }
                if (face!=null)
                    face.onSuccess("");
                Log.d("wangshu", "文件下载成功");
            }
        });
    }

    /**
     * 同步下载
     * @return
     */
    public static boolean downExecuteFile(String url, final File path) {
        if (TextUtils.isEmpty(Constants.Deviceid)){
            new Handler(Application.getContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(Application.getContext(), "请先进入后台管理设置设备ID和密码", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
        //OkHttpClient mOkHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response=Application.getmOkHttpClient().newCall(request).execute();
            if (response!=null){
                InputStream inputStream = response.body().byteStream();
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(path);
                    byte[] buffer = new byte[2048];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Log.i("wangshu", "IOException");
                    e.printStackTrace();
                }
                Log.d("wangshu", "文件下载成功");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    //异步上传多文件
    private void sendMultipart(){
        //OkHttpClient mOkHttpClient=new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", "wangshu")
                .addFormDataPart("image", "wangshu.jpg",
                        RequestBody.create(MEDIA_TYPE_PNG, new File("/sdcard/wangshu.jpg")))
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + "...")
                .url("https://api.imgur.com/3/image")
                .post(requestBody)
                .build();

        Application.getmOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("wangshu", response.body().string());
            }
        });
    }
}
