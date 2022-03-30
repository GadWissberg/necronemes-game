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

uniform sampler2D u_depthMapDir;
uniform samplerCube u_depthMapCube;
uniform float u_cameraFar;
uniform vec3 u_lightPosition;
uniform float u_type;


varying vec4 v_position;
varying vec4 v_positionLightTrans;

void main()
{
    // Default is to not add any color
    float intensity=0.0;
    // Vector light-current position
    vec3 lightDirection=v_position.xyz-u_lightPosition;
    float lenToLight=length(lightDirection)/u_cameraFar;
    // By default assume shadow
    float lenDepthMap=-1.0;

    // Directional light, check if in field of view and get the depth
    if (u_type==1.0){
        vec3 depth = (v_positionLightTrans.xyz / v_positionLightTrans.w)*0.5+0.5;
        if (v_positionLightTrans.z>=0.0 && (depth.x >= 0.0) && (depth.x <= 1.0) && (depth.y >= 0.0) && (depth.y <= 1.0)) {
            lenDepthMap = texture2D(u_depthMapDir, depth.xy).a;
        }
    }
    // Point light, just get the depth given light vector
    else if (u_type==2.0){
        lenDepthMap = textureCube(u_depthMapCube, lightDirection).a;
    }

    if (lenDepthMap>lenToLight-0.0022 && lenToLight < 0.4){
        vec3 light_color = vec3(1.0);
        float attenuation = 4.0 / (1.0 + (0.01*lenToLight) + (128.0*lenToLight*lenToLight));
        float value_to_add = (attenuation);
        intensity += value_to_add;
    } else if (lenToLight > 0.5 && lenToLight < 1.5){
        intensity=0.001*(1.0-lenToLight) + 0.0001*(1.0-lenToLight)*(1.0-lenToLight)*(1.0-lenToLight);
    }

    gl_FragColor     = vec4(intensity);

}

