package com.gadarts.necronemes.utils;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.assets.MapJsonKeys;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.awt.*;
import java.util.Base64;
import java.util.stream.IntStream;

import static com.gadarts.necromine.assets.Assets.SurfaceTextures.MISSING;
import static com.gadarts.necromine.assets.MapJsonKeys.*;
import static java.lang.String.format;

public class MapBuilder implements Disposable {
	public static final String MAP_PATH_TEMP = "assets/maps/%s.json";
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
		return createMapGraph(mapJsonObj);
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
