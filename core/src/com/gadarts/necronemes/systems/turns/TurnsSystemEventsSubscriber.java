package com.gadarts.necronemes.systems.turns;

import com.gadarts.necronemes.systems.SystemEventsSubscriber;

public interface TurnsSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onEnemyTurn(final long currentTurnId) {

	}
	default void onPlayerTurn(final long currentTurnId) {

	}
}
