package com.example.mazepovgame.gles.renderer;


import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BasicRenderer implements GLSurfaceView.Renderer {
    protected Context context;
    protected GLSurfaceView surface;
    protected float clearScreen[];
    protected Point currentScreen;
    protected String TAG = "BASIC RENDERER";

    public BasicRenderer(float r, float g, float b, float a){
        this.clearScreen = new float[]{r,g,b,a};
        this.currentScreen = new Point(0,0);
    }

    public BasicRenderer(){
        this.clearScreen = new float[]{0,0,0,0};
        this.currentScreen = new Point(0,0);
    }

    public void setContextAndSurface(Context context, GLSurfaceView surface){
        this.context = context;
        this.surface = surface;
    }

    public Context getContext(){
        return context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(clearScreen[0],clearScreen[1],clearScreen[2],clearScreen[3]);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        glViewport(0,0,w,h);
        currentScreen.x = w;
        currentScreen.y = h;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);
    }
}
