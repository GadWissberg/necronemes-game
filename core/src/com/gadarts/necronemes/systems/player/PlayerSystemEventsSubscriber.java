package com.gadarts.necronemes.systems.player;

import com.gadarts.necronemes.components.player.Item;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;
import com.gadarts.necronemes.systems.character.CharacterCommand;

import java.util.List;

public interface PlayerSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onPlayerPathCreated( ) {

	}

	default void onPlayerAppliedCommand(CharacterCommand command) {

	}

	default void onItemAddedToStorage(Item item) {

	}

	default void onPlayerFinishedTurn( ) {

	}

	default void onAttackModeDeactivated( ) {

	}

	default void onAttackModeActivated(List<MapGraphNode> availableNodes) {
	}

	default void onPlayerStatusChanged(boolean status) {

	}
}
