/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data;

import co.sahr.android.picturesque.data.models.TagList;
import co.sahr.android.picturesque.data.models.WallList;

public class WallListSingleton {
    private static final WallListSingleton ourInstance = new WallListSingleton();

    public static WallListSingleton getInstance() {
        return ourInstance;
    }

    private WallList likedWallList;
    private TagList tagList;

    private WallListSingleton() {
        this.likedWallList = new WallList();
        this.tagList = new TagList();
    }

    public WallList getLikedWallList() {
        return this.likedWallList;
    }

    public void setLikedWallList(WallList list) {
        this.likedWallList = list;
    }

    public TagList getTagList() {
        return tagList;
    }

    public void setTagList(final TagList list) {
        this.tagList = list;
    }
}
