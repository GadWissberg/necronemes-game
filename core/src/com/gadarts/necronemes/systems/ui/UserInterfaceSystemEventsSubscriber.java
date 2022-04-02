package com.gadarts.necronemes.systems.ui;

import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;

public interface UserInterfaceSystemEventsSubscriber extends SystemEventsSubscriber {


	default void onUserSelectedNodeToApplyTurn(final MapGraphNode node) {

	}

}
