package com.example.mazepovgame.gles.renderer;


import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

import android.opengl.GLES30;
import android.opengl.Matrix;

import androidx.annotation.NonNull;

import com.example.mazepovgame.utils.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class PlayerDrawer {
    private int VAO[];
    private int colorLoc;
    private int MVPLoc;

    private int shaderHandle;
    protected int countFacesElement;

    protected float modelM[];
    protected float viewM[];
    protected float orthoM[];
    protected float temp[];
    protected float MVP[];

    private Vector3f color;

    public PlayerDrawer(int shaderHandle, float[] viewM, float[] orthoM){
        this.VAO = new int[1];
        this.shaderHandle = shaderHandle;
        this.viewM = viewM;
        this.orthoM = orthoM;
        this.temp = new float[16];
        this.modelM = new float[16];
        this.MVP = new float[16];
        this.color = new Vector3f();
        Matrix.setIdentityM(modelM, 0);
        Matrix.setIdentityM(MVP, 0);
        initRenderData();
    }

    private void initRenderData(){
        float vertices[] = new float[]{
                -1f, 0f, -1f,
                -1f, 0f, 1f,
                1f, 0f, 0f
        };

        int indices[] = new int[]{
                0,1,2
        };

        FloatBuffer vertexData =
                ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);

        IntBuffer indexData =
                ByteBuffer.allocateDirect(indices.length * Integer.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asIntBuffer();
        indexData.put(indices);
        indexData.position(0);

        countFacesElement = indices.length;
        colorLoc = glGetUniformLocation(shaderHandle, "colorUni");
        MVPLoc = glGetUniformLocation(shaderHandle, "MVP");
        int attrPos = glGetAttribLocation(shaderHandle, "vPos");
        GLES30.glGenVertexArrays(1, VAO, 0);
        GLES30.glBindVertexArray(VAO[0]);

        int VBO[] = new int[2]; //0: vpos, 1: indices

        glGenBuffers(2, VBO, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBO[0]);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * vertexData.capacity(),
                vertexData, GL_STATIC_DRAW);
        glVertexAttribPointer(attrPos, 3, GL_FLOAT, false,  0, 0); //vpos
        glEnableVertexAttribArray(attrPos);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBO[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indexData.capacity(), indexData,
                GL_STATIC_DRAW);
        GLES30.glBindVertexArray(0);
    }

    public void setDrawColor(float r, float g, float b){
        this.color.x = r;
        this.color.y = g;
        this.color.z = b;
    }

    public void Draw(@NonNull Vector3f size, @NonNull Vector3f pos, float rdegree, @NonNull int[] raxis){
        glUseProgram(shaderHandle);
        Matrix.multiplyMM(temp, 0 ,orthoM, 0, viewM,0);
        Matrix.setIdentityM(modelM, 0);
        Matrix.translateM(modelM, 0, pos.x, pos.y, pos.z);

        if (rdegree != 0.0f){
            Matrix.rotateM(modelM, 0, rdegree, raxis[0], raxis[1], raxis[2]); //rotate
        }

        Matrix.scaleM(modelM, 0, size.x, size.y, size.z);
        Matrix.multiplyMM(MVP, 0, temp, 0, modelM,0); // m * v * p

        GLES30.glBindVertexArray(this.VAO[0]);
        glUniformMatrix4fv(MVPLoc, 1, false, MVP, 0);
        if (this.color != null) glUniform3f(colorLoc, this.color.x, this.color.y, this.color.z);
        else glUniform3f(colorLoc, 1, 0, 0); //default color is red
        glDrawElements(GL_TRIANGLES, countFacesElement, GL_UNSIGNED_INT, 0);
        GLES30.glBindVertexArray(0);

        glUseProgram(0);
    }

}
