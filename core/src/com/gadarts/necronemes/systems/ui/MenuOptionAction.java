package com.gadarts.necronemes.systems.ui;

import java.util.List;

public interface MenuOptionAction {
	void run(MenuHandler menuHandler, List<UserInterfaceSystemEventsSubscriber> uiSystemEventsSubscribers);
}
