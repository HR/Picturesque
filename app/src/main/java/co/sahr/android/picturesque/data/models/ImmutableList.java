/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data.models;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ImmutableList<T> implements Serializable {
    @SerializedName("items")
    @Expose
    protected T list;

    @SerializedName("nextPageToken")
    @Expose
    @Nullable
    private String nextPageToken;

    // Subclass has to pass a new instance of T from its constructor to super constructor (this)
    public ImmutableList(T instance) {
        this.list = instance;
    }

    // Alternatively, subclass can pass T's class reference to super constructor (this)
    //    public ImmutableList(Class<T> clazz) {
    //        try {
    //            this.list = clazz.newInstance();
    //        } catch (Exception e) {
    //            logger.e(e);
    //        }
    //    }

    public T getList() {
        return this.list;
    }

    public void setList(T list) {
        this.list = list;
    }

    @Nullable
    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(@Nullable final String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}
