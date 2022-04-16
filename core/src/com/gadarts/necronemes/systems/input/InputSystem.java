package com.gadarts.necronemes.systems.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;

public class InputSystem extends GameSystem<InputSystemEventsSubscriber> implements InputProcessor {
	private CameraInputController debugInput;

	public InputSystem(SystemsCommonData systemsCommonData, SoundPlayer soundPlayer, GameAssetsManager assetsManager) {
		super(systemsCommonData, soundPlayer, assetsManager);
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
		addInputProcessor(getSystemsCommonData().getUiStage());
	}

	private void addInputProcessor(final InputProcessor inputProcessor) {
		if (DefaultGameSettings.DEBUG_INPUT) return;
		InputMultiplexer inputMultiplexer = (InputMultiplexer) Gdx.input.getInputProcessor();
		inputMultiplexer.addProcessor(0, inputProcessor);
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
		if (DefaultGameSettings.DEBUG_INPUT) return false;
		for (InputSystemEventsSubscriber subscriber : subscribers) {
			subscriber.keyDown(keycode);
		}
		return true;
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
	public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
		if (DefaultGameSettings.DEBUG_INPUT) return false;
		for (InputSystemEventsSubscriber subscriber : subscribers) {
			subscriber.touchDown(screenX, screenY, button);
		}
		return true;
	}

	@Override
	public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
		if (DefaultGameSettings.DEBUG_INPUT) return false;
		for (InputSystemEventsSubscriber subscriber : subscribers) {
			subscriber.touchUp(screenX, screenY, button);
		}
		return true;
	}

	@Override
	public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
		if (DefaultGameSettings.DEBUG_INPUT) return false;
		for (InputSystemEventsSubscriber subscriber : subscribers) {
			subscriber.touchDragged(screenX, screenY);
		}
		return true;
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
