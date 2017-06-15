package com.hwx.safelock.safelock.activity.broadcast;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.hwx.safelock.safelock.R;
import com.hwx.safelock.safelock.util.DrawableUtil;

import java.util.List;

/**
 *
 */
public class ItemClickAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public ItemClickAdapter(List<String> data) {
        super(R.layout.item, data);
    }


    @Override
    protected void convert(final BaseViewHolder helper, final String item) {
        helper.setText(R.id.text,item);
    }
}