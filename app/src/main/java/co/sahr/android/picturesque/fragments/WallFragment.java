/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;

import co.sahr.android.picturesque.R;
import co.sahr.android.picturesque.adapters.WallAdAdapter;
import co.sahr.android.picturesque.adapters.WallAdapter;
import co.sahr.android.picturesque.data.models.ItemList;
import co.sahr.android.picturesque.data.models.WallList;
import co.sahr.android.picturesque.utilities.Utils;

import static co.sahr.android.picturesque.Consts.ITEMS_PER_AD;
import static co.sahr.android.picturesque.Consts.LOAD_MORE_VISIBLE_THRESHOLD;
import static co.sahr.android.picturesque.Consts.WALL_FRAGMENT_NATIVE_AD_HEIGHT;

public abstract class WallFragment extends BaseFragment {
    // Wall grid item click listener
    private OnWallListClickListener mListener;
    // Snackbar loading indicator
    private Snackbar mLoadingSnackbar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WallFragment() {
    }

    /**
     * Checks if the item grid has ads enabled
     */
    public boolean hasAds() {
        return mAdID != null;
    }

    /**
     * Constructs RecyclerViewItems from wall list
     * Adds the ad views to the list
     */
    public void swapRecyclerViewItems() {
        // Clear current list
        mRecyclerViewItems.clear();
        // Append all of wall list to the  RecyclerViewItems
        mRecyclerViewItems.addAll(mItemList);
        // Update the RecyclerView item's list with wall items and Native Express ads.
        addNativeExpressAds();
    }

    /**
     * Adds Native Express ads to the items list.
     */
    private void addNativeExpressAds() {
        // Loop through the items array and place a new Native Express ad in every ith position in
        // the items List.
        for (int i = 0; i < mRecyclerViewItems.size(); i += ITEMS_PER_AD) {
            if (!(mRecyclerViewItems.get(i) instanceof NativeExpressAdView)) {
                // Only add ad at position if it does not already exist
                final NativeExpressAdView adView = new NativeExpressAdView(getActivity());
                mRecyclerViewItems.add(i, adView);
                // Notify of insert
                //            mRecyclerAdapter.notifyItemInserted(i);
                // Load ads
                setUpAndLoadNativeExpressAd(adView);
            }
        }
    }

    /**
     * Sets up and loads the passed in Native Express Ad
     */
    public void setUpAndLoadNativeExpressAd(final NativeExpressAdView adView) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                AdSize adSize = new AdSize(AdSize.FULL_WIDTH, WALL_FRAGMENT_NATIVE_AD_HEIGHT);
                adView.setAdSize(adSize);
                adView.setAdUnitId(mAdID);
                // load the ad
                loadNativeExpressAd(adView);
            }
        });
    }

    /**
     * Loads the passed in Native Express Ad
     */
    private void loadNativeExpressAd(final NativeExpressAdView adView) {

        // Set an AdListener on the NativeExpressAdView to wait for the previous Native Express ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // The previous Native Express ad loaded successfully, call this method again to
                // load the next ad in the items list.
                //                loadNativeExpressAd(index + ITEMS_PER_AD);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // The previous Native Express ad failed to load. Call this method again to load
                // the next ad in the items list.
                logger.e("Ad load failed. Retrying...");
                // Retry loading it
                loadNativeExpressAd(adView);
            }
        });

        // Load the Native Express ad.
        adView.loadAd(new AdRequest.Builder().build());
    }

    /**
     * Show loading snackbar
     * Only show it if not already shown or create if nonexistent
     */
    public void showLoadingSnackbar() {
        if (mLoadingSnackbar == null) {
            mLoadingSnackbar = Utils.showSnackbar(mSnackbarContainer, R.string
                    .message_loading_more, Utils.SNACKBAR_INDEFINITE);
        } else if (!mLoadingSnackbar.isShown()) {
            mLoadingSnackbar.show();
        }
    }

    /**
     * Hide loading snackbar
     */
    public void hideLoadingSnackbar() {
        if (mLoadingSnackbar != null) mLoadingSnackbar.dismiss();
    }

    /**
     * Sets listener to get callback for load more
     */
    private void setLoadMoreListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView mRecyclerView, int dx, int dy) {
                super.onScrolled(mRecyclerView, dx, dy);

                int totalItemCount = mGridLayoutManager.getItemCount();
                int lastVisibleItem = mGridLayoutManager.findLastVisibleItemPosition();
                int visibleThreshold = (totalItemCount - lastVisibleItem);
                // Ensure not loading and max visible item count (for next load) is greater than
                // total and there is a next page to load
                if (!mItemList.isLoading() && LOAD_MORE_VISIBLE_THRESHOLD >= visibleThreshold &&
                        mItemList.getNextPageToken() != null) {
                    logger.v("Loading next wall page...");

                    // There is one, start loading with the latest loading state
                    ItemList.StateChange newState = mItemList.getLoadingState();

                    // Ensure valid load state
                    if (newState != null) {
                        // Show loading indicator
                        showLoadingSnackbar();
                        // Set the change to apply to current list to append fetched list
                        newState.setListChange(ItemList.LIST_APPEND);
                        // Set the page to fetch
                        newState.setNextPageToken(mItemList.getNextPageToken());
                        // Trigger load
                        mItemList.setState(newState);
                    }
                }
            }
        });
    }

    /**
     * Sets listener to refresh if data stale on swipe
     */
    private void setSwipeRefreshListener() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Ensure that the first item is a wall and list is not loading
                //                if (!(mItemList.get(0) instanceof WallItem) && !mItemList
                // .isLoading()) return;
                //
                //                WallItem wall = (WallItem) mItemList.get(0);

                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Request the permission after DELAY
                        // Stop refresh animation
                        mSwipeRefreshLayout.setRefreshing(false);
                        Utils.showSnackbar(mRecyclerView, R.string.message_refresh_uptodate);
                    }
                }, Utils.SNACKBAR_SHORT_DELAY);

            }
        });
    }

    /**
     * Subclasses must override this method to set their initial recycler view adapter
     * <p>
     * By default, returns null for adapter.
     */
    @Override
    protected RecyclerView.Adapter initialRecyclerAdapter() {
        if (hasAds()) {
            // Setup and load ads
            if (!mItemList.isEmpty()) {
                // Config changes, recreate mRecyclerViewItems if mItemList not empty
                swapRecyclerViewItems();
            }

            // Set ads to occupy entire row
            mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return WallAdAdapter.isAd(position) ? mGridLayoutManager.getSpanCount() : 1;
                }
            });

            // Init wall ad adapter with initial wall list & the click listener
            return new WallAdAdapter(getActivity(), mRecyclerViewItems, mListener);
        }

        // Init wall ad-free adapter with initial wall list & the click listener
        return new WallAdapter((WallList) mItemList, mListener);
    }

    /**
     * Callback for item list change subject subscription emission (onNext)
     * By default, notifies the recycler adapter to only update the items changed
     *
     * @param itemListChange
     */
    @Override
    protected void onItemListChange(final ItemList.Change itemListChange) {
        // Dismiss loading indicator
        hideLoadingSnackbar();
        if (!hasAds()) {
            // Use super implementation for Ad-free
            super.onItemListChange(itemListChange);
            return;
        }

        // Recreate mRecyclerViewItems list (from mItemList) from scratch
        swapRecyclerViewItems();
        // Notify adapter of the change
        mRecyclerAdapter.notifyDataSetChanged();
        // finish load
        hideProgressBar();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnWallListClickListener) {
            mListener = (OnWallListClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement " +
                    "OnWallListClickListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This fragment has menu items to contribute
        setHasOptionsMenu(true);
    }

    /**
     * Ads the sort by menu item to the R.menu.menu_main menu inflated in
     * {@link co.sahr.android.picturesque.MainActivity} using setHasOptionsMenu(true)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_sort, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
        // Set load more listener
        setLoadMoreListener();
        // Set swipe to refresh listener
        setSwipeRefreshListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
