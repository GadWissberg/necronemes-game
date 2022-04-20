package com.gadarts.necronemes.systems.ui.menu;

public interface MenuHandler {
	void toggleMenu(boolean active);
	void applyMenuOptions(final MenuOptionDefinition[] options);

}
