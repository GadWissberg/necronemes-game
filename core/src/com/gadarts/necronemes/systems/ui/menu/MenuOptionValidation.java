package com.gadarts.necronemes.systems.ui.menu;

import com.badlogic.ashley.core.Entity;

public interface MenuOptionValidation {
	boolean validate(Entity player);
}
