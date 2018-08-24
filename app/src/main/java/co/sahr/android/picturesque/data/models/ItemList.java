/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data.models;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

import co.sahr.android.picturesque.data.remote.Api;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class ItemList<E> extends ArrayList<E> implements Serializable {
    // Item list changes
    public static final int LIST_SWAP = 0;
    public static final int LIST_APPEND = 1;
    public static final int LIST_PREPEND = 2;
    public static final int ADD = 3;
    public static final int REMOVE = 4;

    // Item list states
    public static final int UNINITIALIZED = 0;
    public static final int LOADING = 1;
    public static final int LOADED = 2;
    public static final int ERRORED = 3;

    // Holds next page token if next page exists
    private String nextPageToken;
    // Holds the latest loading state
    private StateChange loadingState;

    // Observables
    private final PublishSubject<Change> changeSubject;
    /**
     * Behaviour subject used to enable the UI fragment to restore its UI state after config
     * change by being able to obtain the latest state of the item list (when re-subbed to it)
     */
    private final BehaviorSubject<StateChange> stateSubject;


    public ItemList() {
        this.changeSubject = PublishSubject.create();
        this.stateSubject = BehaviorSubject.create();
        // Initialize with uninitialized state
        stateSubject.onNext(new StateChange(UNINITIALIZED));
    }

    public PublishSubject<Change> getChangeSubject() {
        return changeSubject;
    }

    public BehaviorSubject<StateChange> getStateSubject() {
        return stateSubject;
    }

    public StateChange getLoadingState() {
        return loadingState;
    }


    /**
     * Item list (internal) change object enables the UI fragment to efficiently update the UI
     */
    public static class Change {
        private final int change;
        private final int position;
        // Optional param (nullable) as only used for notifyItemRange* methods
        private final Integer changeSize;

        private Change(final int change, final int position) {
            this.position = position;
            this.change = change;
            this.changeSize = null;
        }

        private Change(final int change, final int position, @Nullable final Integer changeSize) {
            this.position = position;
            this.change = change;
            this.changeSize = changeSize;
        }

        public int getChange() {
            return change;
        }

        public int getPosition() {
            return position;
        }

        public Integer getChangeSize() {
            return changeSize;
        }
    }

    /**
     * State change object enables Inter Fragment Communication via fragment subscriptions (PubSub)
     * Holds all the state change info required by each fragment to do its job
     */
    public static class StateChange {
        private final int stateChange;
        // The change to apply to the item list, append by default
        private Integer listChange = LIST_APPEND;
        private Integer method;
        private String isPremium;
        private String nextPageToken;
        private String tagId;
        private String query;
        // Default limit
        private String pageSize = String.valueOf(Api.WALL_LIST_PAGE_SIZE);
        private String sortOrder;

        public StateChange(final int stateChange) {
            this.stateChange = stateChange;
        }

        public int getStateChange() {
            return stateChange;
        }

        public Integer getListChange() {
            return listChange;
        }

        public StateChange setListChange(final Integer listChange) {
            this.listChange = listChange;
            return this;
        }

        public Integer getMethod() {
            return method;
        }

        public StateChange setMethod(final Integer method) {
            this.method = method;
            return this;
        }

        public String getIsPremium() {
            return isPremium;
        }

        // The @param is boolean and converted to string as to prevent typos
        public StateChange setIsPremium(final boolean isPremium) {
            this.isPremium = String.valueOf(isPremium);
            return this;
        }

        public String getNextPageToken() {
            return nextPageToken;
        }

        public StateChange setNextPageToken(final String nextPageToken) {
            this.nextPageToken = nextPageToken;
            return this;
        }

        public String getTagId() {
            return tagId;
        }

        public StateChange setTagId(final Long tagId) {
            this.tagId = String.valueOf(tagId);
            return this;
        }

        public String getQuery() {
            return query;
        }

        public StateChange setQuery(final String query) {
            this.query = query;
            return this;
        }

        public String getPageSize() {
            return pageSize;
        }

        public StateChange setPageSize(final int pageSize) {
            this.pageSize = String.valueOf(pageSize);
            return this;
        }

        public String getSortOrder() {
            return sortOrder;
        }

        public StateChange setSortOrder(final String sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }
    }

    /**
     * Change state of the item list
     */
    public void setState(StateChange stateChange) {
        // Cache the latest loading state (to allow its replay for pagination)
        if (stateChange.getStateChange() == LOADING) {
            loadingState = stateChange;
        }
        // Emit new state
        stateSubject.onNext(stateChange);
    }

    /**
     * Get the latest state constant of the item list
     */
    public int getStateConst() {
        return stateSubject.getValue()
                           .getStateChange();
    }

    /**
     * Returns whether the list is loading (i.e. in loading state)
     */
    public boolean isLoading() {
        return stateSubject.getValue()
                           .getStateChange() == LOADING;
    }

    /**
     * Returns whether the list is uninitialized (i.e. in uninitialized state)
     */
    public boolean isUninitialized() {
        return stateSubject.getValue()
                           .getStateChange() == UNINITIALIZED;
    }

    /**
     * Get the latest state of the item list
     */
    public StateChange getState() {
        return stateSubject.getValue();
    }


    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(final String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }


    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If the list does not contain the element, it is
     * unchanged.  More formally, removes the element with the lowest index
     * <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * (if such an element exists).  Returns <tt>true</tt> if this list
     * contained the specified element (or equivalently, if this list
     * changed as a result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    @Override
    public boolean remove(final Object o) {
        boolean hasChanged = super.remove(o);
        if (hasChanged) {
            // Publish whether the super method^ has changed the list or not
            changeSubject.onNext(new Change(REMOVE, super.indexOf(o)));
        }
        return hasChanged;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param item element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link super#add})
     */
    @Override
    public boolean add(final E item) {
        boolean hasChanged = super.add(item);
        if (hasChanged) {
            // Publish whether the super method^ has changed the list or not
            changeSubject.onNext(new Change(ADD, super.indexOf(item)));
        }
        return hasChanged;
    }

    /**
     * Swaps this item list (reference item list object) with @param itemList
     */
    public boolean swapList(ItemList<E> itemList) {
        // Empty list first
        super.clear();
        // Append list to this item list
        boolean hasChanged = super.addAll(itemList);
        if (hasChanged) {
            // Publish whether the super method^ has changed the list or not
            changeSubject.onNext(new Change(LIST_SWAP, 0, itemList.size()));
        }
        return hasChanged;
    }

    /**
     * Appends @param itemList to this item list (reference item list object)
     */
    public boolean appendList(ItemList<E> itemList) {
        // Prepend to the list
        boolean hasChanged = super.addAll(itemList);
        if (hasChanged) {
            int lastItemIndex = itemList.size() - 1;
            // Publish whether the super method^ has changed the list or not
            changeSubject.onNext(new Change(LIST_APPEND, lastItemIndex, itemList.size()));
        }
        return hasChanged;
    }

    /**
     * Prepends @param itemList to this item list (reference item list object)
     */
    public boolean prependList(ItemList<E> itemList) {
        // Prepend to the list
        boolean hasChanged = super.addAll(0, itemList);
        if (hasChanged) {
            // Publish whether the super method^ has changed the list or not
            changeSubject.onNext(new Change(LIST_PREPEND, 0, itemList.size()));
        }
        return hasChanged;
    }

    /**
     * Applies the passed in change to the list
     * By default, swaps the list
     */
    public boolean applyChange(int change, ItemList<E> itemList) {
        switch (change) {
            case ItemList.LIST_PREPEND:
                return this.prependList(itemList);
            case ItemList.LIST_APPEND:
                return this.appendList(itemList);
            case ItemList.LIST_SWAP:
                // fallthrough
            default:
                return this.swapList(itemList);
        }
    }
}
