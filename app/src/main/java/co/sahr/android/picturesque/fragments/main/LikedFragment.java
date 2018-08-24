/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import co.sahr.android.picturesque.adapters.WallAdapter;
import co.sahr.android.picturesque.data.models.ItemList;
import co.sahr.android.picturesque.data.models.WallList;
import co.sahr.android.picturesque.fragments.BaseFragment;
import co.sahr.android.picturesque.fragments.OnWallListClickListener;
import co.sahr.android.picturesque.fragments.headless.NetFragment;

public class LikedFragment extends BaseFragment {
    // Wall grid item click listener
    private OnWallListClickListener mListener;

    public LikedFragment() {
    }

    public static LikedFragment newInstance(WallList wallList, int fallbackLayout) {
        LikedFragment fragment = new LikedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FALLBACK_LAYOUT, fallbackLayout);
        args.putString(ARG_AD_ID, null);
        // Set it to the empty internal wall list initially
        args.putSerializable(ARG_ITEM_LIST, wallList);
        fragment.setArguments(args);
        return fragment;
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
        // Init wall ad-free adapter with initial wall list & the click listener
        return new WallAdapter((WallList) mItemList, mListener);
    }

    /**
     * Subclasses must override this method to enqueue their initial load method call in the
     * headless {@link NetFragment} frag
     * <p>
     * By default, returns null for no initial load.
     */
    @Override
    protected ItemList.StateChange initialLoadState() {
        ItemList.StateChange newState = new ItemList.StateChange(ItemList.LOADING);
        newState.setMethod(NetFragment.LOAD_LIKED_WALL_LIST);
        return newState;
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

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to Activity.onResume of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        // TODO: 03/07/2017 Consider using RxJava ReplaySubject in singleton to emit updates
        if (!mItemList.isLoading() && !mItemList.isUninitialized()) {
            // Only if wall is not loading or uninitialised, check fallback
            if (mItemList.size() == 0) {
                // Show fallback layout
                showFallbackLayout();
            } else {
                // Only if recycler view is hidden toggle visibility
                hideFallbackLayout();
            }
            // Ensure the items are updated
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
