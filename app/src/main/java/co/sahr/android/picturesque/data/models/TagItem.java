/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import static co.sahr.android.picturesque.data.TagContract.TagEntry.COLUMN_NAME;
import static co.sahr.android.picturesque.data.TagContract.TagEntry.COLUMN_TAG_ID;
import static co.sahr.android.picturesque.data.TagContract.TagEntry.COLUMN_THUMB_URI;
import static co.sahr.android.picturesque.data.TagContract.TagEntry.COLUMN_TIMESTAMP;

public class TagItem implements Serializable {
    @SerializedName(COLUMN_TAG_ID)
    @Expose
    // Long/long for primary key
    private Long id;

    @SerializedName(COLUMN_TIMESTAMP)
    @Expose
    private String timestamp;

    @SerializedName(COLUMN_NAME)
    @Expose
    private String name;

    @SerializedName(COLUMN_THUMB_URI)
    @Expose
    private String thumbUri;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(final String thumbUri) {
        this.thumbUri = thumbUri;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }
}