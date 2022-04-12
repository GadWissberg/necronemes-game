package com.gadarts.necronemes.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.map.MapGraph;

public class MainShader extends DefaultShader {

	private static final String UNIFORM_AFFECTED_BY_LIGHT = "u_affectedByLight";
	private final MapGraph map;
	private int uniformLocationAffectedByLight;

	public MainShader(Renderable renderable, Config mainShaderConfig, MapGraph map) {
		super(renderable, mainShaderConfig);
		this.map = map;
	}

	@Override
	public void init() {
		super.init();
		uniformLocationAffectedByLight = program.getUniformLocation(UNIFORM_AFFECTED_BY_LIGHT);
	}

	@Override
	public void render(Renderable renderable) {
		super.render(renderable);
		Entity userData = (Entity) renderable.userData;
		GameModelInstance modelInstance = ComponentsMapper.modelInstance.get(userData).getModelInstance();
		boolean affectedByLight = modelInstance.getAdditionalRenderData().isAffectedByLight();
		program.setUniformf(uniformLocationAffectedByLight, affectedByLight ? 1F : 0F);
	}
}
