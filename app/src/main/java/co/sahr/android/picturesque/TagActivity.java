/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import co.sahr.android.picturesque.data.models.TagItem;
import co.sahr.android.picturesque.fragments.tag.TagWallFragment;

import static co.sahr.android.picturesque.utilities.Utils.resolveAd;
import static co.sahr.android.picturesque.utilities.Utils.setupToolbar;

public class TagActivity extends BaseActivity {
    // Key used to store the wall list in the listHashMap
    private static final Integer WALL_LIST_KEY = 23;

    private TagItem mTag;
    private TagWallFragment mTagFragment;

    /**
     * Activity menu reference
     */
    @Override
    public int getMenuId() {
        return R.menu.menu_tag;
    }

    /**
     * Get current visible wall list's key in
     * {@link co.sahr.android.picturesque.fragments.headless.NetFragment#listHashMap}
     */
    @Override
    protected int getCurrentWallListKey() {
        return WALL_LIST_KEY;
    }

    /**
     * Called when Pro version was purchased successfully
     */
    @Override
    protected void onProChange() {
        // Re-setup the tag (wall) fragment
        setupTagFragment();
    }

    public void setupTagFragment() {
        // Get passed WallList from MainActivity
        mTagFragment = TagWallFragment.newInstance(mNetFragment.getWallList(WALL_LIST_KEY), R
                .layout.fragment_load_error, resolveAd(mApp.isPro(),null), mTag.getId());


        // Add the tag fragment to the activity
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.ll_wall_list_container, mTagFragment)
                                   .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Verify valid launch */
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            // If required extras not passed in, end the activity
            finish();
            return;
        }

        // Setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        /* Get extras */

        // Get passed TagItem from MainActivity
        mTag = (TagItem) extras.getSerializable(Consts.TAG_ITEM_EXTRA);

        // Setup toolbar
        setupToolbar(this, mTag.getName());
        // Create and add the tag (wall) fragment
        setupTagFragment();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                // Open settings activity
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                // Go back to the previous activity
                finish();
                return true;
            default:
                // let Android handle the menu click
                return super.onOptionsItemSelected(item);
        }
    }
}
