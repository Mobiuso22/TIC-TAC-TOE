package com.experiments.tictactoe.models;

import com.experiments.tictactoe.utility.TileStatus;



public class Tile {

   private  TileStatus tileStatus = TileStatus.BLANK;
    private String matrixIndex = "";


    public Tile(TileStatus tileStatus, String matrixIndex) {
        this.tileStatus = tileStatus;
        this.matrixIndex = matrixIndex;
    }


    public String getMatrixIndex() {
        return matrixIndex;
    }

    public void setMatrixIndex(String matrixIndex) {
        this.matrixIndex = matrixIndex;
    }

    public void setTileStatus(TileStatus tileStatus) {
        this.tileStatus = tileStatus;
    }

    public TileStatus getTileStatus() {
        return tileStatus;
    }

    @Override
    public String toString() {
        return "Tile{" +
                "tileStatus=" + tileStatus +
                ", matrixIndex='" + matrixIndex + '\'' +
                '}';
    }
}
