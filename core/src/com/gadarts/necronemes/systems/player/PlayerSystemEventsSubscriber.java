package com.gadarts.necronemes.systems.player;

import com.badlogic.ashley.core.Entity;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;
import com.gadarts.necronemes.systems.character.CharacterCommand;

public interface PlayerSystemEventsSubscriber extends SystemEventsSubscriber {
	void onPlayerPathCreated( );

	void onPlayerAppliedCommand(CharacterCommand command, Entity player);
}
