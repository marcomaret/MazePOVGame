package com.example.mazepovgame.maze;

import static com.example.mazepovgame.maze.MazeMap.WALL_SIZE;

import com.example.mazepovgame.utils.Vector3f;

// This class represents a block in the game
// It can be a wall or not.
// His y position is always zero.
public class MazeBlock {
    private boolean isWall;
    private Vector3f position;

    public MazeBlock(float posX, float posZ, boolean isWall){
        this.position = new Vector3f(posX, 0f, posZ);
        this.isWall = isWall;
    }

    public boolean isWall() {
        return isWall;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getSize(){
        return new Vector3f(WALL_SIZE, 1f, WALL_SIZE);
    }
}
