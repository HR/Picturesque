/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.ads.NativeExpressAdView;

import java.util.List;

import co.sahr.android.picturesque.R;
import co.sahr.android.picturesque.data.models.WallItem;
import co.sahr.android.picturesque.fragments.OnWallListClickListener;
import co.sahr.android.picturesque.utilities.logger;

import static co.sahr.android.picturesque.Consts.ITEMS_PER_AD;

public class WallAdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // A menu item view type.
    public static final int WALL_VIEW_TYPE = 0;

    // The Native Express ad view type.
    public static final int NATIVE_EXPRESS_AD_VIEW_TYPE = 1;


    // The list of Native Express ads and wall items.
    private List<Object> mRecyclerViewItems;
    // An Activity's Context.
    private final Context mContext;
    // wall item click listener
    private final OnWallListClickListener mListener;

    public WallAdAdapter(Context context, List<Object> recyclerViewItems, OnWallListClickListener
            listener) {
        this.mContext = context;
        this.mRecyclerViewItems = recyclerViewItems;
        this.mListener = listener;
    }

    /**
     * Identifies if there is an ad at the specified position (in the mRecyclerViewItems)
     */
    public static boolean isAd(int position) {
        return (position % ITEMS_PER_AD == 0);
    }

    /**
     * The {@link NativeExpressAdViewHolder} class.
     */
    public class NativeExpressAdViewHolder extends RecyclerView.ViewHolder {

        NativeExpressAdViewHolder(View view) {
            super(view);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mRecyclerViewItems.size();
    }

    /**
     * Determines the view type for the given position.
     */
    @Override
    public int getItemViewType(int position) {
        return isAd(position) ? NATIVE_EXPRESS_AD_VIEW_TYPE : WALL_VIEW_TYPE;
    }


    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    /**
     * Creates a new view for a wall item view or a Native Express ad view
     * based on the viewType. This method is invoked by the layout manager.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case WALL_VIEW_TYPE:
                View wallItemLayoutView = LayoutInflater.from(viewGroup.getContext())
                                                        .inflate(R.layout.wall_item_container,
                                                                viewGroup, false);
                return new WallViewHolder(wallItemLayoutView);
            case NATIVE_EXPRESS_AD_VIEW_TYPE:
                // fall through
            default:
                View nativeExpressLayoutView = LayoutInflater.from(viewGroup.getContext())
                                                             .inflate(R.layout
                                                                     .native_express_ad_container, viewGroup, false);
                return new NativeExpressAdViewHolder(nativeExpressLayoutView);
        }

    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(WallViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    /**
     * Replaces the content in the views that make up the menu item view and the
     * Native Express ad view. This method is invoked by the layout manager.
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case WALL_VIEW_TYPE:
                final WallViewHolder wallHolder = (WallViewHolder) holder;
                // Cache item in holder
                wallHolder.mItem = (WallItem) mRecyclerViewItems.get(position);
                // Show or hide label based on if premium or not
                wallHolder.mLabelLayout.setVisibility(wallHolder.mItem.getIsPremium() ? View
                        .VISIBLE : View.GONE);
                try {
                    // Load thumbnail
                    wallHolder.mThumbDraweeView.setImageURI(Uri.parse(wallHolder.mItem
                            .getThumbUri()));
                } catch (Exception e) {
                    logger.e(e);
                }
                wallHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
                            mListener.onWallListClick(wallHolder.mItem);
                        }
                    }
                });
                break;
            case NATIVE_EXPRESS_AD_VIEW_TYPE:
                // fall through
            default:
                NativeExpressAdViewHolder nativeExpressHolder = (NativeExpressAdViewHolder) holder;
                NativeExpressAdView adView = (NativeExpressAdView) mRecyclerViewItems.get(position);
                ViewGroup adItemView = (ViewGroup) nativeExpressHolder.itemView;
                // The NativeExpressAdViewHolder recycled by the RecyclerView may be a different
                // instance than the one used previously for this position. Clear the
                // NativeExpressAdViewHolder of any subviews in case it has a different
                // AdView associated with it, and make sure the AdView for this position doesn't
                // already have a parent of a different recycled NativeExpressAdViewHolder.
                if (adItemView.getChildCount() > 0) {
                    adItemView.removeAllViews();
                }
                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }

                // Add the Native Express ad to the native express ad view.
                adItemView.addView(adView);
        }
    }
}
