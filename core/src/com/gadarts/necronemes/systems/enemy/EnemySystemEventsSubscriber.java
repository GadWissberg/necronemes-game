package com.gadarts.necronemes.systems.enemy;

import com.badlogic.ashley.core.Entity;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;
import com.gadarts.necronemes.systems.character.CharacterCommand;

public interface EnemySystemEventsSubscriber extends SystemEventsSubscriber {
	default void onEnemyAwaken(Entity enemy) {

	}

	default void onEnemyFinishedTurn( ) {

	}

	default void onEnemyAppliedCommand(CharacterCommand auxCommand, Entity enemy) {

	}
}
