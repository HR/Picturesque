/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import co.sahr.android.picturesque.R;
import co.sahr.android.picturesque.data.models.ItemList;
import co.sahr.android.picturesque.data.models.WallList;
import co.sahr.android.picturesque.fragments.WallFragment;
import co.sahr.android.picturesque.fragments.headless.NetFragment;

public class SearchResultFragment extends WallFragment {
    // Stores the most recent query
    private String mQuery = "";

    public SearchResultFragment() {
        // Required empty public constructor
    }

    // Instance factory
    public static SearchResultFragment newInstance(WallList wallList, int fallbackLayout, String
            adId) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FALLBACK_LAYOUT, fallbackLayout);
        args.putString(ARG_AD_ID, adId);
        args.putSerializable(ARG_ITEM_LIST, wallList);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Ads the sort by menu item to the R.menu.menu_main menu inflated in
     * {@link co.sahr.android.picturesque.MainActivity} using setHasOptionsMenu(true)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do not inflate menu
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        //        final SearchView searchView = (SearchView) searchItem.getActionView();
        // Expand search
        searchItem.expandActionView();
    }

    /**
     * Called once the wall list loaded successfully
     *
     * @param stateChange
     */
    @Override
    protected void onLoaded(final ItemList.StateChange stateChange) {
        if (mItemList.size() == 0) {
            // show no results layout
            showFallbackLayout();
        } else {
            hideFallbackLayout();
        }
    }

    /**
     * Subclasses must override this method to set their initial recycler view adapter
     * <p>
     * By default, returns null for adapter.
     */
    @Override
    protected RecyclerView.Adapter initialRecyclerAdapter() {
        // Disable swipe to refresh for tags
        mSwipeRefreshLayout.setEnabled(false);
        // Do super
        return super.initialRecyclerAdapter();
    }


    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mItemList.isLoading()) {
            // Restore loading bar on config change if still loading
            showProgressBar();
        }
    }

    /**
     * Init load on the wall list for the query
     *
     * @param query
     */
    public void loadQuery(String query) {
        // Check if not current query
        if (mQuery.equals(query)) {
            return;
        }
        // Show loading bar
        showProgressBar();

        // Init new load state
        ItemList.StateChange newState = new ItemList.StateChange(ItemList.LOADING);
        newState.setMethod(NetFragment.LOAD_SEARCH_RESULTS)
                .setQuery(query)
                .setListChange(ItemList.LIST_SWAP);
        // Trigger load directly
        mItemList.setState(newState);
        // Cache query
        mQuery = query;
    }

    /**
     * Reset state & UI
     */
    public void reset() {
        mQuery = "";
        hideFallbackLayout();
        mItemList.clear();
        mItemList.setNextPageToken(null);
        // Clear recycler view items if ad-filled
        if (mRecyclerViewItems != null) mRecyclerViewItems.clear();
        mItemList.setState(new ItemList.StateChange(ItemList.UNINITIALIZED));
        mRecyclerAdapter.notifyDataSetChanged();
    }
}
