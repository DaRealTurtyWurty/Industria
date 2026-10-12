#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 spriteCoord;
layout(location = 2) in vec2 maskCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, spriteCoord) * vertexColor * ColorModulator;
    color.a *= texture(Sampler1, maskCoord).a;
    if (color.a <= 0.0) {
        discard;
    }
    fragColor = color;
}
