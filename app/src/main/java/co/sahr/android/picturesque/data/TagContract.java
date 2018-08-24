/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data;

import android.provider.BaseColumns;

public class TagContract {
    /* Inner class that defines the table contents of the wall table */
    public static final class TagEntry implements BaseColumns {

        // Used internally as the name of wall table
        public static final String TABLE_NAME = "tags";

        // Last updated timestamp
        public static final String COLUMN_TIMESTAMP = "timestamp";

        // Wall ID as returned by API, used to identify the icon to be used
        public static final String COLUMN_TAG_ID = "id";


        /* METADATA */

        // Tag name
        public static final String COLUMN_NAME = "name";
        // Tag thumbnail
        public static final String COLUMN_THUMB_URI = "thumb_uri";



    }
}
