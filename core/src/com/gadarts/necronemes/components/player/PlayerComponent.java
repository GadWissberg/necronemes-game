package com.gadarts.necronemes.components.player;

import com.gadarts.necronemes.components.GameComponent;
import com.gadarts.necronemes.components.character.CharacterAnimations;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerComponent implements GameComponent {
	public static final float PLAYER_HEIGHT = 1;
	private final PlayerStorage storage = new PlayerStorage();
	private CharacterAnimations generalAnimations;

	@Setter
	private boolean disabled;

	@Override
	public void reset( ) {
		storage.clear();
	}

	public void init(final Weapon selectedWeapon, final CharacterAnimations general) {
		this.generalAnimations = general;
		storage.setSelectedWeapon(selectedWeapon);
	}
}
