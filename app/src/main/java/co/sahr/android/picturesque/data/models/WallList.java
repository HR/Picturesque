/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data.models;

public class WallList extends ItemList<WallItem> {

    /**
     * Get wall item by wall id
     * returns null if not found
     */
    public WallItem getById(Long wallId) {
        for (WallItem wall : this) {
            if (wall.getId()
                    .equals(wallId)) {
                return wall;
            }
        }
        return null;
    }

    /**
     * Get wall item index by wall id
     * returns -1 if not found
     */
    public int indexOfById(Long wallId) {
        for (int i = 0; i < super.size(); i++) {
            if (super.get(i)
                     .getId()
                     .equals(wallId)) {
                return i;
            }
        }
        return -1;
    }

}
