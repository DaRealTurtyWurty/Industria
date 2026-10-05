#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in vec4 vertexColor;
in vec2 texCoord;
flat in float lineHalfLength;

out vec4 fragColor;

void main() {
    // Distance to the finite segment, including round ends rather than square cutoffs.
    float distance = length(vec2(max(abs(texCoord.x) - lineHalfLength, 0.0), texCoord.y));
    float core = 1.0 - smoothstep(0.08, 0.18, distance);
    float halo = exp(-distance * distance * 6.0) * (1.0 - smoothstep(0.75, 1.0, distance));

    vec4 color = vertexColor * ColorModulator;
    fragColor = vec4(mix(color.rgb, vec3(1.0), core * 0.7), color.a * max(core, halo * 0.45));
}
