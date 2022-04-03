package com.gadarts.necronemes.systems.render;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.characters.CharacterUtils;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.animation.AnimationComponent;
import com.gadarts.necronemes.components.cd.CharacterDecalComponent;
import com.gadarts.necronemes.components.character.CharacterAnimation;
import com.gadarts.necronemes.components.character.CharacterAnimations;
import com.gadarts.necronemes.components.character.CharacterSpriteData;
import com.gadarts.necronemes.components.mi.AdditionalRenderData;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.components.mi.ModelInstanceComponent;
import com.gadarts.necronemes.components.sd.RelatedDecal;
import com.gadarts.necronemes.components.sd.SimpleDecalComponent;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;

import java.util.List;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class RenderSystem extends GameSystem<RenderSystemEventsSubscriber> {
	private final static Vector3 auxVector3_1 = new Vector3();
	private final static Vector3 auxVector3_2 = new Vector3();
	private final static Vector3 auxVector3_3 = new Vector3();
	private final static BoundingBox auxBoundingBox = new BoundingBox();
	private static final int DECALS_POOL_SIZE = 200;
	private final ModelBatch modelBatch;
	private DecalBatch decalBatch;
	private ImmutableArray<Entity> modelInstanceEntities;
	private ImmutableArray<Entity> characterDecalsEntities;
	private ImmutableArray<Entity> simpleDecalsEntities;

	public RenderSystem(SystemsCommonData systemsCommonData, SoundPlayer soundPlayer, GameAssetsManager assetsManager) {
		super(systemsCommonData, soundPlayer, assetsManager);
		this.modelBatch = new ModelBatch();
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		characterDecalsEntities = engine.getEntitiesFor(Family.all(CharacterDecalComponent.class).get());
		simpleDecalsEntities = engine.getEntitiesFor(Family.all(SimpleDecalComponent.class).get());
	}

	@Override
	public Class<RenderSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return RenderSystemEventsSubscriber.class;
	}


	@Override
	public void initializeData( ) {
		modelInstanceEntities = getEngine().getEntitiesFor(Family.all(ModelInstanceComponent.class).get());
		SystemsCommonData systemsCommonData = getSystemsCommonData();
		GameCameraGroupStrategy strategy = new GameCameraGroupStrategy(systemsCommonData.getCamera(), getAssetsManager());
		this.decalBatch = new DecalBatch(DECALS_POOL_SIZE, strategy);
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
		SystemsCommonData systemsCommonData = getSystemsCommonData();
		systemsCommonData.setNumberOfVisible(systemsCommonData.getNumberOfVisible() + 1);
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
		getSystemsCommonData().setNumberOfVisible(0);
		resetDisplay(Color.BLACK);
		renderModels(modelBatch, true);
		renderDecals(deltaTime);
		getSystemsCommonData().getUiStage().draw();
	}

	private void renderDecals(final float deltaTime) {
		Gdx.gl.glDepthMask(false);
		renderLiveCharacters(deltaTime);
		renderSimpleDecals();
		Gdx.gl.glDepthMask(true);
	}

	private void renderSimpleDecals( ) {
		for (Entity entity : simpleDecalsEntities) {
			renderSimpleDecal(decalBatch, entity);
		}
		decalBatch.flush();
	}

	private void handleSimpleDecalAnimation(final Entity entity, final SimpleDecalComponent simpleDecalComponent) {
		if (ComponentsMapper.animation.has(entity) && simpleDecalComponent.isAnimatedByAnimationComponent()) {
			AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
			simpleDecalComponent.getDecal().setTextureRegion(animationComponent.calculateFrame());
		}
	}

	private void faceDecalToCamera(final SimpleDecalComponent simpleDecal, final Decal decal) {
		if (simpleDecal.isBillboard()) {
			Camera camera = getSystemsCommonData().getCamera();
			decal.lookAt(auxVector3_1.set(decal.getPosition()).sub(camera.direction), camera.up);
		}
	}

	private void renderSimpleDecal(final DecalBatch decalBatch, final Entity entity) {
		SimpleDecalComponent simpleDecalComponent = ComponentsMapper.simpleDecal.get(entity);
		if (simpleDecalComponent != null && simpleDecalComponent.isVisible()) {
			if (!simpleDecalComponent.isAffectedByFow()) {
				handleSimpleDecalAnimation(entity, simpleDecalComponent);
				faceDecalToCamera(simpleDecalComponent, simpleDecalComponent.getDecal());
				decalBatch.add(simpleDecalComponent.getDecal());
				renderRelatedDecals(decalBatch, simpleDecalComponent);
			}
		}
	}

	private void renderRelatedDecals(final DecalBatch decalBatch, final SimpleDecalComponent hudDecal) {
		List<RelatedDecal> relatedDecals = hudDecal.getRelatedDecals();
		if (!relatedDecals.isEmpty()) {
			for (RelatedDecal relatedDecal : relatedDecals) {
				if (relatedDecal.isVisible()) {
					faceDecalToCamera(hudDecal, relatedDecal);
					decalBatch.add(relatedDecal);
				}
			}
		}
	}

	private void renderLiveCharacters(final float deltaTime) {
		for (Entity entity : characterDecalsEntities) {
			initializeCharacterDecalForRendering(deltaTime, entity);
			renderCharacterDecal(entity);
		}
	}

	private void renderCharacterDecal(final Entity entity) {
		Decal decal = ComponentsMapper.characterDecal.get(entity).getDecal();
		Vector3 decalPosition = decal.getPosition();
		Camera camera = getSystemsCommonData().getCamera();
		decal.lookAt(auxVector3_1.set(decalPosition).sub(camera.direction), camera.up);
		decalBatch.add(decal);
	}

	private void initializeCharacterDecalForRendering(float deltaTime, Entity entity) {
		Camera camera = getSystemsCommonData().getCamera();
		CharacterSpriteData charSpriteData = ComponentsMapper.character.get(entity).getCharacterSpriteData();
		Direction direction = CharacterUtils.calculateDirectionSeenFromCamera(camera, charSpriteData.getFacingDirection());
		SpriteType spriteType = charSpriteData.getSpriteType();
		boolean sameSpriteType = spriteType.equals(ComponentsMapper.characterDecal.get(entity).getSpriteType());
		if ((!sameSpriteType || !ComponentsMapper.characterDecal.get(entity).getDirection().equals(direction))) {
			updateCharacterDecalSprite(entity, direction, spriteType, sameSpriteType);
		} else {
			updateCharacterDecalFrame(deltaTime, entity, charSpriteData, spriteType);
		}
	}

	private void updateCharacterDecalFrame(float delta,
										   Entity entity,
										   CharacterSpriteData charSpriteData,
										   SpriteType spriteType) {
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(entity);
		Decal decal = characterDecalComponent.getDecal();
		AnimationComponent aniComp = ComponentsMapper.animation.get(entity);
		if (ComponentsMapper.animation.has(entity) && aniComp.getAnimation() != null) {
			AtlasRegion currentFrame = (AtlasRegion) decal.getTextureRegion();
			AtlasRegion newFrame = calculateCharacterDecalNewFrame(delta, entity, aniComp, currentFrame);
			if (characterDecalComponent.getSpriteType() == spriteType && currentFrame != newFrame) {
				updateCharacterDecalTextureAccordingToAnimation(entity, charSpriteData, spriteType, newFrame);
			}
		}
	}

	private void updateCharacterDecalTextureAccordingToAnimation(Entity entity,
																 CharacterSpriteData characterSpriteData,
																 SpriteType spriteType,
																 AtlasRegion newFrame) {
		CharacterDecalComponent characterDecalComponent = ComponentsMapper.characterDecal.get(entity);
		CharacterAnimations animations = characterDecalComponent.getAnimations();
		Decal decal = characterDecalComponent.getDecal();
		decal.setTextureRegion(newFrame);
		Direction facingDirection = characterSpriteData.getFacingDirection();
		if (spriteType.isSingleAnimation()) {
			facingDirection = Direction.SOUTH;
		}
		if (animations.contains(spriteType)) {
			animations.get(spriteType, facingDirection);
		} else {
			if (ComponentsMapper.player.has(entity)) {
				CharacterAnimations generalAnim = ComponentsMapper.player.get(entity).getGeneralAnimations();
				generalAnim.get(spriteType, facingDirection);
			}
		}
	}

	private AtlasRegion calculateCharacterDecalNewFrame(float deltaTime,
														Entity entity,
														AnimationComponent animationComponent,
														AtlasRegion currentFrame) {
		AtlasRegion newFrame = animationComponent.calculateFrame();
		if (currentFrame.index != newFrame.index) {
			for (RenderSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onFrameChanged(entity, deltaTime, newFrame);
			}
		}
		return newFrame;
	}

	private void updateCharacterDecalSprite(Entity entity,
											Direction direction,
											SpriteType spriteType,
											boolean sameSpriteType) {
		AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
		ComponentsMapper.characterDecal.get(entity).initializeSprite(spriteType, direction);
		if (ComponentsMapper.animation.has(entity)) {
			if (spriteType.isSingleAnimation()) {
				if (!animationComponent.getAnimation().isAnimationFinished(animationComponent.getStateTime())) {
					direction = Direction.SOUTH;
				}
			}
			initializeCharacterAnimationBySpriteType(entity, direction, spriteType, sameSpriteType);
		}
	}

	private void initializeCharacterAnimationBySpriteType(Entity entity,
														  Direction direction,
														  SpriteType spriteType,
														  boolean sameSpriteType) {
		CharacterAnimation animation;
		AnimationComponent animationComponent = ComponentsMapper.animation.get(entity);
		CharacterAnimations animations = ComponentsMapper.characterDecal.get(entity).getAnimations();
		float animationDuration = spriteType.getAnimationDuration();
		if (animations.contains(spriteType)) {
			animation = animations.get(spriteType, direction);
			animationComponent.init(animationDuration, animation);
		} else if (ComponentsMapper.player.has(entity)) {
			animation = ComponentsMapper.player.get(entity).getGeneralAnimations().get(spriteType, direction);
			animationComponent.init(animationDuration, animation);
		}
		if (!sameSpriteType) {
			animationComponent.resetStateTime();
		}
	}

	@Override
	public void dispose( ) {

	}

}
