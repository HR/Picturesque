/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import co.sahr.android.picturesque.R;
import co.sahr.android.picturesque.data.models.ItemList;
import co.sahr.android.picturesque.fragments.headless.NetFragment;
import co.sahr.android.picturesque.utilities.logger;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public abstract class BaseFragment extends Fragment {
    protected static final String ARG_FALLBACK_LAYOUT = "fallback_layout";
    protected static final String ARG_ITEM_LIST = "item_list";
    protected static final String ARG_AD_ID = "ad_id";

    // Fallback layout id
    protected int mFallbackLayoutId;
    // Ad id string
    protected String mAdID;
    // TODO: 10/07/2017 Listen to changes to mItemList with Rx and update mRecyclerViewItems
    // Holds the item list
    protected ItemList mItemList;
    // Fallback linear layout given by mFallbackLayoutId
    protected LinearLayout mFallbackLinearLayout;
    // The RecyclerView that holds and displays Native Express ads and list items.
    protected RecyclerView mRecyclerView;
    // swipe to refresh layout
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    // Grid layout manager
    protected GridLayoutManager mGridLayoutManager;
    // Progress indicator
    protected FrameLayout mProgressBar;
    protected Button mRetryLoadButton;
    protected CoordinatorLayout mSnackbarContainer;
    // List Item adapter
    protected RecyclerView.Adapter mRecyclerAdapter;
    // List of Native Express ads and WallItems that populate the RecyclerView.
    protected List<Object> mRecyclerViewItems = new ArrayList<>();
    // Rx composite disposable to simplify the mItemList subscription(s) management
    // i.e. Unsubscribe from all of em' at once
    protected CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    protected logger logger = new logger(this);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BaseFragment() {
    }

    /* Only show if not visible */
    public void showProgressBar() {
        if (mProgressBar.getVisibility() == View.GONE) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void showFallbackLayout() {
        if (mFallbackLinearLayout.getVisibility() == View.GONE) {
            mFallbackLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    /* Only hide if visible */
    public void hideProgressBar() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void hideFallbackLayout() {
        if (mFallbackLinearLayout.getVisibility() == View.VISIBLE) {
            mFallbackLinearLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Handles the item list's state change
     * Subscribes to state change & provides callback to handle state change emissions
     */
    private void subscribeItemListStateChange() {
        DisposableObserver disposable = new DisposableObserver<ItemList.StateChange>() {
            /**
             * Provides the Observer with a new item to observe.
             * <p>
             * The {@link Observable} may call this method 0 or more times.
             * <p>
             * The {@code Observable} will not call this method again after it calls either
             * {@link #onComplete} or
             * {@link #onError}.
             *
             * @param stateChange the item emitted by the Observable
             */
            @Override
            public void onNext(@NonNull final ItemList.StateChange stateChange) {
                switch (stateChange.getStateChange()) {
                    case ItemList.UNINITIALIZED:
                        logger.v("State: UNINITIALIZED");
                        // Load items
                        ItemList.StateChange newState = initialLoadState();
                        if (newState != null) {
                            // Set the state if specified to trigger load
                            mItemList.setState(newState);
                            // Update UI to show loading
                            showProgressBar();
                        }
                        break;
                    case ItemList.LOADING:
                        logger.v("State: LOADING");
                        break;
                    case ItemList.LOADED:
                        logger.v("State: LOADED");
                        // Update UI to show loading
                        hideProgressBar();
                        // Callback
                        onLoaded(stateChange);
                        break;
                    case ItemList.ERRORED:
                        logger.v("State: ERRORED");
                        hideProgressBar();
                        // Update UI to show fallback
                        showFallbackLayout();
                        break;
                }
            }

            /**
             * Notifies the Observer that the {@link Observable} has experienced an error condition.
             * <p>
             * If the {@link Observable} calls this method, it will not thereafter call
             * {@link #onNext} or
             * {@link #onComplete}.
             *
             * @param e the exception encountered by the Observable
             */
            @Override
            public void onError(@NonNull final Throwable e) {

            }

            /**
             * Notifies the Observer that the {@link Observable} has finished sending push-based
             * notifications.
             * <p>
             * The {@link Observable} will not call this method if it calls {@link #onError}.
             */
            @Override
            public void onComplete() {

            }
        };

        // subscribe method returns void; Use subscribeWith to return the subscription.
        mCompositeDisposable.add((Disposable) mItemList.getStateSubject()
                                                       .observeOn(AndroidSchedulers.mainThread())
                                                       .subscribeWith(disposable));
    }

    /**
     * Handles the item list changes
     * Subscribes to changes & provides callback to handle change emissions
     */
    private void subscribeItemListChange() {
        DisposableObserver disposable = new DisposableObserver<ItemList.Change>() {
            /**
             * Provides the Observer with a new item to observe.
             * <p>
             * The {@link Observable} may call this method 0 or more times.
             * <p>
             * The {@code Observable} will not call this method again after it calls either
             * {@link #onComplete} or
             * {@link #onError}.
             *
             * @param itemListChange the item emitted by the Observable
             */
            @Override
            public void onNext(@NonNull final ItemList.Change itemListChange) {
                logger.v("Item List Change: " + itemListChange.getChange() + " Change size: " +
                        itemListChange.getChangeSize());
                onItemListChange(itemListChange);
            }

            /**
             * Notifies the Observer that the {@link Observable} has experienced an error condition.
             * <p>
             * If the {@link Observable} calls this method, it will not thereafter call
             * {@link #onNext} or
             * {@link #onComplete}.
             *
             * @param e the exception encountered by the Observable
             */
            @Override
            public void onError(@NonNull final Throwable e) {

            }

            /**
             * Notifies the Observer that the {@link Observable} has finished sending push-based
             * notifications.
             * <p>
             * The {@link Observable} will not call this method if it calls {@link #onError}.
             */
            @Override
            public void onComplete() {

            }
        };

        // subscribe method returns void; Use subscribeWith to return the subscription.
        mCompositeDisposable.add((Disposable) mItemList.getChangeSubject()
                                                       .observeOn(AndroidSchedulers.mainThread())
                                                       .subscribeWith(disposable));
    }

    /**
     * Triggers a reload on the list with its latest loading state (if existent)
     */
    public void reload() {
        ItemList.StateChange loadingState = mItemList.getLoadingState();
        // Check if list has loading state
        if (loadingState != null) {
            // Trigger reload
            mItemList.setState(loadingState);
            // Update UI
            // Show loading indicator
            showProgressBar();
            // hide fallback from errored
            hideFallbackLayout();
        }
    }


    /**
     * Subclass overrides
     ******************************/

    /**
     * Subclasses must override this method to enqueue their initial load method call in the
     * headless {@link NetFragment} frag
     * <p>
     * By default, returns null for no initial load.
     */
    protected ItemList.StateChange initialLoadState() {
        return null;
    }

    /**
     * Subclasses must override this method to set their initial recycler view adapter
     * <p>
     * By default, returns null for adapter.
     */
    protected abstract RecyclerView.Adapter initialRecyclerAdapter();

    /**
     * Callback for item list change subject subscription emission (onNext)
     * By default, notifies the recycler adapter to only update the items changed
     */
    protected void onItemListChange(final ItemList.Change itemListChange) {
        // Check which change has occurred
        switch (itemListChange.getChange()) {
            case ItemList.LIST_APPEND:
                // TODO: 23/08/2017 Fix this
                //                mRecyclerAdapter.notifyItemRangeInserted(itemListChange
                // .getPosition(),
                //                        itemListChange.getChangeSize());
                //                break;

            case ItemList.LIST_SWAP:
                // fallthrough to next (default)

            default:
                mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Called once the wall list loaded successfully
     */
    protected void onLoaded(final ItemList.StateChange stateChange) {}


    /**
     * Fragment lifecycle callbacks
     ******************************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // Get args
            mFallbackLayoutId = getArguments().getInt(ARG_FALLBACK_LAYOUT);
            mAdID = getArguments().getString(ARG_AD_ID);
            mItemList = (ItemList) getArguments().getSerializable(ARG_ITEM_LIST);
            // Subscribe to the item list state subject to listen for changes
            subscribeItemListStateChange();
            // Subscribe to the item list change subject to listen for changes
            subscribeItemListChange();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        boolean shouldAttachToParentImmediately = false;
        FrameLayout rootLayout = (FrameLayout) inflater.inflate(R.layout.fragment_item_grid,
                container, shouldAttachToParentImmediately);
        // Inflate fallback layout in rootLayout
        inflater.inflate(mFallbackLayoutId, rootLayout, true);

        // Get refs
        mFallbackLinearLayout = (LinearLayout) rootLayout.findViewById(R.id.fallback_linear_layout);
        mRetryLoadButton = (Button) mFallbackLinearLayout.findViewById(R.id.retry_load);
        mRecyclerView = (RecyclerView) rootLayout.findViewById(R.id.item_grid);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootLayout.findViewById(R.id.swipeRefreshLayout);
        mSnackbarContainer = (CoordinatorLayout) rootLayout.findViewById(R.id.snackbar_container);
        mProgressBar = (FrameLayout) rootLayout.findViewById(R.id.fl_loading_indicator);

        if (mRetryLoadButton != null) {
            mRetryLoadButton.setOnClickListener(new Button.OnClickListener() {
                /**
                 * Called when a view has been clicked.
                 *
                 * @param v The view that was clicked.
                 */
                @Override
                public void onClick(final View v) {
                    // Trigger reload on the list
                    reload();
                }
            });
        }
        /**
         * Default normal layout column size is 2
         * Get column count from respective layout-<size>/integer.xml to make columns a bit
         * responsive
         */
        mGridLayoutManager = new GridLayoutManager(getContext(), getActivity().getResources()
                                                                              .getInteger(R
                                                                                      .integer
                                                                                      .column_count));
        // Set the manager
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        // Improves performance
        mRecyclerView.setHasFixedSize(true);
        // Set recycler adapter up

        mRecyclerAdapter = initialRecyclerAdapter();
        // Set the adapter
        mRecyclerView.setAdapter(mRecyclerAdapter);

        return rootLayout;
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Dispose of all subscriptions
        mCompositeDisposable.clear();
    }
}
