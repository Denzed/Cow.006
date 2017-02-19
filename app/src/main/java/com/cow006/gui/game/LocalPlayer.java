package com.cow006.gui.game;

import android.content.DialogInterface;
import android.util.Pair;
import android.widget.ViewFlipper;

import com.cow006.gui.R;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

import Backend.Board;
import Backend.GameConstants;
import Backend.Player;

class LocalPlayer extends Player {
    GameView gameView;

    public LocalPlayer(GameView gameView, int remoteNumber, int botsNumber) {
        super(remoteNumber, botsNumber);
        this.gameView = gameView;
    }

    public LocalPlayer(GameView gameView, int remoteNumber, int botsNumber,
                       String username, String usedID) {
        super(remoteNumber, botsNumber, username, usedID);
        this.gameView = gameView;
    }

    @Override
    protected void setChoosingRowToTake(boolean value) {
        super.setChoosingRowToTake(value);
        if (value) {
            gameView.postInvalidate();
        }
    }

    @Override
    protected void setGameInterrupted() {
        super.setGameInterrupted();
        gameView.drawMessageWithAction("Someone has disconnected! The game will be interrupted.",
                (DialogInterface dialog, int which) ->
                        gameView.parentActivity.goToResults(getFinalScoresAsString()));
    }

    @Override
    protected void setGameFinished() {
        super.setGameFinished();
        gameView.parentActivity.goToResults(getFinalScoresAsString());
    }

    @Override
    protected void setCardsQueue(ArrayDeque<Integer> cardsQueue) {
        super.setCardsQueue(cardsQueue);
        gameView.drawQueue();
    }

    @Override
    protected void setBoard(Board board) {
        super.setBoard(board);
        gameView.drawBoard();
    }

    @Override
    protected void setHand(ArrayList<Integer> hand) {
        GameActivity parentActivity = gameView.parentActivity;
        if (getState() == GameState.NEW_GAME) {
            parentActivity.runOnUiThread(() ->
                    ((ViewFlipper) parentActivity.findViewById(R.id.activity_game)).showNext());
            gameView.drawMessage(parentActivity.getString(R.string.game_start_message_text));
        } else {
            gameView.drawMessage(parentActivity.getString(R.string.next_round_message_text));
        }
        super.setHand(hand);
        gameView.drawHand();
    }

    @Override
    protected synchronized void playRound(GameConstants.SmallestTakeTypes smallestTakeType,
                                          int chosenRowIndex,
                                          ArrayList<Pair<Integer,Integer>> moves) {
        super.playRound(smallestTakeType, chosenRowIndex, moves);
        gameView.setupAnimations();
    }

    @Override
    protected void updateScore(int playerIndex, int points) {
        super.updateScore(playerIndex, points);
        gameView.drawScores();
    }

    @Override
    public void tellCard(int card) {
        super.tellCard(card);
        gameView.unfocusCard();
    }

    @Override
    public void updateOneMove() {
        super.updateOneMove();
        gameView.drawBoard();
    }

    protected String getScoresAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Integer> scoresList = new ArrayList<>(getScores());
        ArrayList<Integer> playerList = new ArrayList<>();
        for (int i = 0; i < getPlayersNumber(); ++i) {
            playerList.add(i);
        }
        for (int i = 0; i < getPlayersNumber(); ++i) {
            addMinimalScore(stringBuilder, scoresList, playerList);
        }
        return stringBuilder.toString();
    }

    private void addMinimalScore(StringBuilder stringBuilder,
                                 ArrayList<Integer> scoresList,
                                 ArrayList<Integer> playerList) {
        Integer topScore = Collections.min(scoresList);
        int index = scoresList.indexOf(topScore);
        if (playerList.get(index) == getId()) {
            stringBuilder.append("YOU ");
        } else {
            stringBuilder.append("Opponent #").append(playerList.get(index));
        }
        stringBuilder.append(" - ").append(scoresList.get(index)).append("; ");
        scoresList.remove(index);
        playerList.remove(index);
    }

    protected String getFinalScoresAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ArrayList<String> line : getFinalResults()) {
            for (String element : line) {
                stringBuilder.append(element).append("\n");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}