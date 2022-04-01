package com.gadarts.necronemes.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.necromine.model.characters.Direction;
import com.gadarts.necromine.model.characters.SpriteType;
import com.gadarts.necronemes.components.FloorComponent;
import com.gadarts.necronemes.components.animation.AnimationComponent;
import com.gadarts.necronemes.components.cd.CharacterDecalComponent;
import com.gadarts.necronemes.components.character.*;
import com.gadarts.necronemes.components.collision.CollisionComponent;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.components.mi.ModelInstanceComponent;
import com.gadarts.necronemes.components.player.PlayerComponent;
import com.gadarts.necronemes.components.player.Weapon;
import lombok.AccessLevel;
import lombok.Setter;

public class EntityBuilder {
	public static final String MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST = "Call beginBuildingEntity() first!";
	private static final EntityBuilder instance = new EntityBuilder();
	@Setter(AccessLevel.PRIVATE)
	private PooledEngine engine;
	private Entity currentEntity;

	public static EntityBuilder beginBuildingEntity(final PooledEngine engine) {
		instance.init(engine);
		return instance;
	}

	public EntityBuilder addModelInstanceComponent(final GameModelInstance modelInstance, final boolean visible) {
		return addModelInstanceComponent(modelInstance, visible, true);
	}

	public EntityBuilder addModelInstanceComponent(final GameModelInstance modelInstance,
												   final boolean visible,
												   final boolean castShadow) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		ModelInstanceComponent component = engine.createComponent(ModelInstanceComponent.class);
		component.init(modelInstance, visible, castShadow);
		currentEntity.add(component);
		component.getModelInstance().userData = currentEntity;
		return instance;
	}

	public EntityBuilder addFloorComponent( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		FloorComponent floorComponent = engine.createComponent(FloorComponent.class);
		currentEntity.add(floorComponent);
		return instance;
	}

	public Entity finishAndAddToEngine( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		engine.addEntity(currentEntity);
		return finish();
	}

	public Entity finish( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		Entity result = currentEntity;
		instance.reset();
		return result;
	}

	private void reset( ) {
		engine = null;
		currentEntity = null;
	}

	public EntityBuilder addAnimationComponent( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		AnimationComponent animComponent = engine.createComponent(AnimationComponent.class);
		currentEntity.add(animComponent);
		return instance;
	}

	public EntityBuilder addCollisionComponent( ) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		CollisionComponent collisionComponent = engine.createComponent(CollisionComponent.class);
		currentEntity.add(collisionComponent);
		return instance;
	}

	public EntityBuilder addCharacterDecalComponent(final CharacterAnimations animations,
													final SpriteType spriteType,
													final Direction direction,
													final Vector3 position) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		CharacterDecalComponent characterDecalComponent = engine.createComponent(CharacterDecalComponent.class);
		characterDecalComponent.init(animations, spriteType, direction, position);
		currentEntity.add(characterDecalComponent);
		return instance;
	}

	public EntityBuilder addCharacterComponent(final CharacterSpriteData characterSpriteData,
											   final CharacterSoundData characterSoundData,
											   final CharacterSkillsParameters skills) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		CharacterComponent charComponent = engine.createComponent(CharacterComponent.class);
		charComponent.init(characterSpriteData, characterSoundData, skills);
		currentEntity.add(charComponent);
		return instance;
	}

	public EntityBuilder addPlayerComponent(final Weapon selectedWeapon, final CharacterAnimations general) {
		if (engine == null) throw new RuntimeException(MSG_FAIL_CALL_BEGIN_BUILDING_ENTITY_FIRST);
		PlayerComponent playerComponent = engine.createComponent(PlayerComponent.class);
		playerComponent.init(selectedWeapon, general);
		currentEntity.add(playerComponent);
		return instance;
	}

	private void init(final PooledEngine engine) {
		this.engine = engine;
		this.currentEntity = engine.createEntity();
	}
}
