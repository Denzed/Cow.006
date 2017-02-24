package com.cow006.gui.game.card;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.cow006.gui.game.GameView;

import Backend.Game.GameConstants;
import Backend.Player.Player;

class CardViewGestureDetector extends GestureDetector.SimpleOnGestureListener {
    final GameView gameView;
    final CardView cardView;

    public CardViewGestureDetector(GameView gameView,
                                   CardView cardView) {
        this.gameView = gameView;
        this.cardView = cardView;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return ((Player) gameView.getPlayer()).getHand().contains(cardView.getCard());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        int card = cardView.getCard();
        Player player = gameView.getPlayer();
        if (player.getHand().contains(card)
                && player.getBoardModificationQueue().isEmpty()
                && player.isChoosingCardToTake()
                && card != gameView.getFocusedCard()) {
            gameView.focusCard(card);
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        int card = cardView.getCard();
        Player player = gameView.getPlayer();
        if (player.getHand().contains(card)
                && player.getBoardModificationQueue().isEmpty()
                && player.isChoosingCardToTake()
                && card != GameConstants.NOT_A_CARD) {
            player.tellCard(card);
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        int card = cardView.getCard();
        Player player = gameView.getPlayer();
        if (player.getHand().contains(card)
                && player.getBoardModificationQueue().isEmpty()
                && player.isChoosingCardToTake()) {
            gameView.dragCardFromHand(card);
        }
        super.onLongPress(event);
    }
}
