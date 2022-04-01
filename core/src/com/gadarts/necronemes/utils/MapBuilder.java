package com.gadarts.necronemes.utils;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.assets.MapJsonKeys;
import com.gadarts.necromine.model.characters.CharacterDefinition;
import com.gadarts.necromine.model.characters.CharacterTypes;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.attributes.Accuracy;
import com.gadarts.necromine.model.characters.attributes.Agility;
import com.gadarts.necromine.model.characters.attributes.Strength;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.components.character.*;
import com.gadarts.necronemes.components.player.Weapon;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.IntStream;

import static com.gadarts.necromine.assets.Assets.Atlases.PLAYER_GENERIC;
import static com.gadarts.necromine.assets.Assets.Atlases.findByRelatedWeapon;
import static com.gadarts.necromine.assets.Assets.SurfaceTextures.MISSING;
import static com.gadarts.necromine.assets.MapJsonKeys.*;
import static com.gadarts.necromine.model.characters.CharacterTypes.BILLBOARD_Y;
import static com.gadarts.necromine.model.characters.CharacterTypes.PLAYER;
import static com.gadarts.necromine.model.characters.SpriteType.IDLE;
import static java.lang.String.format;

public class MapBuilder implements Disposable {
	public static final int PLAYER_HEALTH = 64;

	public static final String MAP_PATH_TEMP = "assets/maps/%s.json";
	private static final CharacterSoundData auxCharacterSoundData = new CharacterSoundData();
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final Vector3 auxVector3_4 = new Vector3();
	private static final Vector3 auxVector3_5 = new Vector3();
	private final static BoundingBox auxBoundingBox = new BoundingBox();
	private final Model floorModel;
	private final PooledEngine engine;
	private final GameAssetsManager assetsManager;
	private final Gson gson = new Gson();

	public MapBuilder(PooledEngine engine, GameAssetsManager assetsManager) {
		this.engine = engine;
		this.assetsManager = assetsManager;
		floorModel = createFloorModel();
	}

	private Model createFloorModel( ) {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder meshPartBuilder = modelBuilder.part("floor",
				GL20.GL_TRIANGLES,
				Usage.Position | Usage.Normal | Usage.TextureCoordinates,
				createFloorMaterial());
		createRect(meshPartBuilder);
		return modelBuilder.end();
	}

	/**
	 * Creates the test map.
	 *
	 * @return The Inflated map.
	 */
	public MapGraph inflateTestMap(final String map) {
		JsonObject mapJsonObj = gson.fromJson(Gdx.files.internal(format(MAP_PATH_TEMP, map)).reader(), JsonObject.class);
		MapGraph mapGraph = createMapGraph(mapJsonObj);
		inflateAllElements(mapJsonObj, mapGraph);
		return mapGraph;
	}

	private void inflateAllElements(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		inflateCharacters(mapJsonObject, mapGraph);
	}

	private Weapon initializeStartingWeapon( ) {
		Weapon weapon = Pools.obtain(Weapon.class);
		Texture image = assetsManager.getTexture(DefaultGameSettings.STARTING_WEAPON.getImage());
		weapon.init(DefaultGameSettings.STARTING_WEAPON, 0, 0, image);
		return weapon;
	}

	private Vector3 inflateCharacterPosition(final JsonElement characterJsonElement, final MapGraph mapGraph) {
		JsonObject asJsonObject = characterJsonElement.getAsJsonObject();
		int col = asJsonObject.get(COL).getAsInt();
		int row = asJsonObject.get(ROW).getAsInt();
		float floorHeight = mapGraph.getNode(col, row).getHeight();
		return auxVector3_1.set(col + 0.5f, floorHeight + BILLBOARD_Y, row + 0.5f);
	}

	private void inflatePlayer(final JsonObject characterJsonObject, final MapGraph mapGraph) {
		Weapon weapon = initializeStartingWeapon();
		CharacterAnimations general = assetsManager.get(PLAYER_GENERIC.name());
		EntityBuilder builder = EntityBuilder.beginBuildingEntity(engine).addPlayerComponent(weapon, general);
		Vector3 position = inflateCharacterPosition(characterJsonObject, mapGraph);
		auxCharacterSoundData.set(Assets.Sounds.PLAYER_PAIN, Assets.Sounds.PLAYER_DEATH, Assets.Sounds.STEP);
		CharacterSkillsParameters skills = new CharacterSkillsParameters(
				PLAYER_HEALTH,
				Agility.HIGH,
				new Strength(1, 3),
				Accuracy.LOW);
		CharacterData data = new CharacterData(
				position,
				Direction.values()[characterJsonObject.get(DIRECTION).getAsInt()],
				skills,
				auxCharacterSoundData);
		Assets.Atlases atlas = findByRelatedWeapon(DefaultGameSettings.STARTING_WEAPON);
		addCharBaseComponents(builder, data, CharacterTypes.PLAYER.getDefinitions()[0], atlas);
		builder.finishAndAddToEngine();
	}

	private CharacterSpriteData createCharacterSpriteData(final CharacterData data, final CharacterDefinition def) {
		CharacterSpriteData characterSpriteData = Pools.obtain(CharacterSpriteData.class);
		characterSpriteData.init(data.getDirection(),
				IDLE,
				def.getMeleeHitFrameIndex(),
				def.getPrimaryAttackHitFrameIndex(),
				def.isSingleDeathAnimation());
		return characterSpriteData;
	}

	private void addCharBaseComponents(final EntityBuilder entityBuilder,
									   final CharacterData data,
									   final CharacterDefinition def,
									   final Assets.Atlases atlasDefinition) {
		CharacterSpriteData characterSpriteData = createCharacterSpriteData(data, def);
		Direction direction = data.getDirection();
		entityBuilder.addCharacterComponent(characterSpriteData, data.getSoundData(), data.getSkills())
				.addCharacterDecalComponent(assetsManager.get(atlasDefinition.name()), IDLE, direction, data.getPosition())
				.addCollisionComponent()
				.addAnimationComponent();
	}

	private void inflateCharacters(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		Arrays.stream(CharacterTypes.values()).forEach(type -> {
			String typeName = type.name().toLowerCase();
			JsonObject charactersJsonObject = mapJsonObject.getAsJsonObject(CHARACTERS);
			if (charactersJsonObject.has(typeName)) {
				JsonArray array = charactersJsonObject.get(typeName).getAsJsonArray();
				array.forEach(characterJsonElement -> {
					if (type == PLAYER) {
						inflatePlayer((JsonObject) characterJsonElement, mapGraph);
					}
				});
			}
		});
	}

	private MapGraph createMapGraph(final JsonObject mapJsonObj) {
		return new MapGraph(
				GeneralUtils.getFloatFromJsonOrDefault(mapJsonObj, MapJsonKeys.AMBIENT, 0),
				inflateNodes(mapJsonObj.get(TILES).getAsJsonObject()),
				engine);
	}

	private Dimension inflateNodes(final JsonObject tilesJsonObject) {
		Dimension mapSize = new Dimension(tilesJsonObject.get(WIDTH).getAsInt(), tilesJsonObject.get(DEPTH).getAsInt());
		String matrix = tilesJsonObject.get(MATRIX).getAsString();
		byte[] matrixByte = Base64.getDecoder().decode(matrix.getBytes());
		floorModel.calculateBoundingBox(auxBoundingBox);
		IntStream.range(0, mapSize.height).forEach(row ->
				IntStream.range(0, mapSize.width).forEach(col -> {
					byte currentValue = matrixByte[row * mapSize.width + col % mapSize.width];
					if (currentValue != 0) {
						inflateNode(row, col, currentValue);
					}
				}));
		return mapSize;
	}

	private void inflateNode(final int row, final int col, final byte chr) {
		Assets.SurfaceTextures definition = Assets.SurfaceTextures.values()[chr - 1];
		if (definition != MISSING) {
			GameModelInstance mi = new GameModelInstance(floorModel);
			defineNode(row, col, definition, mi);
			EntityBuilder.beginBuildingEntity(engine).addModelInstanceComponent(mi, true)
					.addFloorComponent()
					.finishAndAddToEngine();
		}
	}

	private void defineNode(final int row, final int col, final Assets.SurfaceTextures definition, final GameModelInstance mi) {
		mi.materials.get(0).set(TextureAttribute.createDiffuse(assetsManager.getTexture(definition)));
		mi.transform.setTranslation(auxVector3_1.set(col + 0.5f, 0, row + 0.5f));
		mi.getAdditionalRenderData().setBoundingBox(mi.calculateBoundingBox(auxBoundingBox));
		mi.getAdditionalRenderData().setColorWhenOutside(Color.WHITE);
	}

	private Material createFloorMaterial( ) {
		Material material = new Material();
		material.id = "floor_test";
		return material;
	}

	private void createRect(final MeshPartBuilder meshPartBuilder) {
		meshPartBuilder.setUVRange(0, 0, 1, 1);
		final float OFFSET = -0.5f;
		meshPartBuilder.rect(
				auxVector3_4.set(OFFSET, 0, 1 + OFFSET),
				auxVector3_1.set(1 + OFFSET, 0, 1 + OFFSET),
				auxVector3_2.set(1 + OFFSET, 0, OFFSET),
				auxVector3_3.set(OFFSET, 0, OFFSET),
				auxVector3_5.set(0, 1, 0));
	}

	@Override
	public void dispose( ) {
		floorModel.dispose();
	}
}
