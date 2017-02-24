package com.cow006.gui.game.card;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

import com.cow006.gui.game.GameView;

public class CardDragShadowBuilder extends View.DragShadowBuilder {
    public CardDragShadowBuilder(CardView cardView) {
        super(cardView);
        cardView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
        View v = getView();
        final int width = Math.round(v.getWidth() * GameView.FOCUSED_ZOOM);
        final int height = Math.round(v.getHeight() * GameView.FOCUSED_ZOOM);
        shadowSize.set(width, height);
        shadowTouchPoint.set(width / 2, height / 2);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        canvas.scale(GameView.FOCUSED_ZOOM, GameView.FOCUSED_ZOOM);
        getView().draw(canvas);
    }
}
