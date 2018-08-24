/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.github.piasy.biv.indicator.ProgressIndicator;
import com.github.piasy.biv.view.BigImageView;

import co.sahr.android.picturesque.R;

ublic public class WallProgressIndicator implements ProgressIndicator {
    private ProgressBar mProgressIndicatorView;

    @Override
    public View getView(BigImageView parent) {
        mProgressIndicatorView = (ProgressBar) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.progressbar_default, parent, false);
        return mProgressIndicatorView;
    }

    @Override
    public void onStart() {
        // not interested
        mProgressIndicatorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProgress(int progress) {
        if (progress < 0 || progress > 100 || mProgressIndicatorView == null) {
            return;
        }
        mProgressIndicatorView.setProgress(progress);
    }

    @Override
    public void onFinish() {
        // not interested
        mProgressIndicatorView.setVisibility(View.GONE);
    }
}
