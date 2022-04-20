package com.gadarts.necronemes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.gadarts.necronemes.screens.BattleScreen;

public class Necronemes extends Game {
	public static final String TITLE = "necronemes";

	public static final int FULL_SCREEN_RESOLUTION_WIDTH = 1920;
	public static final int FULL_SCREEN_RESOLUTION_HEIGHT = 1080;
	public static final int WINDOWED_RESOLUTION_WIDTH = 800;
	public static final int WINDOWED_RESOLUTION_HEIGHT = 600;
	private final String versionName;
	private final int versionNumber;
	private final GeneralHandler generalHandler;
	
	public Necronemes(String versionName, int versionNumber) {
		this.versionName = versionName;
		this.versionNumber = versionNumber;
		this.generalHandler = new GeneralHandler();
	}

	@Override
	public void create( ) {
		Gdx.app.setLogLevel(DefaultGameSettings.LOG_LEVEL);
		generalHandler.init(versionName, versionNumber);
		generalHandler.startNewGame("mastaba");
		setScreen(new BattleScreen(generalHandler));
	}

	

	
	


	

	

	

	

	@Override
	public void dispose( ) {
		generalHandler.dispose();
	}
}
