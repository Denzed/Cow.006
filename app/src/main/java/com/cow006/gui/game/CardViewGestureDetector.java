package com.cow006.gui.game;

import android.view.GestureDetector;
import android.view.MotionEvent;

import Backend.GameConstants;

class CardViewGestureDetector extends GestureDetector.SimpleOnGestureListener {
    GameView gameView;
    CardView cardView;

    public CardViewGestureDetector(GameView gameView,
                                   CardView cardView) {
        this.gameView = gameView;
        this.cardView = cardView;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return gameView.player.getHand().contains(cardView.getCard());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        int card = cardView.getCard();
        if (gameView.player.getHand().contains(card)
                && gameView.player.getQueue().isEmpty()
                && gameView.player.isChoosingCardToTake()
                && card != gameView.focusedCard) {
            gameView.focusCard(card);
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        int card = cardView.getCard();
        if (gameView.player.getHand().contains(card)
                && gameView.player.getQueue().isEmpty()
                && gameView.player.isChoosingCardToTake()
                && card != GameConstants.NOT_A_CARD) {
            gameView.player.tellCard(card);
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        int card = cardView.getCard();
        if (gameView.player.getHand().contains(card)
                && gameView.player.getQueue().isEmpty()
                && gameView.player.isChoosingCardToTake()) {
            gameView.focusCard(card);
            gameView.dragCardFromHand(card);
        }
        super.onLongPress(event);
    }
}