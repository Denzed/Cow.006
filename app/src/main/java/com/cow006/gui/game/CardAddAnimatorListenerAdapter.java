package com.cow006.gui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

import Backend.GameConstants;

public class CardAddAnimatorListenerAdapter extends AnimatorListenerAdapter {
    GameView gameView;
    CardView cardView;
    boolean isAddCard;

    public CardAddAnimatorListenerAdapter(GameView gameView, CardView cardView, boolean isAddCard) {
        this.gameView = gameView;
        this.cardView = cardView;
        this.isAddCard = isAddCard;
    }

    @Override
    public void onAnimationStart(Animator animator) {
        super.onAnimationStart(animator);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (isAddCard) {
            gameView.player.getCardsQueue().addFirst(GameConstants.NOT_A_CARD);
            gameView.player.updateOneMove();
            if (!gameView.player.getQueue().isEmpty()) {
                gameView.setupAnimations();
            }
        } else {
            cardView.setVisibility(View.INVISIBLE);
        }
        super.onAnimationEnd(animation);
    }
}
