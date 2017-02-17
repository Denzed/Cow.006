package com.cow006.gui.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.widget.ImageView;

import Backend.GameConstants;

public class CardView extends ImageView {
    private int card;
    static private TextPaint mTextPaint;
    static private float mTextHeight;
    static private float STROKE_WIDTH = 2;
    static private Paint strokePaint,
                         cardPaints[],
                         bitmapPaint;

    private int width, height;
    private GestureDetectorCompat gestureDetector;

    static {
        // Set up paints
        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(STROKE_WIDTH);

        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);

        cardPaints = new Paint[5];
        for (int i = 0; i < 5; ++i) {
            Paint pt = new Paint();
            pt.setStyle(Paint.Style.FILL_AND_STROKE);
            pt.setStrokeWidth(STROKE_WIDTH);
            cardPaints[i] = pt;
        }
        cardPaints[0].setColor(Color.GREEN);
        cardPaints[1].setColor(Color.BLUE);
        cardPaints[2].setColor(Color.YELLOW);
        cardPaints[3].setColor(Color.rgb(255, 165, 0)); // ORANGE
        cardPaints[4].setColor(Color.RED);

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.BLACK);
    }

    static private Paint getCardPaint(int number) {
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

    public CardView(Context context, int card, float width, float height) {
        super(context);
        this.width = Math.round(width);
        this.height = Math.round(height);

        // Adjust text size to fit cards
        mTextPaint.setTextSize(Misc.calcTextSize(this.width,
                                                 this.height,
                                                 "" + GameConstants.DECK_SIZE));
        mTextHeight = mTextPaint.getFontMetrics().bottom;

        this.card = card;
        Bitmap bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(bitmap);
        tempCanvas.drawRect(0, 0, this.width, this.height, getCardPaint(card));
        tempCanvas.drawRect(0, 0, this.width, this.height, strokePaint);
        String text = Integer.toString(card);
        tempCanvas.drawText(text,
                this.width / 2f,
                this.height / 2f + mTextHeight,
                mTextPaint);
        setImageBitmap(bitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    int getCard() {
        return card;
    }

    public void setGestureDetector(GestureDetectorCompat gestureDetector) {
        this.gestureDetector = gestureDetector;
    }
}
