/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data.models;

public class TagList extends ItemList<TagItem> {
    /**
     * Get wall item by wall id
     * returns null if not found
     */
    public TagItem getById(Long tagId) {
        for (TagItem tag : this) {
            if (tag.getId().equals(tagId)) {
                return tag;
            }
        }
        return null;
    }

}
