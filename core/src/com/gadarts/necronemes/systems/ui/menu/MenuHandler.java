package com.gadarts.necronemes.systems.ui.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.systems.SystemsCommonData;

public interface MenuHandler {
	void toggleMenu(boolean active);

	void applyMenuOptions(final MenuOptionDefinition[] options);

	void init(Table table, GameAssetsManager assetsManager, SystemsCommonData systemsCommonData, SoundPlayer soundPlayer);
}
