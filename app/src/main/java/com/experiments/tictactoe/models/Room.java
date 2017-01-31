package com.experiments.tictactoe.models;



public class Room {
    private String createdAt;
    private Player playerA;
    private Player playerB;

    public Room() {

    }

    public Player getPlayerA() {
        return playerA;
    }

    public void setPlayerA(Player playerA) {
        this.playerA = playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public void setPlayerB(Player playerB) {
        this.playerB = playerB;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Room{" +
                "createdAt='" + createdAt + '\'' +
                ", playerA=" + playerA +
                ", playerB=" + playerB +
                '}';
    }
}
