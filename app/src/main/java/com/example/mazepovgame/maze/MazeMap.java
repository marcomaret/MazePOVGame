package com.example.mazepovgame.maze;

import android.util.Log;

import com.example.mazepovgame.maze.generation.Cell;
import com.example.mazepovgame.maze.generation.Maze;
import com.example.mazepovgame.utils.Vector3f;

public class MazeMap {

    private Cell[][] maze;
    public MazeBlock[][] mazeCells;
    public Player player;

    private final int MAX_WIDTH = 80;
    private final int MAX_LENGTH = 80;
    public final int width;
    public final int length;

    public static float WALL_SIZE = 1.0f;
    public static float CELL_SIZE = 6.0f * WALL_SIZE; // 6 because 3(number of walls in a cellmap) * 2 scale direction


    private MazeMap(int width, int length) {
        this.width = Math.min(width, MAX_WIDTH);
        this.length = Math.min(length, MAX_LENGTH);
        this.mazeCells = new MazeBlock[width * 3][length * 3];
        setUpMazeStructure();
    }

    private MazeMap(int width, int length, Player player) {
        this(width, length);
        this.player = player;
    }

    public MazeMap(int width, int length, Player player, float wallSize) {
        this(width, length, player);
        WALL_SIZE = wallSize;
    }

    private void setUpMazeStructure(){
        Maze mazeGenerator = new Maze(this.width, this.length);
        this.maze = mazeGenerator.getMaze();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < length; j++) {
                setupMazeCellFromCell(this.maze[i][j]);
            }
        }
    }

    private void setupMazeCellFromCell(Cell cell){
        float offset = CELL_SIZE/2; //Offset to make top corner of maze start in coords (0,0)
        float posX = cell.getX() * CELL_SIZE + offset;
        float posZ = cell.getY() * CELL_SIZE + offset;
        int i = cell.getX()*3;
        int j = cell.getY()*3;

        this.mazeCells[j + 0][i + 0] = new MazeBlock(posX - 2*WALL_SIZE, posZ - 2*WALL_SIZE, (cell.getLeft() || cell.getTop()));
        this.mazeCells[j + 0][i + 1] = new MazeBlock(posX, posZ - 2*WALL_SIZE, cell.getTop());
        this.mazeCells[j + 0][i + 2] = new MazeBlock(posX + 2*WALL_SIZE, posZ - 2*WALL_SIZE, (cell.getRight() || cell.getTop()));

        this.mazeCells[j + 1][i + 0] = new MazeBlock(posX - 2*WALL_SIZE, posZ, cell.getLeft());
        this.mazeCells[j + 1][i + 1] = new MazeBlock(posX, posZ, false);
        this.mazeCells[j + 1][i + 2] = new MazeBlock(posX + 2*WALL_SIZE, posZ, cell.getRight());

        this.mazeCells[j + 2][i + 0] = new MazeBlock(posX - 2*WALL_SIZE, posZ + 2*WALL_SIZE, (cell.getLeft() || cell.getBottom()));
        this.mazeCells[j + 2][i + 1] = new MazeBlock(posX, posZ + 2*WALL_SIZE, cell.getBottom());
        this.mazeCells[j + 2][i + 2] = new MazeBlock(posX + 2*WALL_SIZE, posZ + 2*WALL_SIZE, (cell.getRight() || cell.getBottom()));
    }

    public boolean movePlayer(float velocity){
        int pos[] = this.player.tryToMove(velocity);
        if (pos[0] < 0 || pos[1] < 0 || pos[0] >= (width*3) || pos[1] >= (length*3) || this.mazeCells[pos[1]][pos[0]].isWall()){
            Log.e("PLAYER", "Cannot to move to ("+pos[0]+","+pos[1]+")");
            return false;
        }
        Log.d("PLAYER", "Moving to ("+pos[0]+","+pos[1]+")");
        this.player.movePlayer(velocity);
        return true;
    }

    public void rotateEye(float rotation_angle, float rotation_velocity) {
        this.player.rotateEye(rotation_angle, rotation_velocity);
    }

    public Vector3f getFinishCellPosition(){
        //Finish cell is always in bottom right corner
        int lastI = (width * 3) - 2;
        int lastJ = (length * 3) - 1;
        return mazeCells[lastI][lastJ].getPosition();
    }

    public Vector3f getMazeMapSize(){
        float sizeX = CELL_SIZE * (width) / 2;
        float sizeZ = CELL_SIZE * (length) / 2;
        return new Vector3f(sizeX, 1f, sizeZ);
    }
}
