/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.utilities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.WorkerThread;

import co.sahr.android.picturesque.data.models.WallItem;
import co.sahr.android.picturesque.data.models.WallList;

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
import static co.sahr.android.picturesque.data.WallContract.WallEntry.TABLE_NAME;

/**
 * Database utilities
 * Must all run on a worker thread
 */
@WorkerThread
public class DBUtils {
    public static final String SQL_NOW_TIMESTAMP = " time('now') ";
    public static final String TAG = DBUtils.class.getSimpleName();

    public static ContentValues createWallValues(WallItem wall) {
        ContentValues wallValues = new ContentValues();
        // Create wall record
        wallValues.put(COLUMN_TIMESTAMP, SQL_NOW_TIMESTAMP);
        wallValues.put(COLUMN_WALL_ID, wall.getId());
        wallValues.put(COLUMN_NAME, wall.getName());
        wallValues.put(COLUMN_IMAGE_URI, wall.getImageUri());
        wallValues.put(COLUMN_THUMB_URI, wall.getThumbUri());
        wallValues.put(COLUMN_HEIGHT, wall.getHeight());
        wallValues.put(COLUMN_WIDTH, wall.getWidth());
        wallValues.put(COLUMN_SIZE, wall.getSize());
        wallValues.put(COLUMN_IS_LIKED, wall.getIsLiked());
        wallValues.put(COLUMN_IS_PREMIUM, wall.getIsPremium());
        wallValues.put(COLUMN_TAG_ID, wall.getTagId());
        wallValues.put(COLUMN_AUTHOR, wall.getAuthor());

        return wallValues;
    }

    public static boolean insertOrUpdateLike(SQLiteDatabase db, WallItem wall) {
        ContentValues wallLikeValues = new ContentValues();
        // Create wall record
        wallLikeValues.put(COLUMN_TIMESTAMP, SQL_NOW_TIMESTAMP);
        wallLikeValues.put(COLUMN_IS_LIKED, wall.getIsLiked());

        // WHERE wall id = the like wall id
        String whereClause = COLUMN_WALL_ID + " = ?";
        String[] whereArgs = {String.valueOf(wall.getId())};
        // Query db
        int updatedRows = db.update(TABLE_NAME, wallLikeValues, whereClause, whereArgs);

        if (updatedRows == 0) {
            // No rows updated i.e. row does not exist
            logger.i(TAG, "Wall not found with wall_id = " + wall.getId());
            // Insert and replace row if conflicts
            long insertedRowId = db.insertWithOnConflict(TABLE_NAME, null, createWallValues(wall)
                    , SQLiteDatabase.CONFLICT_REPLACE);

            logger.i(TAG, "Wall insert succeeded? " + (insertedRowId != -1));
            // If insertedRowId not -1 then insert successful
            return insertedRowId != -1;
        }

        logger.i(TAG, "Wall updated with wall_id = " + wall.getId());

        // Otherwise, row has been updated
        return true;
    }

    /**
     * Get the liked status from the database
     * Returns false if not found
     */
    public static boolean getLike(SQLiteDatabase db, Long wallId) {
        boolean result = false;
        String[] projection = {COLUMN_IS_LIKED};
        String selection = COLUMN_WALL_ID + " = ?";
        String[] selectionArgs = {String.valueOf(wallId)};

        Cursor cursor = db.query(TABLE_NAME, projection, selection, selectionArgs, null, null,
                null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            // Boolean true or false stored as 1 or 0 in SQLite, respectively
            result = intToBool(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_LIKED)));
            logger.i(TAG, "Does exist in db, liked: " + result);
        } else {
            logger.i(TAG, "Does NOT exist in db, so false");
        }

        return result;
    }

    public static boolean intToBool(int i) {
        // i can only be 1 or 0 -> true or false
        return (i > 0);
    }

    public static WallList cursorToWallList(Cursor cursor) {
        WallList wallList = new WallList();
//        int COLUMN_TIMESTAMP_INDEX = cursor.getColumnIndex(COLUMN_TIMESTAMP);
        int COLUMN_WALL_ID_INDEX = cursor.getColumnIndex(COLUMN_WALL_ID);
        int COLUMN_NAME_INDEX = cursor.getColumnIndex(COLUMN_NAME);
        int COLUMN_IMAGE_URI_INDEX = cursor.getColumnIndex(COLUMN_IMAGE_URI);
        int COLUMN_THUMB_URI_INDEX = cursor.getColumnIndex(COLUMN_THUMB_URI);
        int COLUMN_HEIGHT_INDEX = cursor.getColumnIndex(COLUMN_HEIGHT);
        int COLUMN_WIDTH_INDEX = cursor.getColumnIndex(COLUMN_WIDTH);
        int COLUMN_SIZE_INDEX = cursor.getColumnIndex(COLUMN_SIZE);
        int COLUMN_IS_LIKED_INDEX = cursor.getColumnIndex(COLUMN_IS_LIKED);
        int COLUMN_IS_PREMIUM_INDEX = cursor.getColumnIndex(COLUMN_IS_PREMIUM);
        int COLUMN_TAG_ID_INDEX = cursor.getColumnIndex(COLUMN_TAG_ID);
        int COLUMN_AUTHOR_ID_INDEX = cursor.getColumnIndex(COLUMN_AUTHOR);

        while (cursor.moveToNext()) {
            WallItem wallItem = new WallItem();
            wallItem.setId(cursor.getLong(COLUMN_WALL_ID_INDEX));
            wallItem.setName(cursor.getString(COLUMN_NAME_INDEX));
            wallItem.setTagId(cursor.getLong(COLUMN_TAG_ID_INDEX));
            wallItem.setSize(cursor.getInt(COLUMN_SIZE_INDEX));
            wallItem.setHeight(cursor.getInt(COLUMN_HEIGHT_INDEX));
            wallItem.setWidth(cursor.getInt(COLUMN_WIDTH_INDEX));
            wallItem.setThumbUri(cursor.getString(COLUMN_THUMB_URI_INDEX));
            wallItem.setImageUri(cursor.getString(COLUMN_IMAGE_URI_INDEX));
            wallItem.setIsLiked(intToBool(cursor.getInt(COLUMN_IS_LIKED_INDEX)));
            wallItem.setIsPremium(intToBool(cursor.getInt(COLUMN_IS_PREMIUM_INDEX)));
            wallItem.setAuthor(cursor.getString(COLUMN_AUTHOR_ID_INDEX));
            wallList.add(wallItem);
        }
        // Close cursor
        cursor.close();

        return wallList;
    }

    /**
     * Get all liked walls from db an convert them to a WallList
     */
    public static WallList getLikeWallList(SQLiteDatabase db) {
        String selection = COLUMN_IS_LIKED + " = ?";
        // Get all liked i.e. is_liked = 1
        String[] selectionArgs = {"1"};
        // Output is limited to a number depending on the system configurations
        String limit = "Limit 10000";
        String orderBy = COLUMN_TIMESTAMP + " DESC " + limit;

        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);

        if (cursor != null && cursor.getCount() > 0) {
            logger.i(TAG, "Found " + cursor.getCount() + " liked walls");
            return cursorToWallList(cursor);
        } else {
            logger.i(TAG, "No liked walls in database!");
        }

        return new WallList();
    }

}
