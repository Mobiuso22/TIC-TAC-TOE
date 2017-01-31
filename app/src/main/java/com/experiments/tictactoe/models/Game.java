package com.experiments.tictactoe.models;

import java.util.ArrayList;
import java.util.List;



public class Game {

    private List<String> matrix = new ArrayList<>();
    private boolean hasZero;


    public Game() {
    }

    public List<String> getMatrix() {
        return matrix;
    }

    public void setMatrix(List<String> matrix) {
        this.matrix = matrix;
    }

    public boolean isHasZero() {
        return hasZero;
    }

    public void setHasZero(boolean hasZero) {
        this.hasZero = hasZero;
    }

    @Override
    public String toString() {
        return "Game{" +
                "matrix=" + matrix +
                ", hasZero=" + hasZero +
                '}';
    }

}
