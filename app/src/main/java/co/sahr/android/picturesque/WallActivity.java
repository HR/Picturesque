/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;

import co.sahr.android.picturesque.data.WallListSingleton;
import co.sahr.android.picturesque.data.models.WallItem;
import co.sahr.android.picturesque.data.models.WallList;
import co.sahr.android.picturesque.data.remote.Api;
import co.sahr.android.picturesque.data.remote.ApiClient;
import co.sahr.android.picturesque.fragments.wall.ControlsFragment;
import co.sahr.android.picturesque.fragments.wall.InfoFragment;
import co.sahr.android.picturesque.fragments.wall.SetWallControlFragment;
import co.sahr.android.picturesque.fragments.wall.SetWallDialogFragment;
import co.sahr.android.picturesque.ui.WallProgressIndicator;
import co.sahr.android.picturesque.utilities.DBUtils;
import co.sahr.android.picturesque.utilities.IOUtils;
import co.sahr.android.picturesque.utilities.Utils;
import co.sahr.android.picturesque.utilities.logger;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static co.sahr.android.picturesque.utilities.IOUtils.joinPaths;
import static co.sahr.android.picturesque.utilities.Utils.SNACKBAR_SHORT_DELAY;
import static co.sahr.android.picturesque.utilities.Utils.openAppSettings;
import static co.sahr.android.picturesque.utilities.Utils.showSnackbarWithAction;


public class WallActivity extends AppCompatActivity implements SetWallControlFragment
        .OnSetWallListener, SetWallDialogFragment.SetWallDialogListener {
    // Request code for the WRITE_EXTERNAL_STORAGE permission request
    private static final int WRITE_EXT_STORAGE_PERMISSION_REQUEST_CODE = 510;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private static final int DEFAULT_SCALE_TYPE = SubsamplingScaleImageView
            .SCALE_TYPE_CENTER_INSIDE;
    private static final int SET_WALL_MODE_SCALE_TYPE = SubsamplingScaleImageView
            .SCALE_TYPE_CENTER_CROP;
    // Required tag name for dialog fragment show method
    private static final String SET_WALL_DIALOG_TAG = "set_wall_category";
    private static final String TAG_CONTROLS_FRAGMENT = "controls_fragment";
    private static final String TAG_INFO_FRAGMENT = "info_fragment";
    // Flag to keep track of UI visibility state, visible by default (i.e. not fullscreen)
    private boolean mVisible = true;
    // Flag to enable and disable fullscreen mode
    private boolean mInSetWallMode = false;

    // Current wall object
    private WallItem mWall;

    private ViewGroup mControlsLayout;
    private View mTopVignetteView;
    private SubsamplingScaleImageView mWallSSIV;
    private BigImageView mWallView;

    // Fragments
    private InfoFragment mInfoFragment;
    private ControlsFragment mControlsFragment;
    private SetWallControlFragment mSetWallControlFragment;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mWallView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View
                    .SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View
                    .SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsContainerLayout.setVisibility(View.VISIBLE);
            mTopVignetteView.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to prevent the hide.
     * This is to prevent the jarring behavior of controls going away while interacting with
     * activity UI.
     */
    private final View.OnTouchListener mPreventHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            /**
             * Returning true will tells the event bubbling that the event was consumed therefore
             * preventing the event from bubbling to other views i.e. {@link mWallView}
             */
            return true;
        }
    };
    // Liked wall list singleton
    private WallList mLikedWallList = WallListSingleton.getInstance()
                                                       .getLikedWallList();
    private NativeExpressAdView mAdView;
    private ViewGroup mContainerLayout;
    private ViewGroup mControlsContainerLayout;

    private logger logger = new logger(this);

    private App mApp;
    private Api mService;

    /**
     * Toggle hide or show UI based on UI visibility
     */
    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    /**
     * Hide UI & enter fullscreen
     */
    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsContainerLayout.setVisibility(View.GONE);
        mTopVignetteView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Show UI & exit fullscreen
     */
    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mWallView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                .SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Request external storage permissions
     * creates sys dialog
     * Result passed to  {@link #onRequestPermissionsResult}
     */
    public void requestExternalStoragePermission() {
        ActivityCompat.requestPermissions(WallActivity.this, new String[]{Manifest.permission
                .WRITE_EXTERNAL_STORAGE}, WRITE_EXT_STORAGE_PERMISSION_REQUEST_CODE);
    }

    /**
     * Add fragment to the controls layout
     *
     * @param fragment
     */
    public void addFragToControls(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                                   .add(R.id.fullscreen_content_controls, fragment, tag)
                                   .commit();
    }

    /**
     * Replace all controls fragments (i.e. all frags in R.id.fullscreen_content_controls) with
     *
     * @param fragment
     */
    public void replaceControlsFragsWith(Fragment fragment, boolean shouldAddToBackStack) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (shouldAddToBackStack) {
            // Add to back stack so can navigate back to original mode
            transaction.addToBackStack(null);
        }

        transaction.replace(R.id.fullscreen_content_controls, fragment)
                   .commit();
    }

    /**
     * Remove fragment from the current layout
     *
     * @param fragment
     */
    public void removeFrag(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                                   .remove(fragment)
                                   .commit();
    }

    public void showSnackbar(final int stringId) {
        Utils.showSnackbar(mControlsContainerLayout, stringId);
    }

    private void setupAdView() {
        mAdView = new NativeExpressAdView(WallActivity.this);
        mAdView.setAdUnitId(null);
        mAdView.setAdSize(new AdSize(AdSize.FULL_WIDTH, Consts.WALL_ACTIVITY_NATIVE_AD_HEIGHT));

        // Ad view
        mControlsContainerLayout.addView(mAdView);

        // Load Ad
        mAdView.loadAd(new AdRequest.Builder().build());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);

        /* Verify valid launch */
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            // If Wall not passed in, end the activity
            finish();
            return;
        }


        /* Get globals */
        mApp = (App) getApplication();

        // Init retrofit api service with API key to call with every request
        mService = ApiClient.createService();

        /* Setup WallItem */
        // Get passed WallItem object from MainActivity
        mWall = (WallItem) extras.getSerializable(Consts.WALL_ITEM_EXTRA);

        /**
         * Look for the passed in mWall record in the db (i.e.
         * singleton) since the WallList is from the server. If the WallList is in db it is
         * liked, so just use it.
         */

        // Look passed wall up in like wall list
        WallItem wallItem = mLikedWallList.getById(mWall.getId());
        // Check if it exists in the liked wall list
        if (wallItem != null) {
            // Is liked
            logger.v("In Liked Walls. Using cached");
            // Set wall to cached wall
            mWall = wallItem;
        } else {
            // Is not liked
            logger.v("NOT in Liked Walls");
        }


        /* Setup toolbar */
        Utils.setupToolbar(this, mWall.getName());


        /* Get the layout & view references */
        mControlsLayout = (ViewGroup) findViewById(R.id.fullscreen_content_controls);
        mControlsContainerLayout = (ViewGroup) findViewById(R.id
                .fullscreen_content_controls_container);
        mWallView = (BigImageView) findViewById(R.id.fullscreen_content);
        mTopVignetteView = findViewById(R.id.v_top_vignette);
        mContainerLayout = (ViewGroup) findViewById(R.id.fullscreen_content_container);

        // Get the SubsamplingScaleImageView
        mWallSSIV = mWallView.getSSIV();


        /* Wall view setup */

        mWallView.setFailureImage(AppCompatResources.getDrawable(WallActivity.this, R.drawable
                .app_wall_fail));

        // Make sure system UI is transparent from the beginning
        mWallView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                .SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Listen to when image has been loaded or error has occurred
        mWallView.setImageLoaderCallback(new ImageLoader.Callback() {
            @Override
            public void onCacheHit(final File image) {}

            @Override
            public void onCacheMiss(final File image) {}

            @Override
            public void onStart() {}

            @Override
            public void onProgress(final int progress) {}

            @Override
            public void onFinish() {}

            @Override
            public void onSuccess(final File image) {
                logger.v("onSuccess, Loaded image");
                // TODO: 03/08/2017 Consider using {@link Utils#getFrag}
                /**
                 * These fragments have their states retained
                 * Hence, find & use frag if retained otherwise create a new one
                 */

                FragmentManager fm = getSupportFragmentManager();

                // Add controls fragment with initial liked state once image has loaded
                mControlsFragment = (ControlsFragment) fm.findFragmentByTag(TAG_CONTROLS_FRAGMENT);

                // create the fragment the first time
                if (mControlsFragment == null) {
                    // Create & add the fragment
                    mControlsFragment = ControlsFragment.newInstance(mWall.getIsLiked());
                    // Add frag to controls
                    addFragToControls(mControlsFragment, TAG_CONTROLS_FRAGMENT);
                }

                // Restore info fragment if it exists
                mInfoFragment = (InfoFragment) fm.findFragmentByTag(TAG_INFO_FRAGMENT);

                // create the fragment the first time
                if (mInfoFragment == null) {
                    // Create the fragment
                    mInfoFragment = mInfoFragment.newInstance(mWall);
                }
            }

            @Override
            public void onFail(final Exception error) {
                logger.e("onFail, Failed to load image");
                // Show fail message
                showSnackbar(R.string.message_wall_load_fail);
            }
        });


        // Set the native progress bar
        mWallView.setProgressIndicator(new WallProgressIndicator());

        // Set the image uri to start fetching
        mWallView.showImage(Uri.parse(mWall.getImageUri()));


        mWallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Only toggle fullscreen if not in set wall mode
                if (!mInSetWallMode) {
                    toggle();
                }
            }
        });

        // Upon interacting with UI controls, prevent the hide
        mControlsLayout.setOnTouchListener(mPreventHideTouchListener);


        /* Ad setup */
        if (!mApp.isPro()) {
            // If not pro, setup the ad view to show ad
//             setupAdView();
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wall, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_report:
                String subject = getString(R.string.subject_wall_report);
                String[] tos = new String[]{getString(R.string.app_support_email)};

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_EMAIL, tos);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                shareIntent.setType("text/plain");
                Utils.resolveShareIntent(WallActivity.this, mControlsContainerLayout,
                        shareIntent, R.string.title_wall_report);
                return true;
            case android.R.id.home:
                // Go back to the previous activity
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }

        if (mInSetWallMode) {
            // Exit set wall mode
            exitSetWallMode();
        }
    }

    private void exitSetWallMode() {
        if (mControlsFragment == null) return;

        // Enable fullscreen
        mInSetWallMode = false;

        // Make image center cropped
        mWallSSIV.setMinimumScaleType(DEFAULT_SCALE_TYPE);
        mWallSSIV.resetScaleAndCenter();

        // Let addToBackStack replace set wall frag with previous one (ControlsFrag)
    }


    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[]
            grantResults) {
        switch (requestCode) {
            case WRITE_EXT_STORAGE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {

                    // permission was granted
                    logger.v("onRequestPermissionsResult, PERMISSION GRANTED");
                    // Save wallpaper to External
                    saveWallToExtStorage();

                } else {

                    // permission denied
                    logger.v("onRequestPermissionsResult, PERMISSION NOT GRANTED");

                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale
                            (WallActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (showRationale) {
                        // Show permission explanation
                        showSnackbar(R.string.message_permission_storage);
                    } else {
                        // user also CHECKED "never ask again"
                        logger.v("onRequestPermissionsResult, Checked never ask again");
                        // Direct to the app setting
                        showSnackbarWithAction(mControlsContainerLayout, R.string
                                .message_permission_storage, R.string.action_button_sys_settings,
                                new View.OnClickListener() {
                            // Called when settings action has been clicked
                            @Override
                            public void onClick(final View v) {
                                // Open app settings to allow for permission to be granted
                                openAppSettings(WallActivity.this);
                            }
                        });
                    }
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Save wallpaper to the external storage
     * Copies wall to /Pictures/Picturesque/{Wall name}
     */
    private void saveWallToExtStorage() {
        // TODO: Check for sufficient free space
        // https://developer.android.com/training/basics/data-storage/files.html#GetFreeSpace
        Single<Integer> saveWallSingle = Single.create(new SingleOnSubscribe<Integer>() {
            /**
             * Called for each SingleObserver that subscribes.
             *
             * @param emitter the safe emitter instance, never null
             * @throws Exception on error
             */
            @Override
            public void subscribe(@NonNull final SingleEmitter<Integer> emitter) throws Exception {
                final File wallFile = mWallView.getCurrentImageFile();

                // Save dir is /Pictures/Picturesque/{Wall name}
                String wallSaveName = mWall.getName() + IOUtils.getFileExtension(wallFile);
                String saveFileRelPath = joinPaths(Environment.DIRECTORY_PICTURES, getString(R
                        .string.app_name), wallSaveName);
                File saveFile = Environment.getExternalStoragePublicDirectory(saveFileRelPath);

                // TODO: 23/06/2017 Convert WEBP to JPEG
                IOUtils.copyFile(wallFile, saveFile);
                // Paths for MediaScanner to scan
                String[] saveFilePathArr = {saveFile.getAbsolutePath()};
                // Scan file once copied so it shows up in gallery immediately
                MediaScannerConnection.scanFile(getBaseContext(), saveFilePathArr, null, new
                        MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        logger.v("Scanned: " + path + ", uri: " + uri);
                    }
                });

                emitter.onSuccess(R.string.message_wall_save_successful);
            }
        });
        // TODO: 03/08/2017 Consider doing this in headless frag
        saveWallSingle.subscribeOn(Schedulers.io()) // Run saveWallSingle in io thread pool
                      .observeOn(AndroidSchedulers.mainThread()) // Observe on main thread
                      .subscribe(new DisposableSingleObserver<Integer>() {
                          @Override
                          public void onSuccess(@NonNull final Integer successMessage) {
                              // Increment the remote download count
                              updateWallDownloads();
                              // Show success message
                              showSnackbar(successMessage);
                              // Dispose of observer
                              dispose();
                          }

                          @Override
                          public void onError(@NonNull final Throwable e) {
                              logger.e(e);
                              // Show
                              showSnackbar(R.string.message_wall_save_unsuccessful);
                              // Dispose of observer
                              dispose();
                          }
                      });

    }

    /**
     * Save Wallpaper to Gallery
     * (Click handler)
     */
    public void saveWall(View view) {
        // Check if the image has loaded yet
        if (mWallView.getCurrentImageFile() == null) {
            logger.w("saveWall, Wallpaper image has not been loaded yet");
            return;
        }

        // Check external storage is available
        if (!IOUtils.isExternalStorageWritable()) {
            logger.w("saveWall, External storage is unavailable (not writable)!");
            showSnackbar(R.string.message_external_media_unavailable);
            return;

        }

        logger.v("saveWall, External storage is available (writable)");


        // Check permissions
        if (IOUtils.isExternalStorageGranted(WallActivity.this)) {
            // Save wallpaper to External
            saveWallToExtStorage();
        } else {
            // Request permission

            // Check if explanation needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(WallActivity.this, Manifest
                    .permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Utils.showSnackbar(mControlsContainerLayout, R.string.message_permission_storage,
                        SNACKBAR_SHORT_DELAY);

                logger.v("saveWall, Show request permission rationale");

                final int DELAY = SNACKBAR_SHORT_DELAY + 300; // 0.3s after snackbar

                mWallView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Request the permission after DELAY
                        requestExternalStoragePermission();
                    }
                }, DELAY);

            } else {
                logger.v("saveWall, No permission rationale needed");

                // No explanation needed, we can request the permission
                requestExternalStoragePermission();
            }
        }

    }

    /**
     * Set wallpaper for wallpaper category if supported on device
     *
     * @param setWallCategory
     */
    public void setWallpaper(final int setWallCategory) {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance
                (getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!wallpaperManager.isWallpaperSupported() || !wallpaperManager
                    .isSetWallpaperAllowed()) {
                // Setting wall is not available
                showSnackbar(R.string.message_wall_set_unavailable);
                return;
            }
        }

        if (mWallView.getCurrentImageFile() == null) {
            logger.w("setWall, wall image has not been loaded yet");
            return;
        }

        logger.v("setWall, " + String.format("Height: %d, Width: %d", mWallView.getHeight(),
                mWallView.getWidth()));

        // TODO: 07/07/2017 Just do in background and show snackbar? Maybe start selfStop service?
        // Create progress dialog to show while decoding & setting wall
        final ProgressDialog progressDialog = Utils.uncancelableProgressDialog(WallActivity.this,
                R.string.message_dialog_set_wall);
        progressDialog.show();

        Single<Integer> wallSetSingle = Single.create(new SingleOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<Integer> emitter) throws Exception {
                // Get visible region source coordinates
                PointF leftTopCoord = mWallSSIV.viewToSourceCoord(new PointF(0, 0));
                PointF rightBottomCoord = mWallSSIV.viewToSourceCoord(new PointF(mWallView
                        .getWidth(), mWallView.getHeight()));

                // Create visible region rectangle from source coordinates
                RectF visibleRectF = new RectF(leftTopCoord.x, leftTopCoord.y, rightBottomCoord
                        .x, rightBottomCoord.y);
                final Rect visibleRect = new Rect();
                // Convert RectF to Rect (as required by BitmapRegionDecoder) by round all values
                visibleRectF.round(visibleRect);

                logger.v("setWall, Visible region, bottom: " + visibleRect.bottom + ", left: "
                        + visibleRect.left + ", top: " + visibleRect.top + ", right: " +
                        visibleRect.right);

                // Allow the decoder to keep a shallow reference to the input (image)
                boolean isShareable = true;
                InputStream wallInputStream = new FileInputStream(mWallView.getCurrentImageFile());
                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(wallInputStream,
                        isShareable);

                BitmapFactory.Options options = new BitmapFactory.Options();
                // Subsample the original image to reduce size
                options.inSampleSize = Utils.calculateInSampleSize(WallActivity.this, mWallSSIV
                        .getSHeight(), mWallSSIV.getSWidth());

                logger.v("calculated inSampleSize: " + options.inSampleSize);
                // Decode the visible region of the wallpaper to a Bitmap
                Bitmap visibleWallBitmap = decoder.decodeRegion(visibleRect, options);

                if (visibleWallBitmap != null) {
                    // Decoded successfully
                    logger.v("BitmapRegionDecoder, decoding successful, size: " +
                            visibleWallBitmap.getHeight() + "x" + visibleWallBitmap.getWidth());

                    if (setWallCategory > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        boolean allowWallBackup = true;
                        // Set wallpaper for
                        wallpaperManager.setBitmap(visibleWallBitmap, null, allowWallBackup,
                                setWallCategory);
                    } else {
                        // Set wallpaper for both lock & home screen
                        wallpaperManager.setBitmap(visibleWallBitmap);
                    }

                } else {
                    emitter.onError(new Exception("visibleWallBitmap is null! Decoding " +
                            "unsuccessful"));
                }
                // Call onSuccess when no error thrown
                emitter.onSuccess(R.string.message_wall_set_successful);
            }
        });

        wallSetSingle.subscribeOn(Schedulers.computation()) // Run wallSetSingle in background
                     .observeOn(AndroidSchedulers.mainThread()) // DisposableSingleObserver on main
                     .subscribe(new DisposableSingleObserver<Integer>() {
                         @Override
                         public void onSuccess(@NonNull final Integer successMessage) {
                             progressDialog.dismiss();
                             // Go back and exit set wall mode to show result snackbar
                             onBackPressed();
                             // Increment the remote download count
                             updateWallDownloads();
                             // Show success message
                             showSnackbar(successMessage);
                             // Dispose of observer
                             dispose();
                         }

                         @Override
                         public void onError(@NonNull final Throwable e) {
                             progressDialog.dismiss();
                             // Go back and exit set wall mode to show result snackbar
                             onBackPressed();
                             showSnackbar(R.string.message_wall_set_unsuccessful);
                             // Dispose of observer
                             dispose();
                         }
                     });
    }

    /**
     * SetWallDialogFragment's callback method
     * Called when wallpaper category in dialog is clicked
     */
    @Override
    public void onSetWallDialogClick(final int setWallCategory) {
        // Set wall the user specified wall category
        setWallpaper(setWallCategory);
    }

    /**
     * SetWallControlFragment's callback method
     * Called when set wallpaper is clicked
     */
    @Override
    public void onSetWall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Show the set wall category dialog for >= Android N devices
            SetWallDialogFragment setWallDialog = new SetWallDialogFragment();
            setWallDialog.show(getSupportFragmentManager(), SET_WALL_DIALOG_TAG);
        } else {
            // Otherwise just set both lock & home
            setWallpaper(0);
        }
    }

    /**
     * Center source image (wall) in the view
     */
    public void centerWall() {
        // Source image center
        PointF sCenter = new PointF(mWallSSIV.getSWidth() / 2, mWallSSIV.getSHeight() / 2);
        mWallSSIV.setScaleAndCenter(mWallSSIV.getScale(), sCenter);
    }

    /**
     * Set Wallpaper as User's Wallpaper
     * (Click handler)
     */
    public void setWall(View view) {
        /**
         * SET_WALLPAPER normal permission automatically granted so no need to request it
         * manually (users cannot revoke normal permissions) https://goo.gl/YNqyCD
         */

        /**
         * Enter set wall mode
         */

        // Disable fullscreen
        mInSetWallMode = true;

        // Make image center cropped
        mWallSSIV.setMinimumScaleType(SET_WALL_MODE_SCALE_TYPE);

        // Center wallpaper image
        centerWall();

        // Show controls if not visible
        if (!mVisible) {
            show();
        }

        if (mSetWallControlFragment == null) {
            // Create if not exist already
            mSetWallControlFragment = new SetWallControlFragment();
        }

        // Only show the set wall control frag in controls
        replaceControlsFragsWith(mSetWallControlFragment, true);
    }

    /**
     * Like or Unlike Wallpaper
     * (Click handler)
     */
    public void toggleWallLike(View view) {
        // Toggle wall like
        final boolean likeToggle = !mWall.getIsLiked();

        // Update wall item state
        mWall.setIsLiked(likeToggle);

        // Optimistically update the UI
        mControlsFragment.setLikeIcon(likeToggle);

        // Update wall item in the db
        Single.fromCallable(new Callable<Boolean>() {
            /**
             * Computes a result, or throws an exception if unable to do so.
             *
             * @return computed result
             * @throws Exception if unable to compute a result
             */
            @Override
            public Boolean call() throws Exception {
                return DBUtils.insertOrUpdateLike(mApp.getDbHelper()
                                                      .getWritableDatabase(), mWall);
            }
        })
              .subscribeOn(Schedulers.io()) // Run on io thread pool
              .observeOn(AndroidSchedulers.mainThread()) // Observe on main thread
              .subscribe(new DisposableSingleObserver<Boolean>() {
                  @Override
                  public void onSuccess(@NonNull final Boolean aBoolean) {
                      // Insert/update successful
                      // Update singleton
                      if (likeToggle) {
                          // Liked wall, add to the wall list
                          mLikedWallList.add(mWall);
                      } else {
                          // Unliked wall, remove the wall from list
                          mLikedWallList.remove(mWall);
                      }
                      // dispose of observer
                      dispose();
                  }

                  @Override
                  public void onError(@NonNull final Throwable e) {
                      // Insert/update unsuccessful
                      // dispose of observer
                      dispose();
                  }
              });
    }

    /**
     * Hide or show info fragment
     */
    public void toggleInfoFragment() {
        // Check if frag not added
        if (!mInfoFragment.isAdded()) {
            // Add the fragment to activity
            addFragToControls(mInfoFragment, TAG_INFO_FRAGMENT);
        } else {
            // Remove the fragment from activity
            removeFrag(mInfoFragment);
        }
    }

    /**
     * Show or Hide Wallpaper Info
     * (Click handler)
     */
    public void toggleWallInfo(View view) {
        mControlsFragment.toggleInfoIcon();
        toggleInfoFragment();
    }

    /**
     * Make an API request to increment the remote download count
     */
    public void updateWallDownloads() {
        Call<WallItem> call = mService.updateWallDownloads(String.valueOf(mWall.getId()));
        call.enqueue(new Callback<WallItem>() {
            @Override
            public void onResponse(final Call<WallItem> call, final Response<WallItem> response) {
                logger.v("updateWallDownloads, SUCCESSFULLY incremented download count for " +
                        mWall.getId());
            }

            @Override
            public void onFailure(final Call<WallItem> call, final Throwable t) {
                logger.v("updateWallDownloads, FAILED to incremented download count for " +
                        mWall.getId());
                t.printStackTrace();
            }
        });
    }
}
