#version 300 es

in vec3 vPos;
in vec2 texCoord;
out vec2 varyingTexCoord;
uniform mat4 MVP;

void main(){
varyingTexCoord = texCoord;
gl_Position = MVP * vec4(vPos,1);
}
