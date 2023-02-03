package com.example.mazepovgame.maze.timer;

import static com.example.mazepovgame.maze.MazeMap.CELL_SIZE;
import static com.example.mazepovgame.maze.MazeMap.WALL_SIZE;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.mazepovgame.maze.MazeMap;

import java.util.TimerTask;

public class UVTimer extends TimerTask {
    private Object lock;
    private boolean moving;
    private MazeMap mazeMap;
    private float buffer;
    private float step;
    private boolean axisUorV;

    public UVTimer(MazeMap mazeMap, float step, boolean axisUorV){
       moving = false;
       this.mazeMap = mazeMap;
       this.step = step;
       this.axisUorV = axisUorV;
       this.buffer = 0;
       this.lock = new Object();
    }

    public void startMoving(){
        moving = true;
        buffer = 0;
        synchronized (lock) {
            lock.notify();
        }
    }

    public void stopMoving(){
        moving = false;
        buffer = 0;
        synchronized (lock) {
            lock.notify();
        }
    }

    public boolean isMoving(){
        return moving;
    }

    @Override
    public void run() {
        if (isMoving()){
            buffer += Math.abs(step);
            if (axisUorV){ //  U movement
                if (buffer <= Math.PI/2){
                    this.mazeMap.rotateEye(step, 1f);
                } else {
                    stopMoving();
                }
            } else { //  V movement
                if (buffer <= 2*WALL_SIZE){
                    boolean canMove = this.mazeMap.movePlayer(step);
                    if (!canMove) {
                        stopMoving();
                    }
                } else {
                    stopMoving();
                }
            }
        } else {
            this.cancel();
        }
    }

}
