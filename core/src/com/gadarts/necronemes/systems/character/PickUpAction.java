package com.gadarts.necronemes.systems.character;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.character.CharacterComponent;
import com.gadarts.necronemes.components.character.CharacterMotivation;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.systems.character.actions.ToDoAfterDestinationReached;

public class PickUpAction implements ToDoAfterDestinationReached {

	private final static Vector2 auxVector2 = new Vector2();

	@Override
	public void run(final Entity character,
					final MapGraph map,
					final SoundPlayer soundPlayer,
					final Object itemToPickup) {
		Vector2 charPos = ComponentsMapper.characterDecal.get(character).getNodePosition(auxVector2);
		Entity pickup = map.getPickupFromNode(map.getNode(charPos));
		CharacterComponent characterComponent = ComponentsMapper.character.get(character);
		if (pickup != null) {
			characterComponent.getRotationData().setRotating(true);
			characterComponent.setMotivation(CharacterMotivation.TO_PICK_UP, itemToPickup);
		} else {
			characterComponent.setMotivation(CharacterMotivation.END_MY_TURN, itemToPickup);
		}
	}
}
