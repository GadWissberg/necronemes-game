package com.gadarts.necronemes.systems.ui;

public interface MenuOptionDefinition {
	String getLabel();

	MenuOptionAction getAction();

	MenuOptionDefinition[] getSubOptions();

	default MenuOptionValidation getValidation() {
		return player -> true;
	}
}
