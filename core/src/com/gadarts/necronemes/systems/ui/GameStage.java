package com.gadarts.necronemes.systems.ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.player.PlayerComponent;

import java.util.ArrayList;
import java.util.List;

public class GameStage extends Stage {
	public static final int GRID_SIZE = 256;
	public static final int GRID_CELL_SIZE = 32;
	private final PlayerComponent playerComponent;
	private final SoundPlayer soundPlayer;

	public GameStage(final FitViewport fitViewport, final PlayerComponent playerComponent, final SoundPlayer soundPlayer) {
		super(fitViewport);
		this.playerComponent = playerComponent;
		this.soundPlayer = soundPlayer;
	}















}
