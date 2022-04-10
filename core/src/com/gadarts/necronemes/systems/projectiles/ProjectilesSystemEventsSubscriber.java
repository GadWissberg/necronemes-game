package com.gadarts.necronemes.systems.projectiles;

import com.badlogic.ashley.core.Entity;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;

public interface ProjectilesSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onBulletCollisionWithWall(Entity bullet, MapGraphNode node) {

	}

	default void onProjectileCollisionWithAnotherEntity(Entity bullet, Entity collidable) {

	}
}
