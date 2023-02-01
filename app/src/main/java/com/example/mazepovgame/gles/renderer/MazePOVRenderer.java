package com.example.mazepovgame.gles.renderer;

import static android.opengl.GLES10.GL_BACK;
import static android.opengl.GLES10.GL_CCW;
import static android.opengl.GLES10.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES10.glViewport;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCullFace;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glFrontFace;

import static com.example.mazepovgame.maze.MazeMap.CELL_SIZE;
import static com.example.mazepovgame.maze.MazeMap.WALL_SIZE;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.mazepovgame.R;
import com.example.mazepovgame.gles.renderer.utils.ShaderCompiler;
import com.example.mazepovgame.maze.MazeMap;
import com.example.mazepovgame.utils.Vector3f;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MazePOVRenderer extends BasicRenderer{
    private int minimapSizeX;
    private int minimapSizeY;
    private int shaderHandle;
    private int shaderPlayerHandle;
    private float viewM[];
    private float projM[];
    private float orthoM[];
    private ObjectDrawer wallRenderer;
    private ObjectDrawer floorRenderer;
    private ObjectDrawer ceilingRenderer;
    private ObjectDrawer borderRenderer;
    private PlayerDrawer triangleRenderer;
    //Player data, probably need to put in other class
    private MazeMap mazeMap;

    public MazePOVRenderer(MazeMap mazeMap){
        super(0,0,0,0);
        viewM = new float[16];
        projM = new float[16];
        orthoM = new float[16];
        this.minimapSizeX = currentScreen.x / 4;
        this.minimapSizeY = currentScreen.y / 4;
        this.mazeMap = mazeMap;
        Matrix.setIdentityM(viewM, 0);
        Matrix.setIdentityM(projM, 0);
        Matrix.setIdentityM(orthoM, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        super.onSurfaceChanged(gl10, w, h);

        float aspect = ((float) w) / ((float) (h == 0 ? 1 : h));
        float aspectMinimap = ((float) w/4) / ((float) (h == 0 ? 1 : h/2));
        Log.i("ASPECT", "Width" + w + " Height" + h + "Aspect ratio: "+aspect);
        Log.i("ASPECT", "Width" + w + " Height" + h + "Aspect ratio minimap: "+aspectMinimap);

        if (context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
            this.minimapSizeX = currentScreen.x/2 ;
            this.minimapSizeY = currentScreen.y/4;
        }else {
            this.minimapSizeX = currentScreen.x / 4;
            this.minimapSizeY = currentScreen.y / 2;
        }

        Matrix.perspectiveM(projM, 0, 45f, aspect, 0.1f, 100f);
        float MAZE_WIDTH  = CELL_SIZE * mazeMap.width;
        float MAZE_LENGTH = CELL_SIZE * mazeMap.length;
        Matrix.orthoM(orthoM, 0,
                (-1f * MAZE_WIDTH / 2) - WALL_SIZE,
                (MAZE_WIDTH / 2) + WALL_SIZE,
                (-1f * MAZE_LENGTH / 2) - WALL_SIZE,
                (MAZE_LENGTH / 2) + WALL_SIZE,
                0.5f, 10f);
        Matrix.setLookAtM(viewM, 0, mazeMap.player.getCamera().x, mazeMap.player.getCamera().y, mazeMap.player.getCamera().z,
                mazeMap.player.getEye().x, mazeMap.player.getEye().y, mazeMap.player.getEye().z,
                0, 1, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);
        InputStream isVPlayer, isFPlayer, isV, isF,isBorder = null, isCube = null, isPlane = null, isCeil = null;
        try {
            isV = context.getAssets().open("cube.glslv");
            isF = context.getAssets().open("cube.glslf");
            isCube = context.getAssets().open("cube.ply");
            isBorder = context.getAssets().open("cube.ply");
            isPlane = context.getAssets().open("plane.ply");
            isCeil = context.getAssets().open("plane.ply");

            isVPlayer = context.getAssets().open("player.glslv");
            isFPlayer = context.getAssets().open("player.glslf");

            shaderHandle = ShaderCompiler.createProgram(isV,isF);
            shaderPlayerHandle = ShaderCompiler.createProgram(isVPlayer,isFPlayer);
        }catch(IOException | NumberFormatException e){
            e.printStackTrace();
            System.exit(-1);
        }
        if(shaderHandle == -1 || shaderPlayerHandle == -1)
            System.exit(-1);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled=false;
        Bitmap bitmap_wall = BitmapFactory.decodeResource(context.getResources(), R.drawable.brickwall,opts);
        if(bitmap_wall!=null)
            Log.v("WALL_RENDERER","bitmap of size " + bitmap_wall.getWidth()+"x"+bitmap_wall.getHeight()+ " loaded " +
                    "with format " + bitmap_wall.getConfig().name());

        Bitmap bitmap_floor = BitmapFactory.decodeResource(context.getResources(), R.drawable.grass,opts);
        Bitmap bitmap_ceiling = BitmapFactory.decodeResource(context.getResources(), R.drawable.rock ,opts);
        Bitmap bitmap_border = BitmapFactory.decodeResource(context.getResources(), R.drawable.border ,opts);
        wallRenderer     = new ObjectDrawer(shaderHandle, bitmap_wall, viewM, projM, orthoM, isCube);
        borderRenderer   = new ObjectDrawer(shaderHandle, bitmap_border, viewM, projM, orthoM,  isBorder);
        floorRenderer    = new ObjectDrawer(shaderHandle, bitmap_floor, viewM, projM, orthoM, isPlane);
        ceilingRenderer  = new ObjectDrawer(shaderHandle, bitmap_ceiling, viewM, projM, orthoM, isCeil);
        triangleRenderer = new PlayerDrawer(shaderPlayerHandle, viewM, orthoM);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);
        glViewport(0,0, currentScreen.x, currentScreen.y);
        Matrix.setLookAtM(viewM, 0, mazeMap.player.getCamera().x, mazeMap.player.getCamera().y, mazeMap.player.getCamera().z,
                mazeMap.player.getEye().x, mazeMap.player.getEye().y, mazeMap.player.getEye().z,
                0, 1, 0);
        drawPOV();
        glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        glViewport(currentScreen.x - minimapSizeX,0, minimapSizeX, minimapSizeY);
        Matrix.setLookAtM(viewM, 0,
                CELL_SIZE*mazeMap.width/2,2f,CELL_SIZE*mazeMap.length/2,
                CELL_SIZE*mazeMap.width/2,0f,CELL_SIZE*mazeMap.length/2, //LookAt center of maze
                1f, 0f,0f
        );
        drawMinimap();
        glClear(GLES20.GL_DEPTH_BUFFER_BIT);
    }

    private void drawPOV(){
        wallRenderer.setOrthoView(false);
        floorRenderer.setOrthoView(false);
        borderRenderer.setOrthoView(false);
        glCullFace(GL_BACK);
        drawFloor();
        drawWalls();
        drawBorders();
        glCullFace(GLES20.GL_FRONT);
        drawCeiling();
    }

    private void drawMinimap(){
        floorRenderer.setOrthoView(true);
        wallRenderer.setOrthoView(true);
        borderRenderer.setOrthoView(true);
        glCullFace(GL_BACK);
        drawBorders();
        drawFloor();
        drawWalls();
        drawPlayer();
        drawFinishTriangle();
    }

    private void drawWalls(){
        for (int i = 0; i < this.mazeMap.width * 3; i++) {
            for (int j = 0; j < this.mazeMap.length * 3; j++){
                if (this.mazeMap.mazeCells[i][j].isWall()){
                    wallRenderer.Draw(
                            this.mazeMap.mazeCells[i][j].getSize(),
                            this.mazeMap.mazeCells[i][j].getPosition(),
                            0, new int[]{0,0,0});
                }
            }
        }
    }

    private void drawBorders(){
        glCullFace(GLES20.GL_BACK);
        borderRenderer.Draw( //TOP BORDER
                new Vector3f(mazeMap.getMazeMapSize().x, 1.0f, WALL_SIZE),
                new Vector3f(mazeMap.getMazeMapSize().x, 0.0f, -WALL_SIZE/2),           //pos
                0, new int[]{0,0,0});            //rotation
        borderRenderer.Draw( //BOTTOM BORDER
                new Vector3f(mazeMap.getMazeMapSize().x, 1.0f, WALL_SIZE),
                new Vector3f(mazeMap.getMazeMapSize().x, 0.0f, CELL_SIZE*mazeMap.length+(WALL_SIZE/2)),           //pos
                0, new int[]{0,0,0});            //rotation
        borderRenderer.Draw( //LEFT BORDER
                new Vector3f(WALL_SIZE, 1.0f, mazeMap.getMazeMapSize().z + WALL_SIZE),
                new Vector3f(-WALL_SIZE/2, 0.0f, mazeMap.getMazeMapSize().z),           //pos
                0, new int[]{0,0,0});            //rotation
        borderRenderer.Draw( //RIGHT BORDER
                new Vector3f(WALL_SIZE, 1.0f, mazeMap.getMazeMapSize().z + WALL_SIZE),
                new Vector3f(CELL_SIZE*mazeMap.length+(WALL_SIZE/2), 0.0f, mazeMap.getMazeMapSize().z),           //pos
                0, new int[]{0,0,0});            //rotation
    }

    private void drawPlayer(){
        glCullFace(GLES20.GL_BACK);
        triangleRenderer.setDrawColor(1,0,0);
        triangleRenderer.Draw( // render player
                new Vector3f(0.6f, 1f, 1f),
                mazeMap.player.getCamera(),
                mazeMap.player.getDirection(), new int[]{0,1,0}
        );
    }

    private void drawFinishTriangle(){
        glCullFace(GLES20.GL_BACK);
        triangleRenderer.setDrawColor(0,0,1);
        triangleRenderer.Draw( // render finish
                new Vector3f(0.6f, 1f, 1f),
                mazeMap.getFinishCellPosition(),
                0, new int[]{0,0,0}
        );
    }

    private void drawFloor(){
        floorRenderer.Draw(
                this.mazeMap.getMazeMapSize(),
                new Vector3f(this.mazeMap.getMazeMapSize().x, -1f, this.mazeMap.getMazeMapSize().z),
                0, new int[]{0,0,0}
        );
    }

    private void drawCeiling(){
        ceilingRenderer.Draw(
                this.mazeMap.getMazeMapSize(),
                new Vector3f(this.mazeMap.getMazeMapSize().x, 1f, this.mazeMap.getMazeMapSize().z),
                0, new int[]{0,0,0}
        );
    }


}
