package com.gadarts.necronemes.systems.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.player.Item;
import com.gadarts.necronemes.systems.SystemsCommonData;

import java.util.ArrayList;
import java.util.List;

public class GameStage extends Stage {
	private final SoundPlayer soundPlayer;
	private final List<GameWindow> windows = new ArrayList<>();

	public GameStage(final FitViewport fitViewport, final SoundPlayer soundPlayer) {
		super(fitViewport);
		this.soundPlayer = soundPlayer;
	}

	private void createStorageWindow(GameAssetsManager assetsManager,
									 SystemsCommonData systemsCommonData,
									 List<UserInterfaceSystemEventsSubscriber> subscribers) {
		Texture ninePatchTexture = assetsManager.getTexture(Assets.UiTextures.NINEPATCHES);
		NinePatch patch = new NinePatch(ninePatchTexture, 12, 12, 12, 12);
		Window.WindowStyle style = new Window.WindowStyle(new BitmapFont(), Color.BLACK, new NinePatchDrawable(patch));
		StorageWindow window = new StorageWindow(style, assetsManager, soundPlayer, systemsCommonData, subscribers);
		defineStorageWindow(window);
		addActor(window);
		windows.add(window);
	}

	void openStorageWindow(GameAssetsManager assetsManager,
						   SystemsCommonData systemsCommonData,
						   List<UserInterfaceSystemEventsSubscriber> subscribers) {
		GameWindow windowByName = getWindowByName(StorageWindow.NAME);
		if (windowByName == null) {
			createStorageWindow(assetsManager, systemsCommonData, subscribers);
		} else {
			windowByName.setVisible(true);
		}
	}

	private void defineStorageWindow(final StorageWindow window) {
		window.setName(StorageWindow.NAME);
		window.setSize(100, 100);
		window.pack();
		window.setPosition(
				getWidth() / 2 - window.getPrefWidth() / 2,
				getHeight() / 2 - window.getPrefHeight() / 2
		);
		window.initialize();
	}

	private GameWindow getWindowByName(final String name) {
		GameWindow result = null;
		for (GameWindow window : windows) {
			if (window.getName().equals(name)) {
				result = window;
				break;
			}
		}
		return result;
	}


	public void onItemAddedToStorage(Item item) {
		StorageWindow window = (StorageWindow) getWindowByName(StorageWindow.NAME);
		window.onItemAddedToStorage(item);
	}
}
