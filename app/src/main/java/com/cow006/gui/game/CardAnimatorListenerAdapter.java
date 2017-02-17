package com.cow006.gui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

import Backend.GameConstants;

public class CardAnimatorListenerAdapter extends AnimatorListenerAdapter {
    GameView gameView;
    CardView cardView;
    boolean isAddCard;

    public CardAnimatorListenerAdapter(GameView gameView, CardView cardView, boolean isAddCard) {
        this.gameView = gameView;
        this.cardView = cardView;
        this.isAddCard = isAddCard;
    }

    @Override
    public void onAnimationStart(Animator animator) {
        gameView.animatedCards.add(cardView.getCard());
        super.onAnimationStart(animator);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (isAddCard) {
            gameView.player.getCardsQueue().addFirst(GameConstants.NOT_A_CARD);
            gameView.player.updateOneMove();
            gameView.drawBoard();
        } else {
            cardView.setVisibility(View.GONE);
        }
        gameView.animatedCards.remove(cardView.getCard());
        gameView.postInvalidate();
        super.onAnimationEnd(animation);
    }
}
