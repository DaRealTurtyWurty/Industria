#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vColor;
out vec2 vUv;
out vec2 vLocal;

void main() {
    vec4 mv = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * mv;
    vColor = Color;
    vUv = UV0;

    vLocal = Position.xz * 2.0;
}