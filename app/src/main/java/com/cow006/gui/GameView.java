package com.cow006.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

import Backend.AbstractPlayer;


public class GameView extends View {
    private float mTextHeight;
    private TextPaint mTextPaint;
    private Paint cardPaints[], strokePaint;

    private float fieldsOffsetInCards = 0.17f;
    private float cardCoefficient = 1 / (6 + 7 * fieldsOffsetInCards),
                  cardWidth, cardHeight;
    private float focusedZoom = (21 * fieldsOffsetInCards + 1) / (2 - cardCoefficient) / 2;

    private long lastEvent = 0,
                 recoil = 300;

    private int focusedCard = 0;
    private AbstractPlayer player;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Set up paints to use for drawing cards
        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.BLACK);

        cardPaints = new Paint[5];
        for (int i = 0; i < 5; ++i) {
            Paint pt = new Paint();
            pt.setStyle(Paint.Style.FILL_AND_STROKE);
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

        // Set up player
        player = null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        cardWidth = cardCoefficient * w;
        cardHeight = cardCoefficient * h;
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

    protected Paint getCard(int number) {
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

    protected void drawCard(Canvas canvas,
                            float paddingLeft,
                            float paddingTop,
                            int number) {
        float zoom = (number == focusedCard ? focusedZoom : 1),
              zoomedWidth = zoom * cardWidth,
              zoomedHeight = zoom * cardHeight;
        paddingLeft -= zoomedWidth - cardWidth;
        paddingTop -= zoomedHeight - cardHeight;
        canvas.drawRect(paddingLeft,
                        paddingTop,
                        paddingLeft + zoomedWidth,
                        paddingTop + zoomedHeight,
                        getCard(number));
        canvas.drawRect(paddingLeft,
                        paddingTop,
                        paddingLeft + zoomedWidth,
                        paddingTop + zoomedHeight,
                        strokePaint);
        String text = Integer.toString(number);
        canvas.drawText(text,
                        paddingLeft + zoomedWidth / 2,
                        paddingTop + zoomedHeight / 2 + mTextHeight,
                        mTextPaint);
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
        if (player.getQueue() == null) {
            return;
        }
        float paddingLeft = getWidth() - cardWidth * (1 + fieldsOffsetInCards / 2),
              paddingTop = cardHeight * fieldsOffsetInCards / 2;
        for (int card: player.getQueue()) {
            drawCard(canvas, paddingLeft, paddingTop, card);
            paddingTop += cardHeight * (1 + fieldsOffsetInCards / 2);
        }
    }

    protected void drawBoard(Canvas canvas) {
        if (player.getBoard() == null) {
            return;
        }
        float paddingTop = cardHeight * fieldsOffsetInCards / 2;
        for (ArrayList<Integer> row: player.getBoard()) {
            float paddingLeft = cardWidth * fieldsOffsetInCards / 2;
            if (player.isChoosingRowToTake()) {
                canvas.drawRect(paddingLeft / 2,
                                paddingTop / 2,
                                paddingLeft +
                                        4 * cardWidth * (1 + fieldsOffsetInCards / 2) -
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
        drawQueue(canvas);
        drawBoard(canvas);
        drawHand(canvas);
        if (!player.getQueue.isEmpty() && !player.isChoosingRowToTake()) {
            player.update();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // Ignore
            }
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
                        !player.getQueue.isEmpty())) {
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
                    player.tellMove(card);
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

    public void setPlayer(AbstractPlayer p) {
        player = p;
        invalidate();
    }
}