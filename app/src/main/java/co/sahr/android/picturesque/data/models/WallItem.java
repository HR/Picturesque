/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data.models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_AUTHOR;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_HEIGHT;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_IMAGE_URI;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_IS_LIKED;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_IS_PREMIUM;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_NAME;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_SIZE;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_TAG_ID;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_THUMB_URI;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_TIMESTAMP;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_WALL_ID;
import static co.sahr.android.picturesque.data.WallContract.WallEntry.COLUMN_WIDTH;

public class WallItem implements Serializable {
    @SerializedName(COLUMN_WALL_ID)
    @Expose
    @NonNull
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

    @SerializedName(COLUMN_IMAGE_URI)
    @Expose
    private String imageUri;

    @SerializedName(COLUMN_HEIGHT)
    @Expose
    private Integer height;

    @SerializedName(COLUMN_WIDTH)
    @Expose
    private Integer width;

    @SerializedName(COLUMN_SIZE)
    @Expose
    private Integer size;

    @SerializedName(COLUMN_IS_PREMIUM)
    @Expose
    private Boolean isPremium;

    @SerializedName(COLUMN_IS_LIKED)
    private Boolean isLiked;

    @SerializedName(COLUMN_TAG_ID)
    @Expose
    @NonNull
    private Long tagId;

    @SerializedName(COLUMN_AUTHOR)
    @Expose
    private String author;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(String thumbUri) {
        this.thumbUri = thumbUri;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(Boolean isPremium) {
        this.isPremium = isPremium;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public Boolean getIsLiked() {
        // Return false if not set (default value)
        return (isLiked == null) ? false : isLiked;
    }

    public void setIsLiked(final Boolean isLiked) {
        this.isLiked = isLiked;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }
}