package com.gadarts.necronemes.systems.ui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NewGameMenuOptions implements MenuOptionDefinition {
	MASTABA(
			"Mastaba - Test Map",
			(menuHandler, uiSystemEventsSubscribers) -> uiSystemEventsSubscribers.forEach(UserInterfaceSystemEventsSubscriber::onNewGameSelectedInMenu));
	private final String label;
	private final MenuOptionAction action;

	@Override
	public MenuOptionDefinition[] getSubOptions() {
		return null;
	}
}
