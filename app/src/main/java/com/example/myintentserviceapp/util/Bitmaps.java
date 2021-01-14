package com.example.myintentserviceapp.util;

import android.graphics.BitmapFactory;

import java.io.InputStream;

public class Bitmaps {
    // The new size we want to scale to
    static final int REQUIRED_SIZE = 300;

    // Decodes image and scales it to reduce memory consumption
    public android.graphics.Bitmap decodeFile(InputStream f) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(f, null, o);


        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(f, null, o2);
    }

    public int calcScaleInSampleSize(InputStream f) {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(f, null, o);

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }

        return scale;
    }
}
