package com.cow006.gui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextPaint;
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
    private static final long TIMER = 200; // animation length

    GameActivity parentActivity;
    private TextPaint mTextPaint;
    private float strokeWidth = 2;
    private Paint strokePaint;
    private float cardCoefficient = 0.16f;
    float fieldsOffsetInCards = 0.5f - cardCoefficient * 11 / 4;
    float cardWidth;
    float cardHeight;
    private float focusedZoom = 2 * fieldsOffsetInCards / cardCoefficient + 1;

    private CardView cardViews[]; // To use in default DragShadowBuilder
    LinkedHashSet<Integer> animatedCards = new LinkedHashSet<>();
    int focusedCard = GameConstants.NOT_A_CARD;

    LocalPlayer player;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setupStrokePaint();
        setupTextPaint();

        calcCardSizeBasedOnScreenResolution(context);
        calcTextSizeToFitCards();

        // Generate ImageViews with cards inside
        cardViews = new CardView[GameConstants.DECK_SIZE];
        for (int card = 1; card <= GameConstants.DECK_SIZE; ++card) {
            cardViews[card - 1] = new CardView(parentActivity,
                                               card,
                                               cardWidth,
                                               cardHeight);
            cardViews[card - 1].setGestureDetector(new GestureDetectorCompat(context,
                    new GameViewGestureDetector(this, cardViews[card - 1])));
            this.addView(cardViews[card - 1]);
            cardViews[card - 1].setVisibility(View.GONE);
        }

        player = null;
        setOnDragListener(new GameViewCardDragListener());
        setWillNotDraw(false);
    }

    private void calcTextSizeToFitCards() {
        mTextPaint.setTextSize(Misc.calcTextSize(cardWidth * 0.95f, cardHeight * 0.95f, "100"));
    }

    private void calcCardSizeBasedOnScreenResolution(Context context) {
        Point size = new Point(160, 90);
        if (context instanceof GameActivity) {
            Display display;
            parentActivity = (GameActivity) context;
            display = parentActivity.getWindowManager().getDefaultDisplay();
            display.getSize(size);
        }
        cardWidth = size.x * cardCoefficient;
        cardHeight = size.y * cardCoefficient;
    }

    private void setupTextPaint() {
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.BLACK);
    }

    private void setupStrokePaint() {
        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(strokeWidth);
    }

    protected void setPlayer(LocalPlayer p) {
        player = p;
        invalidate();
    }

    private float[] getQueueTopPosition() {
        return new float[] {getWidth() - cardWidth * (1 + fieldsOffsetInCards / 2),
                cardHeight * fieldsOffsetInCards / 2};
    }

    private float[] getFieldCellPosition(int row, int column) {
        return new float[] {cardWidth * fieldsOffsetInCards / 2 +
                column * cardWidth * (1 + fieldsOffsetInCards / 2),
                cardHeight * fieldsOffsetInCards / 2 +
                        row * cardHeight * (1 + fieldsOffsetInCards / 2)};
    }

    void returnCardToHand(int card) {
        ArrayList<Integer> hand = player.getHand();
        int index = 0;
        while (index < hand.size() && hand.get(index) < card) {
            index++;
        }
        player.getHand().add(index, card);
        focusedCard = GameConstants.NOT_A_CARD;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            cardWidth = cardCoefficient * (right - left);
            cardHeight = cardCoefficient * (bottom - top);

            mTextPaint.setTextSize(Misc.calcTextSize(cardWidth * 0.95f,
                    cardHeight * 0.95f,
                    Integer.toString(GameConstants.DECK_SIZE)));
            invalidate();
        }
    }

    protected void drawScores() {
        TextView scoresView = (TextView) parentActivity.findViewById(R.id.game_scores);
        String scores = "SCORES: " + player.getScoresAsString(false);
        scoresView.setText(scores);
    }

    protected void drawCard(float paddingLeft,
                            float paddingTop,
                            int card) {
        float zoom = (card == focusedCard ? focusedZoom : 1);
        float zoomedWidth = zoom * cardWidth;
        float zoomedHeight = zoom * cardHeight;
        paddingLeft -= zoomedWidth - cardWidth;
        paddingTop -= zoomedHeight - cardHeight;
        cardViews[card - 1].setVisibility(View.VISIBLE);
        cardViews[card - 1].layout(Math.round(paddingLeft),
                                   Math.round(paddingTop),
                                   Math.round(paddingLeft + zoomedWidth),
                                   Math.round(paddingTop + zoomedHeight));
    }

    protected void drawHand() {
        if (player.getHand() == null) {
            return;
        }
        int n = player.getHand().size();
        float paddingLeft = (getWidth() - cardWidth * (n + 1) / 2) / 2,
                paddingTop = getHeight() - cardHeight * (1 + fieldsOffsetInCards);
        for (int card: player.getHand()) {
            drawCard(paddingLeft, paddingTop, card);
            paddingLeft += cardWidth / 2;
        }
    }

    protected void drawQueue() {
        float paddingLeft = getWidth() - cardWidth * (1 + fieldsOffsetInCards / 2),
                paddingTop = cardHeight * fieldsOffsetInCards / 2;
        for (int card: player.getCardsQueue()) {
            drawCard(paddingLeft, paddingTop, card);
            paddingTop += cardHeight * (1 + fieldsOffsetInCards / 2);
        }
    }

    protected void drawBoard() {
        float paddingTop = cardHeight * fieldsOffsetInCards / 2;
        for (ArrayList<Integer> row: player.getBoard()) {
            float paddingLeft = cardWidth * fieldsOffsetInCards / 2;
            for (int card: row) {
                drawCard(paddingLeft, paddingTop, card);
                paddingLeft += cardWidth * (1 + fieldsOffsetInCards / 2);
            }
            paddingTop += cardHeight * (1 + fieldsOffsetInCards / 2);
        }
    }

    protected void drawRowChooser(Canvas canvas) {
        float paddingTop = cardHeight * fieldsOffsetInCards / 2;
        for (int i = 0; i < GameConstants.ROWS; i++) {
            float paddingLeft = cardWidth * fieldsOffsetInCards / 2;
            if (player.isChoosingRowToTake()) {
                canvas.drawRect(paddingLeft / 2,
                        paddingTop,
                        paddingLeft +
                                5 * cardWidth * (1 + fieldsOffsetInCards / 2) -
                                cardWidth * fieldsOffsetInCards / 4,
                        paddingTop + cardHeight * (1 + fieldsOffsetInCards / 4),
                        strokePaint);

            }
            paddingTop += cardHeight * (1 + fieldsOffsetInCards / 2);
        }
    }

    protected void drawMessage(String message) {
        parentActivity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(message)
                    .setNeutralButton("OK", (dialogInterface, i) -> {/* do nothing */})
                    .create()
                    .show();
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (player == null || player.getState() == AbstractPlayer.GameState.NEW_GAME) {
            canvas.drawText("Waiting for other players!",
                            getWidth() / 2f,
                            getHeight() / 2,
                            mTextPaint);
            return;
        }
        drawBoard();
        drawQueue();
        drawScores();
        drawHand();
        drawRowChooser(canvas);
        if (!player.getQueue().isEmpty() &&
                !player.isChoosingRowToTake() &&
                animatedCards.isEmpty()) {
            setupAnimations();
        } else if (player.getState() == AbstractPlayer.GameState.FINISHED ||
                   player.getState() == AbstractPlayer.GameState.INTERRUPTED) {
            parentActivity.goToResults(player.getScoresAsString(true));
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
        invalidate();
    }

    private Animator animateCardTranslation(int card, float[] p1, float[] p2,
                                            AnimatorListenerAdapter listenerAdapter) {
        ObjectAnimator animator = ObjectAnimator.ofMultiFloat(this, "", new float[][] {p1, p2});
        animator.addListener(listenerAdapter);
        animator.setDuration(TIMER);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener((ValueAnimator animation) -> {
            float[] animatedValue = (float[]) animation.getAnimatedValue();
            drawCard(animatedValue[0], animatedValue[1], card);
        });
        return animator;
    }

    private void setupAnimations() {
        AbstractPlayer.Move move = player.getQueue().peek();
        int row = move.rowIndex,
                column = (move.type == AbstractPlayer.updateStateTypes.CLEAR_ROW
                        ? 0
                        : player.getBoard().get(row).size());
        float[] p2 = getFieldCellPosition(row, column);
        // animate card's transition from queue to its place on the field
        if (move.type == AbstractPlayer.updateStateTypes.CLEAR_ROW) {
            // clear the row and then move our card there
            AnimatorSet animatorSet = new AnimatorSet();
            LinkedList<Animator> animators = new LinkedList<>();
            int col = 0;
            float[] p2Outside = {-cardWidth, p2[1]};
            for (int card: player.getBoard().get(row)) {
                animators.add(animateCardTranslation(card,
                        getFieldCellPosition(row, col),
                        p2Outside,
                        new CardAnimatorListenerAdapter(this,
                                                        cardViews[card - 1],
                                                        false)));
                col++;
            }
            player.getCardsQueue().addFirst(GameConstants.NOT_A_CARD);
            player.updateOneMove();
            animatorSet.playTogether(animators);
            animatorSet.start();
        } else {
            player.getCardsQueue().poll();
            animateCardTranslation(move.card,
                getQueueTopPosition(),
                p2,
                new CardAnimatorListenerAdapter(this,
                                                cardViews[move.card - 1],
                                                true)).start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (player.isChoosingRowToTake()) {
            float x = event.getX();
            float y = event.getY();
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
                    player.tellRow(i);
                    invalidate();
                    return true;
                }
                paddingTop += cardHeight * (1 + fieldsOffsetInCards / 2);
            }
        } else {
            focusedCard = GameConstants.NOT_A_CARD;
            return true;
        }
        return false;
    }
}