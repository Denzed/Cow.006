package com.cow006.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import Backend.Player;


public class GameView extends View {
    GameActivity parentActivity;
    private float mTextHeight;
    private TextPaint mTextPaint;
    private Bitmap cardBitmaps[];
    private float strokeWidth = 2;
    private Paint strokePaint,
                  cardPaints[],
                  bitmapPaint;

    private float fieldsOffsetInCards = 0.17f;
    private float cardCoefficient = 1 / (6 + 7 * fieldsOffsetInCards),
    cardWidth,
    cardHeight;
    private float focusedZoom = (25 * fieldsOffsetInCards + 1) / (2 - cardCoefficient) / 2;

    private long lastEvent = 0,
                 recoil = 400;

    private int focusedCard = 0;
    private LocalPlayer player;

    public class LocalPlayer extends Player {
        LocalPlayer(int nPlayers) {
            super(nPlayers);
        }

        @Override
        protected synchronized void playRound(boolean smallestTook,
                                    int chosenRowIndex,
                                    ArrayList<Map.Entry<Integer,Integer>> moves) {
            super.playRound(smallestTook, chosenRowIndex, moves);
            postInvalidate();
        }
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
        Display display = null;
        if (context instanceof GameActivity) {
            parentActivity = (GameActivity) context;
            display = parentActivity.getWindowManager().getDefaultDisplay();
        }
        Point size = new Point();
        display.getSize(size);
        cardWidth = size.x * cardCoefficient;
        cardHeight = size.y * cardCoefficient;

        // Adjust text size to fit cards
        mTextPaint.setTextSize(calcTextSize(cardWidth * 0.95f, cardHeight * 0.95f, "100"));
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

        // Set up player
        player = null;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        cardWidth = cardCoefficient * (right - left);
        cardHeight = cardCoefficient * (bottom - top);

        mTextPaint.setTextSize(calcTextSize(cardWidth * 0.95f, cardHeight * 0.95f, "100"));
        mTextHeight = mTextPaint.getFontMetrics().bottom;
    }

    protected float calcTextSize(float width, float height, String text) {
        TextPaint tp = new TextPaint();
        float l = 0,
                r = width,
                eps = 1e-5f;
        while (l + eps < r) {
            float m = (l + r) / 2;
            tp.setTextSize(m);
            if (tp.getFontMetrics().bottom > height ||
                    tp.measureText(text) > width) {
                r = m;
            } else {
                l = m;
            }
        }
        return l;
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

    protected void drawScores() {
        int id = player.getId();
        ArrayList<Integer> scoresList = new ArrayList<>(player.getScores()),
                           playerList = new ArrayList<>();
        for (int i = 0; i < player.getPlayersNumber(); ++i) {
            playerList.add(i);
        }
        StringBuilder stringBuilder = new StringBuilder("Current scores: ");
        for (int i = 0; i < player.getPlayersNumber(); ++i) {
            Integer topScore = Collections.max(scoresList);
            int index = scoresList.indexOf(topScore);
            if (playerList.get(index) == id) {
                for (char c : ("" + playerList.get(index)).toCharArray()) {
                    stringBuilder.append(c);
                    stringBuilder.append("\u0332");
                }
            } else {
                stringBuilder.append(playerList.get(index));
            }
            stringBuilder.append(" - ");
            stringBuilder.append(scoresList.get(index));
            stringBuilder.append("; ");
            scoresList.remove(index);
            playerList.remove(index);
        }
//        System.out.println(stringBuilder.toString());
        TextView scoresView = (TextView) parentActivity.findViewById(R.id.game_scores);
        scoresView.setText(stringBuilder.toString());
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
//        System.out.println("Got queue of size " + player.getCardsFromQueue().size());
        for (int card: player.getCardsFromQueue()) {
            drawCard(canvas, paddingLeft, paddingTop, card);
            paddingTop += cardHeight * (1 + fieldsOffsetInCards / 2);
        }
    }

    protected void drawBoard(Canvas canvas) {
        float paddingTop = cardHeight * fieldsOffsetInCards / 2;
        for (ArrayList<Integer> row: player.getBoard()) {
            float paddingLeft = cardWidth * fieldsOffsetInCards / 2;
            if (player.isChoosingRowToTake()) {
                canvas.drawRect(paddingLeft / 2,
                                paddingTop / 2,
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (player == null) {
            return;
        }
        drawScores();
        drawQueue(canvas);
        drawBoard(canvas);
        drawHand(canvas);
        if (!player.getCardsFromQueue().isEmpty() && !player.isChoosingRowToTake()) {
            player.updateOneMove();
            try {
                Thread.sleep(recoil);
            } catch (InterruptedException e) {
                // Ignore
            }
            invalidate();
        }
    }

    private boolean insideRect(float x, float y,
                               float xLeft, float yTop,
                               float xRight, float yBottom) {
        return xLeft <= x && x < xRight &&
                yTop <= y && y < yBottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!player.isChoosingRowToTake() &&
                (System.currentTimeMillis() - lastEvent < recoil ||
                        !player.getCardsFromQueue().isEmpty())) {
            return false;
        }
        lastEvent = System.currentTimeMillis();
        float x = event.getX(),
              y = event.getY();
        if (player.isChoosingRowToTake()) {
            float paddingTop = cardHeight * fieldsOffsetInCards / 2;
            for (int i = 0; i < 4; ++i) {
                float paddingLeft = cardWidth * fieldsOffsetInCards / 2,
                      paddingRight = paddingLeft +
                              4 * cardWidth * (1 + fieldsOffsetInCards / 2) -
                              cardWidth * fieldsOffsetInCards / 4,
                      paddingBottom = paddingTop + cardHeight * (1 + fieldsOffsetInCards / 4);
                if (insideRect(x, y, paddingLeft, paddingTop, paddingRight, paddingBottom)) {
                    player.tellRow(i);
                    invalidate();
                    return true;
                }
                paddingTop += cardHeight * (1 + fieldsOffsetInCards / 2);
            }
            return false;
        }
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
            if (insideRect(x, y, zoomedPaddingLeft, zoomedPaddingTop,
                    zoomedPaddingLeft + zoomedWidth, zoomedPaddingTop + zoomedHeight)) {
                if (card == focusedCard) {
                    focusedCard = 0;
                    player.tellCard(card);
                } else {
                    focusedCard = card;
                }
                invalidate();
                return true;
            }
            paddingLeft -= cardWidth / 2;
        }
        return false;
    }

    public void setPlayer(LocalPlayer p) {
        player = p;
        invalidate();
    }
}