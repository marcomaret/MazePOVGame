package com.example.mazepovgame.utils;

public class Vector3f {
    public float x;
    public float y;
    public float z;

    public Vector3f(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(){
        this(0,0,0);
    }

    public void rotateBy(float angle){
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        this.x += cos;
        this.z -= sin;
    }

    public Vector3f getRotatedBy(float angle){
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Vector3f(this.x + cos, this.y, this.z - sin);
    }

    @Override
    public String toString() {
        return "Vector3f{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
