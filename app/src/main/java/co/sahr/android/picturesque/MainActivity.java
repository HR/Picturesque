/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import co.sahr.android.picturesque.data.WallListSingleton;
import co.sahr.android.picturesque.data.models.TagItem;
import co.sahr.android.picturesque.fragments.main.HomeFragment;
import co.sahr.android.picturesque.fragments.main.LikedFragment;
import co.sahr.android.picturesque.fragments.main.PremiumFragment;
import co.sahr.android.picturesque.fragments.main.SearchResultFragment;
import co.sahr.android.picturesque.fragments.main.TagFragment;
import co.sahr.android.picturesque.utilities.Utils;

import static co.sahr.android.picturesque.utilities.Utils.resolveAd;

public class MainActivity extends BaseActivity implements TagFragment.OnTagListClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    // Number of total tabs
    private static final int TAB_NUM = 4;

    /* Tab indexes */
    private static final int TAB_LIKED = 3;
    // & List map keys
    private static final int TAB_HOME = 0;
    private static final int TAB_PREMIUM = 1;
    private static final int TAB_TAGS = 2;
    private static final int TAB_SEARCH = 4;

    // Search Results Frag tag
    private static final String TAG_SEARCH_FRAGMENT = "SearchResultsFragment";


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    // Tabs
    private TabLayout mTabLayout;

    private SearchResultFragment mSearchResultFragment;

    private void setupSharedPreferences() {
        // Sets the default values from an mPreferences XML
        // readAgain = false (last arg) guarantees user pref won't be overwritten
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // Register pref change listener
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Sets up the tags and launch shortcuts
     *
     * @param tabLayout
     */

    private void setupTabs(final TabLayout tabLayout) {
        // Add tabs
        tabLayout.getTabAt(TAB_HOME)
                 .setIcon(R.drawable.ic_home);
        tabLayout.getTabAt(TAB_PREMIUM)
                 .setIcon(R.drawable.ic_premium);
        tabLayout.getTabAt(TAB_TAGS)
                 .setIcon(R.drawable.ic_tags);
        tabLayout.getTabAt(TAB_LIKED)
                 .setIcon(R.drawable.ic_liked);

        /* *
         * Launch shortcuts
         *
         * Select the appropriate tab for shortcut action
         * reportShortcutUsed to launcher apps to build a prediction model so they can promote
         * the shortcuts that are likely to be used at the moment.
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            String shortcut = getIntent().getAction();
            if (getString(R.string.action_go_premium).equals(shortcut)) {
                shortcutManager.reportShortcutUsed(shortcut);
                // Select premium tab
                tabLayout.getTabAt(TAB_PREMIUM)
                         .select();
            } else if (getString(R.string.action_go_tags).equals(shortcut)) {
                shortcutManager.reportShortcutUsed(shortcut);
                // Select premium tab
                tabLayout.getTabAt(TAB_TAGS)
                         .select();
            } else if (getString(R.string.action_go_liked).equals(shortcut)) {
                shortcutManager.reportShortcutUsed(shortcut);
                // Select premium tab
                tabLayout.getTabAt(TAB_LIKED)
                         .select();
            }
        }
    }

    public void setupPagers() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Setup with the pager adapter
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // Setup with the view pager
        mTabLayout.setupWithViewPager(mViewPager);
        // Setup the tabs
        setupTabs(mTabLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set to actual app theme once splash screen has gone
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup settings
        setupSharedPreferences();

        // Setup toolbar with no title and home as up disabled
        Utils.setupToolbar(this, getString(R.string.app_name), false);

        // Get refs
        mViewPager = (ViewPager) findViewById(R.id.container);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        // Restore search results frag if it exists (to prevent duplicates)
        mSearchResultFragment = (SearchResultFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_SEARCH_FRAGMENT);

        // Setup the pagers and tabs
        setupPagers();
    }

    /**
     * Activity menu reference
     */
    @Override
    protected int getMenuId() {
        return R.menu.menu_main;
    }

    /**
     * Get current visible wall list's key in
     * {@link co.sahr.android.picturesque.fragments.headless.NetFragment#listHashMap}
     */
    @Override
    protected int getCurrentWallListKey() {
        // Since tab position was the key for every wall list, return it
        return mTabLayout.getSelectedTabPosition();
    }

    /**
     * Called when Pro version was purchased successfully
     */
    @Override
    protected void onProChange() {
        // Force all fragments to be recreated (Ad-free)
        // Re-setup the pagers and tabs
        setupPagers();
    }

    /**
     * Activity menu
     * Get reference to menu so the actions can be changed dynamically during runtime
     *
     * @param menu
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // inflate menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // Make full width
        searchView.setMaxWidth(Integer.MAX_VALUE);
        // Add hint & other info
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String sanitizedQuery = query.trim();
                // Sanity check
                if (!TextUtils.isEmpty(sanitizedQuery)) {
                    // Start load
                    mSearchResultFragment.loadQuery(sanitizedQuery);
                }
                // has been handled here (no need for SearchResultsActivity)
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Update contents of the fragment
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat
                .OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // hide all menu items except search bar item
                setItemsVisibility(menu, searchItem, false);
                // Check if not already added
                if (mSearchResultFragment == null) {
                    mSearchResultFragment = SearchResultFragment.newInstance(mNetFragment
                            .getWallList(TAB_SEARCH), R.layout.fragment_search_not_found,
                            resolveAd(mApp.isPro(), null));
                }

                // Check if not added to activity yet
                if (!mSearchResultFragment.isAdded()) {
                    // Add search fragment on top of tabs (& view pager)
                    // Add to back stack so can navigate back to tabs
                    getSupportFragmentManager().beginTransaction()
                                               .add(R.id.content_container,
                                                       mSearchResultFragment, TAG_SEARCH_FRAGMENT)
                                               .addToBackStack(null)
                                               .commit();
                    // Complete transaction synchronously
                    getSupportFragmentManager().executePendingTransactions();
                }

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // show all menu items
                setItemsVisibility(menu, null, true);
                // Reset search
                mSearchResultFragment.reset();
                // Go back, reset back stack (esp. search results fragment)
                getSupportFragmentManager().popBackStack(null, FragmentManager
                        .POP_BACK_STACK_INCLUSIVE);
                return true;
            }
        });
        // display menu
        return true;
    }

    /**
     * Shows & hides all the menu items with the exception of one (i.e. search bar item)
     */
    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
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
            default:
                // let BasicActivity handle the menu item
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO: 15/06/2017 Implement notifications
        if (key.equals(getString(R.string.pref_wallofday_key))) {
            // wall of day notification changed
        }
    }


    /**
     * When the user clicks a tag from the Tag list in {@link TagFragment}
     * Opens the {@link TagActivity} with the tag's fetched walls
     * Interface implementation for TagFragments
     */
    @Override
    public void onTagListClick(final TagItem tagItem) {
        Intent openTagActivityIntent = new Intent(this, TagActivity.class);
        // Pass the tag item onto the Wall Activity
        openTagActivityIntent.putExtra(Consts.TAG_ITEM_EXTRA, tagItem);
        // Open it in tag activity
        startActivity(openTagActivityIntent);

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return the appropriate tab fragment.

            // Instantiate tab fragments
            switch (position) {
                case TAB_HOME:
                    return HomeFragment.newInstance(mNetFragment.getWallList(TAB_HOME), R.layout
                            .fragment_load_error, resolveAd(mApp.isPro(), null));
                case TAB_PREMIUM:
                    return PremiumFragment.newInstance(mNetFragment.getWallList(TAB_PREMIUM), R
                            .layout.fragment_load_error, resolveAd(mApp.isPro(), null));
                case TAB_TAGS:
                    return TagFragment.newInstance(mNetFragment.getTagList(TAB_TAGS), R.layout
                            .fragment_load_error);
                case TAB_LIKED:
                    return LikedFragment.newInstance(WallListSingleton.getInstance()
                                                                      .getLikedWallList(), R
                            .layout.fragment_none_liked);

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show TAB_NUM total view pages
            return TAB_NUM;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // removes title
            return "";
        }
    }

    @Override
    protected void onDestroy() {
        // Unregister pref change listener to avoid any memory leaks.
        mPreferences.unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }

}
