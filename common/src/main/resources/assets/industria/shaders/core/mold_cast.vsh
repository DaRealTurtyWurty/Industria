#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

out vec4 vertexColor;
out vec2 spriteCoord;
out vec2 maskCoord;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    spriteCoord = UV0;
    // UV2 carries the mask's local 0..1 coordinates, rather than lightmap data.
    maskCoord = vec2(UV2);
}
