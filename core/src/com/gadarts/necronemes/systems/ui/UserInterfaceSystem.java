package com.gadarts.necronemes.systems.ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.input.InputSystemEventsSubscriber;
import com.gadarts.necronemes.utils.EntityBuilder;

import static com.badlogic.gdx.Application.LOG_DEBUG;
import static com.gadarts.necronemes.DefaultGameSettings.FULL_SCREEN;
import static com.gadarts.necronemes.Necronemes.FULL_SCREEN_RESOLUTION_HEIGHT;
import static com.gadarts.necronemes.Necronemes.FULL_SCREEN_RESOLUTION_WIDTH;
import static com.gadarts.necronemes.Necronemes.WINDOWED_RESOLUTION_HEIGHT;
import static com.gadarts.necronemes.Necronemes.WINDOWED_RESOLUTION_WIDTH;

public class UserInterfaceSystem extends GameSystem<UserInterfaceSystemEventsSubscriber> implements InputSystemEventsSubscriber {
	static final String TABLE_NAME_HUD = "hud";
	private static final BoundingBox auxBoundingBox = new BoundingBox();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final String BUTTON_NAME_STORAGE = "button_storage";
	private static final float BUTTON_PADDING = 40;
	private final SoundPlayer soundPlayer;
	private final boolean showBorders = DefaultGameSettings.DISPLAY_HUD_OUTLINES;
	private CursorHandler cursorHandler;

	public UserInterfaceSystem(SystemsCommonData systemsCommonData,
							   SoundPlayer soundPlayer,
							   GameAssetsManager assetsManager) {
		super(systemsCommonData, soundPlayer, assetsManager);
		this.soundPlayer = soundPlayer;
		createUiStage();
		Table hudTable = addTable();
		hudTable.setName(TABLE_NAME_HUD);
		addStorageButton(hudTable);
	}

	private void addStorageButton(final Table table) {
		Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
		GameAssetsManager assetsManager = getAssetsManager();
		Button button = createButtonStyle(buttonStyle, assetsManager);
		button.setName(BUTTON_NAME_STORAGE);
		button.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				super.clicked(event, x, y);
				SystemsCommonData commonData = getSystemsCommonData();
				commonData.getUiStage().openStorageWindow(assetsManager, commonData, subscribers);
				getSoundPlayer().playSound(Assets.Sounds.UI_CLICK);
			}
		});
		table.add(button).expand().left().bottom().pad(BUTTON_PADDING);
	}

	private Button createButtonStyle(Button.ButtonStyle buttonStyle, GameAssetsManager assetsManager) {
		buttonStyle.up = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE));
		buttonStyle.down = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE_DOWN));
		buttonStyle.over = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_STORAGE_HOVER));
		Button button = new Button(buttonStyle);
		return button;
	}

	@SuppressWarnings("ConstantConditions")
	private Table addTable() {
		Table table = new Table();
		Stage stage = getSystemsCommonData().getUiStage();
		stage.setDebugAll(Gdx.app.getLogLevel() == LOG_DEBUG && showBorders);
		table.setFillParent(true);
		stage.addActor(table);
		return table;
	}

	private void createUiStage() {
		int width = FULL_SCREEN ? FULL_SCREEN_RESOLUTION_WIDTH : WINDOWED_RESOLUTION_WIDTH;
		int height = FULL_SCREEN ? FULL_SCREEN_RESOLUTION_HEIGHT : WINDOWED_RESOLUTION_HEIGHT;
		FitViewport fitViewport = new FitViewport(width, height);
		GameStage stage = new GameStage(fitViewport, soundPlayer);
		getSystemsCommonData().setUiStage(stage);
		stage.setDebugAll(DefaultGameSettings.DISPLAY_HUD_OUTLINES);
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {
		MapGraph map = getSystemsCommonData().getMap();
		MapGraphNode newNode = map.getRayNode(screenX, screenY, getSystemsCommonData().getCamera());
		ModelInstance cursorModelInstance = cursorHandler.getCursorModelInstance();
		MapGraphNode oldNode = map.getNode(cursorModelInstance.transform.getTranslation(auxVector3_2));
		if (newNode != null && !newNode.equals(oldNode)) {
			cursorHandler.onMouseEnteredNewNode(newNode);
		}
	}


	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		getSystemsCommonData().getUiStage().act();
		cursorHandler.handleCursorFlicker(deltaTime);
	}

	@Override
	public Class<UserInterfaceSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return UserInterfaceSystemEventsSubscriber.class;
	}

	@Override
	public void initializeData( ) {
		getSystemsCommonData().setCursor(createAndAdd3dCursor());
		cursorHandler = new CursorHandler(getSystemsCommonData());
		cursorHandler.init();
	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (getSystemsCommonData().isCameraIsRotating() || getSystemsCommonData().getUiStage().hasOpenWindows()) return;
		if (button == Input.Buttons.LEFT && getSystemsCommonData().getCurrentCommand() == null) {
			onUserSelectedNodeToApplyTurn();
		}
	}

	private void onUserSelectedNodeToApplyTurn( ) {
		MapGraphNode cursorNode = cursorHandler.getCursorNode();
		for (UserInterfaceSystemEventsSubscriber sub : subscribers) {
			sub.onUserSelectedNodeToApplyTurn(cursorNode);
		}
	}

	private Entity createAndAdd3dCursor( ) {
		Model model = getAssetsManager().getModel(Assets.Models.CURSOR);
		model.calculateBoundingBox(auxBoundingBox);
		return EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addModelInstanceComponent(new GameModelInstance(model, auxBoundingBox, false), true, false)
				.finishAndAddToEngine();
	}

	@Override
	public void dispose( ) {
		cursorHandler.dispose();
	}

}
