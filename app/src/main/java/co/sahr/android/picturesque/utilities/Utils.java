/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.utilities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.List;

import co.sahr.android.picturesque.Consts;
import co.sahr.android.picturesque.R;
import co.sahr.android.picturesque.WallActivity;
import co.sahr.android.picturesque.data.models.WallItem;

public final class Utils {
    public static final int SNACKBAR_LENGTH_LONG = 3500; // 3.5 seconds
    public static final int SNACKBAR_SHORT_DELAY = 2000; // 2 seconds
    public static final int SNACKBAR_INDEFINITE = Snackbar.LENGTH_INDEFINITE;
    // Required tag name for dialog fragment show method
    public static final String UNLOCK_WALL_DIALOG_TAG = "unlock_wall";

    /**
     * Adds the Headless Worker Fragment to the activity
     */
    public static void addHeadlessFrag(AppCompatActivity activity, Fragment frag, String tag) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(frag, tag)
                .commit();
    }

    public static void setupToolbar(AppCompatActivity activity, String title) {
        Utils.setupToolbar(activity, title, true);
    }

    public static void setupToolbar(AppCompatActivity activity, String title, boolean back) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

        activity.setSupportActionBar(toolbar);
        // Setup action bar
        ActionBar actionBar = activity.getSupportActionBar();

        // Set the action bar back button to look like an up button
        actionBar.setDisplayHomeAsUpEnabled(back);

        if (title == null) {
            return;
        }

        if (title.equals("")) {
            // disable title
            actionBar.setDisplayShowTitleEnabled(false);
            return;
        }

        // Set toolbar title to the wallpaper name
        actionBar.setTitle(title);
    }

    public static void resolveShareIntent(Context context, View view, Intent shareIntent, int
            title) {
        resolveShareIntent(context, view, shareIntent, context.getString(title));
    }

    public static void resolveShareIntent(Context context, View view, Intent shareIntent, String
            title) {
        // Verify the intent will resolve to at least one activity
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(shareIntent, title));
        } else {
            // Let user know it is unhandled
            Utils.showSnackbar(view, R.string.message_intent_unresolved);
        }
    }

    public static void openWallActivity(Context context, WallItem wall) {
        Intent openWallActivityIntent = new Intent(context, WallActivity.class);
        // Pass the clicked wall onto the Wall Activity
        openWallActivityIntent.putExtra(Consts.WALL_ITEM_EXTRA, wall);
        // Open it in wall activity
        context.startActivity(openWallActivityIntent);
    }

    // Get current app version
    public static String getAppVersion(Context context) {
        String version = "<3";
        try {
            PackageInfo info = context.getPackageManager()
                                      .getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            logger.e(e);
        }
        return version;
    }

    // Open the app settings in system settings
    public static void openAppSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static int getSizeDP(Context context, int size) {
        float scale = context.getResources()
                             .getDisplayMetrics().density;
        return (int) (size / scale);
    }

    public static int getDisplayWidthDP(Context context) {
        DisplayMetrics metrics = context.getResources()
                                        .getDisplayMetrics();
        int width = 0;
        width = (int) (metrics.widthPixels / metrics.density);
        return width;
    }

    // Verify there is an activity available that can respond to the intent
    public static boolean isIntentSafe(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List activities = packageManager.queryIntentActivities(intent, PackageManager
                .MATCH_DEFAULT_ONLY);
        return (activities.size() > 0);
    }

    // Check if connected to internet
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static ProgressDialog uncancelableProgressDialog(Context context, int m) {
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.AppTheme_dialog_alert);
        progressDialog.setMessage(context.getString(m));
        // Make indeterminate
        progressDialog.setIndeterminate(true);
        // Disable dismiss by tapping outside of the dialog
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    /**
     * Calculates the minimum inSampleSize value of the image for the display
     * To reduce file size
     *
     * @param sHeight source image height
     * @param sWidth  source image width
     */
    public static int calculateInSampleSize(AppCompatActivity activity, int sHeight, int sWidth) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        // Get display dimensions
        final int vHeight = displayMetrics.heightPixels;
        final int vWidth = displayMetrics.widthPixels;

        int inSampleSize = 1;

        if (sHeight > vHeight || sWidth > vWidth) {

            final int halfHeight = Math.round(sHeight / 2);
            final int halfWidth = Math.round(sWidth / 2);

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // image height and width larger than the display height and width.
            while ((halfHeight / inSampleSize) >= vHeight && (halfWidth / inSampleSize) >= vWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    /**
     * Snackbar builders
     * Uses md_grey_900 as background color by default
     *
     * @param view
     * @param m
     */

    public static Snackbar showSnackbar(View view, int m) {
        return showSnackbar(view, m, Snackbar.LENGTH_LONG);
    }

    public static Snackbar showSnackbar(View view, int m, int length) {
        return showSnackbar(view, m, length, R.color.md_grey_900);
    }

    public static Snackbar showSnackbar(View view, int m, int length, int color) {
        Snackbar mySnackbar = Snackbar.make(view, m, length);
        mySnackbar.getView()
                  .setBackgroundColor(ContextCompat.getColor(view.getContext(), color));
        mySnackbar.setDuration(length);
        mySnackbar.show();
        return mySnackbar;
    }

    public static void showSnackbarWithAction(View view, int mString, int actionString, View
            .OnClickListener listner) {
        Snackbar mySnackbar = Snackbar.make(view, mString, Snackbar.LENGTH_LONG);
        mySnackbar.setAction(actionString, listner);
        mySnackbar.show();
    }

    /**
     * Gets the headless fragment
     * Creates and adds it to the activity if it does not exist
     *
     * @param activity        the activity that holds the fragment
     * @param instance        an instance of the fragment to get to be
     * @param tag             the tag used to add it
     * @param containerViewId id of container view
     * @param add             whether to add
     * @param <T>
     * @return
     */
    private <T extends Fragment> T getFrag(AppCompatActivity activity, T instance, String tag,
                                           int containerViewId, boolean add) {
        FragmentManager fm = activity.getSupportFragmentManager();

        T frag = (T) fm.findFragmentByTag(tag);

        // create the fragment the first time
        if (frag == null) {
            logger.v("getFrag", "Frag does NOT exist, creating: " + tag);
            // Create & add the fragment
            frag = instance;

            if (add) {
                // Add frag to controls
                fm.beginTransaction()
                  .add(containerViewId, frag, tag)
                  .commit();
            }
        } else {
            // Already exists
            logger.v("getFrag", "Frag EXISTS, nulling: " + tag);
            // Allow it to be remove by gc
            instance = null;
        }

        return frag;
    }

    /**
     * Resolves whether to show an ad or not
     *
     * @return ad id string or null
     */
    public static String resolveAd(boolean isPro, String adId) {
        return isPro ? null : adId;
    }
}
