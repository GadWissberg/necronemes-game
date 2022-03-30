package com.gadarts.necronemes.systems.ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.input.InputSystemEventsSubscriber;
import com.gadarts.necronemes.utils.EntityBuilder;

public class UserInterfaceSystem extends GameSystem implements InputSystemEventsSubscriber {
	private final static BoundingBox auxBoundingBox = new BoundingBox();
	private final static Vector3 auxVector3_2 = new Vector3();
	private final GameAssetsManager assetsManager;
	private CursorHandler cursorHandler;

	public UserInterfaceSystem(SystemsCommonData systemsCommonData, GameAssetsManager assetsManager) {
		super(systemsCommonData);
		this.assetsManager = assetsManager;
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
		cursorHandler.handleCursorFlicker(deltaTime);
	}

	@Override
	public Class getEventsSubscriberClass( ) {
		return null;
	}

	@Override
	public void initializeData( ) {
		getSystemsCommonData().setCursor(createAndAdd3dCursor());
		cursorHandler = new CursorHandler(getSystemsCommonData());
		cursorHandler.init();
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

	@Override
	public void subscribeForEvents(Object sub) {

	}
}
