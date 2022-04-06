package com.gadarts.necronemes.systems.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.systems.SystemsCommonData;
import lombok.Data;

import java.util.List;

@Data
public class WindowEventParameters {
	private GameWindowEvent windowEvent;
	private SoundPlayer soundPlayer;
	private ItemSelectionHandler selectedItem;
	private Table target;
	private SystemsCommonData systemsCommonData;
	private List<UserInterfaceSystemEventsSubscriber> subscribers;
}
