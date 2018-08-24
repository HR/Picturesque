/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.fresco.FrescoImageLoader;
import com.google.android.gms.ads.MobileAds;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import co.sahr.android.picturesque.data.WallDbHelper;
import co.sahr.android.picturesque.utilities.billing.IabHelper;
import co.sahr.android.picturesque.utilities.billing.IabResult;
import co.sahr.android.picturesque.utilities.billing.Inventory;
import co.sahr.android.picturesque.utilities.logger;


// TODO: 05/09/2017 ONLY STORE STUFF THAT IS OKAY TO BE RE-INIT AND LOST
public class App extends Application {
    private logger logger = new logger(this);

    // Db helper
    private WallDbHelper mDbHelper;
    // In-app billing helper
    private IabHelper mIaBHelper;
    // whether user is pro or not, not by default
    private boolean mIsPro = false;
    // Prefs
    private SharedPreferences mPreferences;

    public boolean isPro() {
        return mIsPro;
    }

    public void setPro(final boolean mIsPro) {
        this.mIsPro = mIsPro;
    }

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Use compat for vector drawable lookups app-wide
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        // Initialize AdMob with APP ID
        MobileAds.initialize(this, null);
        // Initialize BIV & Fresco
        BigImageViewer.initialize(FrescoImageLoader.with(getApplicationContext()));
        // Setup db
        mDbHelper = new WallDbHelper(getApplicationContext());

        // Get shared preferences
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Restore purchase state (false, if inaccessible)
        mIsPro = mPreferences.getBoolean(getString(R.string.prm), mIsPro);
        logger.i("Pref purchased? " + mIsPro);

        try {
            // Init in-app billing helper
            mIaBHelper = new IabHelper(getApplicationContext());
            if (BuildConfig.DEBUG) {
                mIaBHelper.enableDebugLogging(true);
            }
            // Bind to the Google Play In-app Billing service
            mIaBHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        // Oh no, there was a problem.
                        logger.e("IaB setup failed: " + result);
                    } else {
                        logger.i("IaB setup successful: " + result);
                        // Check if Pro user or not
                        resolvePro();
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            logger.e(e);
        }
    }

    /**
     * Checks if user is Pro and saves it in {@link this#mIsPro}
     */
    private void resolvePro() {
        // List of SKUs to query
        List skuQueryList = new ArrayList();
        skuQueryList.add(Consts.SKU);

        // (update it on purchase)
        IabHelper.QueryInventoryFinishedListener getInventoryListener = new IabHelper
                .QueryInventoryFinishedListener() {
            /**
             * Called to notify that an inventory query operation completed.
             *
             * @param result The result of the operation.
             * @param inv    The inventory.
             */
            @Override
            public void onQueryInventoryFinished(final IabResult result, final Inventory inv) {
                if (result.isFailure()) {
                    // handle error here
                    logger.e("IaB query failed: " + result.toString());
                } else {
                    // Check if the user has the pro version
                    boolean isPro = inv.hasPurchase(Consts.SKU);

                    logger.i("IaB purchased? " + isPro);

                    if (mIsPro != isPro) {
                        // Purchase state has changed
                        logger.v("Purchased state has changed to: " + isPro);
                        // Update pro state
                        mIsPro = isPro;
                        // Update preferences
                        mPreferences.edit()
                                    .putBoolean(getString(R.string.prm), isPro)
                                    .commit();

                        // TODO: 08/08/2017 Restart app or find a way to let MainActivity know
                    }
                }
            }
        };

        try {
            mIaBHelper.queryInventoryAsync(true, skuQueryList, null, getInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            logger.e(e);
        }
    }

    /* Getters */

    public WallDbHelper getDbHelper() {
        return mDbHelper;
    }

    public IabHelper getIaBHelper() {
        return mIaBHelper;
    }

    /**
     * This method is for use in emulated process environments.  It will
     * never be called on a production Android device, where processes are
     * removed by simply killing them; no user code (including this callback)
     * is executed when doing so.
     */
    @Override
    public void onTerminate() {
        // Unbind from Google Play In-app Billing service to prevent mem leak
        if (mIaBHelper != null) {
            mIaBHelper.disposeWhenFinished();
            mIaBHelper = null;
        }
        super.onTerminate();
    }
}
