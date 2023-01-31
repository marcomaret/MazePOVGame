package com.example.mazepovgame.maze;

import com.example.mazepovgame.maze.generation.Cell;

public class MazeMap {

    private Cell[][] maze;
    public MazeBlock[][] mazeCells;
    public Player player;

    private final int MAX_WIDTH = 80;
    private final int MAX_LENGTH = 80;
    public final int width;
    public final int length;

    public static float WALL_SIZE = 1.0f;
    public static float CELL_SIZE;


    public MazeMap(int width, int length) {
        this.width = Math.min(width, MAX_WIDTH);
        this.length = Math.min(length, MAX_LENGTH);
        this.mazeCells = new MazeBlock[width * 3][length * 3];
        CELL_SIZE = 6.0f * WALL_SIZE; // 6 because 3(number of walls in a cellmap) * 2 scale direction
        this.player = new Player(0, 0);
    }
}
