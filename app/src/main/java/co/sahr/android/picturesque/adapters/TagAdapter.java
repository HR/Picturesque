/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import co.sahr.android.picturesque.R;
import co.sahr.android.picturesque.data.models.TagItem;
import co.sahr.android.picturesque.data.models.TagList;
import co.sahr.android.picturesque.fragments.main.TagFragment;
import co.sahr.android.picturesque.utilities.logger;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private TagList mTagList;
    private final TagFragment.OnTagListClickListener mListener;

    public TagAdapter(TagList tags, TagFragment.OnTagListClickListener listener) {
        mTagList = tags;
        mListener = listener;
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
    @Override
    public TagAdapter.TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.fragment_tag, parent, false);
        return new TagAdapter.TagViewHolder(view);
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
     * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final TagViewHolder holder, final int position) {
        // Cache item in holder
        holder.mItem = mTagList.get(position);
        holder.mTagTextView.setText(holder.mItem.getName());
        try {
            // Load thumbnail
            holder.mThumbDraweeView.setImageURI(Uri.parse(holder.mItem.getThumbUri()));
        } catch (Exception e) {
            logger.e(e);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onTagListClick(holder.mItem);
                }
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mTagList.size();
    }

    public class TagViewHolder extends RecyclerView.ViewHolder {
        public final SimpleDraweeView mThumbDraweeView;
        public final TextView mTagTextView;
        public TagItem mItem;

        public TagViewHolder(View view) {
            super(view);
            mTagTextView = (TextView) view.findViewById(R.id.tv_tag_text);
            mThumbDraweeView = (SimpleDraweeView) view.findViewById(R.id.iv_tag_thumb);
        }
    }
}
