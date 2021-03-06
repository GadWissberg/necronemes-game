package com.gadarts.necronemes.systems.character;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.characters.CharacterTypes;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necromine.model.characters.attributes.Agility;
import com.gadarts.necromine.model.characters.attributes.Strength;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;
import com.gadarts.necronemes.GameLifeCycleHandler;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.animation.AnimationComponent;
import com.gadarts.necronemes.components.cd.CharacterDecalComponent;
import com.gadarts.necronemes.components.character.*;
import com.gadarts.necronemes.components.player.PlayerComponent;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.map.MapGraphConnection;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.map.MapGraphPath;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.enemy.EnemySystemEventsSubscriber;
import com.gadarts.necronemes.systems.player.PlayerSystemEventsSubscriber;
import com.gadarts.necronemes.systems.projectiles.BulletSystemEventsSubscriber;
import com.gadarts.necronemes.systems.render.RenderSystemEventsSubscriber;
import com.gadarts.necronemes.utils.EntityBuilder;

import java.util.Map;

import static com.gadarts.necromine.model.characters.SpriteType.*;
import static com.gadarts.necronemes.components.ComponentsMapper.*;
import static com.gadarts.necronemes.components.character.CharacterMotivation.*;
import static com.gadarts.necronemes.map.MapGraphConnectionCosts.CLEAN;
import static com.gadarts.necronemes.utils.GeneralUtils.EPSILON;

public class CharacterSystem extends GameSystem<CharacterSystemEventsSubscriber> implements
		PlayerSystemEventsSubscriber,
		RenderSystemEventsSubscriber,
		EnemySystemEventsSubscriber,
		BulletSystemEventsSubscriber {
	private static final int ROT_INTERVAL = 125;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final Vector2 auxVector2_2 = new Vector2();
	private static final Vector2 auxVector2_3 = new Vector2();
	private static final CharacterCommand auxCommand = new CharacterCommand();
	private static final float CHARACTER_STEP_SIZE = 0.22f;
	private static final long CHARACTER_PAIN_DURATION = 1000;
	private final static Vector3 auxVector3_3 = new Vector3();
	private final static Vector3 auxVector3_4 = new Vector3();
	private final MapGraphPath currentPath = new MapGraphPath();
	private final Map<SpriteType, OnFrameChangedEvent> onFrameChangedEvents = Map.of(
			RUN, this::applyRunning,
			PICKUP, this::handlePickup,
			ATTACK, this::applyMeleeAttack,
			ATTACK_PRIMARY, this::applyPrimaryAttack
	);
	private ParticleEffect bloodSplatterEffect;
	private ImmutableArray<Entity> characters;

	public CharacterSystem(SystemsCommonData systemsCommonData,
						   SoundPlayer soundPlayer,
						   GameAssetsManager assetsManager,
						   GameLifeCycleHandler lifeCycleHandler) {
		super(systemsCommonData, soundPlayer, assetsManager, lifeCycleHandler);
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		characters = getEngine().getEntitiesFor(Family.all(CharacterComponent.class).get());
	}

	@Override
	public Class<CharacterSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return CharacterSystemEventsSubscriber.class;
	}

	@Override
	public void initializeData( ) {
		bloodSplatterEffect = getAssetsManager().getParticleEffect(Assets.ParticleEffects.BLOOD_SPLATTER);
	}

	/**
	 * Applies a given command on the given character.
	 *
	 * @param command
	 * @param character
	 */
	@SuppressWarnings("JavaDoc")
	public void applyCommand(final CharacterCommand command,
							 final Entity character) {
		SystemsCommonData systemsCommonData = getSystemsCommonData();
		systemsCommonData.setCurrentCommand(command);
		systemsCommonData.getCurrentCommand().setStarted(false);
		if (ComponentsMapper.character.get(character).getCharacterSpriteData().getSpriteType() != PAIN) {
			beginProcessingCommand(command, character, systemsCommonData);
		}
	}

	private void beginProcessingCommand(final CharacterCommand command,
										final Entity character,
										SystemsCommonData systemsCommonData) {
		systemsCommonData.getCurrentCommand().setStarted(true);
		currentPath.clear();
		if (command.getType().isRequiresMovement()) {
			applyMovementOfCommandWithAgility(command, character);
		}
		if (currentPath.nodes.size > 1) {
			commandSet(character);
		} else {
			destinationReached(character);
		}
	}

	public void destinationReached(final Entity character) {
		if (getSystemsCommonData().getCurrentCommand().getType().getToDoAfterDestinationReached() != null) {
			executeActionsAfterDestinationReached(character);
		} else {
			commandDone(character);
		}
	}

	private void executeActionsAfterDestinationReached(final Entity character) {
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onDestinationReached(character);
		}
		CharacterCommand currentCommand = getSystemsCommonData().getCurrentCommand();
		currentCommand.getType()
				.getToDoAfterDestinationReached()
				.run(character, getSystemsCommonData().getMap(), getSoundPlayer(), currentCommand.getAdditionalData());
	}

	private void commandSet(final Entity character) {
		MapGraphNode destNode = currentPath.get(1);
		initDestinationNode(ComponentsMapper.character.get(character), destNode);
	}

	public void initDestinationNode(final CharacterComponent characterComponent,
									final MapGraphNode destNode) {
		characterComponent.getRotationData().setRotating(true);
		characterComponent.setDestinationNode(destNode);
	}

	private void applyMovementOfCommandWithAgility(final CharacterCommand command, final Entity character) {
		Agility agility = ComponentsMapper.character.get(character).getSkills().getAgility();
		Array<MapGraphNode> nodes = command.getPath().nodes;
		int agilityValue = agility.getValue();
		if (nodes.size > agilityValue) {
			nodes.removeRange(agilityValue, nodes.size - 1);
		}
		currentPath.nodes.addAll(nodes);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		CharacterCommand currentCommand = getSystemsCommonData().getCurrentCommand();
		if (currentCommand != null) {
			handleCurrentCommand(currentCommand);
		}
		for (Entity character : characters) {
			handlePain(character);
		}
	}

	private void handlePain(final Entity character) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		long lastDamage = characterComponent.getSkills().getHealthData().getLastDamage();
		CharacterSpriteData spriteData = characterComponent.getCharacterSpriteData();
		if (spriteData.getSpriteType() == PAIN && TimeUtils.timeSinceMillis(lastDamage) > CHARACTER_PAIN_DURATION) {
			painDone(character, characterComponent, spriteData);
		}
	}

	private void applyDamageToCharacter(final Entity attacked, final int damage) {
		CharacterComponent characterComponent = character.get(attacked);
		characterComponent.dealDamage(damage);
		handleDeath(attacked);
		Vector3 pos = ComponentsMapper.characterDecal.get(attacked).getNodePosition(auxVector3_1);
		float height = calculateSplatterEffectHeight(attacked, pos);
		addSplatterEffect(auxVector3_1.set(pos.x + 0.5F, height, pos.z + 0.5F));
	}

	private void addSplatterEffect(final Vector3 pos) {
		EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addParticleEffectComponent((PooledEngine) getEngine(), bloodSplatterEffect, pos)
				.finishAndAddToEngine();
	}

	private float calculateSplatterEffectHeight(final Entity attacked, final Vector3 pos) {
		float height = pos.y;
		if (ComponentsMapper.enemy.has(attacked)) {
			height += ComponentsMapper.enemy.get(attacked).getEnemyDefinition().getHeight() / 2F;
		} else if (ComponentsMapper.player.has(attacked)) {
			height += PlayerComponent.PLAYER_HEIGHT;
		}
		return height;
	}

	private void handleDeath(final Entity character) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		CharacterHealthData healthData = characterComponent.getSkills().getHealthData();
		CharacterSoundData soundData = characterComponent.getSoundData();
		if (healthData.getHp() <= 0) {
			characterDies(character, characterComponent, soundData);
		} else {
			characterInPain(character, characterComponent, healthData, soundData);
		}
	}

	private void characterInPain(Entity character,
								 CharacterComponent characterComponent,
								 CharacterHealthData healthData,
								 CharacterSoundData soundData) {
		getSoundPlayer().playSound(soundData.getPainSound());
		characterComponent.getCharacterSpriteData().setSpriteType(PAIN);
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCharacterGotDamage(character);
		}
		if (healthData.getHp() > 0) {
			characterComponent.setMotivation(null);
		}
	}

	private void characterDies(Entity character, CharacterComponent characterComponent, CharacterSoundData soundData) {
		CharacterSpriteData charSpriteData = characterComponent.getCharacterSpriteData();
		charSpriteData.setSpriteType(charSpriteData.isSingleDeathAnimation() ? LIGHT_DEATH_1 : randomLightDeath());
		if (animation.has(character)) {
			animation.get(character).resetStateTime();
		}
		characterComponent.setMotivation(null);
		getSoundPlayer().playSound(soundData.getDeathSound());
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCharacterDies(character);
		}
	}

	private void painDone(Entity character, CharacterComponent characterComponent, CharacterSpriteData spriteData) {
		characterComponent.setMotivation(null);
		spriteData.setSpriteType(IDLE);
		CharacterCommand currentCommand = getSystemsCommonData().getCurrentCommand();
		if (currentCommand != null && !currentCommand.isStarted()) {
			applyCommand(currentCommand, character);
		}
	}

	public void commandDone(final Entity character) {
		currentPath.clear();
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		characterComponent.setMotivation(null);
		characterComponent.getCharacterSpriteData().setSpriteType(SpriteType.IDLE);
		CharacterCommand lastCommand = getSystemsCommonData().getCurrentCommand();
		getSystemsCommonData().setCurrentCommand(null);
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCharacterCommandDone(character, lastCommand);
		}
	}

	private void handleModeWithNonLoopingAnimation(final Entity character) {
		AnimationComponent animationComponent = animation.get(character);
		Animation<TextureAtlas.AtlasRegion> animation = animationComponent.getAnimation();
		if (animation.isAnimationFinished(animationComponent.getStateTime())) {
			SpriteType spriteType = ComponentsMapper.character.get(character).getCharacterSpriteData().getSpriteType();
			if (spriteType.isAddReverse()) {
				handleAnimationReverse(character, animationComponent, animation, spriteType);
			} else {
				commandDone(character);
			}
		}
	}

	private void handleAnimationReverse(Entity character,
										AnimationComponent animationComponent,
										Animation<TextureAtlas.AtlasRegion> animation,
										SpriteType spriteType) {
		if (animationComponent.isDoingReverse()) {
			commandDone(character);
			animation.setPlayMode(Animation.PlayMode.NORMAL);
		} else {
			animation.setPlayMode(Animation.PlayMode.REVERSED);
			animation.setFrameDuration(spriteType.getAnimationDuration());
			animationComponent.resetStateTime();
		}
		animationComponent.setDoingReverse(!animationComponent.isDoingReverse());
	}

	private void handleCurrentCommand(final CharacterCommand currentCommand) {
		CharacterComponent characterComponent = character.get(currentCommand.getCharacter());
		SpriteType spriteType = characterComponent.getCharacterSpriteData().getSpriteType();
		if (spriteType == ATTACK || spriteType == PICKUP || spriteType == ATTACK_PRIMARY) {
			handleModeWithNonLoopingAnimation(currentCommand.getCharacter());
		} else if (characterComponent.getMotivationData().getMotivation() == END_MY_TURN) {
			commandDone(currentCommand.getCharacter());
		} else {
			handleRotation(currentCommand.getCharacter(), characterComponent);
		}
	}

	private Direction calculateDirectionToDestination(final Entity character) {
		Vector3 characterPos = auxVector3_1.set(characterDecal.get(character).getDecal().getPosition());
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		MapGraphNode destinationNode = characterComponent.getDestinationNode();
		Vector2 destPos = destinationNode.getCenterPosition(auxVector2_2);
		Vector2 directionToDest = destPos.sub(characterPos.x, characterPos.z).nor();
		return Direction.findDirection(directionToDest);
	}


	private void handleRotation(final Entity character, final CharacterComponent charComponent) {
		if (charComponent.getCharacterSpriteData().getSpriteType() == PAIN) return;
		CharacterRotationData rotationData = charComponent.getRotationData();
		if (rotationData.isRotating() && TimeUtils.timeSinceMillis(rotationData.getLastRotation()) > ROT_INTERVAL) {
			for (CharacterSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onCharacterRotated(character);
			}
			Direction directionToDest = initializeRotation(character, charComponent, rotationData);
			rotate(charComponent, rotationData, directionToDest);
		}
	}

	private void rotate(CharacterComponent charComponent, CharacterRotationData rotationData, Direction directionToDest) {
		if (charComponent.getCharacterSpriteData().getFacingDirection() != directionToDest) {
			CharacterSpriteData characterSpriteData = charComponent.getCharacterSpriteData();
			Vector2 currentDirVector = characterSpriteData.getFacingDirection().getDirection(auxVector2_1);
			float diff = directionToDest.getDirection(auxVector2_2).angleDeg() - currentDirVector.angleDeg();
			int side = auxVector2_3.set(1, 0).setAngleDeg(diff).angleDeg() > 180 ? -1 : 1;
			characterSpriteData.setFacingDirection(Direction.findDirection(currentDirVector.rotateDeg(45f * side)));
		} else {
			rotationDone(rotationData, charComponent.getCharacterSpriteData());
		}
	}

	private Direction calculateDirectionToTarget(final Entity character) {
		Vector3 pos = auxVector3_1.set(characterDecal.get(character).getDecal().getPosition());
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		Entity target = characterComponent.getTarget();
		MapGraph map = getSystemsCommonData().getMap();
		MapGraphNode targetNode = map.getNode(characterDecal.get(target).getDecal().getPosition());
		Vector2 destPos = targetNode.getCenterPosition(auxVector2_2);
		Vector2 directionToDest = destPos.sub(pos.x, pos.z).nor();
		return Direction.findDirection(directionToDest);
	}

	private Direction initializeRotation(Entity character,
										 CharacterComponent charComponent,
										 CharacterRotationData rotationData) {
		rotationData.setLastRotation(TimeUtils.millis());
		if (charComponent.getMotivationData().getMotivation() == CharacterMotivation.TO_ATTACK) {
			return calculateDirectionToTarget(character);
		} else if (charComponent.getMotivationData().getMotivation() == TO_PICK_UP) {
			return charComponent.getCharacterSpriteData().getFacingDirection();
		} else {
			return calculateDirectionToDestination(character);
		}
	}

	private void rotationDone(CharacterRotationData rotationData, CharacterSpriteData characterSpriteData) {
		rotationData.setRotating(false);
		CharacterCommand currentCommand = getSystemsCommonData().getCurrentCommand();
		CharacterComponent characterComponent = character.get(currentCommand.getCharacter());
		characterSpriteData.setSpriteType(RUN);
		if (characterComponent.getMotivationData().getMotivation() == CharacterMotivation.TO_ATTACK) {
			characterSpriteData.setSpriteType(decideAttackSpriteType(characterComponent.getMotivationData()));
		} else if (characterComponent.getMotivationData().getMotivation() == CharacterMotivation.TO_PICK_UP) {
			characterSpriteData.setSpriteType(PICKUP);
		}
	}

	private SpriteType decideAttackSpriteType(CharacterMotivationData motivationData) {
		SpriteType spriteType;
		Integer motivationAdditionalData = (Integer) motivationData.getMotivationAdditionalData();
		if (motivationAdditionalData != null && motivationAdditionalData == USE_PRIMARY) {
			spriteType = ATTACK_PRIMARY;
		} else {
			spriteType = ATTACK;
		}
		return spriteType;
	}

	@Override
	public void dispose( ) {

	}

	@Override
	public void onPlayerPathCreated( ) {

	}

	@Override
	public void onEnemyAppliedCommand(CharacterCommand auxCommand, Entity enemy) {
		onCharacterAppliedCommand(auxCommand, enemy);
	}

	@Override
	public void onPlayerAppliedCommand(CharacterCommand command) {
		onCharacterAppliedCommand(command, getSystemsCommonData().getPlayer());
	}

	@Override
	public void onHitScanCollisionWithAnotherEntity(WeaponsDefinitions definition, Entity collidable) {
		if (ComponentsMapper.character.has(collidable)) {
			applyDamageToCharacter(collidable, definition.getDamage());
		}
	}

	private void onCharacterAppliedCommand(CharacterCommand command, Entity character) {
		auxCommand.init(command);
		applyCommand(auxCommand, character);
	}

	private void handlePickup(Entity character, TextureAtlas.AtlasRegion newFrame) {
		if (newFrame.index == 1 && animation.get(character).isDoingReverse()) {
			CharacterMotivation mode = ComponentsMapper.character.get(character).getMotivationData().getMotivation();
			CharacterCommand currentCommand = getSystemsCommonData().getCurrentCommand();
			if (mode == TO_PICK_UP && currentCommand.getAdditionalData() != null) {
				Entity itemPickedUp = (Entity) currentCommand.getAdditionalData();
				for (CharacterSystemEventsSubscriber subscriber : subscribers) {
					subscriber.onItemPickedUp(itemPickedUp);
				}
			}
		}
	}

	private void applyMeleeDamageToCharacter(final Entity attacker, final Entity attacked) {
		Strength strength = character.get(attacker).getSkills().getStrength();
		applyDamageToCharacter(attacked, MathUtils.random(strength.getMinDamage(), strength.getMaxDamage()));
	}

	@Override
	public void onProjectileCollisionWithAnotherEntity(final Entity bullet, final Entity collidable) {
		if (ComponentsMapper.character.has(collidable)) {
			applyDamageToCharacter(collidable, ComponentsMapper.bullet.get(bullet).getDamage());
		}
	}

	@Override
	public void onFrameChanged(final Entity character, final float deltaTime, final TextureAtlas.AtlasRegion newFrame) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		CharacterSpriteData characterSpriteData = characterComponent.getCharacterSpriteData();
		OnFrameChangedEvent onFrameChangedEvent = onFrameChangedEvents.get(characterSpriteData.getSpriteType());
		if (onFrameChangedEvent != null) {
			onFrameChangedEvent.run(character, newFrame);
		}
	}

	private void applyPrimaryAttack(Entity character,
									TextureAtlas.AtlasRegion newFrame) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		if (newFrame.index == characterComponent.getCharacterSpriteData().getPrimaryAttackHitFrameIndex()) {
			CharacterDecalComponent charDecalComp = characterDecal.get(character);
			MapGraphNode positionNode = getSystemsCommonData().getMap().getNode(charDecalComp.getDecal().getPosition());
			Vector3 positionNodeCenterPosition = positionNode.getCenterPosition(auxVector3_4);
			Vector3 direction = calculateDirectionToTarget(characterComponent, positionNodeCenterPosition);
			for (CharacterSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onCharacterEngagesPrimaryAttack(character, direction, positionNodeCenterPosition);
			}
		}
	}

	private Vector3 calculateDirectionToTarget(CharacterComponent characterComp, Vector3 positionNodeCenterPosition) {
		CharacterDecalComponent targetDecalComp = characterDecal.get(characterComp.getTarget());
		MapGraphNode targetNode = getSystemsCommonData().getMap().getNode(targetDecalComp.getDecal().getPosition());
		Vector3 targetNodeCenterPosition = targetNode.getCenterPosition(auxVector3_3);
		targetNodeCenterPosition.y += 0.5f;
		return targetNodeCenterPosition.sub(positionNodeCenterPosition);
	}

	private void applyMeleeAttack(Entity character,
								  TextureAtlas.AtlasRegion newFrame) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		if (newFrame.index != characterComponent.getCharacterSpriteData().getMeleeHitFrameIndex()) return;
		Entity target = characterComponent.getTarget();
		if (player.has(character)) {
			if (((WeaponsDefinitions) getSystemsCommonData().getStorage().getSelectedWeapon().getDefinition()).isMelee()) {
				applyMeleeDamageToCharacter(character, target);
			}
		} else {
			applyMeleeDamageToCharacter(character, target);
		}
	}

	private void applyRunning(final Entity character,
							  final TextureAtlas.AtlasRegion newFrame) {
		if (newFrame.index == 0 || newFrame.index == 5) {
			getSoundPlayer().playSound(ComponentsMapper.character.get(character).getSoundData().getStepSound());
		}
		MapGraphNode dest = ComponentsMapper.character.get(character).getDestinationNode();
		Decal decal = characterDecal.get(character).getDecal();
		if (auxVector2_1.set(decal.getX(), decal.getZ()).dst2(dest.getCenterPosition(auxVector2_2)) < EPSILON) {
			reachedNodeOfPath(character, dest);
		} else {
			takeStep(character);
		}
	}

	private void takeStep(final Entity entity) {
		CharacterDecalComponent characterDecalComponent = characterDecal.get(entity);
		MapGraph map = getSystemsCommonData().getMap();
		MapGraphNode oldNode = map.getNode(characterDecalComponent.getNodePosition(auxVector2_3));
		translateCharacter(entity, characterDecalComponent);
		MapGraphNode newNode = map.getNode(characterDecalComponent.getNodePosition(auxVector2_3));
		if (oldNode != newNode) {
			enteredNewNode(entity, oldNode, newNode);
		}
	}

	private void fixHeightPositionOfDecals(final Entity entity, final MapGraphNode newNode) {
		CharacterDecalComponent characterDecalComponent = characterDecal.get(entity);
		Decal decal = characterDecalComponent.getDecal();
		Vector3 position = decal.getPosition();
		float newNodeHeight = newNode.getHeight();
		decal.setPosition(position.x, newNodeHeight + CharacterTypes.BILLBOARD_Y, position.z);
	}

	private void enteredNewNode(final Entity entity, final MapGraphNode oldNode, final MapGraphNode newNode) {
		fixHeightPositionOfDecals(entity, newNode);
		for (CharacterSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onCharacterNodeChanged(entity, oldNode, newNode);
		}
	}

	private void translateCharacter(final Entity entity, final CharacterDecalComponent characterDecalComponent) {
		character.get(entity).getDestinationNode().getCenterPosition(auxVector2_2);
		Decal decal = characterDecalComponent.getDecal();
		Vector2 velocity = auxVector2_2.sub(auxVector2_1.set(decal.getX(), decal.getZ())).nor().scl(CHARACTER_STEP_SIZE);
		decal.translate(auxVector3_1.set(velocity.x, 0, velocity.y));
	}

	private void reachedNodeOfPath(final Entity character,
								   final MapGraphNode oldDest) {
		MapGraphNode newDest = currentPath.getNextOf(oldDest);
		MapGraphConnection connection = getSystemsCommonData().getMap().findConnection(oldDest, newDest);
		if (newDest != null && connection != null && connection.getCost() == CLEAN.getCostValue()) {
			initDestinationNode(ComponentsMapper.character.get(character), newDest);
			takeStep(character);
		} else {
			destinationReached(character);
		}
	}

	private interface OnFrameChangedEvent {
		void run(Entity character, TextureAtlas.AtlasRegion newFrame);
	}
}
