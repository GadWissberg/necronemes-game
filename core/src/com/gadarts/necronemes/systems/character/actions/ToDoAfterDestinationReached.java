package com.gadarts.necronemes.systems.character.actions;

import com.badlogic.ashley.core.Entity;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.map.MapGraph;

public interface ToDoAfterDestinationReached {
	void run(Entity character, MapGraph map, SoundPlayer soundPlayer, Object additionalData);
}
