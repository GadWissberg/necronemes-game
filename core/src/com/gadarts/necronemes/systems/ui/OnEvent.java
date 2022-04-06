package com.gadarts.necronemes.systems.ui;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.systems.SystemsCommonData;

public interface OnEvent {
	boolean execute(WindowEventParameters windowEventParameters);
}
