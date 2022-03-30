package com.gadarts.necronemes.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.systems.input.InputSystemEventsSubscriber;

public class InputSystem extends GameSystem<InputSystemEventsSubscriber> implements InputProcessor {
	private CameraInputController debugInput;

	public InputSystem(SystemsCommonData systemsCommonData) {
		super(systemsCommonData);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		if (debugInput != null) {
			debugInput.update();
		}
	}

	@Override
	public Class<InputSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return InputSystemEventsSubscriber.class;
	}

	@Override
	public void initializeData( ) {
		initializeInputProcessor();
	}

	private void initializeInputProcessor( ) {
		InputProcessor input;
		if (DefaultGameSettings.DEBUG_INPUT) {
			input = createDebugInput();
		} else {
			input = createMultiplexer();
		}
		Gdx.input.setInputProcessor(input);
	}

	private InputProcessor createMultiplexer( ) {
		InputProcessor input;
		InputMultiplexer multiplexer = new InputMultiplexer();
		input = multiplexer;
		multiplexer.addProcessor(this);
		return input;
	}

	private InputProcessor createDebugInput( ) {
		InputProcessor input;
		debugInput = new CameraInputController(getSystemsCommonData().getCamera());
		input = debugInput;
		debugInput.autoUpdate = true;
		return input;
	}

	@Override
	public void dispose( ) {

	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		if (DefaultGameSettings.DEBUG_INPUT) return false;
		for (InputSystemEventsSubscriber subscriber : subscribers) {
			subscriber.mouseMoved(screenX, screenY);
		}
		return true;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}
