/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.tag;

import android.os.Bundle;

import co.sahr.android.picturesque.data.models.ItemList;
import co.sahr.android.picturesque.data.models.WallList;
import co.sahr.android.picturesque.data.remote.Api;
import co.sahr.android.picturesque.fragments.WallFragment;
import co.sahr.android.picturesque.fragments.headless.NetFragment;

public class TagWallFragment extends WallFragment {

    private static final String ARG_TAG_ID = "tag_id";
    private Long mTagID;

    public TagWallFragment() {
        // Required empty public constructor
    }

    // Construct Ad-free WallFragment
    public static TagWallFragment newInstance(WallList wallList, int fallbackLayout, String adId,
                                              Long tagId) {
        TagWallFragment fragment = new TagWallFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FALLBACK_LAYOUT, fallbackLayout);
        args.putString(ARG_AD_ID, adId);
        args.putSerializable(ARG_ITEM_LIST, wallList);
        args.putLong(ARG_TAG_ID, tagId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Set tag id
            mTagID = getArguments().getLong(ARG_TAG_ID);
        }
    }

    @Override
    protected ItemList.StateChange initialLoadState() {
        ItemList.StateChange newState = new ItemList.StateChange(ItemList.LOADING);
        newState.setSortOrder(Api.MOST_RECENT)
                .setMethod(NetFragment.LOAD_WALLS)
                .setTagId(mTagID);
        return newState;
    }
}
