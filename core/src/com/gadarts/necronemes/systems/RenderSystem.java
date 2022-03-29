package com.gadarts.necronemes.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class RenderSystem extends GameSystem {
	public RenderSystem(SystemsCommonData systemsCommonData) {
		super(systemsCommonData);
	}

	@Override
	public void initializeData( ) {

	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
	}

	@Override
	public void dispose( ) {

	}
}
