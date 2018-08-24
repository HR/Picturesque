/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data.models;

public class ImmutableWallList extends ImmutableList<WallList> {

    public ImmutableWallList() {
        super(new WallList());
    }
}
