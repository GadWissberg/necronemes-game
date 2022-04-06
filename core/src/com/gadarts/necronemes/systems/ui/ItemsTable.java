package com.gadarts.necronemes.systems.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public abstract class ItemsTable extends Table {
	protected final ItemSelectionHandler itemSelectionHandler;

	public ItemsTable(final ItemSelectionHandler itemSelectionHandler) {
		this.itemSelectionHandler = itemSelectionHandler;
	}

	protected void onRightClick() {
		fire(new GameWindowEvent(this, GameWindowEventType.CLICK_RIGHT));
	}

	public abstract void removeItem(ItemDisplay item);
}
