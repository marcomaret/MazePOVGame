package com.example.mazepovgame.gles.renderer;

import static android.opengl.GLES10.GL_TEXTURE0;
import static android.opengl.GLES10.glGetString;
import static android.opengl.GLES11Ext.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_EXTENSIONS;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_REPEAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetFloatv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mazepovgame.gles.renderer.utils.PlyObject;
import com.example.mazepovgame.utils.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ObjectDrawer {
    protected int VAO[];
    protected int texObjId[];
    protected int MVPLoc;
    protected int texUni;
    protected int shaderHandle;
    protected int countFacesElement;

    protected float modelM[];
    protected float projM[];
    protected float viewM[];
    protected float orthoM[];
    protected float temp[];
    protected float MVP[];

    protected boolean drawOrtho;

    protected Bitmap bitmap;


    public ObjectDrawer(int shaderHandle, Bitmap bitmap, float[] viewM, float[] projM, float[] orthoM, InputStream is){
        this.shaderHandle = shaderHandle;
        this.VAO = new int[1]; //wall
        this.texObjId = new int[1]; //texture
        this.modelM = new float[16];
        this.MVP = new float[16];
        this.temp = new float[16];
        this.orthoM = orthoM;
        this.viewM = viewM;
        this.projM = projM;
        Matrix.setIdentityM(modelM, 0);
        Matrix.setIdentityM(MVP, 0);
        this.bitmap = bitmap; //image for texture
        this.drawOrtho = false;
        initRenderData(is);
    }

    private void initRenderData(InputStream is){
        float[] vertices=null;
        int[] indices=null;
        try{
            PlyObject po = new PlyObject(is);
            po.parse();
            vertices = po.getVertices();
            indices = po.getIndices();

        } catch (IOException | NumberFormatException e){
            e.printStackTrace();
            System.exit(-1);
        }
        if (vertices == null || indices == null){
            Log.i("OBJECT_RENDERER", "ERROR PARSING PlyObject");
            System.exit(-1);
        }
        countFacesElement = indices.length;
        FloatBuffer vertexData = ByteBuffer.allocateDirect(vertices.length * Float.BYTES).
                order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);
        IntBuffer indexData = ByteBuffer.allocateDirect(indices.length * Integer.BYTES).
                order(ByteOrder.nativeOrder())
                .asIntBuffer();
        indexData.put(indices);
        indexData.position(0);
        int attrPos = glGetAttribLocation(shaderHandle, "vPos");
        int texPos = glGetAttribLocation(shaderHandle, "texCoord");
        MVPLoc = glGetUniformLocation(shaderHandle, "MVP");
        GLES30.glGenVertexArrays(1, this.VAO, 0);
        GLES30.glBindVertexArray(VAO[0]);
        int[] VBO = new int[2];
        glGenBuffers(2, VBO, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBO[0]);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * vertexData.capacity(), vertexData, GL_STATIC_DRAW);
        glVertexAttribPointer(attrPos, 3, GL_FLOAT, false, Float.BYTES * 5, 0);
        glVertexAttribPointer(texPos, 2, GL_FLOAT, false, Float.BYTES * 5, Float.BYTES*3);
        glEnableVertexAttribArray(attrPos);
        glEnableVertexAttribArray(texPos);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBO[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indexData.capacity(), indexData, GL_STATIC_DRAW);
        GLES30.glBindVertexArray(0);
        setupTexture();
        activateTexture();
    }

    private void setupTexture(){
        texUni = glGetUniformLocation(shaderHandle, "tex");
        glGenTextures(1, texObjId, 0);
        glBindTexture(GL_TEXTURE_2D, texObjId[0]);

        String extensions = glGetString(GL_EXTENSIONS);
        if(extensions.contains("anisotropic")){
            Log.v("TEXTURE","Anisotropic filtering supported");
            float maxAF[] = new float[1];
            glGetFloatv(GLES11Ext.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAF, 0);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAF[0]);
        }
        else  Log.v("TEXTURE","Anisotropic filtering NOT supported. " +
                "(Might be false if an emulator is used...)");

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D,0,bitmap,0);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,0);
    }

    private void activateTexture(){
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,texObjId[0]);
        glUseProgram(shaderHandle);
        glUniform1i(texUni,0);
        glUseProgram(0);
        glBindTexture(GL_TEXTURE_2D,0);
        bitmap.recycle();
    }

    public void setOrthoView(boolean flag){
        this.drawOrtho = flag;
    }

    public void Draw(Vector3f size, Vector3f pos, float rdegree, @NonNull int[] raxis){
        glUseProgram(shaderHandle);

        if (drawOrtho) Matrix.multiplyMM(temp, 0, orthoM, 0, viewM, 0);
        else Matrix.multiplyMM(temp, 0, projM, 0, viewM, 0);

        Matrix.setIdentityM(modelM, 0);
        Matrix.translateM(modelM, 0, pos.x, pos.y, pos.z);
        if (rdegree != 0.0f){
            Matrix.translateM(modelM, 0, 0.5f*size.x, 0.5f*size.y, 0.5f*size.z); // move origin of rotation to center of cube
            Matrix.rotateM(modelM, 0, rdegree, raxis[0], raxis[1], raxis[2]); //rotate
            Matrix.translateM(modelM, 0, -0.5f*size.x, -0.5f*size.y, -0.5f*size.z); // move origin back
        }
        Matrix.scaleM(modelM, 0, size.x, size.y, size.z);
        Matrix.multiplyMM(MVP, 0, temp, 0, modelM,0); // m * v * p
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjId[0]);
        GLES30.glBindVertexArray(this.VAO[0]);
        glUniformMatrix4fv(MVPLoc, 1, false, MVP, 0);
        glDrawElements(GL_TRIANGLES, countFacesElement, GL_UNSIGNED_INT, 0);
        GLES30.glBindVertexArray(0);
        glUseProgram(0);
    }
}
