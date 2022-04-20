package com.gadarts.necronemes.systems.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.Necronemes;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.systems.SystemsCommonData;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

import static com.gadarts.necronemes.systems.SystemsCommonData.TABLE_NAME_HUD;


@Getter
public class MenuHandlerImpl implements MenuHandler {
	private static final String TABLE_NAME_MENU = "menu";
	private final GameStage uiStage;
	private final List<UserInterfaceSystemEventsSubscriber> subscribers;
	private Table menuTable;

	public MenuHandlerImpl(GameStage uiStage, List<UserInterfaceSystemEventsSubscriber> subscribers) {
		this.uiStage = uiStage;
		this.subscribers = subscribers;
	}

	public void toggleMenu(final boolean active) {
		toggleMenu(active, uiStage);
		subscribers.forEach(subscriber -> subscriber.onMenuToggled(active));
	}

	@Override
	public void applyMenuOptions(MenuOptionDefinition[] options) {
		
	}

	private Label createLogo(GameAssetsManager assetsManager) {
		BitmapFont largeFont = assetsManager.getFont(Assets.Fonts.CHUBGOTHIC_LARGE);
		Label.LabelStyle logoStyle = new Label.LabelStyle(largeFont, MenuOption.FONT_COLOR_REGULAR);
		return new Label(Necronemes.TITLE, logoStyle);
	}

	void addMenuTable(Table table,
					  GameAssetsManager assetsManager,
					  SystemsCommonData systemsCommonData,
					  SoundPlayer soundPlayer) {
		menuTable = table;
		menuTable.setName(TABLE_NAME_MENU);
		menuTable.add(createLogo(assetsManager)).row();
		applyMenuOptions(MainMenuOptions.values(), assetsManager, systemsCommonData, soundPlayer);
		menuTable.toFront();
		toggleMenu(DefaultGameSettings.MENU_ON_STARTUP);
	}

	public void applyMenuOptions(MenuOptionDefinition[] options,
								 GameAssetsManager assetsManager,
								 SystemsCommonData systemsCommonData,
								 SoundPlayer soundPlayer) {
		menuTable.clear();
		BitmapFont smallFont = assetsManager.getFont(Assets.Fonts.CHUBGOTHIC_SMALL);
		Label.LabelStyle style = new Label.LabelStyle(smallFont, MenuOption.FONT_COLOR_REGULAR);
		Arrays.stream(options).forEach(o -> {
			if (o.getValidation().validate(systemsCommonData.getPlayer())) {
				menuTable.add(new MenuOption(o, style, soundPlayer, this, subscribers)).row();
			}
		});
	}

	public void toggleMenu(final boolean active, final GameStage stage) {
		getMenuTable().setVisible(active);
		stage.getRoot().findActor(TABLE_NAME_HUD).setTouchable(active ? Touchable.disabled : Touchable.enabled);
	}
}
