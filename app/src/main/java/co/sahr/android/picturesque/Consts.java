/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque;

public class Consts {
    // Name of the main (walls) database.
    public static final String DATABASE_NAME = "walls.db";
    public static final String WALL_ITEM_EXTRA = "WALL_ITEM_EXTRA";
    public static final String TAG_ITEM_EXTRA = "TAG_ITEM_EXTRA";
    // A Native Express ad is placed in every nth position in the RecyclerView.
    public static final int ITEMS_PER_AD = 9;
    // Threshold for the max visible walls at bottom to trigger load of next page
    public static final int LOAD_MORE_VISIBLE_THRESHOLD = 5;

    // The Native Express ad height.
    public static final int WALL_FRAGMENT_NATIVE_AD_HEIGHT = 150;
    public static final int WALL_ACTIVITY_NATIVE_AD_HEIGHT = 90;

    public static final String SKU = "be creative";
}
