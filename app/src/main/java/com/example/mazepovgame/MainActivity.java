package com.example.mazepovgame;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.example.mazepovgame.gles.renderer.BasicRenderer;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener {
    private GLSurfaceView surface;
    private GestureDetector mGestureDetector;
    private boolean isSurfaceCreated;

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

        GLSurfaceView.Renderer renderer = new BasicRenderer();
        setContentView(surface);
        ((BasicRenderer) renderer).setContextAndSurface(this,surface);
        surface.setRenderer(renderer);
        isSurfaceCreated = true;
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
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
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
