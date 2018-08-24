/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import co.sahr.android.picturesque.R;
import co.sahr.android.picturesque.data.models.WallItem;

public class WallViewHolder extends RecyclerView.ViewHolder {
    public final SimpleDraweeView mThumbDraweeView;
    public final RelativeLayout mLabelLayout;
    public WallItem mItem;

    public WallViewHolder(View view) {
        super(view);
        mLabelLayout = (RelativeLayout) view.findViewById(R.id.wall_label);
        mThumbDraweeView = (SimpleDraweeView) view.findViewById(R.id.iv_wall_thumb);
    }
}
