package com.gadarts.necronemes.systems.ui.menu;

public interface MenuOptionDefinition {
	String getLabel();

	MenuOptionAction getAction();

	MenuOptionDefinition[] getSubOptions();

	default MenuOptionValidation getValidation() {
		return player -> true;
	}
}
