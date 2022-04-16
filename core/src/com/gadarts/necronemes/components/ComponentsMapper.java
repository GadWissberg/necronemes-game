package com.gadarts.necronemes.components;

import com.badlogic.ashley.core.ComponentMapper;
import com.gadarts.necronemes.components.animation.AnimationComponent;
import com.gadarts.necronemes.components.cd.CharacterDecalComponent;
import com.gadarts.necronemes.components.character.CharacterComponent;
import com.gadarts.necronemes.components.enemy.EnemyComponent;
import com.gadarts.necronemes.components.mi.ModelInstanceComponent;
import com.gadarts.necronemes.components.player.PlayerComponent;
import com.gadarts.necronemes.components.sd.SimpleDecalComponent;

/**
 * Easy to access component mappers.
 */
public class ComponentsMapper {
	public static final ComponentMapper<ModelInstanceComponent> modelInstance = ComponentMapper.getFor(ModelInstanceComponent.class);
	public static final ComponentMapper<FloorComponent> floor = ComponentMapper.getFor(FloorComponent.class);
	public static final ComponentMapper<CharacterComponent> character = ComponentMapper.getFor(CharacterComponent.class);
	public static final ComponentMapper<AnimationComponent> animation = ComponentMapper.getFor(AnimationComponent.class);
	public static final ComponentMapper<PlayerComponent> player = ComponentMapper.getFor(PlayerComponent.class);
	public static final ComponentMapper<CharacterDecalComponent> characterDecal = ComponentMapper.getFor(CharacterDecalComponent.class);
	public static final ComponentMapper<SimpleDecalComponent> simpleDecal = ComponentMapper.getFor(SimpleDecalComponent.class);
	public static final ComponentMapper<PickUpComponent> pickup = ComponentMapper.getFor(PickUpComponent.class);
	public static final ComponentMapper<EnemyComponent> enemy = ComponentMapper.getFor(EnemyComponent.class);
	public static final ComponentMapper<FlowerSkillIconComponent> flowerSkillIcon = ComponentMapper.getFor(FlowerSkillIconComponent.class);
	public static final ComponentMapper<BulletComponent> bullet = ComponentMapper.getFor(BulletComponent.class);
	public static final ComponentMapper<ParticleComponent> particle = ComponentMapper.getFor(ParticleComponent.class);
	public static final ComponentMapper<ParticleEffectParentComponent> particleParent = ComponentMapper.getFor(ParticleEffectParentComponent.class);
	public static final ComponentMapper<ShadowlessLightComponent> shadowlessLight = ComponentMapper.getFor(ShadowlessLightComponent.class);
	public static final ComponentMapper<StaticLightComponent> staticLight = ComponentMapper.getFor(StaticLightComponent.class);
	public static final ComponentMapper<ObstacleComponent> obstacle = ComponentMapper.getFor(ObstacleComponent.class);
}

