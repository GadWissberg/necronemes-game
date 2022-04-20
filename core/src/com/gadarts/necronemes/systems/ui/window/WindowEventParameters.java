package com.gadarts.necronemes.systems.ui.window;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.ui.storage.ItemSelectionHandler;
import com.gadarts.necronemes.systems.ui.UserInterfaceSystemEventsSubscriber;
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
