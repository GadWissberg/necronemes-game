package com.gadarts.necronemes.systems.player;

import com.badlogic.ashley.core.Entity;
import com.gadarts.necronemes.map.MapGraphPath;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.character.CharacterCommand;

public interface PlayerSystemEventsSubscriber extends SystemEventsSubscriber {
	void onPathCreated( );

	void onPlayerAppliedCommand(CharacterCommand command, Entity player);
}
