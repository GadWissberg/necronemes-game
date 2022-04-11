package com.gadarts.necronemes.systems.projectiles;

import com.badlogic.ashley.core.Entity;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;

public interface BulletSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onBulletCollisionWithWall(Entity bullet, MapGraphNode node) {

	}

	default void onProjectileCollisionWithAnotherEntity(Entity bullet, Entity collidable) {

	}

	default void onHitScanCollisionWithAnotherEntity(WeaponsDefinitions definition, Entity collidable) {
	}
}
