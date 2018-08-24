/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.wall;


import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import co.sahr.android.picturesque.R;

public class ControlsFragment extends Fragment {
    // Info selected level in level-list
    private static final int UNSELECTED = 0;
    // Info unselected level in level-list
    private static final int SELECTED = 1;
    // Initial like state
    private static final String WALL_IS_LIKED_PARAM = "like_state";
    // Flag to save info icon level state
    private int mInfoIconLevel;
    // Flag to save like icon level state
    private int mLikeIconLevel = 3;

    private LevelListDrawable mInfoIcon;
    private LevelListDrawable mLikeIcon;

    public ControlsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InfoFragment.
     */
    public static ControlsFragment newInstance(boolean isLiked) {
        ControlsFragment fragment = new ControlsFragment();
        Bundle args = new Bundle();
        args.putBoolean(WALL_IS_LIKED_PARAM, isLiked);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_controls, container, false);
    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Activity)} and before
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * <p>
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>Any restored child fragments will be created before the base
     * <code>Fragment.onCreate</code> method returns.</p>
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment instance
        setRetainInstance(true);
    }

    /**
     * Update icon drawable state when wall set mode is exited i.e. back nav is clicked
     * <p>
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Restore drawables' state
        mInfoIcon.setLevel(mInfoIconLevel);
        mLikeIcon.setLevel(mLikeIconLevel);
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
        // Get state drawables and cache them
        ImageView infoIconImageView = (ImageView) getView().findViewById(R.id.iv_info_control);
        mInfoIcon = (LevelListDrawable) infoIconImageView.getDrawable();
        ImageView likeIconImageView = (ImageView) getView().findViewById(R.id.iv_like_control);
        mLikeIcon = (LevelListDrawable) likeIconImageView.getDrawable();

        // Set initial like state only if not set i.e. 3
        if (getArguments() != null && mLikeIconLevel == 3) {
            boolean isLiked = getArguments().getBoolean(WALL_IS_LIKED_PARAM);
            setLikeIcon(isLiked);
        }
    }

    public void toggleInfoIcon() {
        // Toggle state
        mInfoIconLevel = (mInfoIconLevel == UNSELECTED) ? SELECTED : UNSELECTED;

        // Set state
        mInfoIcon.setLevel(mInfoIconLevel);
    }

    public void setLikeIcon(boolean isLiked) {
        // Toggle state
        mLikeIconLevel = (isLiked) ? SELECTED : UNSELECTED;

        // Set state
        mLikeIcon.setLevel(mLikeIconLevel);
    }

}
