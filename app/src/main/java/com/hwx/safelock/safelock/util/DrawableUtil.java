package com.hwx.safelock.safelock.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hwx.safelock.safelock.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DrawableUtil {

    public static void displayImage(Context context, ImageView imageView, String url) {
        Glide.with(context)
                .load(url)
                .placeholder(R.mipmap.ic_launcher)
                .crossFade()
                .into(imageView);
    }
    public static void displayImage(Context context, ImageView imageView, int res) {
        Glide.with(context)
                .load(res)
                .placeholder(R.mipmap.ic_launcher)
                .crossFade()
                .into(imageView);
    }
    public static boolean isMobileNO(String mobiles) {
        if (TextUtils.isEmpty(mobiles)) {
            return false;
        }
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(14[6-9])|(17[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }
    /**
     * 把drawable转成BITMAP
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
//		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
//				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
//				: Bitmap.Config.RGB_565);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

   public static Drawable getImgResourceId(Context context,String name){
       Drawable drawable =null;
       try {
           int resID=0;
           resID = context.getResources().getIdentifier(name, "drawable",context.getApplicationInfo().packageName);
           return context.getResources().getDrawable(resID);
       } catch (Exception e) {
           e.printStackTrace();
           return null;
       }
    }
    public static int getDrawableResourceId(Context context,String name){
        int resID = context.getResources().getIdentifier(name, "drawable", context.getApplicationInfo().packageName);
        //Drawable image = context.getResources().getDrawable(resID);
        return resID;
    }
    public static int getResourceId(Context context,String name,String type){
        int resID = context.getResources().getIdentifier(name, type, context.getApplicationInfo().packageName);
        //Drawable image = context.getResources().getDrawable(resID);
        return resID;
    }




}

