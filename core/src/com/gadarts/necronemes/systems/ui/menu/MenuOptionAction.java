package com.gadarts.necronemes.systems.ui.menu;

import com.gadarts.necronemes.systems.ui.UserInterfaceSystemEventsSubscriber;

import java.util.List;

public interface MenuOptionAction {
	void run(MenuHandler menuHandler, List<UserInterfaceSystemEventsSubscriber> uiSystemEventsSubscribers);
}
