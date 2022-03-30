#if defined(diffuseTextureFlag) || defined(emissiveTextureFlag)
#define textureFlag
#endif

attribute vec3 a_position;
uniform mat4 u_projViewTrans;

#if defined(colorFlag)
varying vec4 v_color;
attribute vec4 a_color;
#endif// colorFlag

#ifdef normalFlag
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
varying vec3 v_normal;
#endif// normalFlag

#ifdef textureFlag
attribute vec2 a_texCoord0;
#endif// textureFlag

#ifdef diffuseTextureFlag
uniform vec4 u_diffuseUVTransform;
varying vec2 v_diffuseUV;
#endif

#ifdef emissiveTextureFlag
uniform vec4 u_emissiveUVTransform;
varying vec2 v_emissiveUV;
#endif

#ifdef boneWeight1Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight1;
#endif//boneWeight1Flag

#ifdef boneWeight2Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight2;
#endif//boneWeight2Flag

#ifdef boneWeight3Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight3;
#endif//boneWeight3Flag

#ifdef boneWeight4Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight4;
#endif//boneWeight4Flag

#ifdef boneWeight5Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight5;
#endif//boneWeight5Flag

#ifdef boneWeight6Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight6;
#endif//boneWeight6Flag

#ifdef boneWeight7Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight7;
#endif//boneWeight7Flag

uniform mat4 u_worldTrans;

#if defined(numBones)
#if numBones > 0
uniform mat4 u_bones[numBones];
#endif//numBones
#endif

#ifdef shininessFlag
uniform float u_shininess;
#else
const float u_shininess = 20.0;
#endif// shininessFlag

#ifdef blendedFlag
uniform float u_opacity;
varying float v_opacity;

#ifdef alphaTestFlag
uniform float u_alphaTest;
varying float v_alphaTest;
#endif//alphaTestFlag
#endif// blendedFlag

#ifdef lightingFlag
varying vec3 v_lightDiffuse;


#ifdef specularFlag
varying vec3 v_lightSpecular;
#endif// specularFlag


#ifdef fogFlag
varying float v_fog;
#endif// fogFlag


#if numDirectionalLights > 0
struct DirectionalLight
{
    vec3 color;
    vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif// numDirectionalLights

#if numPointLights > 0
struct PointLight
{
    vec3 color;
    vec3 position;
};
uniform PointLight u_pointLights[numPointLights];
#endif// numPointLights

#ifdef shadowMapFlag
uniform mat4 u_shadowMapProjViewTrans;
varying vec3 v_shadowMapUv;
#endif//shadowMapFlag

#endif// lightingFlag

varying vec3 v_frag_pos;

void main() {


    #ifdef diffuseTextureFlag
    v_diffuseUV = u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw;
    #endif//diffuseTextureFlag

    #ifdef emissiveTextureFlag
    v_emissiveUV = u_emissiveUVTransform.xy + a_texCoord0 * u_emissiveUVTransform.zw;
    #endif//emissiveTextureFlag


    #if defined(colorFlag)
    v_color = a_color;
    #endif// colorFlag

    #ifdef blendedFlag
    v_opacity = u_opacity;
    #ifdef alphaTestFlag
    v_alphaTest = u_alphaTest;
    #endif//alphaTestFlag
    #endif// blendedFlag

    vec4 pos = u_worldTrans * vec4(a_position, 1.0);

    gl_Position = u_projViewTrans * pos;
    v_frag_pos = vec3(u_worldTrans * vec4(a_position.x, a_position.y, a_position.z, 1.0));

    #ifdef shadowMapFlag
    vec4 spos = u_shadowMapProjViewTrans * pos;
    v_shadowMapUv.xyz = (spos.xyz / spos.w) * 0.5 + 0.5;
    v_shadowMapUv.z = min(v_shadowMapUv.z, 0.998);
    #endif//shadowMapFlag

    #if defined(normalFlag)
    vec3 normal = normalize(u_normalMatrix * a_normal);
    v_normal = normal;
    #endif// normalFlag
    v_lightDiffuse = vec3(0.0);
    #ifdef specularFlag
    v_lightSpecular = vec3(0.0);
    vec3 viewVec = normalize(u_cameraPosition.xyz - pos.xyz);
    #endif// specularFlag

    #if (numDirectionalLights > 0) && defined(normalFlag)
    for (int i = 0; i < numDirectionalLights; i++) {
        vec3 lightDir = -u_dirLights[i].direction;
        float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
        vec3 value = u_dirLights[i].color * NdotL;
        v_lightDiffuse += value;
        #ifdef specularFlag
        float halfDotView = max(0.0, dot(normal, normalize(lightDir + viewVec)));
        v_lightSpecular += value * pow(halfDotView, u_shininess);
        #endif// specularFlag
    }
        #endif// numDirectionalLights
}