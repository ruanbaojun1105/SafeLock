package com.hwx.safelock.safelock.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.util.DrawableUtil;

/**
 * Created by Administrator on 2016/12/10.
 */

public class TestActivity  extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_acativity);
        ImageView background = (ImageView) findViewById(R.id.background);
        DrawableUtil.displayImage(this,background,R.drawable.background);
    }
}
