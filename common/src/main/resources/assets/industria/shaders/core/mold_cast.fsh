#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 vertexColor;
in vec2 spriteCoord;
in vec2 maskCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, spriteCoord) * vertexColor * ColorModulator;
    color.a *= texture(Sampler1, maskCoord).a;
    if (color.a <= 0.0) {
        discard;
    }
    fragColor = color;
}
