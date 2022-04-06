package com.gadarts.necronemes.systems.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.systems.player.PlayerStorage;

public interface OnEvent {
	boolean execute(Event event, SoundPlayer soundPlayer, ItemSelectionHandler selectedItem, Table target, PlayerStorage storage);
}
