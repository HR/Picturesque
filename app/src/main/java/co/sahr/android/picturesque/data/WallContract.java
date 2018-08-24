/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data;

import android.provider.BaseColumns;

public class WallContract {

    /* Inner class that defines the table contents of the wall table */
    public static final class WallEntry implements BaseColumns {

        // Used internally as the name of wall table
        public static final String TABLE_NAME = "walls";

        // Added timestamp
        public static final String COLUMN_TIMESTAMP = "timestamp";

        // Wall ID as returned by API, used to identify the icon to be used
        public static final String COLUMN_WALL_ID = "id";


        /* METADATA */

        // Wall name
        public static final String COLUMN_NAME = "name";
        // Wall URIs
        public static final String COLUMN_THUMB_URI = "thumb_uri";
        public static final String COLUMN_IMAGE_URI = "image_uri";

        // Image resolution attrs
        public static final String COLUMN_HEIGHT = "height";
        public static final String COLUMN_WIDTH = "width";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_AUTHOR = "author";

        // Foreign key for categories
        public static final String COLUMN_TAG_ID = "tag_id";

        /* flags */
        // To mark user liked wall
        public static final String COLUMN_IS_LIKED = "is_liked";
        public static final String COLUMN_IS_PREMIUM = "is_premium";

    }
}
