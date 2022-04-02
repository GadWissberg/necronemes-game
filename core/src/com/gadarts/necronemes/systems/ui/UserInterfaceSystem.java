package com.gadarts.necronemes.systems.ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.input.InputSystemEventsSubscriber;
import com.gadarts.necronemes.utils.EntityBuilder;

import static com.gadarts.necronemes.DefaultGameSettings.FULL_SCREEN;
import static com.gadarts.necronemes.Necronemes.*;

public class UserInterfaceSystem extends GameSystem<UserInterfaceSystemEventsSubscriber> implements InputSystemEventsSubscriber {
	private final static BoundingBox auxBoundingBox = new BoundingBox();
	private final static Vector3 auxVector3_2 = new Vector3();
	private final GameAssetsManager assetsManager;
	private final SoundPlayer soundPlayer;
	private CursorHandler cursorHandler;

	public UserInterfaceSystem(SystemsCommonData systemsCommonData,
							   GameAssetsManager assetsManager,
							   SoundPlayer soundPlayer) {
		super(systemsCommonData, soundPlayer);
		this.assetsManager = assetsManager;
		this.soundPlayer = soundPlayer;
	}

	private void createUiStage( ) {
		int width = FULL_SCREEN ? FULL_SCREEN_RESOLUTION_WIDTH : WINDOWED_RESOLUTION_WIDTH;
		int height = FULL_SCREEN ? FULL_SCREEN_RESOLUTION_HEIGHT : WINDOWED_RESOLUTION_HEIGHT;
		FitViewport fitViewport = new FitViewport(width, height);
		Entity player = getSystemsCommonData().getPlayer();
		GameStage stage = new GameStage(fitViewport, ComponentsMapper.player.get(player), soundPlayer);
		getSystemsCommonData().setUiStage(stage);
		stage.setDebugAll(DefaultGameSettings.DISPLAY_HUD_OUTLINES);
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {
		MapGraph map = getSystemsCommonData().getMap();
		MapGraphNode newNode = map.getRayNode(screenX, screenY, getSystemsCommonData().getCamera());
		ModelInstance cursorModelInstance = cursorHandler.getCursorModelInstance();
		MapGraphNode oldNode = map.getNode(cursorModelInstance.transform.getTranslation(auxVector3_2));
		if (newNode != null && !newNode.equals(oldNode)) {
			cursorHandler.onMouseEnteredNewNode(newNode);
		}
	}


	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		getSystemsCommonData().getUiStage().act();
		cursorHandler.handleCursorFlicker(deltaTime);
	}

	@Override
	public Class<UserInterfaceSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return UserInterfaceSystemEventsSubscriber.class;
	}

	@Override
	public void initializeData( ) {
		createUiStage();
		getSystemsCommonData().setCursor(createAndAdd3dCursor());
		cursorHandler = new CursorHandler(getSystemsCommonData());
		cursorHandler.init();
	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (getSystemsCommonData().isCameraIsRotating()) return;
		if (button == Input.Buttons.LEFT && getSystemsCommonData().getCurrentCommand() == null) {
			onUserSelectedNodeToApplyTurn();
		}
	}

	private void onUserSelectedNodeToApplyTurn( ) {
		MapGraphNode cursorNode = cursorHandler.getCursorNode();
		for (UserInterfaceSystemEventsSubscriber sub : subscribers) {
			sub.onUserSelectedNodeToApplyTurn(cursorNode);
		}
	}

	private Entity createAndAdd3dCursor( ) {
		Model model = assetsManager.getModel(Assets.Models.CURSOR);
		model.calculateBoundingBox(auxBoundingBox);
		return EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addModelInstanceComponent(new GameModelInstance(model, auxBoundingBox, false), true, false)
				.finishAndAddToEngine();
	}

	@Override
	public void dispose( ) {
		cursorHandler.dispose();
	}

}
