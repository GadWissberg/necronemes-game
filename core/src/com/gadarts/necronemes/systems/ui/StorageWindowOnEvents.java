package com.gadarts.necronemes.systems.ui;

import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.systems.player.PlayerStorage;

enum StorageWindowOnEvents {

	ITEM_SELECTED(GameWindowEventType.ITEM_SELECTED, (event, soundPlayer, selectedItem, toBeAppliedOn, storage) -> {
		soundPlayer.playSound(Assets.Sounds.UI_ITEM_SELECT);
		ItemDisplay target = (ItemDisplay) event.getTarget();
		StorageWindow storageWindow = (StorageWindow) toBeAppliedOn;
		if (selectedItem.getSelection() != target) {
			storageWindow.applySelectedItem(target);
		} else {
			storageWindow.clearSelectedItem();
		}
		return false;
	}),

	ITEM_PLACED(GameWindowEventType.ITEM_PLACED, (event, soundPlayer, selectedItem, toBeAppliedOn, storage) -> {
		StorageWindow storageWindow = (StorageWindow) toBeAppliedOn;
		soundPlayer.playSound(Assets.Sounds.UI_ITEM_PLACED);
		if (event.getTarget() instanceof PlayerLayout) {
			storageWindow.findActor(StorageGrid.NAME).notify(event, false);
		} else {
			storageWindow.findActor(PlayerLayout.NAME).notify(event, false);
		}
		storageWindow.clearSelectedItem();
		return true;
	}),

	CLICK_RIGHT(GameWindowEventType.CLICK_RIGHT, (event, soundPlayer, selectedItem, toBeAppliedOn, storage) -> {
		StorageWindow storageWindow = (StorageWindow) toBeAppliedOn;
		return storageWindow.onRightClick();
	}),

	WINDOW_CLOSED(GameWindowEventType.WINDOW_CLOSED, (event, soundPlayer, selectedItem, toBeAppliedOn, storage) -> {
		StorageWindow storageWindow = (StorageWindow) toBeAppliedOn;
		if (event.getTarget() == storageWindow) {
			if (storageWindow.getPlayerLayout().getWeaponChoice() == null) {
				StorageGrid storageGrid = storageWindow.getStorageGrid();
				ItemDisplay itemDisplay = storageGrid.findItemDisplay(WeaponsDefinitions.KNIFE.getId());
				storageWindow.getPlayerLayout().applySelectionToSelectedWeapon(storageGrid, itemDisplay, storage, subscribers);
			}
		}
		return false;
	});

	private final GameWindowEventType type;
	private final OnEvent onEvent;

	StorageWindowOnEvents(final GameWindowEventType type, final OnEvent onEvent) {
		this.type = type;
		this.onEvent = onEvent;
	}

	public static boolean execute(GameWindowEvent event,
								  SoundPlayer soundPlayer,
								  ItemSelectionHandler selectedItem,
								  StorageWindow storageWindow, 
								  PlayerStorage storage) {
		StorageWindowOnEvents[] values = values();
		for (StorageWindowOnEvents e : values) {
			if (e.type == event.getType()) {
				return e.onEvent.execute(event, soundPlayer, selectedItem, storageWindow, storage);
			}
		}
		return false;
	}
}
