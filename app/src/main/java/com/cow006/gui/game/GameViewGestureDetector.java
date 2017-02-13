package com.cow006.gui.game;

import android.view.GestureDetector;
import android.view.MotionEvent;

import Backend.GameConstants;

class GameViewGestureDetector extends GestureDetector.SimpleOnGestureListener {
    GameView gameView;

    public GameViewGestureDetector(GameView gameView) {
        this.gameView = gameView;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        float x = event.getX(),
              y = event.getY(),
              cardWidth = gameView.cardWidth,
              cardHeight = gameView.cardHeight,
              fieldsOffsetInCards = gameView.fieldsOffsetInCards;
        if (gameView.player.isChoosingRowToTake()) {
            float paddingTop = cardHeight * fieldsOffsetInCards / 2;
            for (int i = 0; i < 4; ++i) {
                float paddingLeft = cardWidth * fieldsOffsetInCards / 2,
                      paddingRight = paddingLeft +
                              4 * cardWidth * (1 + fieldsOffsetInCards / 2) -
                              cardWidth * fieldsOffsetInCards / 4,
                      paddingBottom = paddingTop + cardHeight * (1 + fieldsOffsetInCards / 4);
                if (Misc.insideRect(x, y,
                        paddingLeft, paddingTop,
                        paddingRight, paddingBottom)) {
                    gameView.player.tellRow(i);
                    gameView.postInvalidate();
                    return true;
                }
                paddingTop += cardHeight * (1 + fieldsOffsetInCards / 2);
            }
            return true;
        }
        int card = gameView.getCardFromCoordinates(x, y);
        if (gameView.player.getQueue().isEmpty() &&
                gameView.player.isChoosingCardToTake() &&
                card != gameView.focusedCard) {
            gameView.focusedCard = card;
            gameView.postInvalidate();
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        int card = gameView.getCardFromCoordinates(event.getX(), event.getY());
        if (gameView.player.getQueue().isEmpty() &&
                gameView.player.isChoosingCardToTake() &&
                card != GameConstants.NOT_A_CARD) {
            gameView.focusedCard = GameConstants.NOT_A_CARD;
            gameView.player.tellCard(card);
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        float x = event.getX(),
                y = event.getY();
        if (gameView.player.getQueue().isEmpty() && gameView.player.isChoosingCardToTake()) {
            int card = gameView.getCardFromCoordinates(x, y);
            if (card == GameConstants.NOT_A_CARD) {
                return;
            }
            gameView.dragCardFromHand(card);
        }
        super.onLongPress(event);
    }
}
