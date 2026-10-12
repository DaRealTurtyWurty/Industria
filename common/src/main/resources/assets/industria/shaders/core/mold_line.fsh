#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 texCoord;
layout(location = 2) flat in float lineHalfLength;

layout(location = 0) out vec4 fragColor;

void main() {
    // Distance to the finite segment, including round ends rather than square cutoffs.
    float distance = length(vec2(max(abs(texCoord.x) - lineHalfLength, 0.0), texCoord.y));
    float core = 1.0 - smoothstep(0.08, 0.18, distance);
    float halo = exp(-distance * distance * 6.0) * (1.0 - smoothstep(0.75, 1.0, distance));

    vec4 color = vertexColor * ColorModulator;
    fragColor = vec4(mix(color.rgb, vec3(1.0), core * 0.7), color.a * max(core, halo * 0.45));
}
