/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import co.sahr.android.picturesque.data.models.ItemList;
import co.sahr.android.picturesque.data.models.WallList;
import co.sahr.android.picturesque.data.remote.Api;
import co.sahr.android.picturesque.fragments.WallFragment;
import co.sahr.android.picturesque.fragments.headless.NetFragment;

public class HomeFragment extends WallFragment {
    public HomeFragment() {
        // Required empty public constructor
    }

    // Construct Ad-free WallFragment
    public static HomeFragment newInstance(WallList wallList, int fallbackLayout, String adId) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FALLBACK_LAYOUT, fallbackLayout);
        args.putString(ARG_AD_ID, adId);
        args.putSerializable(ARG_ITEM_LIST, wallList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected ItemList.StateChange initialLoadState() {
        ItemList.StateChange newState = new ItemList.StateChange(ItemList.LOADING);
        newState.setMethod(NetFragment.LOAD_WALLS)
                .setSortOrder(Api.MOST_RECENT);
        return newState;
    }
}
