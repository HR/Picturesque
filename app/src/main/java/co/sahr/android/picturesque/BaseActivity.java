/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import co.sahr.android.picturesque.data.models.ItemList;
import co.sahr.android.picturesque.data.models.WallItem;
import co.sahr.android.picturesque.data.models.WallList;
import co.sahr.android.picturesque.fragments.OnWallListClickListener;
import co.sahr.android.picturesque.fragments.WallFragment;
import co.sahr.android.picturesque.fragments.headless.NetFragment;
import co.sahr.android.picturesque.fragments.headless.RewardVideoAdFragment;
import co.sahr.android.picturesque.fragments.main.SortDialogFragment;
import co.sahr.android.picturesque.fragments.main.UnlockWallDialogFragment;
import co.sahr.android.picturesque.utilities.Utils;
import co.sahr.android.picturesque.utilities.billing.IabHelper;
import co.sahr.android.picturesque.utilities.billing.IabResult;
import co.sahr.android.picturesque.utilities.billing.Purchase;
import co.sahr.android.picturesque.utilities.logger;

public abstract class BaseActivity extends AppCompatActivity implements OnWallListClickListener,
        UnlockWallDialogFragment.UnlockWallDialogListener, RewardVideoAdFragment
                .RewardVideoAdListener, SortDialogFragment.SortDialogListener {
    // Fragment tags
    public static final String TAG_RVAD_FRAGMENT = "RewardVideoAdFragment";
    public static final String TAG_NET_FRAGMENT = "NetFragment";

    private static final String SORT_DIALOG_TAG = "sort_by";

    // Premium purchase request code
    private static final int PREMIUM_PURCHASE_REQUEST_CODE = 21;

    // Temporarily holds the current user clicked premium wall due to be unlocked
    private WallItem mPendingWall;
    // Reward video ad load progress dialog
    protected ProgressDialog mProgressDialog;
    // Headless frags
    protected RewardVideoAdFragment mRewardVideoAdFragment;
    protected NetFragment mNetFragment;
    // Access application class
    protected App mApp;
    // Preferences
    protected SharedPreferences mPreferences;

    /* Logging */
    protected logger logger = new logger(this);


    /**
     * Subclass overrides
     ******************************/

    /**
     * Get activity's menu reference
     */
    protected abstract int getMenuId();

    /**
     * Get current visible wall list's key in {@link NetFragment#listHashMap}
     */
    protected abstract int getCurrentWallListKey();

    /**
     * Called when Pro purchase status changes
     */
    protected abstract void onProChange();


    /**
     * Activity lifecycle callbacks
     ******************************/

    /**
     * Activity menu
     * Get reference to menu so the actions can be changed dynamically during runtime
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the subclass activity-specific menu this adds items to the action bar if it is
        // present.
        getMenuInflater().inflate(getMenuId(), menu);
        return true;
    }

    /**
     * Perform menu item action
     * Called when a menu item is clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_sort:
                // Get current sort from list after its lookup
                String sortOrder = mNetFragment.getWallList(getCurrentWallListKey())
                                               .getLoadingState()
                                               .getSortOrder();
                logger.v("Sort menu item, current list's loadingMethod: " + sortOrder);
                // Create & show the sort walls by dialog
                SortDialogFragment sortDialog = SortDialogFragment.newInstance(sortOrder);
                sortDialog.show(getSupportFragmentManager(), SORT_DIALOG_TAG);
                return true;
            case R.id.action_settings:
                // Open settings activity
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                // let Android handle the menu click
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sort dialog click callback
     *
     * @param sortOrder the selected sort order
     */
    @Override
    public void onSortDialogClick(final String sortOrder) {
        logger.v("Sort dialog, selected sortOrder: " + sortOrder);
        WallList wallList = mNetFragment.getWallList(getCurrentWallListKey());
        // Build from current loading state to retain attrs e.g. isPremium
        ItemList.StateChange newState = wallList.getLoadingState();
        // Set new sort order, swap the list and reset the next page token
        newState.setSortOrder(sortOrder)
                .setListChange(ItemList.LIST_SWAP)
                .setNextPageToken(null);
        wallList.setState(newState);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get globals
        mApp = (App) getApplication();
        // Get shared preferences
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // find the retained headless fragments on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        mNetFragment = (NetFragment) fm.findFragmentByTag(TAG_NET_FRAGMENT);

        // create the fragment the first time
        if (mNetFragment == null) {
            // Create & add the fragment
            mNetFragment = new NetFragment();
            Utils.addHeadlessFrag(this, mNetFragment, TAG_NET_FRAGMENT);
        }

        // If pro, do not setup reward video
        if (mApp.isPro()) {
            return;
        }

        ensureRewardVideoFragExists();

        // Init reward video ad load progress dialog
        mProgressDialog = Utils.uncancelableProgressDialog(this, R.string.message_loading);
        // Check if reward ad video was requested and is loading
        if (mRewardVideoAdFragment.isLoading()) {
            // Restore loading dialog
            mProgressDialog.show();
        }

    }

    /**
     * Get or create RewardVideoFrag
     */
    public void ensureRewardVideoFragExists() {
        mRewardVideoAdFragment = (RewardVideoAdFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_RVAD_FRAGMENT);

        // create the fragment the first time
        if (mRewardVideoAdFragment == null) {
            // Create & add the fragment
            mRewardVideoAdFragment = new RewardVideoAdFragment();
            Utils.addHeadlessFrag(this, mRewardVideoAdFragment, TAG_RVAD_FRAGMENT);
        }
    }

    /**
     * When the user clicks a wall from the Wall list in {@link WallFragment}
     * Opens the {@link WallActivity} with the clicked wall item details
     * Interface implementation for wall fragments (incl. extended LikedFragment)
     */
    @Override
    public void onWallListClick(final WallItem wall) {
        // Show unlock wall dialog if wall is premium and user is not
//        if (wall.getIsPremium()) {
//            // Wall is premium
//            if (mApp.isPro()) {
//                // User has purchased subscription
//                Utils.openWallActivity(this, wall);
//            } else {
//                // User has NOT purchased subscription
//
//                // Set pending wall
//                mPendingWall = wall;
//                // Show locked wall dialog with pending (to be opened) wall
//                DialogFragment unlockWallDialog = new UnlockWallDialogFragment();
//                unlockWallDialog.show(getSupportFragmentManager(), Utils.UNLOCK_WALL_DIALOG_TAG);
//            }
//        } else {
            Utils.openWallActivity(this, wall);
//        }
    }

    /**
     * When the user clicks a unlock wall dialog Go Pro button
     * Initiates pro purchase
     */
    @Override
    public void onUnlockWallGoProClick() {
        IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper
                .OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                // Check if purchase was successful or already owned
                if (result.isSuccess()) {
                    // Set App pro ref to true
                    mApp.setPro(true);
                    // Save purchase state in preferences
                    mPreferences.edit()
                                .putBoolean(getString(R.string.prm), mApp.isPro())
                                .commit();

                    logger.i("IaB, purchase successful: " + result);
                    // Alert subclass to update UI
                    onProChange();
                    // give user access to premium content and update the UI
                    Utils.openWallActivity(BaseActivity.this, mPendingWall);
                } else if (result.isCancel()) {
                    logger.i("IaB, purchase was cancelled by user " + result);
                } else {
                    logger.e("IaB, purchase error: " + result);
                    Bundle bundle = new Bundle();
                    bundle.putString("message", result.getMessage());
                    bundle.putInt("response", result.getResponse());
                }
            }
        };
        // Developer payload can use to send supplemental info about an order (can be empty string)
        // If you specify a string value, Google Play returns this string along with the purchase
        // response. Subsequently, when you make queries about this purchase, Google Play returns
        // this string together with the purchase details.
        String developerPayload = "";

        // Start purchase flow
        try {
            mApp.getIaBHelper()
                .launchPurchaseFlow(BaseActivity.this, Consts.SKU,
                        PREMIUM_PURCHASE_REQUEST_CODE, mPurchaseFinishedListener, developerPayload);
        } catch (Exception e) {
            logger.e(e);
            Toast.makeText(BaseActivity.this, R.string.message_error_inappbilling, Toast
                    .LENGTH_LONG)
                 .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mApp.getIaBHelper() == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        // Pass on the activity result to the helper for handling
        if (!mApp.getIaBHelper()
                 .handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            logger.v("onActivityResult handled by IaB");
        }
    }

    /**
     * When the user clicks a unlock wall dialog Go Pro button
     * Initiates reward video
     */
    @Override
    public void onUnlockWallWatchAdClick() {
        // Edge case: IaB returns false (refund) then create it to prevent null exception
        if (mRewardVideoAdFragment == null) ensureRewardVideoFragExists();
        // Add reward video and ensure user views entire video for wall
        // Forward event to frag
        mRewardVideoAdFragment.onUnlockWallWatchAdClick(mPendingWall);
    }

    /**
     * RewardVideoAdFragment listener callbacks
     */
    @Override
    public void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public void showProgressDialog() {
        mProgressDialog.show();
    }

    @Override
    protected void onDestroy() {
        // Dismiss progress dialog if still showing
        if (mProgressDialog != null) mProgressDialog.dismiss();
        super.onDestroy();
    }
}
