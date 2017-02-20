package com.cow006.gui.game.card;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.cow006.gui.game.GameView;

import Backend.Game.GameConstants;

public class CardView extends ImageView {
    final private int card;
    final private GestureDetectorCompat gestureDetector;

    public CardView(Context context) {
        super(context);
        card = GameConstants.NOT_A_CARD;
        gestureDetector = null;
    }

    public CardView(Context context, GameView gameView, int card) {
        super(context);
        this.card = card;
        gestureDetector = new GestureDetectorCompat(context,
                new CardViewGestureDetector(gameView, this));
        setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            setPivotX(right - left);
            setPivotY(bottom - top);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    int getCard() {
        return card;
    }

    public void setScale(float newScale) {
        setScaleX(newScale);
        setScaleY(newScale);
    }

    public void generateCardBitmap(int width, int height) {
        setImageBitmap(CardBitmapGenerator.generateCardBitmap(card, width, height));
    }
}
