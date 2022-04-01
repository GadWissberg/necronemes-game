package com.gadarts.necronemes.systems.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Family;
import com.gadarts.necronemes.components.player.PlayerComponent;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;

public class PlayerSystem extends GameSystem<PlayerSystemEventsSubscriber> {
	public PlayerSystem(SystemsCommonData systemsCommonData) {
		super(systemsCommonData);
	}

	@Override
	public Class<PlayerSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return PlayerSystemEventsSubscriber.class;
	}

	@Override
	public void initializeData( ) {
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		getSystemsCommonData().setPlayer(getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).first());
	}

	@Override
	public void dispose( ) {

	}
}
