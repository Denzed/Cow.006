package com.cow006.gui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cow006.gui.R;

import java.util.ArrayList;
import java.util.LinkedList;

import Backend.AbstractPlayer;
import Backend.GameConstants;

public class GameView extends FrameLayout {
    protected static final float CARD_COEFFICIENT = 0.16f;
    protected static final float FIELDS_OFFSET_IN_CARDS = 0.5f - CARD_COEFFICIENT * 11 / 4;
    protected static final float FOCUSED_ZOOM = 2 * FIELDS_OFFSET_IN_CARDS / CARD_COEFFICIENT + 1;
    protected static final float QUEUE_CARD_SCALE = 0.5f;
    private static final long ANIMATION_LENGTH = 300;
    GameActivity parentActivity;
    int cardWidth;
    int cardHeight;
    int focusedCard = GameConstants.NOT_A_CARD;
    LocalPlayer player = null;
    private CardView cardViews[];

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parentActivity = (GameActivity) context;
        generateCardViews();
        setOnDragListener(new CardDragListener());
        setWillNotDraw(false);
    }

    private void generateCardViews() {
        cardViews = new CardView[GameConstants.DECK_SIZE];
        for (int card = 1; card <= GameConstants.DECK_SIZE; ++card) {
            cardViews[card - 1] = new CardView(parentActivity, this, card);
            this.addView(cardViews[card - 1]);
        }
    }

    private void generateCardBitmaps() {
        for (CardView cardView : cardViews) {
            cardView.setImageBitmap(CardBitmapGenerator.generateCardBitmap(cardView.getCard(),
                    cardWidth,
                    cardHeight));
        }
    }

    protected void setPlayer(LocalPlayer p) {
        player = p;
    }

    private float[] getQueueTopPosition() {
        float scaledWidth = cardWidth * QUEUE_CARD_SCALE;
        float scaledHeight = cardHeight * QUEUE_CARD_SCALE;
        float paddingRight = getWidth() - scaledWidth * FIELDS_OFFSET_IN_CARDS / 2;
        float paddingBottom = scaledHeight * (1 + FIELDS_OFFSET_IN_CARDS / 2);
        return new float[]{paddingRight - cardWidth, paddingBottom - cardHeight};
    }

    private float[] getFieldCellPosition(int row, int column) {
        return new float[]{cardWidth * FIELDS_OFFSET_IN_CARDS / 2
                + column * cardWidth * (1 + FIELDS_OFFSET_IN_CARDS / 2),
                cardHeight * FIELDS_OFFSET_IN_CARDS / 2
                        + row * cardHeight * (1 + FIELDS_OFFSET_IN_CARDS / 2)};
    }

    void returnCardToHand(int card) {
        ArrayList<Integer> hand = player.getHand();
        int index = 0;
        while (index < hand.size() && hand.get(index) < card) {
            index++;
        }
        player.getHand().add(index, card);
        unfocusCard();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            cardWidth = Math.round(CARD_COEFFICIENT * (right - left));
            cardHeight = Math.round(CARD_COEFFICIENT * (bottom - top));
            generateCardBitmaps();
            for (CardView card : cardViews) {
                LayoutParams params = (LayoutParams) card.getLayoutParams();
                params.width = cardWidth;
                params.height = cardHeight;
            }
        }
        updateCards();
    }

    protected void drawScores() {
        TextView scoresView = (TextView) parentActivity.findViewById(R.id.game_scores);
        String scores = "SCORES: " + player.getScoresAsString();
        scoresView.setText(scores);
    }

    protected void drawCard(float paddingLeft,
                            float paddingTop,
                            int card,
                            float scale) {
        cardViews[card - 1].setScale((card == focusedCard ? FOCUSED_ZOOM : 1) * scale);
        cardViews[card - 1].setX(paddingLeft);
        cardViews[card - 1].setY(paddingTop);
        parentActivity.runOnUiThread(() -> cardViews[card - 1].setVisibility(View.VISIBLE));
    }

    protected void drawHand() {
        int n = player.getHand().size();
        float paddingLeft = (getWidth() - cardWidth * (n + 1) / 2) / 2;
        float paddingTop = getHeight() - cardHeight * (1 + FIELDS_OFFSET_IN_CARDS);
        for (int card: player.getHand()) {
            drawCard(paddingLeft, paddingTop, card, 1);
            paddingLeft += cardWidth / 2;
        }
    }

    protected void drawQueue() {
        float scaledHeight = cardHeight * QUEUE_CARD_SCALE;
        float[] paddingLeftTop = getQueueTopPosition();
        for (int card: player.getCardsQueue()) {
            drawCard(paddingLeftTop[0], paddingLeftTop[1],
                    card, QUEUE_CARD_SCALE);
            paddingLeftTop[1] += scaledHeight * (1 + FIELDS_OFFSET_IN_CARDS / 2);
        }
    }

    protected void drawBoard() {
        float paddingTop = cardHeight * FIELDS_OFFSET_IN_CARDS / 2;
        for (ArrayList<Integer> row: player.getBoard()) {
            float paddingLeft = cardWidth * FIELDS_OFFSET_IN_CARDS / 2;
            for (int card: row) {
                drawCard(paddingLeft, paddingTop, card, 1);
                paddingLeft += cardWidth * (1 + FIELDS_OFFSET_IN_CARDS / 2);
            }
            paddingTop += cardHeight * (1 + FIELDS_OFFSET_IN_CARDS / 2);
        }
    }

    protected void drawRowChooser(Canvas canvas) {
        float paddingTop = cardHeight * FIELDS_OFFSET_IN_CARDS / 2;
        float paddingLeft = cardWidth * FIELDS_OFFSET_IN_CARDS / 2;
        float paddingRight = paddingLeft + 5 * cardWidth * (1 + FIELDS_OFFSET_IN_CARDS / 2)
                - cardWidth * FIELDS_OFFSET_IN_CARDS / 4;
        for (int i = 0; i < GameConstants.ROWS; i++) {
            if (player.isChoosingRowToTake()) {
                canvas.drawRect(paddingLeft / 2,
                        paddingTop,
                        paddingRight,
                        paddingTop + cardHeight * (1 + FIELDS_OFFSET_IN_CARDS / 4),
                        Misc.strokePaint);

            }
            paddingTop += cardHeight * (1 + FIELDS_OFFSET_IN_CARDS / 2);
        }
    }

    protected void drawMessageWithAction(String message,
                                         DialogInterface.OnClickListener action) {
        parentActivity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(message)
                    .setNeutralButton("OK", action)
                    .create()
                    .show();
        });
    }

    protected void drawMessage(String message) {
        drawMessageWithAction(message, (DialogInterface dialog, int which) -> {/* do nothing */});
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawRowChooser(canvas);
    }

    public void dragCardFromHand(int card) {
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(cardViews[card - 1]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startDragAndDrop(data, shadowBuilder, card, 0);
        } else {
            startDrag(data, shadowBuilder, card, 0);
        }
        player.getHand().remove(Integer.valueOf(card));
        drawHand();
        cardViews[card - 1].setVisibility(View.INVISIBLE);
    }

    private ObjectAnimator animateCardTranslation(int card,
                                                  float[] p1, float[] p2,
                                                  AnimatorListenerAdapter listenerAdapter) {
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(cardViews[card - 1],
                PropertyValuesHolder.ofFloat("scaleX", cardViews[card - 1].getScaleX(), 1),
                PropertyValuesHolder.ofFloat("scaleY", cardViews[card - 1].getScaleY(), 1),
                PropertyValuesHolder.ofFloat("x", p1[0], p2[0]),
                PropertyValuesHolder.ofFloat("y", p1[1], p2[1]));
        animator.addListener(listenerAdapter);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator.setDuration(ANIMATION_LENGTH);
    }

    void setupAnimations() {
        parentActivity.runOnUiThread(() -> {
            AbstractPlayer.Move move = player.getQueue().peek();
            int row = move.rowIndex;
            int column = (move.type == AbstractPlayer.updateStateTypes.CLEAR_ROW
                    ? 0
                    : player.getBoard().get(row).size());
            if (move.type == AbstractPlayer.updateStateTypes.CLEAR_ROW) {
                setupRowClearAnimation(row);
            } else {
                setupCardAddAnimation(move.card, row, column);
            }
        });
    }

    private void setupCardAddAnimation(int card, int row, int column) {
        cardViews[card - 1].bringToFront();
        ObjectAnimator animator = animateCardTranslation(card,
                getQueueTopPosition(), getFieldCellPosition(row, column),
                new CardAddAnimatorListenerAdapter(this, cardViews[card - 1], true));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                player.getCardsQueue().poll();
            }
        });
        animator.start();
    }

    private void setupRowClearAnimation(int row) {
        AnimatorSet animatorSet = new AnimatorSet();
        LinkedList<Animator> animators = new LinkedList<>();
        float[] outsideTheField = {-cardWidth, getFieldCellPosition(row, 0)[1]};
        for (int column = 0;
             column < Math.min(player.getBoard().get(row).size(), GameConstants.COLUMNS);
             column++) {
            int card = player.getBoard().get(row).get(column);
            animators.add(animateCardTranslation(card,
                    getFieldCellPosition(row, column), outsideTheField,
                    new CardAddAnimatorListenerAdapter(this, cardViews[card - 1], false)));
        }
        animatorSet.addListener(new RowClearAnimatorListenerAdapter(this));
        animatorSet.playTogether(animators);
        animatorSet.start();
    }

    private boolean isInsideRow(int row, float x, float y) {
        float paddingTop = cardHeight * FIELDS_OFFSET_IN_CARDS / 2
                + row * cardHeight * (1 + FIELDS_OFFSET_IN_CARDS / 4);
        float paddingLeft = cardWidth * FIELDS_OFFSET_IN_CARDS / 2;
        float paddingRight = paddingLeft
                + 4 * cardWidth * (1 + FIELDS_OFFSET_IN_CARDS / 2)
                - cardWidth * FIELDS_OFFSET_IN_CARDS / 4;
        float paddingBottom = paddingTop + cardHeight * (1 + FIELDS_OFFSET_IN_CARDS / 4);
        return paddingLeft <= x && x < paddingRight
                && paddingTop <= y && y < paddingBottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (player != null && player.isChoosingRowToTake()) {
            for (int row = 0; row < 4; ++row) {
                if (isInsideRow(row, event.getX(), event.getY())) {
                    player.tellRow(row);
                    invalidate();
                    return true;
                }
            }
        } else {
            unfocusCard();
            return true;
        }
        return false;
    }

    public void focusCard(int card) {
        focusedCard = card;
        drawHand();
    }

    public void unfocusCard() {
        focusCard(GameConstants.NOT_A_CARD);
    }

    public void updateCards() {
        drawScores();
        drawBoard();
        drawHand();
        drawQueue();
    }
}