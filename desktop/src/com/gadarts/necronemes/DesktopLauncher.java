package com.gadarts.necronemes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import static com.gadarts.necronemes.Necronemes.*;

public class DesktopLauncher {
	public static void main(String[] arg) {

		Lwjgl3ApplicationConfiguration config = createGameConfig();
		config.setForegroundFPS(60);
		new Lwjgl3Application(new Necronemes(), config);
	}

	private static Lwjgl3ApplicationConfiguration createGameConfig( ) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		if (DefaultGameSettings.FULL_SCREEN) {
			config.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			config.setWindowedMode(WINDOWED_RESOLUTION_WIDTH, WINDOWED_RESOLUTION_HEIGHT);
		}
		config.setBackBufferConfig(8,8,8,8,16,0,4);
		config.setResizable(false);
		return config;
	}
}
