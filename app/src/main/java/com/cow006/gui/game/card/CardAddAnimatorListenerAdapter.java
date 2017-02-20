package com.cow006.gui.game.card;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

import com.cow006.gui.game.GameView;

import Backend.Player.Player;

import static Backend.Game.GameConstants.NOT_A_CARD;

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
            Player player = gameView.getPlayer();
            player.getCardsQueue().addFirst(NOT_A_CARD);
            player.updateOneTurn();
            if (!player.getBoardModificationQueue().isEmpty()) {
                gameView.runTurnAnimation();
            }
        } else {
            cardView.setVisibility(View.INVISIBLE);
        }
        super.onAnimationEnd(animation);
    }
}
