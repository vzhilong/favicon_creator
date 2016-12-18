package com.stupid.lab.favicon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Created by vincent on 2016/12/18.
 */

public class IconGenHelper {

    public static class Builder {
        private int mColor;
        private String mLetters; // only one
        private int mIconWidth;
        private int mIconHeight;
        private int mCornerRadius;


        public Builder setColor(int color) {
            this.mColor = color;
            return this;
        }

        public Builder setLetters(String letters) {
            if (TextUtils.isEmpty(letters)) {
                throw new IllegalArgumentException("give me something !");
            }
            this.mLetters = letters.toUpperCase(Locale.US);
            return this;
        }

        public Builder setIconWidth(int iconWidth) {
            this.mIconWidth = iconWidth;
            return this;
        }

        public Builder setIconHeight(int iconHeight) {
            this.mIconHeight = iconHeight;
            return this;
        }

        public Builder setCornerRadius(int cornerRadius) {
            this.mCornerRadius = cornerRadius;
            return this;
        }
    }

    public static Bitmap genIcon(Builder builder) {
        int iconWidth = builder.mIconWidth;
        int iconHeight = builder.mIconHeight;
        Bitmap bitmap = Bitmap.createBitmap(iconWidth, iconHeight, Bitmap.Config.ARGB_8888);

        Paint bgPaint = new Paint();
        bgPaint.setColor(builder.mColor);
        bgPaint.setAntiAlias(true);

        RectF rectF = new RectF(0, 0, iconWidth, iconHeight);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRoundRect(
                rectF,
                builder.mCornerRadius, builder.mCornerRadius,
                bgPaint
        );

        String word = String.valueOf(getFirstAcceptChar(builder.mLetters));
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);

        int textSize = 1;
        Rect temp = new Rect();
        while (true) {
            textPaint.setTextSize(textSize);
            textPaint.getTextBounds(word, 0, 1, temp);
            if (temp.width() >= iconWidth * 0.5 || temp.height() >= iconHeight * 0.5) {
                break;
            } else {
                textSize++;
            }
        }

        canvas.drawText(
                word,
                iconWidth / 2,
                iconHeight / 2 - (textPaint.ascent() + textPaint.descent()) / 2,
                textPaint
        );

        return bitmap;
    }

    public static char getFirstAcceptChar(String str) {
        if (TextUtils.isEmpty(str)) {
            return 'W';
        }

        for (char c : str.toCharArray()) {
            if (c >= '0' && c <= '9') {
                return c;
            }
            if (c >= 'A' && c <= 'Z') {
                return c;
            }
            if (c >= 'a' && c <= 'z') {
                return (char) (c - ('a' - 'A'));
            }

            Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
            if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                    || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                    || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B) {
                return c;
            }
        }
        return str.charAt(0);
    }

    public static Bitmap getBitmapFromNetwork(String url) {
        HttpURLConnection conn = null;
        InputStream input = null;
        try {
            URL uri = new URL(url + "/favicon.ico");
            conn = (HttpURLConnection) uri.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setDoInput(true);
            conn.connect();
            input = conn.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
            }
        }
        return null;
    }
}
