#version 330 core

uniform float u_time;
uniform vec2 u_resolution;

in vec2 fragCoord;
in vec2 screenPos;

out vec4 fragColor;

float hash(float n) { 
    return fract(sin(n) * 753.5453123); 
}

// Noise function based on Inigo Quilez's work
float noise(in vec2 x) {
    vec2 p = floor(x);
    vec2 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    
    float n = p.x + p.y * 157.0;
    return mix(
        mix(hash(n + 0.0), hash(n + 1.0), f.x),
        mix(hash(n + 157.0), hash(n + 158.0), f.x),
        f.y
    );
}

float fbm(vec2 p, vec3 a) {
    float v = 0.0;
    v += noise(p * a.x) * 0.5;
    v += noise(p * a.y) * 0.25;
    v += noise(p * a.z) * 0.125;
    return v;
}

vec3 drawLines(vec2 uv, vec3 fbmOffset, vec3 color1, vec3 color2) {
    float timeVal = u_time * 0.1;
    vec3 finalColor = vec3(0.0);
    
    // Primary energy lines
    for(int i = 0; i < 3; ++i) {
        float indexAsFloat = float(i);
        float amp = 40.0 + (indexAsFloat * 5.0);
        float period = 2.0 + (indexAsFloat + 2.0);
        float thickness = mix(0.9, 1.0, noise(uv * 10.0));
        float t = abs(0.9 / (sin(uv.x + fbm(uv + timeVal * period, fbmOffset)) * amp) * thickness);
        
        finalColor += t * color1;
    }
    
    // Secondary energy lines
    for(int i = 0; i < 5; ++i) {
        float indexAsFloat = float(i);
        float amp = 40.0;
        float period = 9.0 + (indexAsFloat + 8.0);
        float thickness = mix(0.7, 1.0, noise(uv * 10.0));
        float t = abs(0.8 / (sin(uv.x + fbm(uv + timeVal * period, fbmOffset)) * amp) * thickness);
        
        finalColor += t * color2 * 0.6;
    }
    
    return finalColor;
}

void main() {
    vec2 uv = (screenPos * 2.0 - 1.0);
    uv.x *= u_resolution.x / u_resolution.y;
    uv.xy = uv.yx;

    // Purple/blue energy colors perfect for gaming
    vec3 lineColor1 = vec3(2.3, 0.5, 2.5);   // Purple-pink
    vec3 lineColor2 = vec3(0.3, 0.5, 2.5);   // Blue
    
    vec3 finalColor = vec3(0.0);
    
    // Pulsing effect
    float t = sin(u_time) * 0.5 + 0.5;
    float pulse = mix(0.10, 0.20, t);
    
    // Draw the energy field
    finalColor += drawLines(uv, vec3(1.0, 20.0, 30.0), lineColor1, lineColor2) * pulse;
    finalColor += drawLines(uv, vec3(1.0, 2.0, 4.0), lineColor1, lineColor2);
    
    // Add subtle background gradient
    vec3 bgGradient = vec3(0.05, 0.02, 0.1) * (1.0 - length(uv * 0.5));
    finalColor += bgGradient;
    
    fragColor = vec4(finalColor, 1.0);
}