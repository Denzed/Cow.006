package com.cow006.gui.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import Backend.GameConstants;

public class CardView extends ImageView {
    private int card;
    private int width;
    private int height;
    private GestureDetectorCompat gestureDetector;

    public CardView(Context context, GameView gameView, int card,
                    float width, float height) {
        super(context);
        this.card = card;
        this.width = Math.round(width);
        this.height = Math.round(height);
        setImageBitmap(drawCardBitmap());

        gestureDetector = new GestureDetectorCompat(context,
                new CardViewGestureDetector(gameView, this));
        setVisibility(View.GONE);
    }

    private Bitmap drawCardBitmap() {
        Bitmap cardBitmap = Bitmap.createBitmap(this.width, this.height,
                Bitmap.Config.RGB_565);
        drawCardOnBitmap(new Canvas(cardBitmap));
        return cardBitmap;
    }

    private void drawCardOnBitmap(Canvas bitmapCanvas) {
        bitmapCanvas.drawRect(0, 0, this.width, this.height, Misc.getCardPaint(card));
        bitmapCanvas.drawRect(0, 0, this.width, this.height, Misc.strokePaint);
        float mainSquareSide = Math.min(this.width, this.height);
        drawCardNumberInRect(bitmapCanvas,
                (this.width - mainSquareSide) / 2f, (this.height - mainSquareSide) / 2f,
                (this.width + mainSquareSide) / 2f, (this.height + mainSquareSide) / 2f);
        float supplementarySquareSide = Math.min(this.width / 2f, (this.height - mainSquareSide) / 2f);
        drawCardNumberInRect(bitmapCanvas, 0, 0, supplementarySquareSide, supplementarySquareSide);
        drawCardNumberInRect(bitmapCanvas,
                this.width - supplementarySquareSide, this.height - supplementarySquareSide,
                this.width, this.height);
    }

    private void drawCardNumberInRect(Canvas canvas, float x1, float y1, float x2, float y2) {
        TextPaint textPaint = Misc.generateTextPaint(x2 - x1, y2 - y1,
                Integer.toString(GameConstants.DECK_SIZE));
        float textHeight = textPaint.getFontMetrics().bottom;
        canvas.drawText(Integer.toString(this.card),
                (x1 + x2) / 2f,
                (y1 + y2) / 2f + textHeight,
                textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    int getCard() {
        return card;
    }

    public void resize(float newWidth, float newHeight) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        int deltaX = Math.round(newWidth - width);
        params.leftMargin -= deltaX;
        params.width += deltaX;
        int deltaY = Math.round(newHeight - height);
        params.topMargin -= deltaY;
        params.height += deltaY;
    }
}
