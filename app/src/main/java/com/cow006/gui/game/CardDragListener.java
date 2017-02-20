package com.cow006.gui.game;

import android.view.DragEvent;
import android.view.View;

import Backend.Game.GameConstants;

public class CardDragListener implements View.OnDragListener {
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
                  cardWidth = gv.cardWidth,
                  cardHeight = gv.cardHeight,
                    fieldsOffsetInCards = GameView.FIELDS_OFFSET_IN_CARDS;
            // Check that the card is dragged inside the field box, otherwise reject
            float paddingTop = cardHeight * fieldsOffsetInCards / 2,
                    paddingLeft = cardWidth * fieldsOffsetInCards / 2,
                    paddingRight = cardWidth * (5 + fieldsOffsetInCards),
                    paddingBottom = cardHeight * (4 + fieldsOffsetInCards);
            if (Misc.isInsideRect(x, y,
                    paddingLeft, paddingTop,
                    paddingRight, paddingBottom)) {
                gv.focusedCard = GameConstants.NOT_A_CARD;
                gv.player.tellCard(card);
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

