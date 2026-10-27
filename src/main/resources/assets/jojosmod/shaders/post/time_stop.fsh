#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform JojoTimeConfig { vec4 Time; };
layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

// Original anime-inspired negative-color shockwave, followed by a quiet suspended palette.
void main() {
    float age = Time.x;
    float strength = Time.y;
    float world = Time.z;
    vec2 radial = texCoord - 0.5;
    radial.x *= OutSize.x / OutSize.y;
    float radius = length(radial);
    float progress = clamp(age / 0.55, 0.0, 1.0);
    float shell = exp(-pow((radius - progress * 1.25) * 22.0, 2.0)) * (1.0 - progress);
    vec2 direction = normalize(radial + vec2(0.0001));
    vec2 offset = direction * shell * 0.014 * strength;
    vec3 source = texture(InSampler, texCoord).rgb;
    vec3 displaced = vec3(texture(InSampler, texCoord + offset).r,
                          texture(InSampler, texCoord).g,
                          texture(InSampler, texCoord - offset).b);
    float grey = dot(displaced, vec3(0.299, 0.587, 0.114));
    vec3 tint = mix(vec3(0.78, 0.76, 1.02), vec3(1.04, 0.93, 0.67), world);
    vec3 suspended = mix(displaced, grey * tint, 0.88);
    suspended = (suspended - 0.5) * 1.10 + 0.5;
    float negative = (1.0 - smoothstep(0.12, 0.48, age)) * Time.w;
    vec3 color = mix(suspended, (1.0 - displaced) * tint, negative * 0.85);
    color += shell * mix(vec3(0.12, 0.06, 0.21), vec3(0.20, 0.14, 0.02), world);
    color *= 1.0 - smoothstep(0.30, 1.15, radius) * 0.15;
    fragColor = vec4(mix(source, clamp(color, 0.0, 1.0), strength), 1.0);
}
