/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data.remote;

import co.sahr.android.picturesque.data.models.ImmutableTagList;
import co.sahr.android.picturesque.data.models.ImmutableWallList;
import co.sahr.android.picturesque.data.models.WallItem;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Api {
    int TAG_LIST_PAGE_SIZE = 50;
    // Default limit
    int WALL_LIST_PAGE_SIZE = 30;
    String MOST_RECENT = "-timestamp";
    String MOST_POPULAR = "-downloads";


    /**
     * Optional query params
     * Pass null to any query params that should be ignored by Retrofit for a request
     */


    /**
     * Walls requests
     ************************/

    /* Pass isPremium = null for all walls (Premium & Non-Premium) */
    @GET("walls/list")
    Call<ImmutableWallList> getWallList(@Query("limit") String pageSize, @Query("order") String
            order, @Query("is_premium") String isPremium, @Query("pageToken") String
            nextPageToken, @Query("tag_id") String id);

    @GET("walls/search")
    Call<ImmutableWallList> getSearchWallList(@Query("limit") String pageSize, @Query("q") String
            query, @Query("pageToken") String nextPageToken);

    // Increment wall downloads number
    @GET("walls/{id}/download")
    Call<WallItem> updateWallDownloads(@Path("id") String id);


    /**
     * Tags requests
     ************************/

    // Get alphabetically ordered tag list
    // Keep tag list page size high so can get all the finite tags at once
    @GET("tags/list?limit=" + TAG_LIST_PAGE_SIZE + "&order=name")
    Call<ImmutableTagList> getTagList();

}
