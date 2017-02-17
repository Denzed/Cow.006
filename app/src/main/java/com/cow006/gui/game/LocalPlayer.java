package com.cow006.gui.game;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;

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
    protected void setChoosingCardToTake(boolean value) {
        super.setChoosingCardToTake(value);
    }

    @Override
    protected void setGameInterrupted() {
        super.setGameInterrupted();
        gameView.drawMessage("Someone has disconnected! The game will be interrupted.");
        gameView.postInvalidate();
    }

    @Override
    protected void setGameFinished() {
        super.setGameFinished();
        gameView.postInvalidate();
    }

    @Override
    public void setHand(ArrayList<Integer> hand) {
        gameView.drawMessage(getState() == GameState.NEW_GAME
                ? "The game is starting!"
                : "Prepare for the next round!");
        super.setHand(hand);
        gameView.postInvalidate();
    }

    @Override
    protected synchronized void playRound(GameConstants.SmallestTakeTypes smallestTakeType,
                                          int chosenRowIndex,
                                          ArrayList<Pair<Integer,Integer>> moves) {
        super.playRound(smallestTakeType, chosenRowIndex, moves);
        gameView.postInvalidate();
    }

    @Override
    public void tellCard(int card) {
        super.tellCard(card);
        gameView.focusedCard = GameConstants.NOT_A_CARD;
        gameView.postInvalidate();
    }

    protected String getScoresAsString(boolean isFinal) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isFinal) {
            for (ArrayList<String> line: getFinalResults()) {
                for (String element: line) {
                    stringBuilder.append(element);
                    stringBuilder.append("\n");
                }
                stringBuilder.append("\n");
            }
        } else {
            int id = getId();
            ArrayList<Integer> scoresList = new ArrayList<>(getScores()),
                               playerList = new ArrayList<>();
            for (int i = 0; i < getPlayersNumber(); ++i) {
                playerList.add(i);
            }
            for (int i = 0; i < getPlayersNumber(); ++i) {
                Integer topScore = Collections.min(scoresList);
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
}