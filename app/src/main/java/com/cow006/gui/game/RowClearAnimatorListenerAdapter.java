package com.cow006.gui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import Backend.GameConstants;

public class RowClearAnimatorListenerAdapter extends AnimatorListenerAdapter {
    GameView gameView;

    public RowClearAnimatorListenerAdapter(GameView gameView) {
        this.gameView = gameView;
    }

    @Override
    public void onAnimationStart(Animator animator) {
        super.onAnimationStart(animator);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        gameView.player.getCardsQueue().addFirst(GameConstants.NOT_A_CARD);
        gameView.player.updateOneMove();
        gameView.setupAnimations();
    }
}
