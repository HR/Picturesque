/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.wall;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.sahr.android.picturesque.Consts;
import co.sahr.android.picturesque.R;
import co.sahr.android.picturesque.data.models.WallItem;
import co.sahr.android.picturesque.utilities.IOUtils;

public class InfoFragment extends Fragment {
    private WallItem mWall;

    public InfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InfoFragment.
     */
    public static InfoFragment newInstance(WallItem wallItem) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(Consts.WALL_ITEM_EXTRA, wallItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment instance
        setRetainInstance(true);

        if (getArguments() != null) {
            mWall = (WallItem) getArguments().getSerializable(Consts.WALL_ITEM_EXTRA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    /**
     * Set the UI params here
     * <p>
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
        // Get the TextViews
        TextView resolutionTextView = (TextView) getView().findViewById(R.id.tv_resolution);
        TextView sizeTextView = (TextView) getView().findViewById(R.id.tv_size);
        TextView tagTextView = (TextView) getView().findViewById(R.id.tv_tag);

        // Show resolution in format Width x Height
        String resolution = mWall.getWidth() + "x" + mWall.getHeight();
        resolutionTextView.setText(resolution);
        // Show the formatted size
        sizeTextView.setText(IOUtils.getFormattedSize(getContext(), mWall.getSize()));
        String author = mWall.getAuthor();
        // If wall has an author, then show copyright text with author's name
        if (!TextUtils.isEmpty(author)) {
            tagTextView.setText(String.format(getString(R.string.description_copyright), author));
        }
    }

}
