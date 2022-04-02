package com.gadarts.necronemes.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;

public interface CharacterSystemEventsSubscriber extends SystemEventsSubscriber {
	void onCharacterRotated(Entity character);

	void onCharacterNodeChanged(Entity entity, MapGraphNode oldNode, MapGraphNode newNode);
}
