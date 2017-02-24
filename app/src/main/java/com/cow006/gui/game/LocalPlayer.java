package com.cow006.gui.game;

import android.content.DialogInterface;
import android.util.Pair;
import android.widget.ViewFlipper;

import com.cow006.gui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import Backend.Game.Board;
import Backend.Player.Player;
import Backend.Player.PlayerInformation;

class LocalPlayer extends Player {
    GameView gameView;

    public LocalPlayer(GameView gameView, int playersNumber, PlayerInformation playerInformation) {
        super(playersNumber, playerInformation);
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
    public void setGameInterrupted() {
        super.setGameInterrupted();
        gameView.drawMessage("Someone has disconnected! The game will be interrupted.",
                (DialogInterface dialog, int which) ->
                        gameView.parentActivity.goToResults(getFinalScoresAsString()));
    }

    @Override
    public void setGameFinished() {
        super.setGameFinished();
        gameView.parentActivity.goToResults(getFinalScoresAsString());
    }

    @Override
    public void setCardsQueue(Deque<Integer> cardsQueue) {
        super.setCardsQueue(cardsQueue);
        gameView.drawQueue();
    }

    @Override
    public void setBoard(Board board) {
        super.setBoard(board);
        gameView.drawBoard();
    }

    @Override
    public void setHand(List<Integer> hand) {
        GameActivity parentActivity = gameView.parentActivity;
        if (getState() == GameState.NEW_GAME) {
            parentActivity.runOnUiThread(((ViewFlipper) parentActivity.findViewById(R.id.activity_game))
                    ::showNext);
            gameView.drawMessage(parentActivity.getString(R.string.game_start_message_text));
        } else {
            gameView.drawMessage(parentActivity.getString(R.string.next_round_message_text));
        }
        super.setHand(hand);
        gameView.drawHand();
    }

    @Override
    public synchronized void buildBoardModificationsQueue(int chosenRowIndex) {
        super.buildBoardModificationsQueue(chosenRowIndex);
        gameView.post(gameView::runTurnAnimation);
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
    public void updateOneTurn() {
        super.updateOneTurn();
        gameView.drawBoard();
    }

    protected String getScoresAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Integer> scoresList = new ArrayList<>(getScores());
        List<PlayerInformation> informationList = new ArrayList<>(getPlayersInformations());
        List<Pair<Integer, String>> scores = new ArrayList<>();
        for (int i = 0; i < scoresList.size(); i++){
            scores.add(new Pair<>(scoresList.get(i), informationList.get(i).getUsername()));
        }
        Collections.sort(scores, (p1, p2) -> p1.first - p2.first);
        for (Pair<Integer, String> p : scores){
            stringBuilder.append(p.second).append(" - ").append(p.first).append(" pts; ");
        }
        return stringBuilder.toString();
    }


/*    private void addMinimalScore(StringBuilder stringBuilder,
                                 ArrayList<Integer> scoresList,
                                 ArrayList<Integer> playerList) {
        Integer topScore = Collections.min(scoresList);
        int index = scoresList.indexOf(topScore);
//        if (playerList.get(index) == getId()) {
            stringBuilder.append("YOU ");
//        } else {
            stringBuilder.append("Opponent #").append(playerList.get(index));
//        }
        stringBuilder.append(" - ").append(scoresList.get(index)).append("; ");
        scoresList.remove(index);
        playerList.remove(index);
    }
*/
    protected String getFinalScoresAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (List<String> line : getFinalResults()) {
            for (String element : line) {
                stringBuilder.append(element).append("\n");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}