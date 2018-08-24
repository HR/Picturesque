/*
 * Copyright (C) Habib Rehman (git.io/HR) 2018
 */

package co.sahr.android.picturesque.utilities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

@WorkerThread
public final class IOUtils {
    public static final String IMAGE_MIME_TYPE_PREFIX = "image/";

    public static String getCacheSize(Context context) {
        long size = 0;
        size += getDirSize(context.getCacheDir());
        size += getDirSize(context.getExternalCacheDir());
        return android.text.format.Formatter.formatFileSize(context, size);
    }

    public static String getFormattedSize(Context context, String size) {
        long longSize = Long.parseLong(size);
        return android.text.format.Formatter.formatFileSize(context, longSize);
    }

    public static String getFormattedSize(Context context, int size) {
        long longSize = (long) size;
        return android.text.format.Formatter.formatFileSize(context, longSize);
    }

    public static String getFileExtension(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        String mimeType = options.outMimeType;
        return mimeType.replace(IMAGE_MIME_TYPE_PREFIX, ".");
    }

    public static String joinPaths(String... paths) {
        return TextUtils.join("/", paths);
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists()) destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static boolean clearCache(Context context) {
        boolean res = false;
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
            res = true;
        } catch (Exception e) {
            logger.e(e);
        }
        return res;
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if permission to external storage has been granted */
    public static boolean isExternalStorageGranted(Context ctx) {
        int permissionCheck = ContextCompat.checkSelfPermission(ctx, Manifest.permission
                .WRITE_EXTERNAL_STORAGE);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals
                (state)) {
            return true;
        }
        return false;
    }

    public static boolean ensureDirExists(File dir) {
        boolean created = false;
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                created = true;
            }
        }
        return created;
    }

// Add the wallpaper to the user's gallery
//    public static boolean addToGallery(Context context, File file) {
//        // Insert the image into the MediaStore (used by gallery)
//        String insertResult = MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                file.getAbsolutePath(), file.getName(), "");
//        // Check result
//        if (!TextUtils.isEmpty(insertResult)) {
//            // Insert successful
//            logger.v("saveWallToExtStorage, MediaStore insert successful");
//        } else {
//            // Insert unsuccessful
//            logger.e("saveWallToExtStorage, MediaStore insert unsuccessful");
//        }
//    }
}
