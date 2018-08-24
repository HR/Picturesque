/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import co.sahr.android.picturesque.adapters.TagAdapter;
import co.sahr.android.picturesque.data.models.ItemList;
import co.sahr.android.picturesque.data.models.TagItem;
import co.sahr.android.picturesque.data.models.TagList;
import co.sahr.android.picturesque.fragments.BaseFragment;
import co.sahr.android.picturesque.fragments.headless.NetFragment;

public class TagFragment extends BaseFragment {
    protected OnTagListClickListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TagFragment() {
    }

    public static TagFragment newInstance(TagList tagList, int fallbackLayout) {
        TagFragment fragment = new TagFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FALLBACK_LAYOUT, fallbackLayout);
        args.putSerializable(ARG_ITEM_LIST, tagList);
        args.putSerializable(ARG_AD_ID, null);
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
        // Init wall adapter with initial wall list and its click listener
        return new TagAdapter((TagList) mItemList, mListener);
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
        newState.setMethod(NetFragment.LOAD_TAGS);
        return newState;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTagListClickListener) {
            mListener = (OnTagListClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement " +
                    "OnTagListClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTagListClickListener {
        void onTagListClick(TagItem item);
    }
}
