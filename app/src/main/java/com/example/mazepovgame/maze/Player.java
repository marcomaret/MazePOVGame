package com.example.mazepovgame.maze;

import static com.example.mazepovgame.maze.MazeMap.WALL_SIZE;

import android.util.Log;

import com.example.mazepovgame.utils.Vector3f;

public class Player {
    public static final float PLAYER_SIZE = WALL_SIZE;
    //Position of player inside the maze
    private int posX;
    private int posZ;


    // Camera Position
    private Vector3f camera;
    // LookAt Position
    private Vector3f eye;
    private float direction;

    public Player(int posX, int posZ){
        this.posX = posX;
        this.posZ = posZ;
        this.camera = new Vector3f(posX, 0f, posZ);
        this.direction = 0.0f;
        updateEye();
    }

    private void updateEye() {
        this.eye = this.camera.getRotatedBy(direction);
    }

    private void updatePos(){
        this.posX = (int) Math.floor(this.camera.x/2*WALL_SIZE);
        this.posZ = (int) Math.floor(this.camera.z/2*WALL_SIZE);
        Log.d("PLAYER", "In cell ("+this.posX+","+this.posZ+")");
    }

    public void rotateEye(float rotationAngle, float rotationVelocity){
        this.direction += rotationAngle*rotationVelocity;
        this.direction %= (Math.PI*2); // cut it between 0 and 2*PI
        Log.d("DIRECTION", "Direction" + getDirection());
        updateEye();
    }

    public void movePlayer(float velocity){
        Log.d("PLAYER", "POS"+this.camera);
        this.camera.x +=  Math.cos(direction)*velocity;
        this.camera.z -=  Math.sin(direction)*velocity;
        updatePos();
        updateEye();
    }

    public int[] tryToMove(float velocity){
        float x = this.camera.x, z = this.camera.z;
        x +=  Math.cos(direction)*velocity + (Math.cos(direction)*Math.signum(velocity)*PLAYER_SIZE);
        z -=  Math.sin(direction)*velocity - (Math.sin(direction)*-1f*Math.signum(velocity)*PLAYER_SIZE);
        int posX = (int) Math.floor((x)/2*WALL_SIZE);
        int posZ = (int) Math.floor((z)/2*WALL_SIZE);
        return new int[]{posX, posZ};
    }

    private float getDirection() {
        // in degrees
        return (float) (direction*180/Math.PI);
    }

    public Vector3f getCamera() {
        return camera;
    }

    public Vector3f getEye() {
        return eye;
    }
}

