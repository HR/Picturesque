/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.headless;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import co.sahr.android.picturesque.data.models.WallItem;
import co.sahr.android.picturesque.utilities.Utils;
import co.sahr.android.picturesque.utilities.logger;

public class RewardVideoAdFragment extends Fragment implements RewardedVideoAdListener {
    private RewardVideoAdListener mListener;
    private WallItem mPendingWall;
    private RewardedVideoAd mRewardedVideoAd;
    // Flag for if reward ad video was requested and is loading
    private boolean mIsLoading = false;
    private logger logger = new logger(this);

    public void setPendingWall(final WallItem wall) {
        this.mPendingWall = wall;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public void logEvent(String name) {
        Bundle bundle = new Bundle();
        if (mPendingWall != null) {
            bundle.putLong("wall_id", mPendingWall.getId());
            bundle.putLong("wall_tag_id",  mPendingWall.getTagId());
        }
    }

    public RewardVideoAdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);

        // Init Reward video ad
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getContext());
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        // Request rewarded video ad to preload ad video
        loadRewardedVideoAd();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RewardVideoAdListener) {
            mListener = (RewardVideoAdListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement " +
                    "RewardVideoAdListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Create an ad request and load reward ad video
     */
    private void loadRewardedVideoAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mRewardedVideoAd.loadAd(null, adRequest);
    }

    /**
     * When the user clicks a unlock wall dialog Go Pro button
     * Initiates reward video
     */
    public void onUnlockWallWatchAdClick(WallItem pendingWall) {
        // Set pending wall
        this.mPendingWall = pendingWall;

        if (mRewardedVideoAd.isLoaded()) {
            logger.v("onUnlockWallWatchAdClick wall loaded -> show");
            // Loaded, just show it immediately
            mRewardedVideoAd.show();
        } else {
            logger.v("onUnlockWallWatchAdClick wall NOT loaded -> loading dialog");
            // Ad requested and loading
            mIsLoading = true;
            // Not loaded, wait until loaded and show progress dialog in the interim
            mListener.showProgressDialog();

            logEvent("reward_video_ad_loading");
        }
    }

    /**
     * Reward video ad callbacks
     */

    @Override
    public void onRewardedVideoAdLoaded() {
        logger.v("onRewardedVideoAdLoaded");
        logEvent("load_reward_video_ad");
        // Check if a wall loading
        if (mIsLoading) {
            // Open premium wall is pending
            // Dismiss dialog
            mListener.dismissProgressDialog();
            // Show when loaded
            mRewardedVideoAd.show();
            // Reset flag since ad has been shown
            mIsLoading = false;
        }
    }

    @Override
    public void onRewardedVideoAdOpened() {
        logger.v("onRewardedVideoAdOpened");
        logEvent("reward_video_ad_open");
    }

    @Override
    public void onRewardedVideoStarted() {
        logger.v("onRewardedVideoStarted");
        logEvent("reward_video_ad_start");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        logger.v("onRewardedVideoAdClosed");
        logEvent("reward_video_ad_cancel");
        // User cancelled reward video ad
        // Start new reward video ad preload
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(final RewardItem rewardItem) {
        logger.v(String.format("onRewarded, currency: %s, amount: %d", rewardItem
                .getType(), rewardItem.getAmount()));
        logEvent("reward_video_ad_reward");
        // Open premium wall
        Utils.openWallActivity(getContext(), mPendingWall);
        // Start new reward video ad preload
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        logger.v("onRewardedVideoAdLeftApplication");
        logEvent("reward_video_ad_gone");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(final int errorCode) {
        logger.v("onRewardedVideoAdFailedToLoad");
        logEvent("reward_video_ad_load_fail");
        // Retry loading
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoCompleted() {

    }

    @Override
    public void onResume() {
        // Resume ad
        mRewardedVideoAd.resume(getContext());
        logEvent("reward_video_ad_resume");
        super.onResume();
    }

    @Override
    public void onPause() {
        // Pause ad
        mRewardedVideoAd.pause(getContext());
        logEvent("reward_video_ad_pause");
        super.onPause();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        // Destroy reward ad
        mRewardedVideoAd.destroy(getContext());
        logEvent("reward_video_ad_destroy");
        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface RewardVideoAdListener {
        void dismissProgressDialog();

        void showProgressDialog();
    }
}
