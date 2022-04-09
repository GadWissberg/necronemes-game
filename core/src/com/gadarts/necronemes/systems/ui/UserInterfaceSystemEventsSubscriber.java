package com.gadarts.necronemes.systems.ui;

import com.gadarts.necronemes.components.player.Item;
import com.gadarts.necronemes.components.player.Weapon;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;

public interface UserInterfaceSystemEventsSubscriber extends SystemEventsSubscriber {

	default void itemAddedToStorage(Item item) {

	}

	default void onSelectedWeaponChanged(Weapon selectedWeapon) {

	}

	default void onUserSelectedNodeToApplyTurn(final MapGraphNode node, AttackNodesHandler attackNodesHandler) {

	}

	default void onUserAppliedSelectionToSelectedWeapon(Weapon weapon){
		
	}
}
