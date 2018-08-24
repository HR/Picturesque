/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import co.sahr.android.picturesque.BuildConfig;
import co.sahr.android.picturesque.data.TagContract.TagEntry;
import co.sahr.android.picturesque.data.WallContract.WallEntry;
import co.sahr.android.picturesque.utilities.logger;

import static co.sahr.android.picturesque.Consts.DATABASE_NAME;

public class WallDbHelper extends SQLiteOpenHelper {
    private logger logger = new logger(this);
    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     */

    public WallDbHelper(Context context) {
        super(context, DATABASE_NAME, null, BuildConfig.DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /*
         * The Strings are SQL statements to create tables that will cache the wall data, tag data
         * and store the liked walls.
         */
        final String SQL_CREATE_WALL_TABLE =

                "CREATE TABLE " + WallEntry.TABLE_NAME + " (" +

                        // Column "_ID" inherited from "BaseColumns"

                        // Column "wall_id" to primary key
                        WallEntry.COLUMN_WALL_ID        + " INTEGER PRIMARY KEY NOT NULL,"         +
                        WallEntry.COLUMN_TIMESTAMP      + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +


                        WallEntry.COLUMN_NAME           + " TEXT, "                                +
                        WallEntry.COLUMN_THUMB_URI      + " TEXT NOT NULL, "                       +
                        WallEntry.COLUMN_IMAGE_URI      + " TEXT NOT NULL, "                       +
                        WallEntry.COLUMN_AUTHOR         + " TEXT, "                                +

                        WallEntry.COLUMN_HEIGHT         + " INTEGER NOT NULL, "                    +
                        WallEntry.COLUMN_WIDTH          + " INTEGER NOT NULL, "                    +
                        WallEntry.COLUMN_SIZE           + " INTEGER NOT NULL, "                    +

                        // To mark user liked wall
                        WallEntry.COLUMN_IS_LIKED       + " BOOLEAN NOT NULL DEFAULT 0, "          +

                        // To mark premium wall
                        WallEntry.COLUMN_IS_PREMIUM     + " BOOLEAN NOT NULL, "                    +

                        // Foreign key for categories
                        WallEntry.COLUMN_TAG_ID         + " INTEGER NOT NULL, "                    +

                        " FOREIGN KEY(" + WallEntry.COLUMN_TAG_ID + ")"                            +
                        " REFERENCES " + TagEntry.TABLE_NAME + "(" + TagEntry.COLUMN_TAG_ID + ")"  +

                /*
                 * Declare wall id unique to ensure there is only one wall entry and replace it if
                 * not
                 */
                        " UNIQUE (" + WallEntry.COLUMN_WALL_ID + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_TAG_TABLE =

                "CREATE TABLE " + TagEntry.TABLE_NAME + " (" +

                        // Column "_ID" inherited from "BaseColumns", primary key for now.

                        TagEntry._ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, "   +

                        TagEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +

                        TagEntry.COLUMN_TAG_ID    + " INTEGER NOT NULL,"                     +

                        TagEntry.COLUMN_NAME      + " TEXT NOT NULL, "                       +
                        TagEntry.COLUMN_THUMB_URI + " TEXT NOT NULL, "                       +

                /*
                 * Declare tag id unique to ensure there is only one tag entry and replace it if
                 * not
                 */
                        " UNIQUE (" + TagEntry.COLUMN_TAG_ID + ") ON CONFLICT REPLACE);";


        final String SQL_ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = ON;";

        // Enable foreign keys
        sqLiteDatabase.execSQL(SQL_ENABLE_FOREIGN_KEYS);

        // Create tags table
        sqLiteDatabase.execSQL(SQL_CREATE_TAG_TABLE);

        // Create walls table
        sqLiteDatabase.execSQL(SQL_CREATE_WALL_TABLE);
    }

    /**
     * This database is only a cache for online data, so its upgrade policy is simply to discard
     * the data and call through to onCreate to recreate the table. Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        logger.i("onUpgrade, called, old: " + oldVersion + ", new: " + newVersion);
        // Cleanup: delete all unliked walls
        Boolean success = sqLiteDatabase.delete(WallEntry.TABLE_NAME, WallEntry.COLUMN_IS_LIKED+" = 0", null) > 0;
        logger.i("Deleted, all unliked walls successfully? " + success);
        // Drop tag table and create a new one
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TagEntry.TABLE_NAME);
//        onCreate(sqLiteDatabase);
    }
}
