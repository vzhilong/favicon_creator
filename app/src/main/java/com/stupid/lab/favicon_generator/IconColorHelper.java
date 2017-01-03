package com.stupid.lab.favicon_generator;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.SparseIntArray;

/**
 * Created by vincent on 2016/12/18.
 */

public class IconColorHelper {

    public static int getColor(Bitmap source, int defaultColor) {
        int targetWidth = 8;
        int targetHeight = 8;
        int widthRatio = source.getWidth() / targetWidth;
        int heightRatio = source.getHeight() / targetHeight;
        if (widthRatio > heightRatio) {
            targetHeight = source.getHeight() / widthRatio;
        }
        if (heightRatio > widthRatio) {
            targetWidth = source.getWidth() / heightRatio;
        }
        Bitmap scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
        if (scaled == null) {
            return defaultColor;
        }

        SparseIntArray counter = new SparseIntArray();
        for (int x = 0; x < targetWidth; x++) {
            for (int y = 0; y < targetHeight; y++) {
                int color = scaled.getPixel(x, y);
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                if (r > 230 && g > 230 && b > 230) {
                    continue;
                }

                if (r < 25 && g < 25 && b < 25) {
                    continue;
                }

                r = r - r % 16 + 8;
                g = g - g % 16 + 8;
                b = b - b % 16 + 8;

                color = Color.rgb(r, g, b);
                counter.put(color, counter.get(color, 0) + 1);
            }
        }

        int color = defaultColor;
        int colorCount = 0;
        for (int i = 0; i < counter.size(); i++) {
            if (colorCount < counter.valueAt(i)) {
                color = counter.keyAt(i);
                colorCount = counter.valueAt(i);
            }
        }

        return color;
    }


}
