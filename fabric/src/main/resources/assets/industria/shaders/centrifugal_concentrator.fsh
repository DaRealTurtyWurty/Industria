#version 150

uniform sampler2D Sampler0;   // your water texture
uniform vec2 OuterInner;      // x = outer radius, y = inner radius (in vLocal units)
uniform int  Sides;           // e.g., 8 for octagon

in vec4 vColor;
in vec2 vUv;
in vec2 vLocal;

out vec4 fragColor;

// Regular N-gon signed distance (Inigo Quilez style)
float sdNgon(vec2 p, float r, int n) {
    float ang = 3.14159265/float(n);
    float a = atan(p.y, p.x) + ang;
    float c = cos(mod(a, 2.0*ang) - ang);
    return length(p) * c - r * cos(ang);
}

void main() {
    float dOuter = sdNgon(vLocal, OuterInner.x, Sides);
    float dInner = sdNgon(vLocal, OuterInner.y, Sides);

    // keep: inside outer (<=0) AND outside inner (>0)
    if (dOuter > 0.0 || dInner <= 0.0) discard;

    vec4 tex = texture(Sampler0, vUv);
    fragColor = tex * vColor;
}