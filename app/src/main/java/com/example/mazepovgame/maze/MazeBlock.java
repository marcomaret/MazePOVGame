package com.example.mazepovgame.maze;

import com.example.mazepovgame.utils.Vector3f;

// This class represents a block in the game
// It can be a wall or not.
// His y position is always zero.
public class MazeBlock {
    private boolean isWall;
    private Vector3f position;

    public MazeBlock(float posX, float posZ, boolean isWall){
        this.position.x = posX;
        this.position.y = 0;
        this.position.z = posZ;
        this.isWall = isWall;
    }

    public boolean isWall() {
        return isWall;
    }

    public Vector3f getPosition() {
        return position;
    }
}
