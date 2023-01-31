#version 300 es

precision mediump float;

uniform vec3 colorUni;
out vec4 fragColor;

void main() {
fragColor = vec4(colorUni, 1);
}