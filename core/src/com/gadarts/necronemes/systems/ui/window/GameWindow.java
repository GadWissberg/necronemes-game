package com.gadarts.necronemes.systems.ui.window;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;

public class GameWindow extends Window {

	protected final Button closeButton;

	public GameWindow(final String windowNameStorage,
					  final WindowStyle windowStyle,
					  final GameAssetsManager assetsManager) {
		super(windowNameStorage, windowStyle);
		Button.ButtonStyle style = createButtonStyle(assetsManager);
		closeButton = new Button(style);
		add(closeButton).colspan(2).expand().top().right().row();
		closeButton.addListener(new ClickListener() {
			@Override
			public void clicked(final InputEvent event, final float x, final float y) {
				if (closeButton.isDisabled()) return;
				super.clicked(event, x, y);
				GameWindow.this.fire(new GameWindowEvent(GameWindow.this, GameWindowEventType.WINDOW_CLOSED));
			}
		});
	}

	private Button.ButtonStyle createButtonStyle(GameAssetsManager assetsManager) {
		Button.ButtonStyle style = new Button.ButtonStyle();
		style.up = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_CLOSE));
		style.down = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_CLOSE_DOWN));
		style.over = new TextureRegionDrawable(assetsManager.getTexture(Assets.UiTextures.BUTTON_CLOSE_HOVER));
		return style;
	}

}
