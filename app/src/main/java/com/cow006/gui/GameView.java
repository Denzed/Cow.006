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
import java.util.PriorityQueue;
import java.util.Random;

import Backend.AbstractPlayer;

/**
 * TODO: document your custom view class.
 */
public class GameView extends View {
    private float mTextHeight;
    private TextPaint mTextPaint;
    private Paint cardPaints[], strokePaint;

    private float fieldsOffsetInCards = 0.17f;
//    private float cardCoefficient = (2 - fieldsOffsetInCards) / 11,
    private float cardCoefficient = 1 / (6 + 7 * fieldsOffsetInCards),
                  cardWidth, cardHeight;
    private float focusedZoom = (21 * fieldsOffsetInCards + 1) / (2 - cardCoefficient) / 2;

    private long lastEvent = 0,
                 recoil = 300;

    private boolean isChoosingRowToTake = false;
    private int focusedCard = 0;
    private PriorityQueue<Integer> cardQueue;
    private LocalPlayer player;
    private SimpleHandler handler;

    static class LocalPlayer extends AbstractPlayer {
        public int n;
        public SimpleHandler h;

        protected int move = 0,
                row = -1;

        public LocalPlayer(int playersNumber) {
            super(playersNumber);
            n = playersNumber;
        }

        public void setHandler(SimpleHandler sh) {
            h = sh;
        }

        public synchronized void setMove(int card) {
            h.runRound(card);
            move = card;
        }

        public synchronized void setRow(int r) {
            row = r;
        }

        public synchronized int tellMove() {
            while (move == 0) {
            }
            hand.remove(Integer.valueOf(move));
            int res = move;
            move = 0;
            return res;
        }

        public synchronized int tellChosenRow() {

            while(row == -1) {
            }
            int res = row;
            row = -1;
            return res;
        }
    }

    private class SimpleHandler {
        int n;
        ArrayList<ArrayList<Integer>> hands, board;

        public SimpleHandler(int nPlayers) {
            n = nPlayers;
            ArrayList<Integer> deck = new ArrayList<>();
            for (int i = 1; i <= 104; ++i) {
                deck.add(i);
            }
            hands = new ArrayList<>(nPlayers);
            for (int j = 0; j < nPlayers; ++j) {
                hands.add(new ArrayList<>());
                for (int i = 0; i < 10; ++i) {
                    int k = new Random().nextInt(deck.size());
                    hands.get(j).add(deck.get(k));
                    deck.remove(k);
                }
            }
            board = new ArrayList<>(4);
            for (int i = 0; i < 4; ++i) {
                board.add(new ArrayList<>());
                int k = new Random().nextInt(deck.size());
                board.get(i).add(deck.get(k));
                deck.remove(k);
            }
            player = new LocalPlayer(nPlayers);
            updatePlayer();
        }

        public void updatePlayer() {
            player.setBoard(board);
            player.setHand(hands.get(0));
        }

        public int getMinimalTop() {
            int res = 104;
            for (int i = 0; i < 4; ++i) {
                int k = board.get(i).get(board.get(i).size() - 1);
                if (k < res) {
                    res = k;
                }
            }
            return res;
        }

        public void runRound(int playerCard) {
            cardQueue.add(playerCard);
            hands.get(0).remove(Integer.valueOf(playerCard));
            for (int i = 1; i < n; ++i) {
                int k = new Random().nextInt(hands.get(i).size());
                cardQueue.add(hands.get(i).get(k));
                hands.get(i).remove(k);
            }
            updatePlayer();
            if (cardQueue.peek() < getMinimalTop()) {
                if (cardQueue.peek().intValue() == playerCard) {
                    isChoosingRowToTake = true;
                } else {
                    int k = new Random().nextInt(4);
                    takeRow(k);
                }
                invalidate();
            }
        }

        public void runQueue() {
            int card = cardQueue.peek();
            cardQueue.remove();
            int j = -1;
            for (int i = 0; i < 4; ++i) {
                if (card > board.get(i).get(board.get(i).size() - 1) &&
                        (j == -1 || board.get(i).get(board.get(i).size() - 1) > board.get(j).get(board.get(j).size() - 1))) {
                    j = i;
                }
            }
            if (board.get(j).size() == 5) {
                board.get(j).clear();
            }
            board.get(j).add(card);
            updatePlayer();
        }


    }

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

        // Set up emptyQueue
        cardQueue = new PriorityQueue<>();

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
        for (int card: cardQueue) {
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
            if (isChoosingRowToTake) {
                System.out.println("L:");
                System.out.println(paddingLeft / 2);
                System.out.println("R:");
                System.out.println(paddingTop / 2);
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

    protected  void takeRow(int row) {
        handler.board.get(row).clear();
        player.getBoard().get(row).add(cardQueue.remove());
        handler.updatePlayer();
        invalidate();
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
        if (!cardQueue.isEmpty() && !isChoosingRowToTake) {
            handler.runQueue();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isChoosingRowToTake &&
                (System.currentTimeMillis() - lastEvent < recoil || !cardQueue.isEmpty())) {
            return false;
        }
        lastEvent = System.currentTimeMillis();
        float x = event.getX(),
              y = event.getY();
        if (isChoosingRowToTake) {
            float paddingTop = cardHeight * fieldsOffsetInCards / 2;
            for (int i = 0; i < 4; ++i) {
                float paddingLeft = cardWidth * fieldsOffsetInCards / 2,
                      paddingRight = paddingLeft +
                              4 * cardWidth * (1 + fieldsOffsetInCards / 2) -
                              cardWidth * fieldsOffsetInCards / 4,
                      paddingBottom = paddingTop + cardHeight * (1 + fieldsOffsetInCards / 4);
                if (paddingLeft <= x && x < paddingRight &&
                        paddingTop <= y && y < paddingBottom) {
                    isChoosingRowToTake = false;
                    player.setRow(i);
                    takeRow(i);
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
        ArrayList<Integer> handReversed = new ArrayList<>();
        handReversed.addAll(player.getHand());
        Collections.reverse(handReversed);
        for (int card: handReversed) {
            float zoom = (card == focusedCard ? focusedZoom : 1),
                    zoomedWidth = zoom * cardWidth,
                    zoomedHeight = zoom * cardHeight,
                    zoomedPaddingLeft = paddingLeft - zoomedWidth + cardWidth,
                    zoomedPaddingTop = paddingTop - zoomedHeight + cardHeight;
            if (zoomedPaddingLeft <= x && x < zoomedPaddingLeft + zoomedWidth &&
                    zoomedPaddingTop <= y && y < zoomedPaddingTop + zoomedHeight) {
                if (card == focusedCard) {
                    focusedCard = 0;
                    player.setMove(card);
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
        handler = new SimpleHandler(p.n);
        player.setHandler(handler);
        invalidate();
    }
}