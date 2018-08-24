/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.ui;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class WallSubsamplingScaleImageView extends SubsamplingScaleImageView {
    public WallSubsamplingScaleImageView(final Context context, final AttributeSet attr) {
        super(context, attr);
    }

    public WallSubsamplingScaleImageView(final Context context) {
        super(context);
    }

    /**
     * Override to change default image rotation behavior
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        PointF sCenter = getCenter();
        /**
         * Center is null for initial onSizeChanged call on image load
         * So do not reset and let super do its setup as resetting it will
         * use Point(0, 0) as centre and 1 as scale and mess everything it up!
         */
        if (sCenter != null) {
            // When image is rotated (i.e. device orientation changes),
            // reset image scale and center to original
            resetScaleAndCenter();
        }
    }
}
