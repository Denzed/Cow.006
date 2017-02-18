package com.cow006.gui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cow006.gui.R;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import Backend.AbstractPlayer;
import Backend.GameConstants;

public class GameView extends FrameLayout {
    protected static final float CARD_COEFFICIENT = 0.16f;
    protected static final float FIELDS_OFFSET_IN_CARDS = 0.5f - CARD_COEFFICIENT * 11 / 4;
    protected static final float FOCUSED_ZOOM = 2 * FIELDS_OFFSET_IN_CARDS / CARD_COEFFICIENT + 1;
    protected static final float QUEUE_CARD_SCALE = 0.5f;
    private static final long ANIMATION_LENGTH = 200;
    GameActivity parentActivity;
    float cardWidth;
    float cardHeight;
    LinkedHashSet<Integer> animatedCards = new LinkedHashSet<>();
    int focusedCard = GameConstants.NOT_A_CARD;
    LocalPlayer player = null;
    private CardView cardViews[];

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        calcCardSizeBasedOnScreenResolution(context);
        generateCardViews();
        setOnDragListener(new CardDragListener());
        setWillNotDraw(false);
    }

    private void generateCardViews() {
        cardViews = new CardView[GameConstants.DECK_SIZE];
        for (int card = 1; card <= GameConstants.DECK_SIZE; ++card) {
            cardViews[card - 1] = new CardView(parentActivity, this,
                    card,
                    cardWidth, cardHeight);
            this.addView(cardViews[card - 1]);
        }
    }

    private void calcCardSizeBasedOnScreenResolution(Context context) {
        Point size = new Point(160, 90);
        if (context instanceof GameActivity) {
            Display display;
            parentActivity = (GameActivity) context;
            display = parentActivity.getWindowManager().getDefaultDisplay();
            display.getSize(size);
        }
        cardWidth = size.x * CARD_COEFFICIENT;
        cardHeight = size.y * CARD_COEFFICIENT;
    }

    protected void setPlayer(LocalPlayer p) {
        player = p;
    }

    private float[] getQueueTopPosition() {
        return new float[]{getWidth() - cardWidth * (1 + FIELDS_OFFSET_IN_CARDS / 2),
                cardHeight * FIELDS_OFFSET_IN_CARDS / 2};
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
            cardWidth = CARD_COEFFICIENT * (right - left);
            cardHeight = CARD_COEFFICIENT * (bottom - top);
            for (CardView cardView : cardViews) {
                cardView.resize(cardWidth, cardHeight);
            }
        }
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
        float zoom = (card == focusedCard ? FOCUSED_ZOOM : 1) * scale;
        float zoomedWidth = zoom * cardWidth;
        float zoomedHeight = zoom * cardHeight;
        final int x1 = Math.round(paddingLeft - (zoomedWidth - cardWidth));
        final int y1 = Math.round(paddingTop - (zoomedHeight - cardHeight));
        final int x2 = x1 + Math.round(zoomedWidth);
        final int y2 = y1 + Math.round(zoomedHeight);
        parentActivity.runOnUiThread(() -> {
            cardViews[card - 1].setVisibility(View.VISIBLE);
            cardViews[card - 1].layout(x1, y1, x2, y2);
        });
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
        float scaledWidth = cardWidth * QUEUE_CARD_SCALE;
        float scaledHeight = cardHeight * QUEUE_CARD_SCALE;
        float paddingRight = getWidth() - scaledWidth * FIELDS_OFFSET_IN_CARDS / 2;
        float paddingBottom = scaledHeight * (1 + FIELDS_OFFSET_IN_CARDS / 2);
        for (int card: player.getCardsQueue()) {
            drawCard(paddingRight - cardWidth, paddingBottom - cardHeight,
                    card, QUEUE_CARD_SCALE);
            paddingBottom += scaledHeight * (1 + FIELDS_OFFSET_IN_CARDS / 2);
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
        if (player == null || player.getState() == AbstractPlayer.GameState.NEW_GAME) {
            return;
        }
        updateCards();
        drawRowChooser(canvas);
        if (!player.getQueue().isEmpty()
                && !player.isChoosingRowToTake()
                && animatedCards.isEmpty()) {
            setupAnimations();
        }
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
        cardViews[card - 1].setVisibility(View.GONE);
    }

    private Animator animateCardTranslation(int card,
                                            float[] p1, float[] p2,
                                            AnimatorListenerAdapter listenerAdapter) {
        ObjectAnimator animator = ObjectAnimator.ofMultiFloat(this, "", new float[][] {p1, p2});
        animator.addListener(listenerAdapter);
        animator.setDuration(ANIMATION_LENGTH);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener((ValueAnimator animation) -> {
            float[] animatedValue = (float[]) animation.getAnimatedValue();
            drawCard(animatedValue[0], animatedValue[1], card, 1);
        });
        return animator;
    }

    private void setupAnimations() {
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
    }

    private void setupCardAddAnimation(int card, int row, int column) {
        player.getCardsQueue().poll();
        animateCardTranslation(card,
                getQueueTopPosition(), getFieldCellPosition(row, column),
                new CardAnimatorListenerAdapter(this, cardViews[card - 1], true)).start();
    }

    private void setupRowClearAnimation(int row) {
        AnimatorSet animatorSet = new AnimatorSet();
        LinkedList<Animator> animators = new LinkedList<>();
        float[] outsideTheField = {-cardWidth, getFieldCellPosition(row, 0)[1]};
        int col = 0;
        for (int card : player.getBoard().get(row)) {
            animators.add(animateCardTranslation(card,
                    getFieldCellPosition(row, col), outsideTheField,
                    new CardAnimatorListenerAdapter(this, cardViews[card - 1], false)));
            col++;
        }
        player.getCardsQueue().addFirst(GameConstants.NOT_A_CARD);
        player.updateOneMove();
        animatorSet.playTogether(animators);
        animatorSet.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (player != null && player.isChoosingRowToTake()) {
            float paddingTop = cardHeight * FIELDS_OFFSET_IN_CARDS / 2;
            float paddingLeft = cardWidth * FIELDS_OFFSET_IN_CARDS / 2;
            float paddingRight = paddingLeft
                    + 4 * cardWidth * (1 + FIELDS_OFFSET_IN_CARDS / 2)
                    - cardWidth * FIELDS_OFFSET_IN_CARDS / 4;
            for (int i = 0; i < 4; ++i) {
                float paddingBottom = paddingTop + cardHeight * (1 + FIELDS_OFFSET_IN_CARDS / 4);
                if (Misc.isInsideRect(event.getX(), event.getY(),
                        paddingLeft, paddingTop,
                        paddingRight, paddingBottom)) {
                    player.tellRow(i);
                    invalidate();
                    return true;
                }
                paddingTop += cardHeight * (1 + FIELDS_OFFSET_IN_CARDS / 2);
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