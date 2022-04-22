package com.gadarts.necronemes.systems.render;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.characters.CharacterUtils;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.GameLifeCycleHandler;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.LightComponent;
import com.gadarts.necronemes.components.ShadowlessLightComponent;
import com.gadarts.necronemes.components.StaticLightComponent;
import com.gadarts.necronemes.components.animation.AnimationComponent;
import com.gadarts.necronemes.components.cd.CharacterDecalComponent;
import com.gadarts.necronemes.components.character.CharacterAnimation;
import com.gadarts.necronemes.components.character.CharacterAnimations;
import com.gadarts.necronemes.components.character.CharacterComponent;
import com.gadarts.necronemes.components.character.CharacterSpriteData;
import com.gadarts.necronemes.components.enemy.EnemyComponent;
import com.gadarts.necronemes.components.mi.AdditionalRenderData;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.components.mi.ModelInstanceComponent;
import com.gadarts.necronemes.components.sd.RelatedDecal;
import com.gadarts.necronemes.components.sd.SimpleDecalComponent;
import com.gadarts.necronemes.console.commands.ConsoleCommandParameter;
import com.gadarts.necronemes.console.commands.ConsoleCommandResult;
import com.gadarts.necronemes.console.commands.ConsoleCommands;
import com.gadarts.necronemes.console.commands.ConsoleCommandsList;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.enemy.EnemyAiStatus;
import com.gadarts.necronemes.systems.input.InputSystemEventsSubscriber;
import com.gadarts.necronemes.systems.render.shaders.DepthMapShader;
import com.gadarts.necronemes.systems.render.shaders.MainShaderProvider;
import com.gadarts.necronemes.systems.render.shaders.ShadowMapShader;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import static com.gadarts.necromine.assets.Assets.Shaders.*;
import static com.gadarts.necromine.assets.Assets.Shaders.SHADOW_FRAGMENT;
import static com.gadarts.necromine.model.characters.SpriteType.ATTACK_PRIMARY;
import static com.gadarts.necronemes.components.ComponentsMapper.*;
import static com.gadarts.necronemes.systems.SystemsCommonData.CAMERA_LIGHT_FAR;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class RenderSystem extends GameSystem<RenderSystemEventsSubscriber> implements InputSystemEventsSubscriber {
	public static final float LIGHT_MAX_RADIUS = 7f;
	public static final float FLICKER_RANDOM_MIN = 0.95F;
	public static final float FLICKER_RANDOM_MAX = 1.05F;
	public static final int DEPTH_MAP_SIZE = 1024;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private static final BoundingBox auxBoundingBox = new BoundingBox();
	private static final int DECALS_POOL_SIZE = 200;
	private static final int ICON_FLOWER_APPEARANCE_DURATION = 1000;
	private static final float DECAL_DARKEST_COLOR = 0.2f;
	private static final Color auxColor = new Color();
	private static final float DECAL_LIGHT_OFFSET = 1.5f;
	private static final List<Entity> auxLightsListToRemove = new ArrayList<>();
	private static final int FLICKER_MAX_INTERVAL = 150;
	private static final Circle auxCircle = new Circle();
	private static final Rectangle auxRect = new Rectangle();
	private final Texture iconFlowerLookingFor;
	private final StringBuilder stringBuilder = new StringBuilder();
	private final GlyphLayout skillFlowerGlyph;
	private final BitmapFont skillFlowerFont;
	private final Environment environment;
	private final MainShaderProvider shaderProvider;
	private final FrameBuffer shadowFrameBuffer;
	private final DrawFlags drawFlags = new DrawFlags();
	private ModelBatch depthModelBatch;
	private ModelBatch modelBatchShadows;
	private ImmutableArray<Entity> shadowlessLightsEntities;
	private ModelBatch modelBatch;
	private SpriteBatch spriteBatch;
	private DecalBatch decalBatch;
	private ImmutableArray<Entity> modelInstanceEntities;
	private ImmutableArray<Entity> characterDecalsEntities;
	private ImmutableArray<Entity> simpleDecalsEntities;
	private ImmutableArray<Entity> enemyEntities;
	private ImmutableArray<Entity> staticLightsEntities;
	private ShaderProgram depthShaderProgram;
	private ShaderProgram shadowsShaderProgram;
	private boolean take;
	private boolean frustumCull = !DefaultGameSettings.DISABLE_FRUSTUM_CULLING;

	public RenderSystem(SystemsCommonData systemsCommonData,
						SoundPlayer soundPlayer,
						GameAssetsManager assetsManager,
						GameLifeCycleHandler lifeCycleHandler) {
		super(systemsCommonData, soundPlayer, assetsManager, lifeCycleHandler);
		shadowFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		shaderProvider = new MainShaderProvider(getAssetsManager(), shadowFrameBuffer);
		createBatches();
		iconFlowerLookingFor = assetsManager.getTexture(Assets.UiTextures.ICON_LOOKING_FOR);
		skillFlowerFont = new BitmapFont();
		skillFlowerGlyph = new GlyphLayout();
		environment = createEnvironment();
	}

	private void handleFrustumCullingCommand(final ConsoleCommandResult consoleCommandResult) {
		frustumCull = !frustumCull;
		final String MESSAGE = "Frustum culling has been %s.";
		String msg = frustumCull ? String.format(MESSAGE, "activated") : String.format(MESSAGE, "disabled");
		consoleCommandResult.setMessage(msg);
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command, final ConsoleCommandResult consoleCommandResult) {
		if (command == ConsoleCommandsList.FRUSTUM_CULLING) {
			handleFrustumCullingCommand(consoleCommandResult);
			return true;
		}
		return false;
	}

	@Override
	public void keyDown(int keycode) {
		if (keycode == Input.Keys.T) {
			take = true;
		}
	}

	private void createBatches( ) {
		this.modelBatch = new ModelBatch(shaderProvider);
		this.spriteBatch = new SpriteBatch();
		depthModelBatch = new ModelBatch(new DefaultShaderProvider() {
			@Override
			protected Shader createShader(final Renderable renderable) {
				return new DepthMapShader(renderable, depthShaderProgram);
			}
		});
		modelBatchShadows = new ModelBatch(new DefaultShaderProvider() {
			@Override
			protected Shader createShader(final Renderable renderable) {
				return new ShadowMapShader(renderable, shadowsShaderProgram, staticLightsEntities);
			}
		});
	}

	private Environment createEnvironment( ) {
		final Environment environment;
		environment = new Environment();
		float ambient = getSystemsCommonData().getMap().getAmbient();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, ambient, ambient, ambient, 0.1f));
		float dirValue = 0.1F;
		environment.add(new DirectionalLight().set(dirValue, dirValue, dirValue, -1F, -1F, -0.5F));
		return environment;
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		characterDecalsEntities = engine.getEntitiesFor(Family.all(CharacterDecalComponent.class).get());
		simpleDecalsEntities = engine.getEntitiesFor(Family.all(SimpleDecalComponent.class).get());
		enemyEntities = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		shadowlessLightsEntities = getEngine().getEntitiesFor(Family.all(ShadowlessLightComponent.class).get());
		staticLightsEntities = getEngine().getEntitiesFor(Family.all(StaticLightComponent.class).get());
	}

	@Override
	public Class<RenderSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return RenderSystemEventsSubscriber.class;
	}

	@Override
	public void initializeData( ) {
		modelInstanceEntities = getEngine().getEntitiesFor(Family.all(ModelInstanceComponent.class).get());
		SystemsCommonData systemsCommonData = getSystemsCommonData();
		GameAssetsManager assetsManager = getAssetsManager();
		GameCameraGroupStrategy strategy = new GameCameraGroupStrategy(systemsCommonData.getCamera(), assetsManager);
		this.decalBatch = new DecalBatch(DECALS_POOL_SIZE, strategy);
		createShaderPrograms(assetsManager);
		createShadowMaps();
	}

	private void createShaderPrograms(GameAssetsManager assetsManager) {
		depthShaderProgram = new ShaderProgram(
				assetsManager.getShader(DEPTHMAP_VERTEX),
				assetsManager.getShader(DEPTHMAP_FRAGMENT));
		shadowsShaderProgram = new ShaderProgram(
				assetsManager.getShader(SHADOW_VERTEX),
				assetsManager.getShader(SHADOW_FRAGMENT));
	}

	private void resetDisplay(@SuppressWarnings("SameParameterValue") final Color color) {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		int sam = Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0;
		Gdx.gl.glClearColor(color.r, color.g, color.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | sam);
	}

	private void renderModels(final ModelBatch modelBatch) {
		renderModels(modelBatch, null, true, getSystemsCommonData().getCamera());
	}

	private boolean isVisible(final Camera camera, final Entity entity) {
		if (!DefaultGameSettings.DISABLE_FRUSTUM_CULLING) return true;
		ModelInstanceComponent modelInstanceComponent = modelInstance.get(entity);
		Vector3 position = modelInstanceComponent.getModelInstance().transform.getTranslation(auxVector3_1);
		AdditionalRenderData additionalRenderData = modelInstanceComponent.getModelInstance().getAdditionalRenderData();
		BoundingBox boundingBox = additionalRenderData.getBoundingBox(auxBoundingBox);
		Vector3 center = boundingBox.getCenter(auxVector3_3);
		Vector3 dim = auxBoundingBox.getDimensions(auxVector3_2);
		return camera.frustum.boundsInFrustum(position.add(center), dim);
	}

	@Override
	public boolean onCommandRun(final ConsoleCommands command,
								final ConsoleCommandResult consoleCommandResult,
								final ConsoleCommandParameter parameter) {
		boolean result = false;
		if (command == ConsoleCommandsList.SKIP_RENDER) {
			drawFlags.applySkipRenderCommand(parameter);
			result = true;
		}
		return result;
	}

	private void renderModels(ModelBatch modelBatch,
							  boolean renderLight,
							  Camera camera) {
		renderModels(modelBatch, null, renderLight, camera);
	}

	private void renderModels(ModelBatch modelBatch,
							  Entity exclude,
							  boolean renderLight,
							  Camera camera) {
		modelBatch.begin(camera);
		for (Entity entity : modelInstanceEntities) {
			renderModel(modelBatch, exclude, camera, entity, renderLight);
		}
		modelBatch.end();
	}

	private void applyLightsOnModel(final ModelInstanceComponent mic) {
		List<Entity> nearbyLights = mic.getModelInstance().getAdditionalRenderData().getNearbyLights();
		nearbyLights.clear();
		if (!DefaultGameSettings.DISABLE_LIGHTS) {
			if (mic.getModelInstance().getAdditionalRenderData().isAffectedByLight()) {
				for (Entity light : shadowlessLightsEntities) {
					addLightIfClose(mic.getModelInstance(), nearbyLights, light);
				}
			}
		}
	}

	private void addLightIfClose(final GameModelInstance modelInstance,
								 final List<Entity> nearbyLights,
								 final Entity light) {
		ShadowlessLightComponent lightComponent = ComponentsMapper.shadowlessLight.get(light);
		Vector3 lightPosition = lightComponent.getPosition(auxVector3_1);
		Vector3 modelPosition = modelInstance.transform.getTranslation(auxVector3_2);
		float distance = lightPosition.dst(modelPosition);
		if (distance <= LIGHT_MAX_RADIUS) {
			nearbyLights.add(light);
		}
	}

	private void renderModel(ModelBatch modelBatch,
							 Entity exclude,
							 Camera camera,
							 Entity entity,
							 boolean renderLight) {
		ModelInstanceComponent modelInstanceComponent = modelInstance.get(entity);
		if (!shouldSkipRenderModel(exclude, camera, entity, modelInstanceComponent)) {
			modelBatch.render(modelInstanceComponent.getModelInstance(), environment);
			getSystemsCommonData().setNumberOfVisible(getSystemsCommonData().getNumberOfVisible() + 1);
			if (ComponentsMapper.floor.has(entity)) {
				renderCharactersShadowsOnFloor(entity);
			}
			if (renderLight) {
				applyLightsOnModel(modelInstanceComponent);
			}
		}
	}

	private void renderCharactersShadowsOnFloor(Entity entity) {
		List<Entity> nearbyCharacters = ComponentsMapper.floor.get(entity).getNearbyCharacters();
		nearbyCharacters.clear();
		for (Entity character : characterDecalsEntities) {
			Vector3 position = characterDecal.get(character).getDecal().getPosition();
			auxCircle.set(position.x, position.z, CharacterComponent.CHAR_RAD * 3F);
			Vector3 floorPos = modelInstance.get(entity).getModelInstance().transform.getTranslation(auxVector3_1);
			auxRect.set(floorPos.x, floorPos.z, 1F, 1F);
			if (Intersector.overlaps(auxCircle, auxRect)) {
				nearbyCharacters.add(character);
				if (nearbyCharacters.size() >= 2) {
					return;
				}
			}
		}
	}

	private boolean shouldSkipRenderModel(Entity exclude,
										  Camera camera,
										  Entity entity,
										  ModelInstanceComponent modelInstanceComponent) {
		return entity == exclude
				|| (!modelInstanceComponent.isVisible())
				|| !isVisible(camera, entity)
				|| floor.has(entity) && !drawFlags.isDrawGround()
				|| obstacle.has(entity) && !drawFlags.isDrawEnv()
				|| getSystemsCommonData().getCursor() == entity && !drawFlags.isDrawCursor();
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		updateLights((PooledEngine) getEngine());
		render(deltaTime);
	}

	private void renderShadows( ) {
		shadowFrameBuffer.begin();
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		renderModels(modelBatchShadows, true, getSystemsCommonData().getCamera());
		handleScreenshot(shadowFrameBuffer);
		shadowFrameBuffer.end();
	}

	private void handleScreenshot(FrameBuffer frameBuffer) {
		if (take) {
			ScreenshotFactory.saveScreenshot(frameBuffer.getWidth(), frameBuffer.getHeight(), "depthmap");
			take = false;
		}
	}

	private void render(float deltaTime) {
		getSystemsCommonData().setNumberOfVisible(0);
		renderShadows();
		resetDisplay(Color.BLACK);
		renderModels(modelBatch);
		renderDecals(deltaTime);
		renderParticleEffects();
		getSystemsCommonData().getUiStage().draw();
		renderSkillFlowersText();
	}

	private void renderParticleEffects( ) {
		modelBatch.begin(getSystemsCommonData().getCamera());
		modelBatch.render(getSystemsCommonData().getParticleSystem(), environment);
		modelBatch.end();
	}

	private void renderSkillFlowersText( ) {
		if (enemyEntities.size() > 0) {
			spriteBatch.begin();
			for (Entity enemy : enemyEntities) {
				if (simpleDecal.has(enemy)) {
					renderSkillFlowerInsideContent(enemy);
				}
			}
			spriteBatch.end();
		}
	}

	private void flipIconDisplayInFlower(final EnemyComponent enemyComponent) {
		if (enemyComponent.getAiStatus() == EnemyAiStatus.SEARCHING) {
			long lastIconDisplayInFlower = enemyComponent.getIconDisplayInFlowerTimeStamp();
			if (TimeUtils.timeSinceMillis(lastIconDisplayInFlower) >= ICON_FLOWER_APPEARANCE_DURATION) {
				enemyComponent.setDisplayIconInFlower(!enemyComponent.isDisplayIconInFlower());
				enemyComponent.setIconDisplayInFlowerTimeStamp(TimeUtils.millis());
			}
		}
	}

	private void renderSkillFlowerInsideContent(final Entity enemy) {
		EnemyComponent enemyComponent = ComponentsMapper.enemy.get(enemy);
		flipIconDisplayInFlower(enemyComponent);
		SimpleDecalComponent simpleDecalComponent = simpleDecal.get(enemy);
		Camera camera = getSystemsCommonData().getCamera();
		Vector3 screenPos = camera.project(auxVector3_1.set(simpleDecalComponent.getDecal().getPosition()));
		if (enemyComponent.getAiStatus() == EnemyAiStatus.SEARCHING && enemyComponent.isDisplayIconInFlower()) {
			renderSkillFlowerIcon(spriteBatch, screenPos);
		} else {
			renderSkillFlowerText(spriteBatch, enemyComponent, screenPos);
		}
	}

	private void renderSkillFlowerIcon(final SpriteBatch spriteBatch, final Vector3 screenPos) {
		float x = screenPos.x - iconFlowerLookingFor.getWidth() / 2F;
		float y = screenPos.y - iconFlowerLookingFor.getHeight() / 2F;
		spriteBatch.draw(iconFlowerLookingFor, x, y);
	}

	private void renderSkillFlowerText(final SpriteBatch spriteBatch,
									   final EnemyComponent enemyComponent,
									   final Vector3 screenPos) {
		stringBuilder.setLength(0);
		String text = stringBuilder.append(enemyComponent.getSkill()).toString();
		skillFlowerGlyph.setText(skillFlowerFont, text);
		float x = screenPos.x - skillFlowerGlyph.width / 2F;
		float y = screenPos.y + skillFlowerGlyph.height / 2F;
		skillFlowerFont.draw(spriteBatch, text, x, y);
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
		if (animation.has(entity) && simpleDecalComponent.isAnimatedByAnimationComponent()) {
			AnimationComponent animationComponent = animation.get(entity);
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
		SimpleDecalComponent simpleDecalComponent = simpleDecal.get(entity);
		if (simpleDecalComponent != null && simpleDecalComponent.isVisible()) {
			handleSimpleDecalAnimation(entity, simpleDecalComponent);
			faceDecalToCamera(simpleDecalComponent, simpleDecalComponent.getDecal());
			decalBatch.add(simpleDecalComponent.getDecal());
			renderRelatedDecals(decalBatch, simpleDecalComponent);
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
			if (shouldRenderPlayer(entity) || shouldRenderEnemy(entity)) {
				initializeCharacterDecalForRendering(deltaTime, entity);
				renderCharacterDecal(entity);
			}
		}
	}

	private boolean shouldRenderEnemy(Entity entity) {
		return enemy.has(entity) && drawFlags.isDrawEnemy();
	}

	private boolean shouldRenderPlayer(Entity entity) {
		return player.has(entity) && !ComponentsMapper.player.get(entity).isDisabled();
	}

	private boolean shouldApplyLightsOnDecal(final Entity entity,
											 final CharacterSpriteData spriteData) {
		Decal decal = ComponentsMapper.characterDecal.get(entity).getDecal();
		TextureAtlas.AtlasRegion textureRegion = (TextureAtlas.AtlasRegion) decal.getTextureRegion();
		if (ComponentsMapper.enemy.has(entity)) {
			return spriteData.getSpriteType() != ATTACK_PRIMARY;
		} else {
			return shouldApplyLightsOnPlayerDecal(spriteData, textureRegion);
		}
	}

	private boolean shouldApplyLightsOnPlayerDecal(final CharacterSpriteData spriteData,
												   final TextureAtlas.AtlasRegion textureRegion) {
		boolean noInHitFrameIndex = textureRegion.index != spriteData.getPrimaryAttackHitFrameIndex();
		boolean noPrimaryAttack = spriteData.getSpriteType() != ATTACK_PRIMARY || noInHitFrameIndex;
		boolean meleeWeapon = getSystemsCommonData().getStorage().getSelectedWeapon().isMelee();
		return noPrimaryAttack || meleeWeapon;
	}

	void setDecalColorAccordingToLights(final Entity entity) {
		Decal decal = ComponentsMapper.characterDecal.get(entity).getDecal();
		if (shouldApplyLightsOnDecal(entity, ComponentsMapper.character.get(entity).getCharacterSpriteData())) {
			findClosestLight(decal);
			float ambient = getSystemsCommonData().getMap().getAmbient();
			Color color = decal.getColor().add(auxColor.set(ambient, ambient, ambient, ambient));
			decal.setColor(color);
		} else {
			decal.setColor(Color.WHITE);
		}
	}

	private void findClosestLight(Decal decal) {
		float minDistance = Float.MAX_VALUE;
		minDistance = applyLightsOnDecal(decal, minDistance);
		if (minDistance == Float.MAX_VALUE) {
			decal.setColor(DECAL_DARKEST_COLOR, DECAL_DARKEST_COLOR, DECAL_DARKEST_COLOR, 1f);
		}
	}

	private float convertDistanceToColorValueForDecal(final float maxLightDistanceForDecal, final float distance) {
		return MathUtils.map(
				0,
				(maxLightDistanceForDecal - DECAL_LIGHT_OFFSET),
				DECAL_DARKEST_COLOR,
				1f,
				maxLightDistanceForDecal - distance);
	}

	private float calculateDecalColorAffectedByLight(final Decal d,
													 float minDistance,
													 final float distance,
													 final float maxLightDistanceForDecal) {
		float newC = convertDistanceToColorValueForDecal(maxLightDistanceForDecal, distance);
		Color c = d.getColor();
		if (minDistance == Float.MAX_VALUE) {
			d.setColor(min(newC, 1f), min(newC, 1f), min(newC, 1f), 1f);
		} else {
			d.setColor(min(max(c.r, newC), 1f), min(max(c.g, newC), 1f), min(max(c.b, newC), 1f), 1f);
		}
		minDistance = min(minDistance, distance);
		return minDistance;
	}

	private void createShadowMaps( ) {
		PerspectiveCamera cameraLight = new PerspectiveCamera(90f, DEPTH_MAP_SIZE, DEPTH_MAP_SIZE);
		cameraLight.near = 0.0001F;
		cameraLight.far = CAMERA_LIGHT_FAR;
		for (Entity light : staticLightsEntities) {
			createShadowMapForLight(light, cameraLight);
		}
	}

	private void createShadowMapForLight(final Entity light,
										 final PerspectiveCamera cameraLight) {
		GameFrameBufferCubeMap frameBuffer = new GameFrameBufferCubeMap(
				Pixmap.Format.RGBA8888,
				DEPTH_MAP_SIZE,
				DEPTH_MAP_SIZE,
				true);

		cameraLight.direction.set(0, 0, -1);
		cameraLight.up.set(0, 1, 0);
		cameraLight.position.set(ComponentsMapper.staticLight.get(light).getPosition(auxVector3_1));
		cameraLight.rotate(Vector3.Y, 0);
		cameraLight.update();
		StaticLightComponent lightComponent = ComponentsMapper.staticLight.get(light);
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		depthShaderProgram.bind();
		depthShaderProgram.setUniformf("u_cameraFar", cameraLight.far);
		depthShaderProgram.setUniformf("u_lightPosition", cameraLight.position);
		for (int s = 0; s <= 5; s++) {
			Cubemap.CubemapSide side = Cubemap.CubemapSide.values()[s];
			frameBuffer.begin();
			frameBuffer.bindSide(side, cameraLight);
			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			renderModels(
					depthModelBatch,
					light,
					false,
					cameraLight);
//			handleScreenshot(frameBuffer);

		}
		frameBuffer.end();
		lightComponent.setShadowFrameBuffer(frameBuffer);
	}

	private float applyLightOnDecal(final Decal decal, float minDistance, LightComponent lightComponent) {
		float distance = lightComponent.getPosition(auxVector3_1).dst(decal.getPosition());
		float maxLightDistanceForDecal = lightComponent.getRadius();
		if (distance <= maxLightDistanceForDecal) {
			minDistance = calculateDecalColorAffectedByLight(decal, minDistance, distance, maxLightDistanceForDecal);
		}
		return minDistance;
	}

	private float applyLightsOnDecal(final Decal decal, float minDistance) {
		for (Entity light : shadowlessLightsEntities) {
			minDistance = applyLightOnDecal(decal, minDistance, shadowlessLight.get(light));
		}
		for (Entity light : staticLightsEntities) {
			minDistance = applyLightOnDecal(decal, minDistance, ComponentsMapper.staticLight.get(light));
		}
		return minDistance;
	}

	private void renderCharacterDecal(final Entity entity) {
		Decal decal = characterDecal.get(entity).getDecal();
		Vector3 decalPosition = decal.getPosition();
		Camera camera = getSystemsCommonData().getCamera();
		float ambient = getSystemsCommonData().getMap().getAmbient();
		decal.setColor(ambient, ambient, ambient, 1F);
		setDecalColorAccordingToLights(entity);
		decal.lookAt(auxVector3_1.set(decalPosition).sub(camera.direction), camera.up);
		decalBatch.add(decal);
	}

	private void updateLights(final PooledEngine engine) {
		for (Entity light : shadowlessLightsEntities) {
			updateLight(light);
		}
		if (!auxLightsListToRemove.isEmpty()) {
			for (Entity light : auxLightsListToRemove) {
				engine.removeEntity(light);
			}
			auxLightsListToRemove.clear();
		}
	}

	private void updateFlicker(final ShadowlessLightComponent lc, final long now) {
		if (lc.isFlicker() && now >= lc.getNextFlicker()) {
			lc.setIntensity(MathUtils.random(FLICKER_RANDOM_MIN, FLICKER_RANDOM_MAX) * lc.getOriginalIntensity());
			lc.setRadius(MathUtils.random(FLICKER_RANDOM_MIN, FLICKER_RANDOM_MAX) * lc.getOriginalRadius());
			lc.setNextFlicker(now + MathUtils.random(FLICKER_MAX_INTERVAL));
		}
	}

	private void updateLight(final Entity light) {
		ShadowlessLightComponent lc = shadowlessLight.get(light);
		long now = TimeUtils.millis();
		updateFlicker(lc, now);
		if (ComponentsMapper.simpleDecal.has(light)) {
			lc.setPosition(ComponentsMapper.simpleDecal.get(lc.getParent()).getDecal().getPosition());
		}
		float duration = lc.getDuration();
		if (duration > 0 && TimeUtils.timeSinceMillis(lc.getBeginTime()) >= (duration * 1000F)) {
			auxLightsListToRemove.add(light);
		}
	}

	private void initializeCharacterDecalForRendering(float deltaTime, Entity entity) {
		Camera camera = getSystemsCommonData().getCamera();
		CharacterSpriteData charSpriteData = character.get(entity).getCharacterSpriteData();
		Direction direction = CharacterUtils.calculateDirectionSeenFromCamera(camera, charSpriteData.getFacingDirection());
		SpriteType spriteType = charSpriteData.getSpriteType();
		boolean sameSpriteType = spriteType.equals(characterDecal.get(entity).getSpriteType());
		if ((!sameSpriteType || !characterDecal.get(entity).getDirection().equals(direction))) {
			updateCharacterDecalSprite(entity, direction, spriteType, sameSpriteType);
		} else {
			updateCharacterDecalFrame(deltaTime, entity, charSpriteData, spriteType);
		}
	}

	private void updateCharacterDecalFrame(float delta,
										   Entity entity,
										   CharacterSpriteData charSpriteData,
										   SpriteType spriteType) {
		CharacterDecalComponent characterDecalComponent = characterDecal.get(entity);
		AnimationComponent aniComp = animation.get(entity);
		SystemsCommonData systemsCommonData = getSystemsCommonData();
		if (!systemsCommonData.getMenuTable().isVisible() && animation.has(entity) && aniComp.getAnimation() != null) {
			AtlasRegion currentFrame = (AtlasRegion) characterDecalComponent.getDecal().getTextureRegion();
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
		CharacterDecalComponent characterDecalComponent = characterDecal.get(entity);
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
			if (player.has(entity)) {
				CharacterAnimations generalAnim = player.get(entity).getGeneralAnimations();
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
		AnimationComponent animationComponent = animation.get(entity);
		characterDecal.get(entity).initializeSprite(spriteType, direction);
		if (animation.has(entity)) {
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
		AnimationComponent animationComponent = animation.get(entity);
		CharacterAnimation animation = null;
		if (characterDecal.get(entity).getAnimations().contains(spriteType)) {
			animation = characterDecal.get(entity).getAnimations().get(spriteType, direction);
		} else if (player.has(entity)) {
			animation = player.get(entity).getGeneralAnimations().get(spriteType, direction);
		}
		if (animation != null) {
			animationComponent.init(spriteType.getAnimationDuration(), animation);
			if (!sameSpriteType) {
				animationComponent.resetStateTime();
			}
		}
	}

	@Override
	public void dispose( ) {
		skillFlowerFont.dispose();
		depthShaderProgram.dispose();
		decalBatch.dispose();
		depthModelBatch.dispose();
		spriteBatch.dispose();
		modelBatch.dispose();
		disposeShadowRelated();
	}

	private void disposeShadowRelated( ) {
		shadowsShaderProgram.dispose();
		shadowFrameBuffer.dispose();
		for (Entity light : staticLightsEntities) {
			ComponentsMapper.staticLight.get(light).getShadowFrameBuffer().dispose();
		}
	}

}
