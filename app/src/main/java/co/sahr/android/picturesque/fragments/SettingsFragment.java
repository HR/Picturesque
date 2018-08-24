/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;

import java.util.concurrent.Callable;

import co.sahr.android.picturesque.R;
import co.sahr.android.picturesque.utilities.IOUtils;
import co.sahr.android.picturesque.utilities.Utils;
import co.sahr.android.picturesque.utilities.logger;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class SettingsFragment extends PreferenceFragmentCompat {
    private String mAppVersion;
    private String mCacheSize;
    private Context mContext;
    private View mRootView;
    private logger logger = new logger(this);

    /**
     * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
     * Subclasses are expected to call {@link #setPreferenceScreen(PreferenceScreen)} either
     * directly or via helper methods such as {@link #addPreferencesFromResource(int)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     * @param rootKey            If non-null, this preference fragment should be rooted at the
     *                           {@link PreferenceScreen} with this key.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mContext = getContext();
        mAppVersion = Utils.getAppVersion(mContext);
        try {
        mCacheSize = IOUtils.getCacheSize(mContext);
        } catch (Exception e) {
            logger.e(e);
            mCacheSize = "unknown";
        }
        // Get root view for snackbar
        mRootView = getActivity().getWindow()
                                 .getDecorView()
                                 .findViewById(android.R.id.content);

        addPreferencesFromResource(R.xml.preferences);

        Preference prefV = this.findPreference(getString(R.string.pref_version_key));
        prefV.setSummary(mAppVersion);

        final Preference prefCache = this.findPreference(getString(R.string.pref_clear_cache_key));
        prefCache.setSummary(getString(R.string.description_pref_clear_cache, mCacheSize));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        super.onPreferenceTreeClick(preference);
        final String KEY = preference.getKey();
        if (KEY == getString(R.string.pref_clear_cache_key)) {
            Single.fromCallable(new Callable<Boolean>() {
                /**
                 * Computes a result, or throws an exception if unable to do so.
                 *
                 * @return computed result
                 * @throws Exception if unable to compute a result
                 */
                @Override
                public Boolean call() throws Exception {
                    return IOUtils.clearCache(mContext);
                }
            })
                  .subscribeOn(Schedulers.io()) // Run on io thread pool
                  .observeOn(AndroidSchedulers.mainThread()) // Observe on main thread
                  .subscribe(new DisposableSingleObserver<Boolean>() {
                      @Override
                      public void onSuccess(@NonNull final Boolean aBoolean) {
                          // Clear cache successful
                          logger.v("Cache successfully cleared!");
                          // Reset cache size
                          mCacheSize = getString(R.string.pref_clear_cache_reset_size);
                          // Update the UI to show cache has been cleared
                          preference.setSummary(getString(R.string.description_pref_clear_cache,
                                  mCacheSize));
                          // Show success message
                          Utils.showSnackbar(mRootView, R.string.message_pref_cache_cleared);
                          // dispose of observer
                          dispose();
                      }

                      @Override
                      public void onError(@NonNull final Throwable e) {
                          // Clear cache unsuccessful
                          logger.v("Cache clearing unsuccessful!");
                          logger.e(e);
                          // dispose of observer
                          dispose();
                      }
                  });
            return true;
        } else if (KEY == getString(R.string.pref_app_share_key)) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string
                    .app_share_data), getString(R.string.app_share_link)));
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_share_subject));
            shareIntent.setType("text/plain");
            Utils.resolveShareIntent(getContext(), mRootView, shareIntent, R.string
                    .title_dialog_share);
            return true;
        }

        // Let sys handle this (i.e. exec from pref xml)
        return false;
    }

}
