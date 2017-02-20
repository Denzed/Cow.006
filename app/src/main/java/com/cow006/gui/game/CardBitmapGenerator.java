package com.cow006.gui.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.TextPaint;

import Backend.Game.GameConstants;

class CardBitmapGenerator {
    static Bitmap generateCardBitmap(int card, int width, int height) {
        Bitmap cardBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
        drawCardOnBitmap(new Canvas(cardBitmap), card, width, height);
        return cardBitmap;
    }

    static private void drawCardOnBitmap(Canvas bitmapCanvas, int card, int width, int height) {
        bitmapCanvas.drawRect(0, 0, width, height, Misc.getCardPaint(card));
        bitmapCanvas.drawRect(0, 0, width, height, Misc.strokePaint);
        float mainSquareSide = Math.min(width, height);
        drawCardNumberInRect(bitmapCanvas, card,
                (width - mainSquareSide) / 2f, (height - mainSquareSide) / 2f,
                (width + mainSquareSide) / 2f, (height + mainSquareSide) / 2f);
        float supplementarySquareSide = Math.min(width / 2f, (height - mainSquareSide) / 2f);
        drawCardNumberInRect(bitmapCanvas, card,
                0, 0,
                supplementarySquareSide, supplementarySquareSide);
        drawCardNumberInRect(bitmapCanvas, card,
                width - supplementarySquareSide, height - supplementarySquareSide,
                width, height);
    }

    static private void drawCardNumberInRect(Canvas canvas, int card,
                                             float x1, float y1,
                                             float x2, float y2) {
        TextPaint textPaint = Misc.generateTextPaint(x2 - x1, y2 - y1,
                Integer.toString(GameConstants.DECK_SIZE));
        float textHeight = textPaint.getFontMetrics().bottom;
        canvas.drawText(Integer.toString(card),
                (x1 + x2) / 2f,
                (y1 + y2) / 2f + textHeight,
                textPaint);
    }
}
