package com.gadarts.necronemes.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;

public interface CharacterSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onCharacterCommandDone(final Entity character, final CharacterCommand lastCommand) {

	}

	default void onCharacterRotated(Entity character) {

	}

	default void onCharacterNodeChanged(Entity entity, MapGraphNode oldNode, MapGraphNode newNode) {

	}

	default void onItemPickedUp(final Entity itemPickedUp) {

	}
}
