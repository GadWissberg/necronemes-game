package com.gadarts.necronemes.systems.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.map.MapGraph;

public class MainShaderProvider extends DefaultShaderProvider {
	private final DefaultShader.Config mainShaderConfig;
	private final MapGraph map;

	public MainShaderProvider(final GameAssetsManager assetsManager, final MapGraph map) {
		this.map = map;
		mainShaderConfig = new DefaultShader.Config();
		mainShaderConfig.vertexShader = assetsManager.getShader(Assets.Shaders.VERTEX);
		mainShaderConfig.fragmentShader = assetsManager.getShader(Assets.Shaders.FRAGMENT);
	}

	@Override
	protected Shader createShader(final Renderable renderable) {
		return new MainShader(renderable, mainShaderConfig, map);
	}
}
