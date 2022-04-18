package com.gadarts.necronemes;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necronemes.components.character.CharacterAnimation;
import com.gadarts.necronemes.components.character.CharacterAnimations;
import com.gadarts.necronemes.map.MapBuilder;
import com.gadarts.necronemes.screens.BattleScreen;
import com.gadarts.necronemes.systems.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Necronemes extends Game {
	public static final String BOUNDING_BOX_PREFIX = "box_";

	public static final int FULL_SCREEN_RESOLUTION_WIDTH = 1920;
	public static final int FULL_SCREEN_RESOLUTION_HEIGHT = 1080;
	public static final int WINDOWED_RESOLUTION_WIDTH = 800;
	public static final int WINDOWED_RESOLUTION_HEIGHT = 600;
	private final Map<
			Class<? extends SystemEventsSubscriber>,
			GameSystem<? extends SystemEventsSubscriber>> subscribersInterfaces = new HashMap<>();
	private final String versionName;
	private final int versionNumber;
	private PooledEngine engine;
	private GameAssetsManager assetsManager;
	private SoundPlayer soundPlayer;

	public Necronemes(String versionName, int versionNumber) {
		this.versionName = versionName;
		this.versionNumber = versionNumber;
	}

	@Override
	public void create( ) {
		Gdx.app.setLogLevel(DefaultGameSettings.LOG_LEVEL);
		engine = new PooledEngine();
		setScreen(new BattleScreen(engine));
		initializeAssets();
		soundPlayer = new SoundPlayer(assetsManager);
		SystemsCommonData systemsCommonData = new SystemsCommonData(versionName, versionNumber);
		systemsCommonData.setMap(new MapBuilder(engine, assetsManager).inflateTestMap("mastaba"));
		initializeSystems(systemsCommonData);
	}

	private void initializeAssets( ) {
		assetsManager = new GameAssetsManager();
		assetsManager.loadGameFiles();
		generateCharactersAnimations();
		applyAlphaOnModels();
		generateModelsBoundingBoxes();
	}

	private void generateModelsBoundingBoxes( ) {
		Arrays.stream(Assets.Models.values())
				.forEach(def -> {
					Model model = assetsManager.get(def.getFilePath(), Model.class);
					assetsManager.addAsset(
							BOUNDING_BOX_PREFIX + def.getFilePath(),
							BoundingBox.class,
							model.calculateBoundingBox(new BoundingBox()));
				});
	}

	private void generateCharactersAnimations( ) {
		Arrays.stream(Assets.Atlases.values())
				.forEach(atlas -> assetsManager.addAsset(
						atlas.name(),
						CharacterAnimations.class,
						createCharacterAnimations(atlas)));
	}

	private void inflateCharacterAnimation(final CharacterAnimations animations,
										   final TextureAtlas atlas,
										   final SpriteType spriteType,
										   final Direction dir) {
		String spriteTypeName = spriteType.name().toLowerCase();
		String name = (spriteType.isSingleAnimation()) ? spriteTypeName : spriteTypeName + "_" + dir.name().toLowerCase();
		CharacterAnimation a = createAnimation(atlas, spriteType, name, dir);
		if (a.getKeyFrames().length > 0) {
			animations.put(spriteType, dir, a);
		}
	}

	private CharacterAnimation createAnimation(final TextureAtlas atlas,
											   final SpriteType spriteType,
											   final String name,
											   final Direction dir) {
		return new CharacterAnimation(
				spriteType.getAnimationDuration(),
				atlas.findRegions(name),
				spriteType.getPlayMode(),
				dir);
	}

	private CharacterAnimations createCharacterAnimations(final Assets.Atlases zealot) {
		CharacterAnimations animations = new CharacterAnimations();
		TextureAtlas atlas = assetsManager.getAtlas(zealot);
		Arrays.stream(SpriteType.values()).forEach(spriteType -> {
			if (spriteType.isSingleAnimation()) {
				inflateCharacterAnimation(animations, atlas, spriteType, Direction.SOUTH);
			} else {
				Direction[] directions = Direction.values();
				Arrays.stream(directions).forEach(dir -> inflateCharacterAnimation(animations, atlas, spriteType, dir));
			}
		});
		return animations;
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
			if (sys.getEventsSubscriberClass() != null) {
				subscribersInterfaces.put(sys.getEventsSubscriberClass(), sys);
			}
		});
		systems.forEach((system) -> Arrays.stream(system.getClass().getInterfaces()).forEach(i -> {
			if (subscribersInterfaces.containsKey(i)) {
				EventsNotifier<SystemEventsSubscriber> s = engine.getSystem(subscribersInterfaces.get(i).getClass());
				s.subscribeForEvents((SystemEventsSubscriber) system);
			}
		}));
	}

	private void addSystems(SystemsCommonData systemsCommonData) {
		Arrays.stream(Systems.values()).forEach(systemDefinition -> {
			try {
				Object system = systemDefinition.getSystemClass().getConstructors()[0].newInstance(
						systemsCommonData,
						soundPlayer,
						assetsManager);
				engine.addSystem((GameSystem<? extends SystemEventsSubscriber>) system);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void dispose( ) {
		engine.getSystems().forEach(system -> ((GameSystem<? extends SystemEventsSubscriber>) system).dispose());
		assetsManager.dispose();
	}
}
