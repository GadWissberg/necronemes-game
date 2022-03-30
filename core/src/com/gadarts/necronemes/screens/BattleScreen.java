package com.gadarts.necronemes.screens;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Screen;

public class BattleScreen implements Screen {
	private final PooledEngine engine;

	public BattleScreen(PooledEngine engine) {
		this.engine = engine;
	}

	@Override
	public void show( ) {

	}

	@Override
	public void render(float delta) {
		engine.update(delta);
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause( ) {

	}

	@Override
	public void resume( ) {

	}

	@Override
	public void hide( ) {

	}

	@Override
	public void dispose( ) {

	}
}
