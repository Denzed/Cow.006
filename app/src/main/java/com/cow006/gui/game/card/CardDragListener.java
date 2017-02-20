package com.cow006.gui.game.card;

import android.view.DragEvent;
import android.view.View;

import com.cow006.gui.game.GameView;

import Backend.Player.Player;

public class CardDragListener implements View.OnDragListener {
    private static boolean isInsideRect(float x, float y,
                                        float xLeft, float yTop,
                                        float xRight, float yBottom) {
        return xLeft <= x && x < xRight && yTop <= y && y < yBottom;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        if (!(v instanceof GameView)) {
            return true;
        }
        GameView gv = (GameView) v;
        int action = event.getAction();
        int card = (Integer) event.getLocalState();
        if (action == DragEvent.ACTION_DROP) {
            float x = event.getX(),
                  y = event.getY(),
                    cardWidth = gv.getCardWidth(),
                    cardHeight = gv.getCardHeight(),
                    fieldsOffsetInCards = GameView.FIELDS_OFFSET_IN_CARDS;
            // Check that the card is dragged inside the field box, otherwise reject
            float paddingTop = cardHeight * fieldsOffsetInCards / 2,
                    paddingLeft = cardWidth * fieldsOffsetInCards / 2,
                    paddingRight = cardWidth * (5 + fieldsOffsetInCards),
                    paddingBottom = cardHeight * (4 + fieldsOffsetInCards);
            if (isInsideRect(x, y,
                    paddingLeft, paddingTop,
                    paddingRight, paddingBottom)) {
                gv.unfocusCard();
                ((Player) gv.getPlayer()).tellCard(card);
            } else {
                gv.returnCardToHand(card);
            }
        } else if (action == DragEvent.ACTION_DRAG_ENDED) {
            if (!event.getResult()) {
                gv.returnCardToHand(card);
            }
        }
        return true;
    }
}

