package com.gadarts.necronemes.components;

import com.gadarts.necronemes.components.player.Item;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickUpComponent implements GameComponent {
	public static final float FLICKER_DELTA = 0.01f;
	private float flicker = FLICKER_DELTA;
	private Item item;

	@Override
	public void reset( ) {
		flicker = FLICKER_DELTA;
	}
}

