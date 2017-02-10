package com.cow006.gui;

import android.text.TextPaint;

public class Misc {
    static public float calcTextSize(float width, float height, String text) {
        TextPaint tp = new TextPaint();
        float l = 0,
                r = width,
                eps = 1e-5f;
        while (l + eps < r) {
            float m = (l + r) / 2;
            tp.setTextSize(m);
            if (tp.getFontMetrics().bottom > height ||
                    tp.measureText(text) > width) {
                r = m;
            } else {
                l = m;
            }
        }
        return l;
    }

    static public boolean insideRect(float x, float y,
                                     float xLeft, float yTop,
                                     float xRight, float yBottom) {
        return xLeft <= x && x < xRight &&
                yTop <= y && y < yBottom;
    }
}
