/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.fragments.headless;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.HashMap;
import java.util.concurrent.Callable;

import co.sahr.android.picturesque.App;
import co.sahr.android.picturesque.BuildConfig;
import co.sahr.android.picturesque.data.WallListSingleton;
import co.sahr.android.picturesque.data.models.ImmutableList;
import co.sahr.android.picturesque.data.models.ImmutableTagList;
import co.sahr.android.picturesque.data.models.ImmutableWallList;
import co.sahr.android.picturesque.data.models.ItemList;
import co.sahr.android.picturesque.data.models.TagList;
import co.sahr.android.picturesque.data.models.WallList;
import co.sahr.android.picturesque.data.remote.Api;
import co.sahr.android.picturesque.data.remote.ApiClient;
import co.sahr.android.picturesque.fragments.main.LikedFragment;
import co.sahr.android.picturesque.utilities.DBUtils;
import co.sahr.android.picturesque.utilities.PackageManagerUtils;
import co.sahr.android.picturesque.utilities.logger;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetFragment extends Fragment {
    // Get Methods
    public static final int LOAD_WALLS = 1;
    public static final int LOAD_TAGS = 2;
    public static final int LOAD_LIKED_WALL_LIST = 3;
    public static final int LOAD_SEARCH_RESULTS = 4;

    // Cache the full GET wall lists in hash map
    private HashMap<Integer, ItemList> listHashMap = new HashMap<>();
    // Rx composite disposable to simplify the wall list subscription(s) management
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    // DB for liked wall list
    private SQLiteDatabase mDb;

    private Api mService;

    private logger logger = new logger(this);


    public NetFragment() {
        // Required empty public constructor
    }


    /**
     * Cache (retain) interface
     ******************************/

    /**
     * Gets the wall list for the passed key
     * Like a singleton, gets existing one if in map. Creates, subscribes & saves it if not
     *
     * @param key
     * @return a wall list for the key
     */
    public WallList getWallList(Integer key) {

        if (listHashMap.containsKey(key)) {
            logger.v("Wall list for key " + key + " already EXISTS, using it");
            // Wall list already exists, get it
            WallList cached = (WallList) listHashMap.get(key);
            // Return the wall list if already exists
            return cached;
        }
        logger.v("Wall list for key " + key + " does NOT exists, init new one");
        // Otherwise, init a new one
        WallList wallList = new WallList();
        // Subscribe to its state
        subscribeListStateChange(wallList);
        // Save the new wall list in the map
        listHashMap.put(key, wallList);
        // return the new one
        return wallList;
    }

    /**
     * Gets the tag list for the passed key
     * Like a singleton, gets existing one if in map. Creates, subscribes & saves it if not
     *
     * @param key
     * @return a wall list for the key
     */
    public TagList getTagList(Integer key) {

        if (listHashMap.containsKey(key)) {
            logger.v("Tag list for key " + key + " already EXISTS, using it");
            // Tag list already exists, get it
            TagList cached = (TagList) listHashMap.get(key);
            // Return the wall list if already exists
            return cached;
        }
        logger.v("Tag list for key " + key + " does NOT exists, init new one");
        // Otherwise, init a new one
        TagList tagList = new TagList();
        // Subscribe to its state
        subscribeListStateChange(tagList);
        // Save the new tag list in the map
        listHashMap.put(key, tagList);
        // return the new one
        return tagList;
    }

    /**
     * Subscribes to the state change of any wall list (incl. not in the hashmap)
     */
    public WallList subscribeWallListStateChange(WallList wallList) {
        subscribeListStateChange(wallList);
        return wallList;
    }

    /**
     * Subscribe to the wall list's state change subject
     *
     * @param list
     */
    private <T extends ItemList> void subscribeListStateChange(final T list) {
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
                if (stateChange.getStateChange() == ItemList.LOADING) {
                    // Trigger load
                    // Check which method to exec
                    switch (stateChange.getMethod()) {
                        case LOAD_WALLS:
                            logger.v("State: loadWalls into wall list " + stateChange
                                    .getSortOrder());
                            loadWalls((WallList) list, stateChange);
                            break;
                        case LOAD_TAGS:
                            logger.v("State: loadTags into tag list");
                            loadTags((TagList) list, stateChange);
                            break;
                        case LOAD_LIKED_WALL_LIST:
                            logger.v("State: loadLikedWalls into singleton list");
                            loadLikedWalls((WallList) list);
                            break;
                        case LOAD_SEARCH_RESULTS:
                            logger.v("State: loadSearchResults into wall list");
                            loadSearchResults((WallList) list, stateChange);
                            break;

                    }
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
        mCompositeDisposable.add((Disposable) list.getStateSubject()
                                                  .subscribeOn(Schedulers.io()) // on io thread pool
                                                  .subscribeWith(disposable));
    }


    /**
     * Methods for loading
     ******************************/

    /**
     * Gets & loads (into the wall list) the most recent or most popular walls.
     * Identified by {@value LOAD_WALLS} constant
     */
    public void loadWalls(final WallList wallList, ItemList.StateChange stateChange) {
        Call<ImmutableWallList> call = mService.getWallList(stateChange.getPageSize(),
                stateChange.getSortOrder(), stateChange.getIsPremium(), stateChange
                        .getNextPageToken(), stateChange.getTagId());
        // Enqueue network load
        enqueueCall(call, wallList, stateChange.getListChange());
    }

    /**
     * Gets & loads (into the tag list) the most tags.
     * Identified by {@value LOAD_TAGS} constant
     */
    public void loadTags(final TagList tagList, ItemList.StateChange stateChange) {
        Call<ImmutableTagList> call = mService.getTagList();
        // Enqueue network load
        enqueueCall(call, tagList, stateChange.getListChange());
    }

    /**
     * Gets & loads (into the wall list) the most tags.
     * Identified by {@value LOAD_SEARCH_RESULTS} constant
     */
    public void loadSearchResults(final WallList wallList, ItemList.StateChange stateChange) {
        Call<ImmutableWallList> call = mService.getSearchWallList(stateChange.getPageSize(),
                stateChange.getQuery(), stateChange.getNextPageToken());
        // Enqueue network load
        enqueueCall(call, wallList, stateChange.getListChange());
    }

    /**
     * Gets & loads (into the wall list) liked walls from DB
     * Identified by {@value LOAD_LIKED_WALL_LIST} constant
     */
    public void loadLikedWalls(final WallList list) {
        Single.fromCallable(new Callable<WallList>() {
            /**
             * Computes a result, or throws an exception if unable to do so.
             *
             * @return computed result
             * @throws Exception if unable to compute a result
             */
            @Override
            public WallList call() throws Exception {
                return DBUtils.getLikeWallList(mDb);
            }
        })
              .subscribeOn(Schedulers.io()) // Run on io thread pool
              .subscribe(new DisposableSingleObserver<WallList>() {
                  @Override
                  public void onSuccess(@NonNull final WallList wallList) {
                      if (wallList != null) {
                          // load successful
                          // Update wall list
                          list.swapList(wallList);
                          // Update the wall list's state
                          list.setState(new ItemList.StateChange(ItemList.LOADED));
                          // dispose of observer
                      }
                      dispose();
                  }

                  @Override
                  public void onError(@NonNull final Throwable e) {
                      // load unsuccessful
                      logger.e(e);
                      // Update the wall list's state (to let main frag know)
                      list.setState(new ItemList.StateChange(ItemList.ERRORED));
                      // dispose of observer
                      dispose();
                  }
              });
    }


    /**
     * Enqueue call with wall list methods' callback
     * Uses generics for item list and immutable list as they can be for walls or tags
     *
     * @param list Wall List or Tag List
     */
    public <T extends ItemList, L extends ImmutableList> void enqueueCall(Call<L> call, final T
            list, final int listChange) {
        call.enqueue(new Callback<L>() {
            @Override
            public void onResponse(Call<L> call, Response<L> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Wall list available

                    // Update next page token
                    list.setNextPageToken(response.body()
                                                  .getNextPageToken());
                    // Update wall list
                    list.applyChange(listChange, (T) response.body()
                                                             .getList());

                    logger.v("enqueueCall: successfully loaded & appended list");
                    // Update the wall list's state
                    list.setState(new ItemList.StateChange(ItemList.LOADED));
                } else {
                    // error response, no access to resource?
                    onFailure(call, new Exception("Response not successful : " + response
                            .toString()));
                }
            }

            @Override
            public void onFailure(Call<L> call, Throwable t) {
                // something went completely south (like no internet connection)
                t.printStackTrace();
                // Update the wall list's state (to let main frag know)
                list.setState(new ItemList.StateChange(ItemList.ERRORED));
            }
        });
    }

    /**
     * Pre-loads liked walls if not already so singleton LikedWallList is available for other
     * tabs rather than waiting for the {@link LikedFragment} to be created and it to trigger the
     * load
     */
    private void loadLikedWallsIntoSingleton() {
        WallList likedWallList = WallListSingleton.getInstance()
                                                  .getLikedWallList();
        // Check if not initialized and not (init) loading yet
        if (likedWallList.isUninitialized() && !likedWallList.isLoading()) {
            // Create liked list loading state
            ItemList.StateChange likedListLoadingState = new ItemList.StateChange(ItemList.LOADING);
            likedListLoadingState.setMethod(NetFragment.LOAD_LIKED_WALL_LIST);
            // subscribe to the list
            subscribeWallListStateChange(likedWallList);
            // set the state to trigger load
            likedWallList.setState(likedListLoadingState);
        }
    }


    /**
     * Activity lifecycle callbacks
     ******************************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);


        App app = (App) getActivity().getApplication();
        // Get readable db
        mDb = app.getDbHelper()
                 .getReadableDatabase();

        // Init retrofit api service with API key to call with every request
        mService = ApiClient.createService();
        // Pre-load liked walls immediately
        loadLikedWallsIntoSingleton();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Dispose of all subscriptions to prevent mem leaks
        mCompositeDisposable.clear();
    }
}
