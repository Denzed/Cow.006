package com.cow006.gui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Display;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import Backend.AbstractPlayer;
import Backend.GameConstants;
import Backend.Player;


public class GameView extends View {
    private static final int NOT_A_CARD = 0;
    private static final long TIMER = 200; // animation length

    GameActivity parentActivity;
    private float mTextHeight;
    private TextPaint mTextPaint;
    private Bitmap cardBitmaps[];
    private float strokeWidth = 2;
    private Paint strokePaint,
                  cardPaints[],
                  bitmapPaint;
    private float cardCoefficient = 0.16f,
                  fieldsOffsetInCards = 0.5f - cardCoefficient * 11 / 4,
                  cardWidth,
                  cardHeight,
                  focusedZoom = 2 * fieldsOffsetInCards / cardCoefficient + 1;
    private GestureDetectorCompat gestureDetector;

    private ImageView cardViews[]; // To use in default DragShadowBuilder

    private int focusedCard = 0;
    private LocalPlayer player;
    private String messageToDisplay;

    private LinkedHashMap<Integer,Pair<Float,Float>> animatedCards = new LinkedHashMap<>();

    public class LocalPlayer extends Player {

        public LocalPlayer(int remoteNumber, int botsNumber) {
            super(remoteNumber, botsNumber);
        }

        public LocalPlayer(int remoteNumber, int botsNumber, String username, String usedID) {
            super(remoteNumber, botsNumber, username, usedID);
        }

        @Override
        protected void setChoosingRowToTake(boolean value) {
            super.setChoosingRowToTake(value);
            if (value) {
                postInvalidate();
            }
        }

        @Override
        protected void setChoosingCardToTake(boolean value) {
            super.setChoosingCardToTake(value);
            if (value) {
                postInvalidate();
            }
        }

        @Override
        protected void setGameInterrupted() {
            super.setGameInterrupted();
            messageToDisplay = "Someone has disconnected! The game will be interrupted.";
            postInvalidate();
        }

        @Override
        protected void setGameFinished() {
            super.setGameFinished();
            postInvalidate();
        }

        @Override
        public void setHand(ArrayList<Integer> hand) {
            messageToDisplay = (getState() == GameState.NEW_GAME
                    ? "The game is starting!"
                    : "Prepare for the next round!");
            super.setHand(hand);
            postInvalidate();
        }

        @Override
        protected synchronized void playRound(GameConstants.SmallestTakeTypes smallestTakeType,
                                              int chosenRowIndex,
                                              ArrayList<Pair<Integer,Integer>> moves) {
            super.playRound(smallestTakeType, chosenRowIndex, moves);
            postInvalidate();
        }

        @Override
        public void tellCard(int card) {
            focusedCard = NOT_A_CARD;
            postInvalidate();
            super.tellCard(card);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            float x = event.getX(),
                  y = event.getY();
            if (player.isChoosingRowToTake() && player.getQueue().isEmpty()) {
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
                return true;
            }
            int card = getCardFromCoordinates(x, y);
            if (player.isChoosingCardToTake() && card != focusedCard) {
                focusedCard = card;
                invalidate();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            int card = getCardFromCoordinates(event.getX(), event.getY());
            if (card != NOT_A_CARD) {
                player.tellCard(card);
                return true;
            }
            return super.onDoubleTap(event);
        }

        @Override
        public void onLongPress(MotionEvent event) {
            float x = event.getX(),
                  y = event.getY();
            if (player.getQueue().isEmpty() && player.isChoosingCardToTake()) {
                int card = getCardFromCoordinates(x, y);
                if (card == NOT_A_CARD) {
                    return;
                }
                ClipData data = ClipData.newPlainText("", "");
                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(cardViews[card - 1]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    startDragAndDrop(data, shadowBuilder, card, 0);
                } else {
                    startDrag(data, shadowBuilder, card, 0);
                }
                player.getHand().remove(Integer.valueOf(card));
                invalidate();
            }
            super.onLongPress(event);
        }
    }

    private class CardDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            int card = (Integer) event.getLocalState();
            switch (action) {
                case DragEvent.ACTION_DROP:
                    float x = event.getX(),
                            y = event.getY();
                    // Check that the card is dragged inside the field box, otherwise reject
                    float paddingTop = cardHeight * fieldsOffsetInCards / 2,
                            paddingLeft = cardWidth * fieldsOffsetInCards / 2,
                            paddingRight = cardWidth * (5 + fieldsOffsetInCards),
                            paddingBottom = cardHeight * (4 + fieldsOffsetInCards);
                    if (Misc.insideRect(x, y,
                            paddingLeft, paddingTop,
                            paddingRight, paddingBottom)) {
                        player.tellCard(card);
                        break;
                    }
                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        ArrayList<Integer> hand = player.getHand();
                        int index = 0;
                        while (index < hand.size() && hand.get(index) < card) {
                            index++;
                        }
                        player.getHand().add(index, card);
                        focusedCard = NOT_A_CARD;
                        invalidate();
                    }
                default:
                    return false;
            }
            return true;
        }
    }

    // Gets card number if there is a card in hand at this coordinates, NOT_A_CARD otherwise
    private int getCardFromCoordinates(float x, float y) {
        int n = player.getHand().size();
        float paddingLeft = (getWidth() + cardWidth * (n - 3) / 2) / 2,
              paddingTop = getHeight() - cardHeight * (1 + fieldsOffsetInCards);
        ArrayList<Integer> handReversed = new ArrayList<>(player.getHand());
        Collections.reverse(handReversed);
        for (int card: handReversed) {
            float zoom = (card == focusedCard ? focusedZoom : 1),
                    zoomedWidth = zoom * cardWidth,
                    zoomedHeight = zoom * cardHeight,
                    zoomedPaddingLeft = paddingLeft - zoomedWidth + cardWidth,
                    zoomedPaddingTop = paddingTop - zoomedHeight + cardHeight;
            if (Misc.insideRect(x, y,
                                zoomedPaddingLeft, zoomedPaddingTop,
                                zoomedPaddingLeft + zoomedWidth, zoomedPaddingTop + zoomedHeight)) {
                return card;
            }
            paddingLeft -= cardWidth / 2;
        }
        return 0;
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Set up paints
        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(strokeWidth);

        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);

        cardPaints = new Paint[5];
        for (int i = 0; i < 5; ++i) {
            Paint pt = new Paint();
            pt.setStyle(Paint.Style.FILL_AND_STROKE);
            pt.setStrokeWidth(strokeWidth);
            cardPaints[i] = pt;
        }
        cardPaints[0].setColor(Color.GREEN);
        cardPaints[1].setColor(Color.BLUE);
        cardPaints[2].setColor(Color.YELLOW);
        cardPaints[3].setColor(Color.rgb(255, 165, 0)); // ORANGE
        cardPaints[4].setColor(Color.RED);

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.BLACK);

        // Get screen size
        Point size = new Point(160, 90);
        if (context instanceof GameActivity) {
            Display display;
            parentActivity = (GameActivity) context;
            display = parentActivity.getWindowManager().getDefaultDisplay();
            display.getSize(size);
        }
        cardWidth = size.x * cardCoefficient;
        cardHeight = size.y * cardCoefficient;

        // Adjust text size to fit cards
        mTextPaint.setTextSize(Misc.calcTextSize(cardWidth * 0.95f, cardHeight * 0.95f, "100"));
        mTextHeight = mTextPaint.getFontMetrics().bottom;

        // Draw card bitmaps
        cardBitmaps = new Bitmap[104];
        for (int i = 1; i <= 104; ++i) {
            cardBitmaps[i - 1] = Bitmap.createBitmap(Math.round(cardWidth),
                                                     Math.round(cardHeight),
                                                     Bitmap.Config.RGB_565);
            Canvas tempCanvas = new Canvas(cardBitmaps[i - 1]);
            tempCanvas.drawRect(0, 0, cardWidth, cardHeight, getCardPaint(i));
            tempCanvas.drawRect(0, 0, cardWidth, cardHeight, strokePaint);
            String text = Integer.toString(i);
            tempCanvas.drawText(text,
                                cardWidth / 2f,
                                cardHeight / 2f + mTextHeight,
                                mTextPaint);
        }

        // Generate ImageViews with cards inside
        cardViews = new ImageView[104];
        for (int card = 1; card <= 104; ++card) {
            cardViews[card - 1] = new ImageView(context, attrs);
            cardViews[card - 1].setImageBitmap(cardBitmaps[card - 1]);
            cardViews[card - 1].layout(0,
                    0,
                    Math.round(cardWidth * focusedZoom),
                    Math.round(cardHeight * focusedZoom));
        }

        // Set up player
        player = null;

        // Set gesture listener
        gestureDetector = new GestureDetectorCompat(context, new GestureListener());

        // Set OnDragListener
        setOnDragListener(new CardDragListener());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            cardWidth = cardCoefficient * (right - left);
            cardHeight = cardCoefficient * (bottom - top);

            mTextPaint.setTextSize(Misc.calcTextSize(cardWidth * 0.95f, cardHeight * 0.95f, "100"));
            mTextHeight = mTextPaint.getFontMetrics().bottom;
            invalidate();
        }
    }

    protected Paint getCardPaint(int number) {
        if (number == 55) {
            return cardPaints[4];
        } else if (number % 10 == number / 10) {
            return cardPaints[3];
        } else if (number % 10 == 0) {
            return cardPaints[2];
        } else if (number % 5 == 0) {
            return cardPaints[1];
        } else {
            return cardPaints[0];
        }
    }

    protected String parseScores(boolean isFinal) {
        int id = player.getId();
        ArrayList<Integer> scoresList = new ArrayList<>(player.getScores()),
                playerList = new ArrayList<>();
        ArrayList<ArrayList<String>> finalScoresList = null;
        if (isFinal) {
            finalScoresList = new ArrayList<>(player.getFinalResults());
        }
        for (int i = 0; i < player.getPlayersNumber(); ++i) {
            playerList.add(i);
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (isFinal) {
            for (ArrayList<String> line: finalScoresList) {
                for (String element: line) {
                    stringBuilder.append(element);
                    stringBuilder.append("\n");
                }
                stringBuilder.append("\n");
            }
        } else {
            for (int i = 0; i < player.getPlayersNumber(); ++i) {
                Integer topScore = Collections.max(scoresList);
                int index = scoresList.indexOf(topScore);
                if (playerList.get(index) == id) {
                    stringBuilder.append("YOU ");
                } else {
                    stringBuilder.append("Opponent #");
                    stringBuilder.append(playerList.get(index));
                }
                stringBuilder.append(" - ");
                stringBuilder.append(scoresList.get(index));
                stringBuilder.append("; ");
                scoresList.remove(index);
                playerList.remove(index);
            }
        }
        return stringBuilder.toString();
    }

    protected void drawScores() {
        TextView scoresView = (TextView) parentActivity.findViewById(R.id.game_scores);
        String scores = "SCORES: " + parseScores(false);
        scoresView.setText(scores);
    }

    protected void drawCard(Canvas canvas,
                            float paddingLeft,
                            float paddingTop,
                            int number) {
        float zoom = (number == focusedCard ? focusedZoom : 1),
                zoomedWidth = zoom * cardWidth,
                zoomedHeight = zoom * cardHeight;
        paddingLeft -= zoomedWidth - cardWidth;
        paddingTop -= zoomedHeight - cardHeight;
        RectF position = new RectF(paddingLeft,
                paddingTop,
                paddingLeft + zoomedWidth,
                paddingTop + zoomedHeight);
        canvas.drawBitmap(cardBitmaps[number - 1], null, position, bitmapPaint);
    }

    protected void drawHand(Canvas canvas) {
        if (player.getHand() == null) {
            return;
        }
        int n = player.getHand().size();
        float paddingLeft = (getWidth() - cardWidth * (n + 1) / 2) / 2,
                paddingTop = getHeight() - cardHeight * (1 + fieldsOffsetInCards);
        for (int card: player.getHand()) {
            drawCard(canvas, paddingLeft, paddingTop, card);
            paddingLeft += cardWidth / 2;
        }
    }

    protected void drawQueue(Canvas canvas) {
        float paddingLeft = getWidth() - cardWidth * (1 + fieldsOffsetInCards / 2),
                paddingTop = cardHeight * fieldsOffsetInCards / 2;
//        System.out.println("QUEUE: ");
        for (int card: player.getCardsQueue()) {
//            System.out.print(card + " ");
            drawCard(canvas, paddingLeft, paddingTop, card);
            paddingTop += cardHeight * (1 + fieldsOffsetInCards / 2);
        }
//        System.out.println();
    }

    protected void drawBoard(Canvas canvas) {
        float paddingTop = cardHeight * fieldsOffsetInCards / 2;
        for (ArrayList<Integer> row: player.getBoard()) {
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
            for (int card: row) {
                drawCard(canvas, paddingLeft, paddingTop, card);
                paddingLeft += cardWidth * (1 + fieldsOffsetInCards / 2);
            }
            paddingTop += cardHeight * (1 + fieldsOffsetInCards / 2);
        }
    }

    protected void drawMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(message)
               .setNeutralButton("OK", (dialogInterface, i) -> {/* do nothing */})
               .create()
               .show();
    }

    private Animator animateCard(int card, float[] p1, float[] p2,
                                            AnimatorListenerAdapter listenerAdapter) {
        ObjectAnimator animator = ObjectAnimator.ofMultiFloat(this, "", new float[][] {p1, p2});
        if (listenerAdapter != null) {
            animator.addListener(listenerAdapter);
        }
        animator.addUpdateListener((ValueAnimator animation) -> {
            float[] animatedValue = (float[]) animation.getAnimatedValue();
            animatedCards.put(card, new Pair<>(animatedValue[0], animatedValue[1]));
            invalidate();
        });
        return animator;
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (player == null || player.getState() == AbstractPlayer.GameState.NEW_GAME) {
            canvas.drawText("Waiting for other players!",
                            getWidth() / 2f,
                            getHeight() / 2,
                            mTextPaint);
            return;
        }
        if (messageToDisplay != null) {
            drawMessage(messageToDisplay);
            messageToDisplay = null;
        }
        drawScores();
        drawQueue(canvas);
        drawBoard(canvas);
        drawHand(canvas);
        if (!player.getQueue().isEmpty() && !player.isChoosingRowToTake()) {
            if (animatedCards.isEmpty()) {
                setupAnimations();
            } else if (!animatedCards.isEmpty()) {
                for (int card: animatedCards.keySet()) {
                    drawCard(canvas,
                            animatedCards.get(card).first,
                            animatedCards.get(card).second,
                            card);
                }
            }
        } else if (player.getState() == AbstractPlayer.GameState.FINISHED ||
                   player.getState() == AbstractPlayer.GameState.INTERRUPTED) {
            parentActivity.goToResults(parseScores(true));
        }
    }

    private void setupAnimations() {
        AbstractPlayer.Move move = player.getQueue().peek();
        int row = move.rowIndex,
                column = (move.type == AbstractPlayer.updateStateTypes.CLEAR_ROW
                        ? 0
                        : player.getBoard().get(row).size());
        float[] p2 = getFieldCellPosition(row, column);
        LinkedList<Animator> animators = new LinkedList<>();
        if (move.type == AbstractPlayer.updateStateTypes.CLEAR_ROW) {
            int col = 0;
            float[] p2Outside = {-cardWidth, p2[1]};
            for (int card: player.getBoard().get(row)) {
                animators.add(animateCard(card,
                                          getFieldCellPosition(row, col),
                                          p2Outside,
                                          new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animatedCards.remove(card);
                        super.onAnimationEnd(animation);
                    }
                }));
                col++;
            }
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(TIMER);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        player.getCardsQueue().poll();
        animators.add(animateCard(move.card,
                                  getQueueTopPosition(),
                                  p2,
                                  new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animatedCards.remove(move.card);
                player.getCardsQueue().addFirst(NOT_A_CARD);
                player.updateOneMove();
                super.onAnimationEnd(animation);
            }
        }));
        animatorSet.playTogether(animators);
        animatorSet.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    public void setPlayer(LocalPlayer p) {
        player = p;
        invalidate();
    }
}