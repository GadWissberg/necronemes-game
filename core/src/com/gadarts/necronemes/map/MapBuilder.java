package com.gadarts.necronemes.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.necromine.WallCreator;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.assets.MapJsonKeys;
import com.gadarts.necromine.model.Coords;
import com.gadarts.necromine.model.characters.CharacterDefinition;
import com.gadarts.necromine.model.characters.CharacterTypes;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.attributes.Accuracy;
import com.gadarts.necromine.model.characters.attributes.Agility;
import com.gadarts.necromine.model.characters.attributes.Strength;
import com.gadarts.necromine.model.characters.enemies.Enemies;
import com.gadarts.necromine.model.characters.enemies.EnemyWeaponsDefinitions;
import com.gadarts.necromine.model.map.MapNodeData;
import com.gadarts.necromine.model.map.MapNodesTypes;
import com.gadarts.necromine.model.map.NodeWalls;
import com.gadarts.necromine.model.map.Wall;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.components.character.CharacterAnimations;
import com.gadarts.necronemes.components.character.CharacterData;
import com.gadarts.necronemes.components.character.CharacterSkillsParameters;
import com.gadarts.necronemes.components.character.CharacterSoundData;
import com.gadarts.necronemes.components.character.CharacterSpriteData;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.components.mi.ModelBoundingBox;
import com.gadarts.necronemes.components.player.PlayerComponent;
import com.gadarts.necronemes.components.sd.RelatedDecal;
import com.gadarts.necronemes.components.sd.SimpleDecalComponent;
import com.gadarts.necronemes.systems.enemy.EnemySystem;
import com.gadarts.necronemes.utils.EntityBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.gadarts.necromine.assets.Assets.Atlases;
import static com.gadarts.necromine.assets.Assets.Atlases.ANUBIS;
import static com.gadarts.necromine.assets.Assets.Atlases.PLAYER_GENERIC;
import static com.gadarts.necromine.assets.Assets.Atlases.findByRelatedWeapon;
import static com.gadarts.necromine.assets.Assets.Models;
import static com.gadarts.necromine.assets.Assets.Sounds;
import static com.gadarts.necromine.assets.Assets.SurfaceTextures;
import static com.gadarts.necromine.assets.Assets.SurfaceTextures.MISSING;
import static com.gadarts.necromine.assets.Assets.UiTextures;
import static com.gadarts.necromine.assets.MapJsonKeys.CHARACTERS;
import static com.gadarts.necromine.assets.MapJsonKeys.COL;
import static com.gadarts.necromine.assets.MapJsonKeys.DEPTH;
import static com.gadarts.necromine.assets.MapJsonKeys.DIRECTION;
import static com.gadarts.necromine.assets.MapJsonKeys.EAST;
import static com.gadarts.necromine.assets.MapJsonKeys.HEIGHT;
import static com.gadarts.necromine.assets.MapJsonKeys.HEIGHTS;
import static com.gadarts.necromine.assets.MapJsonKeys.H_OFFSET;
import static com.gadarts.necromine.assets.MapJsonKeys.MATRIX;
import static com.gadarts.necromine.assets.MapJsonKeys.ROW;
import static com.gadarts.necromine.assets.MapJsonKeys.TILES;
import static com.gadarts.necromine.assets.MapJsonKeys.TYPE;
import static com.gadarts.necromine.assets.MapJsonKeys.V_OFFSET;
import static com.gadarts.necromine.assets.MapJsonKeys.WEST;
import static com.gadarts.necromine.assets.MapJsonKeys.WIDTH;
import static com.gadarts.necromine.model.characters.CharacterTypes.BILLBOARD_SCALE;
import static com.gadarts.necromine.model.characters.CharacterTypes.BILLBOARD_Y;
import static com.gadarts.necromine.model.characters.CharacterTypes.ENEMY;
import static com.gadarts.necromine.model.characters.CharacterTypes.PLAYER;
import static com.gadarts.necromine.model.characters.SpriteType.IDLE;
import static com.gadarts.necronemes.Necronemes.BOUNDING_BOX_PREFIX;
import static com.gadarts.necronemes.components.ComponentsMapper.character;
import static com.gadarts.necronemes.components.ComponentsMapper.modelInstance;
import static com.gadarts.necronemes.utils.EntityBuilder.beginBuildingEntity;
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
	private static final BoundingBox auxBoundingBox = new BoundingBox();
	private static final String KEY_PICKUPS = "pickups";
	private static final String REGION_NAME_BULLET = "bullet";
	private final static Matrix4 auxMatrix = new Matrix4();
	private final Model floorModel;
	private final PooledEngine engine;
	private final GameAssetsManager assetsManager;
	private final Gson gson = new Gson();
	private final WallCreator wallCreator;
	private final Map<Enemies, Animation<TextureAtlas.AtlasRegion>> enemyBulletsTextureRegions = new HashMap<>();

	public MapBuilder(PooledEngine engine, GameAssetsManager assetsManager) {
		this.engine = engine;
		this.assetsManager = assetsManager;
		this.wallCreator = new WallCreator(assetsManager);
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
		inflateHeights(mapJsonObj, mapGraph);
		inflateAllElements(mapJsonObj, mapGraph);
		mapGraph.init();
		return mapGraph;
	}

	private MapGraphNode getNodeByJson(final MapGraph mapGraph, final JsonObject tileJsonObject) {
		int row = tileJsonObject.get(ROW).getAsInt();
		int col = tileJsonObject.get(COL).getAsInt();
		return mapGraph.getNode(col, row);
	}

	private void inflateHeights(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		JsonElement heightsElement = mapJsonObject.get(TILES).getAsJsonObject().get(HEIGHTS);
		Optional.ofNullable(heightsElement).ifPresent(element -> {
			JsonArray heights = element.getAsJsonArray();
			heights.forEach(jsonElement -> {
				JsonObject tileJsonObject = jsonElement.getAsJsonObject();
				MapGraphNode node = getNodeByJson(mapGraph, tileJsonObject);
				float height = tileJsonObject.get(HEIGHT).getAsFloat();
				node.setHeight(height);
				Optional.ofNullable(node.getEntity())
						.ifPresent(e -> modelInstance.get(e).getModelInstance().transform.translate(0, height, 0));
			});
			heights.forEach(jsonElement -> {
				JsonObject tileJsonObject = jsonElement.getAsJsonObject();
				MapGraphNode node = getNodeByJson(mapGraph, tileJsonObject);
				float height = tileJsonObject.get(HEIGHT).getAsFloat();
				inflateWalls(tileJsonObject, node, height, mapGraph);
			});
		});
	}

	private void inflateEastWall(final JsonObject tileJsonObject,
								 final MapGraphNode node,
								 final float height,
								 final MapGraph mapGraph) {
		if (node.getCol() >= mapGraph.getWidth() - 1) return;
		int row = node.getRow();
		int col = node.getCol();
		int eastCol = col + 1;
		JsonElement east = tileJsonObject.get(EAST);
		if (height != mapGraph.getNode(eastCol, node.getRow()).getHeight() && east != null) {
			JsonObject asJsonObject = east.getAsJsonObject();
			SurfaceTextures definition = SurfaceTextures.valueOf(asJsonObject.get(MapJsonKeys.TEXTURE).getAsString());
			if (definition != MISSING) {
				MapNodeData nodeData = new MapNodeData(row, col, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				NodeWalls walls = nodeData.getWalls();
				walls.setEastWall(WallCreator.createEastWall(nodeData, wallCreator.getWallModel(), assetsManager, definition));
				MapNodeData eastNode = new MapNodeData(row, eastCol, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				nodeData.lift(height);
				eastNode.setHeight(mapGraph.getNode(eastNode.getCoords()).getHeight());
				float vScale = asJsonObject.has(MapJsonKeys.V_SCALE) ? asJsonObject.get(MapJsonKeys.V_SCALE).getAsFloat() : 0;
				float hOffset = asJsonObject.has(H_OFFSET) ? asJsonObject.get(H_OFFSET).getAsFloat() : 0;
				float vOffset = asJsonObject.has(V_OFFSET) ? asJsonObject.get(V_OFFSET).getAsFloat() : 0;
				WallCreator.adjustWallBetweenEastAndWest(eastNode, nodeData, vScale, hOffset, vOffset);
				boolean westHigherThanEast = node.getHeight() > eastNode.getHeight();
				inflateWall(walls.getEastWall(), westHigherThanEast ? eastNode : nodeData);
			}
		}
	}

	private void inflateNorthWall(final JsonObject tileJsonObject,
								  final MapGraphNode node,
								  final float height,
								  final MapGraph mapGraph) {
		int col = node.getCol();
		int row = node.getRow();
		if (row == 0) return;
		int northNodeRow = row - 1;
		JsonElement north = tileJsonObject.get(MapJsonKeys.NORTH);
		if (height != mapGraph.getNode(col, northNodeRow).getHeight() && north != null) {
			JsonObject asJsonObject = north.getAsJsonObject();
			SurfaceTextures definition = SurfaceTextures.valueOf(asJsonObject.get(MapJsonKeys.TEXTURE).getAsString());
			if (definition != MISSING) {
				MapNodeData n = new MapNodeData(row, col, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				n.getWalls().setNorthWall(WallCreator.createNorthWall(n, wallCreator.getWallModel(), assetsManager, definition));
				MapNodeData northNode = new MapNodeData(northNodeRow, n.getCoords().getCol(), MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				n.lift(height);
				northNode.setHeight(mapGraph.getNode(northNode.getCoords()).getHeight());
				float vScale = asJsonObject.has(MapJsonKeys.V_SCALE) ? asJsonObject.get(MapJsonKeys.V_SCALE).getAsFloat() : 0;
				float hOffset = asJsonObject.has(H_OFFSET) ? asJsonObject.get(H_OFFSET).getAsFloat() : 0;
				float vOffset = asJsonObject.has(V_OFFSET) ? asJsonObject.get(V_OFFSET).getAsFloat() : 0;
				WallCreator.adjustWallBetweenNorthAndSouth(n, northNode, vScale, hOffset, vOffset);
				boolean northHigherThanSouth = node.getHeight() > northNode.getHeight();
				inflateWall(n.getWalls().getNorthWall(), northHigherThanSouth ? northNode : n);
			}
		}
	}

	private void avoidZeroDimensions(final BoundingBox bBox) {
		Vector3 center = bBox.getCenter(auxVector3_1);
		if (bBox.getWidth() == 0) {
			center.x += 0.01;
		}
		if (bBox.getHeight() == 0) {
			center.y += 0.01;
		}
		if (bBox.getDepth() == 0) {
			center.z += 0.01;
		}
		bBox.ext(center);
	}

	private void inflateWall(final Wall wall, final MapNodeData parentNode) {
		BoundingBox bBox = wall.getModelInstance().calculateBoundingBox(new BoundingBox());
		avoidZeroDimensions(bBox);
		bBox.mul(auxMatrix.set(wall.getModelInstance().transform).setTranslation(Vector3.Zero));
		GameModelInstance modelInstance = new GameModelInstance(wall.getModelInstance(), bBox, true, Color.WHITE);
		beginBuildingEntity(engine).addModelInstanceComponent(modelInstance, true, false)
				.addWallComponent(parentNode)
				.finishAndAddToEngine();
	}

	private void inflateWalls(final JsonObject tileJsonObject,
							  final MapGraphNode node,
							  final float height,
							  final MapGraph mapGraph) {
		inflateEastWall(tileJsonObject, node, height, mapGraph);
		inflateSouthWall(tileJsonObject, node, height, mapGraph);
		inflateWestWall(tileJsonObject, node, height, mapGraph);
		inflateNorthWall(tileJsonObject, node, height, mapGraph);
	}

	private void inflateWestWall(final JsonObject tileJsonObject,
								 final MapGraphNode node,
								 final float height,
								 final MapGraph mapGraph) {
		int col = node.getCol();
		int row = node.getRow();
		if (col == 0) return;
		int westNodeCol = col - 1;
		JsonElement west = tileJsonObject.get(WEST);
		if (westNodeCol >= 0 && height != mapGraph.getNode(westNodeCol, row).getHeight() && west != null) {
			JsonObject asJsonObject = west.getAsJsonObject();
			SurfaceTextures definition = SurfaceTextures.valueOf(asJsonObject.get(MapJsonKeys.TEXTURE).getAsString());
			if (definition != MISSING) {
				MapNodeData nodeData = new MapNodeData(row, col, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				NodeWalls walls = nodeData.getWalls();
				walls.setWestWall(WallCreator.createWestWall(nodeData, wallCreator.getWallModel(), assetsManager, definition));
				MapNodeData westNodeData = new MapNodeData(nodeData.getCoords().getRow(), westNodeCol, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				nodeData.lift(height);
				westNodeData.setHeight(mapGraph.getNode(westNodeData.getCoords()).getHeight());
				float vScale = asJsonObject.has(MapJsonKeys.V_SCALE) ? asJsonObject.get(MapJsonKeys.V_SCALE).getAsFloat() : 0;
				float hOffset = asJsonObject.has(H_OFFSET) ? asJsonObject.get(H_OFFSET).getAsFloat() : 0;
				float vOffset = asJsonObject.has(V_OFFSET) ? asJsonObject.get(V_OFFSET).getAsFloat() : 0;
				WallCreator.adjustWallBetweenEastAndWest(nodeData, westNodeData, vScale, hOffset, vOffset);
				boolean eastHigherThanWest = node.getHeight() > westNodeData.getHeight();
				inflateWall(walls.getWestWall(), eastHigherThanWest ? westNodeData : nodeData);
			}
		}
	}

	private void inflateSouthWall(final JsonObject tileJsonObject,
								  final MapGraphNode node,
								  final float height,
								  final MapGraph mapGraph) {
		int row = node.getRow();
		int col = node.getCol();
		if (row >= mapGraph.getDepth() - 1) return;
		int southNodeRow = row + 1;
		JsonElement south = tileJsonObject.get(MapJsonKeys.SOUTH);
		if (height != mapGraph.getNode(col, southNodeRow).getHeight() && south != null) {
			JsonObject asJsonObject = south.getAsJsonObject();
			SurfaceTextures definition = SurfaceTextures.valueOf(asJsonObject.get(MapJsonKeys.TEXTURE).getAsString());
			if (definition != MISSING) {
				MapNodeData nodeData = new MapNodeData(row, col, MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				NodeWalls walls = nodeData.getWalls();
				walls.setSouthWall(WallCreator.createSouthWall(nodeData, wallCreator.getWallModel(), assetsManager, definition));
				MapNodeData southNode = new MapNodeData(southNodeRow, nodeData.getCoords().getCol(), MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN);
				nodeData.lift(height);
				southNode.setHeight(mapGraph.getNode(southNode.getCoords()).getHeight());
				float vScale = asJsonObject.has(MapJsonKeys.V_SCALE) ? asJsonObject.get(MapJsonKeys.V_SCALE).getAsFloat() : 0;
				float hOffset = asJsonObject.has(H_OFFSET) ? asJsonObject.get(H_OFFSET).getAsFloat() : 0;
				float vOffset = asJsonObject.has(V_OFFSET) ? asJsonObject.get(V_OFFSET).getAsFloat() : 0;
				WallCreator.adjustWallBetweenNorthAndSouth(southNode, nodeData, vScale, hOffset, vOffset);
				boolean northHigherThanSouth = node.getHeight() > southNode.getHeight();
				inflateWall(walls.getSouthWall(), northHigherThanSouth ? southNode : nodeData);
			}
		}
	}

	private void inflateAllElements(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		inflateCharacters(mapJsonObject, mapGraph);
		inflatePickups(mapJsonObject, mapGraph);
	}

	private void inflatePickups(final JsonObject mapJsonObject, final MapGraph mapGraph) {
		JsonArray pickups = mapJsonObject.getAsJsonArray(KEY_PICKUPS);
		pickups.forEach(element -> {
			JsonObject pickJsonObject = element.getAsJsonObject();
			WeaponsDefinitions type = WeaponsDefinitions.valueOf(pickJsonObject.get(TYPE).getAsString());
			TextureAtlas.AtlasRegion bulletRegion = null;
			if (!type.isMelee()) {
				bulletRegion = assetsManager.getAtlas(findByRelatedWeapon(type)).findRegion(REGION_NAME_BULLET);
			}
			inflatePickupEntity(pickJsonObject, type, bulletRegion, mapGraph);
		});
	}

	private void inflatePickupEntity(final JsonObject pickJsonObject,
									 final WeaponsDefinitions type,
									 final TextureAtlas.AtlasRegion bulletRegion,
									 final MapGraph mapGraph) {
		EntityBuilder builder = beginBuildingEntity(engine);
		inflatePickupModel(builder, pickJsonObject, type, mapGraph);
		builder.addPickUpComponentAsWeapon(type, assetsManager.getTexture(type.getImage()), bulletRegion)
				.finishAndAddToEngine();
	}

	private void inflatePickupModel(final EntityBuilder builder,
									final JsonObject pickJsonObject,
									final WeaponsDefinitions type, final MapGraph mapGraph) {
		Coords coord = new Coords(pickJsonObject.get(ROW).getAsInt(), pickJsonObject.get(COL).getAsInt());
		Models modelDefinition = type.getModelDefinition();
		String fileName = BOUNDING_BOX_PREFIX + modelDefinition.getFilePath();
		ModelBoundingBox boundingBox = assetsManager.get(fileName, ModelBoundingBox.class);
		GameModelInstance modelInstance = new GameModelInstance(assetsManager.getModel(modelDefinition), boundingBox);
		modelInstance.transform.setTranslation(auxVector3_1.set(coord.getCol() + 0.5f, 0, coord.getRow() + 0.5f));
		modelInstance.transform.translate(0, mapGraph.getNode(coord).getHeight(), 0);
		builder.addModelInstanceComponent(modelInstance, true);
	}

	private Vector3 inflateCharacterPosition(final JsonElement characterJsonElement, final MapGraph mapGraph) {
		JsonObject asJsonObject = characterJsonElement.getAsJsonObject();
		int col = asJsonObject.get(COL).getAsInt();
		int row = asJsonObject.get(ROW).getAsInt();
		float floorHeight = mapGraph.getNode(col, row).getHeight();
		return auxVector3_1.set(col + 0.5f, floorHeight + BILLBOARD_Y, row + 0.5f);
	}

	private void inflatePlayer(final JsonObject characterJsonObject, final MapGraph mapGraph) {
		CharacterAnimations general = assetsManager.get(PLAYER_GENERIC.name());
		EntityBuilder builder = beginBuildingEntity(engine).addPlayerComponent(general);
		Vector3 position = inflateCharacterPosition(characterJsonObject, mapGraph);
		auxCharacterSoundData.set(Sounds.PLAYER_PAIN, Sounds.PLAYER_DEATH, Sounds.STEP);
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
		Atlases atlas = findByRelatedWeapon(DefaultGameSettings.STARTING_WEAPON);
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
									   final Atlases atlasDefinition) {
		CharacterSpriteData characterSpriteData = createCharacterSpriteData(data, def);
		Direction direction = data.getDirection();
		entityBuilder.addCharacterComponent(characterSpriteData, data.getSoundData(), data.getSkills())
				.addCharacterDecalComponent(assetsManager.get(atlasDefinition.name()), IDLE, direction, data.getPosition())
				.addCollisionComponent()
				.addAnimationComponent();
	}

	@SuppressWarnings("ConstantConditions")
	private void inflateEnemy(final JsonObject charJsonObject, final MapGraph mapGraph) {
		Enemies type = inflateEnemyType(charJsonObject);
		int skill = Optional.ofNullable(DefaultGameSettings.ENEMIES_SKILL).orElse(1);
		EntityBuilder b = beginBuildingEntity(engine).addEnemyComponent(type, skill, inflateEnemyBulletFrames(type));
		Vector3 position = inflateCharacterPosition(charJsonObject, mapGraph);
		addCharBaseComponents(b, inflateCharData(charJsonObject, type, skill, position), type, type.getAtlasDefinition());
		addEnemySkillFlower(type, b, position);
		Entity enemy = b.finishAndAddToEngine();
		initializeEnemy(position, enemy);
	}

	private void initializeEnemy(Vector3 position, Entity enemy) {
		character.get(enemy).setTarget(engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).first());
		initializeEnemyFlowerSkill(position, enemy);
	}

	private void addEnemySkillFlower(Enemies type, EntityBuilder builder, Vector3 position) {
		Texture skillFlowerTexture = assetsManager.getTexture(UiTextures.SKILL_FLOWER_CENTER_IDLE);
		position.y += type.getHeight() + EnemySystem.SKILL_FLOWER_HEIGHT_RELATIVE;
		builder.addSimpleDecalComponent(position, skillFlowerTexture, true, true);
	}

	private void initializeEnemyFlowerSkill(Vector3 position, Entity enemy) {
		SimpleDecalComponent simpleDecalComponent = enemy.getComponent(SimpleDecalComponent.class);
		List.of(UiTextures.SKILL_FLOWER_1,
				UiTextures.SKILL_FLOWER_2,
				UiTextures.SKILL_FLOWER_3,
				UiTextures.SKILL_FLOWER_4,
				UiTextures.SKILL_FLOWER_5,
				UiTextures.SKILL_FLOWER_6,
				UiTextures.SKILL_FLOWER_7,
				UiTextures.SKILL_FLOWER_8).forEach(flower -> addSkillFlowerDecal(simpleDecalComponent, flower, position));
	}

	private void addSkillFlowerDecal(final SimpleDecalComponent simpleDecalComponent,
									 final UiTextures skillFlower,
									 final Vector3 position) {
		TextureRegion textureRegion = new TextureRegion(assetsManager.getTexture(skillFlower));
		RelatedDecal skillFlowerDecal = RelatedDecal.newDecal(textureRegion, true);
		skillFlowerDecal.setScale(BILLBOARD_SCALE);
		skillFlowerDecal.setPosition(position);
		simpleDecalComponent.addRelatedDecal(skillFlowerDecal);
	}

	private CharacterData inflateCharData(JsonObject characterJsonObject, Enemies type, int skill, Vector3 position) {
		auxCharacterSoundData.set(type.getPainSound(), type.getDeathSound(), type.getStepSound());
		CharacterSkillsParameters skills = new CharacterSkillsParameters(
				type.getHealth().get(skill - 1),
				type.getAgility().get(skill - 1),
				type.getStrength().get(skill - 1),
				type.getAccuracy() != null ? type.getAccuracy()[skill - 1] : null);
		CharacterData data = new CharacterData(position, Direction.values()[characterJsonObject.get(DIRECTION).getAsInt()], skills, auxCharacterSoundData);
		return data;
	}

	private Animation<TextureAtlas.AtlasRegion> inflateEnemyBulletFrames(Enemies type) {
		Animation<TextureAtlas.AtlasRegion> bulletAnimation = enemyBulletsTextureRegions.get(type);
		if (type.getPrimaryAttack() != null && !enemyBulletsTextureRegions.containsKey(type)) {
			String name = EnemyWeaponsDefinitions.ENERGY_BALL.name().toLowerCase();
			Array<TextureAtlas.AtlasRegion> regions = assetsManager.getAtlas(ANUBIS).findRegions(name);
			bulletAnimation = new Animation<>(type.getPrimaryAttack().getFrameDuration(), regions);
			enemyBulletsTextureRegions.put(type, bulletAnimation);
		}
		return bulletAnimation;
	}

	private Enemies inflateEnemyType(JsonObject characterJsonObject) {
		Enemies type;
		try {
			String asString = characterJsonObject.get(TYPE).getAsString();
			type = Optional.of(Arrays.stream(Enemies.values())
							.filter(def -> def.name().equalsIgnoreCase(asString))
							.findFirst())
					.orElseThrow()
					.get();
		} catch (Exception e) {
			int index = characterJsonObject.get(TYPE).getAsInt();
			type = Enemies.values()[index];
		}
		return type;
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
					} else if (type == ENEMY) {
						inflateEnemy((JsonObject) characterJsonElement, mapGraph);
					}
				});
			}
		});
	}

	private MapGraph createMapGraph(final JsonObject mapJsonObj) {
		MapGraph mapGraph = new MapGraph(inflateNodes(mapJsonObj.get(TILES).getAsJsonObject()), engine);
		return mapGraph;
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
		SurfaceTextures definition = SurfaceTextures.values()[chr - 1];
		if (definition != MISSING) {
			GameModelInstance mi = new GameModelInstance(floorModel);
			defineNode(row, col, definition, mi);
			beginBuildingEntity(engine).addModelInstanceComponent(mi, true)
					.addFloorComponent()
					.finishAndAddToEngine();
		}
	}

	private void defineNode(final int row, final int col, final SurfaceTextures definition, final GameModelInstance mi) {
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
