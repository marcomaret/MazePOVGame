package com.example.mazepovgame;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.example.mazepovgame.gles.renderer.BasicRenderer;
import com.example.mazepovgame.gles.renderer.MazePOVRenderer;
import com.example.mazepovgame.maze.MazeMap;
import com.example.mazepovgame.maze.Player;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener {
    private GLSurfaceView surface;
    private GestureDetector mGestureDetector;
    private boolean isSurfaceCreated;
    private MazeMap mazeMap = null;
    // U-V Movements Params
    private final float SWIPE_MIN_DISTANCE = 40.f;
    private final float SWIPE_THRESHOLD_ROTATION_VELOCITY = .5f;
    private final float SWIPE_THRESHOLD_PLAYER_VELOCITY = 1.f;
    private final float ROTATION_VELOCITY = 1.25f;
    private final float PLAYER_VELOCITY = .3f;
    private final float ROTATION_ANGLE = .05f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mGestureDetector = new GestureDetector(this, this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags (WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        int supported = 1;
        if(configurationInfo.reqGlEsVersion>=0x30000) supported = 3;
        else if(configurationInfo.reqGlEsVersion>=0x20000) supported = 2;
        surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(supported);
        surface.setPreserveEGLContextOnPause(true);

        this.mazeMap = new MazeMap(8, 8,
                new Player(1, 1),
                1.0f // wall size
        );
        GLSurfaceView.Renderer renderer = new MazePOVRenderer(mazeMap);
        setContentView(surface);
        ((BasicRenderer) renderer).setContextAndSurface(this,surface);
        surface.setRenderer(renderer);
        isSurfaceCreated = true;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(isSurfaceCreated)
            surface.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(isSurfaceCreated)
            surface.onPause();
    }

    private float oldX, oldY;
    private boolean lock;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            Log.d("GESTURE", "Started Scrolling At" + event.getX());
            oldX = event.getX();
            oldY = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE){
            float dx = oldX - event.getX();
            float dy = oldY - event.getY();
            if (Math.abs(dx) > 10f && Math.abs(dy) <= 50f) {
                this.mazeMap.rotateEye((Math.signum(dx) == 1f) ? -ROTATION_ANGLE : ROTATION_ANGLE, .5f);
            } else if (Math.abs(dy) > 5f && Math.abs(dx) <= 50f){
                this.mazeMap.movePlayer((Math.signum(dy) == 1f) ? PLAYER_VELOCITY : -PLAYER_VELOCITY);
            }
            oldX = event.getX();
            oldY = event.getY();
        }

        if (event.getAction() == MotionEvent.ACTION_UP){
        }

        return super.onTouchEvent(event);
    }
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float dx = e2.getX() - e1.getX();
        float dy = e2.getY() - e1.getY();
        //Log.d("GESTURE", "Distance X\t" + dx + " Velocity X\t" + velocityX);
        //Log.d("GESTURE", "Distance Y\t" + dy + " Velocity Y\t" + velocityY);
        //if (dx > SWIPE_MIN_DISTANCE*2 // left to right
        //        && Math.abs(velocityX) > SWIPE_THRESHOLD_ROTATION_VELOCITY && Math.abs(velocityY) < SWIPE_THRESHOLD_PLAYER_VELOCITY ) {
        //    this.mazeMap.rotateEye(ROTATION_ANGLE, ROTATION_VELOCITY);
        //} else if (-dx > SWIPE_MIN_DISTANCE*2 // right to left
        //        && Math.abs(velocityX) > SWIPE_THRESHOLD_ROTATION_VELOCITY && Math.abs(velocityY) < SWIPE_THRESHOLD_PLAYER_VELOCITY ) {
        //    this.mazeMap.rotateEye(-ROTATION_ANGLE, ROTATION_VELOCITY);
        //} else if (dy > SWIPE_MIN_DISTANCE //top to bottom
        //        && Math.abs(velocityY) > SWIPE_THRESHOLD_PLAYER_VELOCITY && Math.abs(velocityX) < SWIPE_THRESHOLD_ROTATION_VELOCITY) {
        //    this.mazeMap.movePlayer(-PLAYER_VELOCITY);
        //} else if (-dy > SWIPE_MIN_DISTANCE //bottom to top
        //        && Math.abs(velocityY) > SWIPE_THRESHOLD_PLAYER_VELOCITY && Math.abs(velocityX) < SWIPE_THRESHOLD_ROTATION_VELOCITY) {
        //    this.mazeMap.movePlayer(PLAYER_VELOCITY);
        //}
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}
