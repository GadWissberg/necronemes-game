package com.gadarts.necronemes.systems.character.actions;

import com.badlogic.ashley.core.Entity;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.character.CharacterComponent;
import com.gadarts.necronemes.components.character.CharacterMotivation;
import com.gadarts.necronemes.map.MapGraph;

public class PrimaryAttackAction implements ToDoAfterDestinationReached {

	@Override
	public void run(final Entity character,
					final MapGraph map,
					final SoundPlayer soundPlayer,
					final Object itemToPickup) {
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		Entity target = characterComponent.getTarget();
		if (target != null) {
			characterComponent.getRotationData().setRotating(true);
			characterComponent.setMotivation(CharacterMotivation.TO_ATTACK, CharacterMotivation.USE_PRIMARY);
		}
	}
}
