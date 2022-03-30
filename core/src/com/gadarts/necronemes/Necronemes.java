package com.gadarts.necronemes;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.screens.BattleScreen;
import com.gadarts.necronemes.systems.*;
import com.gadarts.necronemes.systems.camera.CameraSystem;
import com.gadarts.necronemes.systems.ui.UserInterfaceSystem;
import com.gadarts.necronemes.utils.MapBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Necronemes extends Game {

	public static final int FULL_SCREEN_RESOLUTION_WIDTH = 1920;
	public static final int FULL_SCREEN_RESOLUTION_HEIGHT = 1080;
	public static final int WINDOWED_RESOLUTION_WIDTH = 800;
	public static final int WINDOWED_RESOLUTION_HEIGHT = 600;
	private final Map<Class<? extends SystemEventsSubscriber>, GameSystem<? extends SystemEventsSubscriber>> subscribersInterfaces = new HashMap<>();
	private PooledEngine engine;
	private GameAssetsManager assetsManager;

	@Override
	public void create( ) {
		Gdx.app.setLogLevel(DefaultGameSettings.LOG_LEVEL);
		engine = new PooledEngine();
		setScreen(new BattleScreen(engine));
		initializeAssets();
		SystemsCommonData systemsCommonData = new SystemsCommonData();
		systemsCommonData.setMap(new MapBuilder(engine, assetsManager).inflateTestMap("mastaba"));
		initializeSystems(systemsCommonData);
	}

	private void initializeAssets( ) {
		assetsManager = new GameAssetsManager();
		assetsManager.loadGameFiles();
		applyAlphaOnModels();
	}

	private void applyAlphaOnModels( ) {
		Arrays.stream(Assets.Models.values()).filter(def -> def.getAlpha() < 1.0f)
				.forEach(def -> {
					Material material = assetsManager.getModel(def).materials.get(0);
					BlendingAttribute attribute = new BlendingAttribute();
					material.set(attribute);
					attribute.opacity = def.getAlpha();
				});
	}

	@SuppressWarnings("unchecked")
	private void initializeSystems(SystemsCommonData systemsCommonData) {
		addSystems(systemsCommonData);
		ImmutableArray<EntitySystem> systems = engine.getSystems();
		systems.forEach(system -> ((GameSystem<? extends SystemEventsSubscriber>) system).initializeData());
		systems.forEach((system) -> {
			GameSystem<? extends SystemEventsSubscriber> sys = (GameSystem<? extends SystemEventsSubscriber>) system;
			subscribersInterfaces.put(sys.getEventsSubscriberClass(), sys);
		});
		systems.forEach((system) -> Arrays.stream(system.getClass().getInterfaces()).forEach(i -> {
			if (subscribersInterfaces.containsKey(i)) {
				EventsNotifier<SystemEventsSubscriber> s = engine.getSystem(subscribersInterfaces.get(i).getClass());
				s.subscribeForEvents((SystemEventsSubscriber) system);
			}
		}));
	}

	private void addSystems(SystemsCommonData systemsCommonData) {
		List.of(new CameraSystem(systemsCommonData),
				new InputSystem(systemsCommonData),
				new UserInterfaceSystem(systemsCommonData, assetsManager),
				new InputSystem(systemsCommonData),
				new RenderSystem(systemsCommonData)).forEach(gameSystem -> engine.addSystem(gameSystem));
	}

	@Override
	public void dispose( ) {
		engine.getSystems().forEach(system -> ((GameSystem<? extends SystemEventsSubscriber>) system).dispose());
		assetsManager.dispose();
	}
}
