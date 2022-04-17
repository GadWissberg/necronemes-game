package com.gadarts.necronemes.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.ShadowlessLightComponent;
import com.gadarts.necronemes.components.mi.AdditionalRenderData;
import com.gadarts.necronemes.components.mi.GameModelInstance;

import java.util.List;

import static com.gadarts.necronemes.components.ComponentsMapper.*;

public class ModelsShader extends DefaultShader {

	private static final String UNIFORM_AFFECTED_BY_LIGHT = "u_affectedByLight";
	private static final String UNIFORM_NUMBER_OF_NEARBY_CHARACTERS = "u_numberOfNearbyCharacters";
	private static final String UNIFORM_NEARBY_CHARACTERS_POSITIONS = "u_nearbyCharactersPositions[0]";
	private static final String UNIFORM_NUMBER_OF_SHADOWLESS_LIGHTS = "u_numberOfShadowlessLights";
	private static final String UNIFORM_SHADOWLESS_LIGHTS_POSITIONS = "u_shadowlessLightsPositions[0]";
	private static final String UNIFORM_SHADOWLESS_LIGHTS_EXTRA_DATA = "u_shadowlessLightsExtraData[0]";
	private static final String UNIFORM_SHADOWLESS_LIGHTS_COLORS = "u_shadowlessLightsColors[0]";
	private static final int MAX_LIGHTS = 16;
	private final static Vector3 auxVector = new Vector3();
	private static final int LIGHT_EXTRA_DATA_SIZE = 3;
	private final static Color auxColor = new Color();
	private static final int MAX_NEARBY_CHARACTERS = 2;
	private final float[] lightsPositions = new float[MAX_LIGHTS * 3];
	private final float[] lightsExtraData = new float[MAX_LIGHTS * LIGHT_EXTRA_DATA_SIZE];
	private final float[] lightsColors = new float[MAX_LIGHTS * 3];
	private final float[] nearbyCharactersPositions = new float[MAX_NEARBY_CHARACTERS * 2];
	private final FrameBuffer shadowFrameBuffer;
	private int uniformLocAffectedByLight;
	private int uniformLocNumberOfNearbyCharacters;
	private int uniformLocNumberOfShadowlessLights;
	private int uniformLocShadowlessLightsPositions;
	private int uniformLocShadowlessLightsExtraData;
	private int uniformLocShadowlessLightsColors;
	private int uniformLocNearbyCharactersPositions;

	public ModelsShader(Renderable renderable, Config mainShaderConfig, FrameBuffer shadowFrameBuffer) {
		super(renderable, mainShaderConfig);
		this.shadowFrameBuffer = shadowFrameBuffer;
	}

	@Override
	public void init( ) {
		super.init();
		final int textureNum = 30;
		shadowFrameBuffer.getColorBufferTexture().bind(textureNum);
		program.bind();
		program.setUniformi("u_shadows", textureNum);
		program.setUniformf("u_screenWidth", Gdx.graphics.getWidth());
		program.setUniformf("u_screenHeight", Gdx.graphics.getHeight());
		uniformLocAffectedByLight = program.getUniformLocation(UNIFORM_AFFECTED_BY_LIGHT);
		uniformLocNumberOfNearbyCharacters = program.getUniformLocation(UNIFORM_NUMBER_OF_NEARBY_CHARACTERS);
		uniformLocNearbyCharactersPositions = program.getUniformLocation(UNIFORM_NEARBY_CHARACTERS_POSITIONS);
		uniformLocNumberOfShadowlessLights = program.getUniformLocation(UNIFORM_NUMBER_OF_SHADOWLESS_LIGHTS);
		uniformLocShadowlessLightsPositions = program.getUniformLocation(UNIFORM_SHADOWLESS_LIGHTS_POSITIONS);
		uniformLocShadowlessLightsExtraData = program.getUniformLocation(UNIFORM_SHADOWLESS_LIGHTS_EXTRA_DATA);
		uniformLocShadowlessLightsColors = program.getUniformLocation(UNIFORM_SHADOWLESS_LIGHTS_COLORS);
		if (program.getLog().length() != 0) {
			System.out.println(program.getLog());
		}
	}

	private void applyLights(final AdditionalRenderData renderData) {
		if (renderData.isAffectedByLight()) {
			program.setUniformi(uniformLocNumberOfShadowlessLights, renderData.getNearbyLights().size());
			if (!renderData.getNearbyLights().isEmpty()) {
				int differentColorIndex = 0;
				for (int i = 0; i < Math.min(renderData.getNearbyLights().size(), MAX_LIGHTS); i++) {
					differentColorIndex = insertToLightsArray(renderData.getNearbyLights(), i, differentColorIndex);
				}
				applyLightsDataUniforms(renderData);
			}
		}
	}

	private void applyLightsDataUniforms(final AdditionalRenderData renderData) {
		int size = renderData.getNearbyLights().size();
		program.setUniform3fv(uniformLocShadowlessLightsPositions, lightsPositions, 0, size * 3);
		int extraDataLength = size * LIGHT_EXTRA_DATA_SIZE;
		program.setUniform3fv(uniformLocShadowlessLightsExtraData, lightsExtraData, 0, extraDataLength);
		program.setUniform3fv(uniformLocShadowlessLightsColors, this.lightsColors, 0, size * 3);
	}

	private void insertLightPositionToArray(final List<Entity> nearbyLights, final int i) {
		ShadowlessLightComponent lightComponent = shadowlessLight.get(nearbyLights.get(i));
		Vector3 position = lightComponent.getPosition(auxVector);
		int positionIndex = i * 3;
		lightsPositions[positionIndex] = position.x;
		lightsPositions[positionIndex + 1] = position.y;
		lightsPositions[positionIndex + 2] = position.z;
	}

	private int insertToLightsArray(final List<Entity> nearbyLights, final int i, final int differentColorIndex) {
		insertLightPositionToArray(nearbyLights, i);
		boolean notWhite = insertExtraDataToArray(nearbyLights, i, differentColorIndex);
		if (notWhite) {
			insertColorToArray(nearbyLights.get(i), differentColorIndex);
		}
		return notWhite ? differentColorIndex + 1 : differentColorIndex;
	}

	private void insertColorToArray(final Entity light, final int i) {
		ShadowlessLightComponent lightComponent = shadowlessLight.get(light);
		int colorIndex = i * 3;
		Color color = lightComponent.getColor(auxColor);
		lightsColors[colorIndex] = color.r;
		lightsColors[colorIndex + 1] = color.g;
		lightsColors[colorIndex + 2] = color.b;
	}

	private boolean insertExtraDataToArray(final List<Entity> nearbyLights, final int i, final int differentColorIndex) {
		ShadowlessLightComponent lightComponent = shadowlessLight.get(nearbyLights.get(i));
		int extraDataInd = i * LIGHT_EXTRA_DATA_SIZE;
		float intensity = lightComponent.getIntensity();
		float radius = lightComponent.getRadius();
		lightsExtraData[extraDataInd] = intensity;
		lightsExtraData[extraDataInd + 1] = radius;
		boolean notWhite = lightComponent.getColor(auxColor).equals(Color.WHITE);
		lightsExtraData[extraDataInd + 2] = notWhite ? differentColorIndex : -1F;
		return notWhite;
	}

	@Override
	public void render(Renderable renderable) {
		Entity userData = (Entity) renderable.userData;
		GameModelInstance modelInstance = ComponentsMapper.modelInstance.get(userData).getModelInstance();
		AdditionalRenderData additionalRenderData = modelInstance.getAdditionalRenderData();
		boolean affectedByLight = additionalRenderData.isAffectedByLight();
		applyLights(additionalRenderData);
		program.setUniformf(uniformLocAffectedByLight, affectedByLight ? 1F : 0F);
		renderCharacterShadows(renderable);
		super.render(renderable);
	}

	private void renderCharacterShadows(Renderable renderable) {
		if (floor.has((Entity) renderable.userData)) {
			List<Entity> nearbyCharacters = floor.get((Entity) renderable.userData).getNearbyCharacters();
			int size = nearbyCharacters.size();
			program.setUniformi(uniformLocNumberOfNearbyCharacters, size);
			for (int i = 0; i < size; i++) {
				Vector3 position = characterDecal.get(nearbyCharacters.get(i)).getDecal().getPosition();
				nearbyCharactersPositions[i * 2] = position.x;
				nearbyCharactersPositions[i * 2 + 1] = position.z;
			}
			program.setUniform2fv(uniformLocNearbyCharactersPositions, this.nearbyCharactersPositions, 0, size * 2);
		}else{
			program.setUniformi(uniformLocNumberOfNearbyCharacters, 0);
		}
	}
}
