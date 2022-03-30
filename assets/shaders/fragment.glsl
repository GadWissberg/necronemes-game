#extension GL_EXT_gpu_shader4 : enable
#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#ifdef normalFlag
varying vec3 v_normal;
#endif//normalFlag

#if defined(colorFlag)
varying vec4 v_color;
#endif

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif//alphaTestFlag
#endif//blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

#ifdef diffuseTextureFlag
varying MED vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
varying MED vec2 v_specularUV;
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
#endif

#ifdef emissiveColorFlag
uniform vec4 u_emissiveColor;
#endif

#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#if    defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif//ambientFlag

#ifdef specularFlag
varying vec3 v_lightSpecular;
#endif//specularFlag

#ifdef shadowMapFlag
uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
varying vec3 v_shadowMapUv;
#define separateAmbientFlag


float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));
}

float getShadow()
{
    return (
    getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
    getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
    getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
    getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
}
    #endif//shadowMapFlag

    #if defined(ambientFlag) && defined(separateAmbientFlag)
varying vec3 v_ambientLight;
#endif//separateAmbientFlag

#endif//lightingFlag

#ifdef fogFlag
uniform vec4 u_fogColor;
varying float v_fog;
#endif// fogFlag

varying vec3 v_frag_pos;
uniform vec3 u_lights_positions[8];
uniform vec3 u_lights_extra_data[8];
uniform vec3 u_lights_colors[8];
uniform int u_number_of_lights;
uniform int u_model_width;
uniform int u_model_height;
uniform int u_model_depth;
uniform float u_fow_map[16];
uniform int u_model_x;
uniform float u_model_y;
uniform int u_model_z;
uniform int u_complete_black;

uniform vec3 u_ambient_light;
uniform vec4 u_color_when_outside;
uniform int u_apply_wall_ambient_occlusion;
uniform int u_apply_floor_ambient_occlusion;
uniform vec3 u_skip_color;
uniform float u_screenWidth;
uniform sampler2D u_shadows;
uniform float u_screenHeight;
uniform vec3 u_light_position;
float map(float value, float min1, float max1, float min2, float max2) {
    return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}

void main() {
    #if defined(normalFlag)
    vec3 normal = v_normal;
    #endif// normalFlag

    vec3 modified_skip_color = u_skip_color;

    #if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor * v_color;
    #elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor;
    modified_skip_color = (vec4(modified_skip_color, 1.0) * u_diffuseColor).rgb;
    #elif defined(diffuseTextureFlag) && defined(colorFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * v_color;
    #elif defined(diffuseTextureFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV);
    #elif defined(diffuseColorFlag) && defined(colorFlag)
    vec4 diffuse = u_diffuseColor * v_color;
    #elif defined(diffuseColorFlag)
    vec4 diffuse = u_diffuseColor;
    #elif defined(colorFlag)
    vec4 diffuse = v_color;
    #else
    vec4 diffuse = vec4(1.0);
    #endif

    #if defined(emissiveColorFlag)
    vec4 emissive = u_emissiveColor;
    #else
    vec4 emissive = vec4(0.0);
    #endif

    #if (!defined(lightingFlag))
    gl_FragColor.rgb = diffuse.rgb + emissive.rgb;
    #elif (!defined(specularFlag))
    #if defined(ambientFlag) && defined(separateAmbientFlag)
    if (u_complete_black == 0){
        int numberOfRows = int(max(v_frag_pos.z, 0.0) - float(max(u_model_z, 0)))*u_model_width;
        int horizontalOffset = int(max(v_frag_pos.x, 0.0)-float(max(u_model_x, 0)));
        bool one_unit_size = u_model_width == 1 && u_model_depth == 1;
        int nodeIndex = one_unit_size ? 0 : numberOfRows + horizontalOffset;
        float bias = 0.1;
        bool frag_outside;
        if (one_unit_size){
            frag_outside = v_frag_pos.x < float(u_model_x) - bias
            || v_frag_pos.x - float(u_model_x) - bias >= float(u_model_width) + 2.0*bias
            || v_frag_pos.z < float(u_model_z) - bias
            || v_frag_pos.z - float(u_model_z) - bias >= float(u_model_depth) + 2.0*bias;
        } else {
            frag_outside = v_frag_pos.z - float(u_model_y) >= float(u_model_depth)
            || v_frag_pos.x - float(u_model_x) >= float(u_model_width);
        }

        int frag_fow_value = (frag_outside) ? 1 : int(u_fow_map[nodeIndex]);
        gl_FragColor.rgb = vec3(0.0);
        if (u_model_width == 0 || (frag_fow_value > 0)){
            bool skip_color_disabled = u_skip_color.r == 0.0 && u_skip_color.g == 0.0 && u_skip_color.b == 0.0;

            bool diff_than_skip_color =  modified_skip_color.r != diffuse.r
            || modified_skip_color.g != diffuse.g
            || modified_skip_color.b != diffuse.b;

            if (skip_color_disabled || diff_than_skip_color){
                if (u_number_of_lights > -1){
                    for (int i = 0; i< u_number_of_lights; i++){
                        vec3 light =u_lights_positions[i];
                        vec3 sub = light.xyz - v_frag_pos.xyz;
                        vec3 lightDir = normalize(sub);
                        float distance = length(sub);
                        vec3 extra = u_lights_extra_data[i];
                        if (distance <= extra.y){
                            int light_color_index = int(extra.z);
                            vec3 light_color;
                            if (light_color_index > -1){
                                light_color = vec3(u_lights_colors[light_color_index]);
                            } else {
                                light_color = vec3(1.0);
                            }
                            float attenuation = 4.0 * extra.x / (1.0 + (0.01*distance) + (0.9*distance*distance));
                            float dot_value = dot(v_normal, lightDir);
                            float intensity = max(dot_value, 0.0);
                            vec3 value_to_add = (diffuse.rgb *light_color.rgb* (attenuation * intensity));
                            value_to_add *= distance > (extra.y*5.0/6.0) ? 0.5 : 1.0;
                            gl_FragColor.rgb += value_to_add;
                        }
                    }
                    gl_FragColor.rgb = (getShadow() == 0.0 ? gl_FragColor.rgb * 0.5 : gl_FragColor.rgb) + emissive.rgb;
                }
                gl_FragColor.rgb += diffuse.rgb * (u_ambient_light.rgb + v_lightDiffuse);
            } else {
                gl_FragColor.rgb = diffuse.rgb;
            }

            float flooredX = one_unit_size ? float(u_model_x) : floor(v_frag_pos.x);
            float flooredZ = one_unit_size ? float(u_model_z) : floor(v_frag_pos.z);
            if (u_apply_wall_ambient_occlusion == 1){
                gl_FragColor.rgb *= min(1.0, map(float(v_frag_pos.y - u_model_y), 0.0, float(u_model_height)/10.0, 0.0, 1.0));
            } else if (u_apply_floor_ambient_occlusion > 0) {

                float strength = 8.0;
                float diag = 1.2;
                // East
                if ((u_apply_floor_ambient_occlusion & 1) == 1){
                    gl_FragColor.rgb *= min(strength*(flooredX + 1.0 - v_frag_pos.x), 1.0);
                }

                // South-East
                if ((u_apply_floor_ambient_occlusion & 2) == 2){
                    gl_FragColor.rgb *= min(strength*diag*length(vec3(flooredX+1.0, v_frag_pos.y, flooredZ+1.0) - vec3(v_frag_pos.xyz)), 1.0);
                }

                // South
                if ((u_apply_floor_ambient_occlusion & 4) == 4){
                    gl_FragColor.rgb *= min(strength*(flooredZ + 1.0 - v_frag_pos.z), 1.0);
                }

                // South-West
                if ((u_apply_floor_ambient_occlusion & 8) == 8){
                    gl_FragColor.rgb *= min(strength*diag*length(vec3(v_frag_pos.xyz)- vec3(flooredX, v_frag_pos.y, flooredZ+1.0)), 1.0);
                }

                // West
                if ((u_apply_floor_ambient_occlusion & 16) == 16){
                    gl_FragColor.rgb *= min(strength*(v_frag_pos.x - flooredX), 1.0);
                }

                // North-West
                if ((u_apply_floor_ambient_occlusion & 32) == 32){
                    gl_FragColor.rgb *= min(strength*diag*length(vec3(v_frag_pos.xyz)- vec3(flooredX, v_frag_pos.y, flooredZ)), 1.0);
                }

                // North
                if ((u_apply_floor_ambient_occlusion & 64) == 64){
                    gl_FragColor.rgb *= min(strength*(v_frag_pos.z - flooredZ), 1.0);
                }

                // North-East
                if ((u_apply_floor_ambient_occlusion & 128) == 128){
                    gl_FragColor.rgb *= min(strength*diag*length(vec3(v_frag_pos.xyz)-vec3(flooredX+1.0, v_frag_pos.y, flooredZ)), 1.0);
                }
            }
            if (!frag_outside && u_apply_wall_ambient_occlusion != 1){
                // Bottom-Right
                if ((frag_fow_value & 2) == 0){
                    gl_FragColor.rgb *= min(2.0*length(vec3(flooredX+1.0, 0.0, flooredZ+1.0) - vec3(v_frag_pos.xyz)), 1.0);
                }
                // Bottom
                if ((frag_fow_value & 4) == 0){
                    gl_FragColor.rgb *= vec3(min(2.0*(flooredZ + 1.0 - v_frag_pos.z), 1.0));
                }
                // Bottom-Left
                if ((frag_fow_value & 8) == 0){
                    gl_FragColor.rgb *= min(2.0*length(vec3(v_frag_pos.xyz)- vec3(flooredX, 0.0, flooredZ+1.0)), 1.0);
                }
                // Right
                if ((frag_fow_value & 16) == 0){
                    gl_FragColor.rgb *= vec3(min(2.0*(flooredX + 1.0 - v_frag_pos.x), 1.0));
                }
                // Left
                if ((frag_fow_value & 32) == 0){
                    gl_FragColor.rgb *= vec3(min(2.0*(v_frag_pos.x - flooredX), 1.0));
                }
                // Top-Right
                if ((frag_fow_value & 64) == 0){
                    gl_FragColor.rgb *= min(2.0*length(vec3(v_frag_pos.xyz)-vec3(flooredX+1.0, 0.0, flooredZ)), 1.0);
                }
                // Top
                if ((frag_fow_value & 128) == 0){
                    gl_FragColor.rgb *= vec3(min(2.0*(v_frag_pos.z - flooredZ), 1.0));
                }
                // Top-Left
                if ((frag_fow_value & 256) == 0){
                    gl_FragColor.rgb *= min(2.0*length(vec3(v_frag_pos.xyz)- vec3(flooredX, 0.0, flooredZ)), 1.0);
                }

            } else {
                gl_FragColor.rgb *= u_color_when_outside.rgb;
            }
        } else {
            gl_FragColor.rgb = vec3(0.0);
        }
        if (u_number_of_lights == -1) {
            gl_FragColor.rgb = diffuse.rgb + emissive.rgb;
        }
        vec2 c= gl_FragCoord.xy;
        c.x/=u_screenWidth;
        c.y/=u_screenHeight;
        vec4 color=texture2D(u_shadows, c);
        gl_FragColor.rgb*=(0.4+color.a);
    } else {
        gl_FragColor.rgb = vec3(0.0);
    }
    #endif
    #else
    #if defined(specularTextureFlag) && defined(specularColorFlag)
    vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * u_specularColor.rgb * v_lightSpecular;
    #elif defined(specularTextureFlag)
    vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * v_lightSpecular;
    #elif defined(specularColorFlag)
    vec3 specular = u_specularColor.rgb * v_lightSpecular;
    #else
    vec3 specular = v_lightSpecular;
    #endif

    #if defined(ambientFlag) && defined(separateAmbientFlag)
    #ifdef shadowMapFlag
    gl_FragColor.rgb = (diffuse.rgb * (getShadow() * v_lightDiffuse + v_ambientLight)) + specular + emissive.rgb;
    #else
    gl_FragColor.rgb = (diffuse.rgb * (v_lightDiffuse + v_ambientLight)) + specular + emissive.rgb;
    #endif//shadowMapFlag
    #else
    #ifdef shadowMapFlag
    gl_FragColor.rgb = getShadow() * ((diffuse.rgb * v_lightDiffuse) + specular) + emissive.rgb;
    #else
    gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse) + specular + emissive.rgb;
    #endif//shadowMapFlag
    #endif
    #endif//lightingFlag

    #ifdef fogFlag
    gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog);
    #endif// end fogFlag

    #ifdef blendedFlag
    gl_FragColor.a = diffuse.a * v_opacity;
    #ifdef alphaTestFlag
    if (gl_FragColor.a <= v_alphaTest)
    discard;
    #endif
    #else
    gl_FragColor.a = 1.0;
    #endif

}