package com.cow006.gui.game;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

public class Misc {
    static final Paint strokePaint;
    static final Paint cardPaints[];
    static final Paint bitmapPaint;
    static final TextPaint textPaint;
    private static final float TEXT_OFFSET = 0.05f;
    private static final float STROKE_WIDTH = 2;

    static {
        strokePaint = setupStrokePaint();
        bitmapPaint = setupBitmapPaint();
        cardPaints = setupCardPaints();
        textPaint = setupTextPaint();
    }

    private static TextPaint setupTextPaint() {
        TextPaint textPaint = new TextPaint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.BLACK);
        return textPaint;
    }

    private static Paint[] setupCardPaints() {
        Paint[] cardPaints = new Paint[5];
        int[] cardColors = {Color.GREEN, Color.BLUE, Color.YELLOW, Color.rgb(255, 165, 0), Color.RED};
        for (int i = 0; i < 5; ++i) {
            Paint pt = new Paint();
            pt.setStyle(Paint.Style.FILL_AND_STROKE);
            pt.setStrokeWidth(STROKE_WIDTH);
            cardPaints[i] = pt;
            cardPaints[i].setColor(cardColors[i]);
        }
        return cardPaints;
    }

    private static Paint setupBitmapPaint() {
        Paint bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);
        return bitmapPaint;
    }

    private static Paint setupStrokePaint() {
        Paint strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(STROKE_WIDTH);
        return strokePaint;
    }

    static Paint getCardPaint(int number) {
        if (number == 55) {
            return cardPaints[4];
        } else if (number % 10 == number / 10) {
            return cardPaints[3];
        } else if (number % 10 == 0) {
            return cardPaints[2];
        } else if (number % 5 == 0) {
            return cardPaints[1];
        } else {
            return cardPaints[0];
        }
    }

    static public float calcTextSize(float width, float height, String text) {
        TextPaint tp = new TextPaint();
        float l = 0;
        float r = width;
        float eps = 1e-5f;
        while (l + eps < r) {
            float m = (l + r) / 2;
            tp.setTextSize(m * (1 + TEXT_OFFSET));
            if (tp.getFontMetrics().bottom > height || tp.measureText(text) > width) {
                r = m;
            } else {
                l = m;
            }
        }
        return l;
    }

    static public TextPaint generateTextPaint(float width, float height, String text) {
        TextPaint textPaint = new TextPaint(Misc.textPaint);
        textPaint.setTextSize(calcTextSize(width, height, text));
        return textPaint;
    }

    static public boolean isInsideRect(float x, float y,
                                       float xLeft, float yTop,
                                       float xRight, float yBottom) {
        return xLeft <= x && x < xRight
                && yTop <= y && y < yBottom;
    }
}
