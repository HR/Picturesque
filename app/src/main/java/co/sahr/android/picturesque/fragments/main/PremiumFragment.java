/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.main;

import android.os.Bundle;

import co.sahr.android.picturesque.data.models.ItemList;
import co.sahr.android.picturesque.data.models.WallList;
import co.sahr.android.picturesque.data.remote.Api;
import co.sahr.android.picturesque.fragments.WallFragment;
import co.sahr.android.picturesque.fragments.headless.NetFragment;

public class PremiumFragment extends WallFragment {
    public PremiumFragment() {
        // Required empty public constructor
    }

    public static PremiumFragment newInstance(WallList wallList, int fallbackLayout, String adId) {
        PremiumFragment fragment = new PremiumFragment();
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
                .setSortOrder(Api.MOST_POPULAR)
                .setIsPremium(true);
        return newState;
    }
}
