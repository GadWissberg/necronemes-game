package com.gadarts.necronemes.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.mi.AdditionalRenderData;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.components.mi.ModelInstanceComponent;
import com.gadarts.necronemes.systems.render.RenderSystemEventsSubscriber;

public class RenderSystem extends GameSystem<RenderSystemEventsSubscriber> {
	private final static Vector3 auxVector3_1 = new Vector3();
	private final static Vector3 auxVector3_2 = new Vector3();
	private final static Vector3 auxVector3_3 = new Vector3();
	private final static BoundingBox auxBoundingBox = new BoundingBox();
	private final ModelBatch modelBatch;
	private ImmutableArray<Entity> modelInstanceEntities;

	public RenderSystem(SystemsCommonData systemsCommonData) {
		super(systemsCommonData);
		this.modelBatch = new ModelBatch();
	}

	@Override
	public Class<RenderSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return RenderSystemEventsSubscriber.class;
	}


	@Override
	public void initializeData( ) {
		modelInstanceEntities = getEngine().getEntitiesFor(Family.all(ModelInstanceComponent.class).get());
	}

	private void resetDisplay(@SuppressWarnings("SameParameterValue") final Color color) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		int sam = Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0;
		Gdx.gl.glClearColor(color.r, color.g, color.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | sam);
	}

	private void renderModels(final ModelBatch modelBatch,
							  final boolean renderWallsAndFloor) {
		renderModels(modelBatch, renderWallsAndFloor, null);
	}

	private boolean isVisible(final Camera camera, final Entity entity) {
		if (!DefaultGameSettings.DISABLE_FRUSTUM_CULLING) return true;
		ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
		Vector3 position = modelInstanceComponent.getModelInstance().transform.getTranslation(auxVector3_1);
		AdditionalRenderData additionalRenderData = modelInstanceComponent.getModelInstance().getAdditionalRenderData();
		BoundingBox boundingBox = additionalRenderData.getBoundingBox(auxBoundingBox);
		Vector3 center = boundingBox.getCenter(auxVector3_3);
		Vector3 dim = auxBoundingBox.getDimensions(auxVector3_2);
		return camera.frustum.boundsInFrustum(position.add(center), dim);
	}

	private void renderModels(final ModelBatch modelBatch,
							  final boolean renderWallsAndFloor,
							  final Entity exclude) {
		Camera camera = getSystemsCommonData().getCamera();
		modelBatch.begin(camera);
		for (Entity entity : modelInstanceEntities) {
			renderModel(modelBatch, renderWallsAndFloor, exclude, camera, entity);
		}
		modelBatch.end();
	}

	private void renderModel(ModelBatch modelBatch,
							 boolean renderWallsAndFloor,
							 Entity exclude,
							 Camera camera,
							 Entity entity) {
		ModelInstanceComponent modelInstanceComponent = ComponentsMapper.modelInstance.get(entity);
		if (shouldSkipRenderModel(renderWallsAndFloor, exclude, camera, entity, modelInstanceComponent)) {
			return;
		}
		GameModelInstance modelInstance = modelInstanceComponent.getModelInstance();
		modelBatch.render(modelInstance);
	}

	private boolean shouldSkipRenderModel(boolean renderWallsAndFloor, Entity exclude, Camera camera, Entity entity, ModelInstanceComponent modelInstanceComponent) {
		return entity == exclude
				|| (!modelInstanceComponent.isVisible())
				|| (!renderWallsAndFloor && (ComponentsMapper.floor.has(entity)))
				|| !isVisible(camera, entity);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		resetDisplay(Color.BLACK);
		renderModels(modelBatch, true);
	}

	@Override
	public void dispose( ) {

	}

}
