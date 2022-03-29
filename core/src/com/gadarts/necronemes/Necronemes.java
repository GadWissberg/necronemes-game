package com.gadarts.necronemes;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Game;
import com.gadarts.necronemes.systems.CameraSystem;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.RenderSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;

public class Necronemes extends Game {

	private PooledEngine engine;
	public static final int FULL_SCREEN_RESOLUTION_WIDTH = 1920;
	public static final int FULL_SCREEN_RESOLUTION_HEIGHT = 1080;
	public static final int WINDOWED_RESOLUTION_WIDTH = 800;
	public static final int WINDOWED_RESOLUTION_HEIGHT = 600;

	@Override
	public void create( ) {
		engine = new PooledEngine();
		SystemsCommonData systemsCommonData = new SystemsCommonData();
		engine.addSystem(new CameraSystem(systemsCommonData));
		engine.addSystem(new RenderSystem(systemsCommonData));
		engine.getSystems().forEach(system -> ((GameSystem)system).initializeData());
	}

	@Override
	public void render( ) {

	}

	@Override
	public void dispose( ) {
		engine.getSystems().forEach(system -> ((GameSystem) system).dispose());
	}
}
