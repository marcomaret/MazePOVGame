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
import com.example.mazepovgame.maze.timer.UVTimer;

import java.util.Timer;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener {
    private GLSurfaceView surface;
    private GestureDetector mGestureDetector;
    private boolean isSurfaceCreated;
    private MazeMap mazeMap = null;
    // U-V Movements Params
    private final float SWIPE_MIN_U_DISTANCE = 5.f;
    private final float SWIPE_MIN_V_DISTANCE = 5.f;
    private final float ROTATION_ANGLE = .02f;
    private final float PLAYER_VELOCITY = .05f;

    private UVTimer uvTimer;
    private Timer timer;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mGestureDetector.onTouchEvent(event);
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
        int rate;
        float step;
        boolean uOrV;

        if (uvTimer != null){
            if (uvTimer.isMoving()){ // I'm already moving, don't do nothing
                Log.e("PLAYER", "Still moving, wait that animation is completed before scrolling");
                return true;
            }
        }

        if (dy > SWIPE_MIN_V_DISTANCE && Math.abs(dx) < SWIPE_MIN_U_DISTANCE*2){ //top to bottom
            step = PLAYER_VELOCITY;
            rate = 10;
            uOrV = false;
        } else if (-dy > SWIPE_MIN_V_DISTANCE && Math.abs(dx) < SWIPE_MIN_U_DISTANCE*2){ //bottom to top
            step = -PLAYER_VELOCITY;
            rate = 10;
            uOrV = false;
        } else if (dx > SWIPE_MIN_U_DISTANCE && Math.abs(dy) < SWIPE_MIN_V_DISTANCE) {
            step = ROTATION_ANGLE;
            rate = 20;
            uOrV = true;
        } else if (-dx > SWIPE_MIN_U_DISTANCE && Math.abs(dy) < SWIPE_MIN_V_DISTANCE) {
            step = -ROTATION_ANGLE;
            rate = 20;
            uOrV = true;
        } else {
            return false;
        }
        timer = new Timer();
        uvTimer = new UVTimer(mazeMap, step, uOrV);
        timer.scheduleAtFixedRate(uvTimer, 5, rate);
        uvTimer.startMoving();

        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}
