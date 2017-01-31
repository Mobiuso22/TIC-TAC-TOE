package com.experiments.tictactoe.models;



public class Player {

    private String playerName;
    private String joinedAt;
    private Game game;

    public Player() {
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public String toString() {
        return "Player{" +
                ", playerName='" + playerName + '\'' +
                ", joinedAt='" + joinedAt + '\'' +
                ", game=" + game +
                '}';
    }
}
