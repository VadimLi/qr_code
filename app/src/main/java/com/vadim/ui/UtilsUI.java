package com.vadim.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Filter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class UtilsUI {
    private static final String TAG = UtilsUI.class.getSimpleName();
    private final static int QUALITY = 100;

    private final Context context;

    public UtilsUI(final Context context) {
        this.context = context;
    }

    public Bitmap convertFileToBitmap(File file) {
        Bitmap originalBitmap = null;
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            originalBitmap = BitmapFactory.decodeStream(is);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return originalBitmap;
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, QUALITY, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public interface UtilsFragment {
        void filter(String text, Filter.FilterListener filterListener) ;
        void displayEmptyListView();
        void displayListView();
    }

}
