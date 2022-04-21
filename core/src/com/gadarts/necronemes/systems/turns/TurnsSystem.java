package com.gadarts.necronemes.systems.turns;

import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.GameLifeCycleHandler;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.enemy.EnemySystemEventsSubscriber;
import com.gadarts.necronemes.systems.player.PlayerSystemEventsSubscriber;

public class TurnsSystem extends GameSystem<TurnsSystemEventsSubscriber> implements
		PlayerSystemEventsSubscriber,
		EnemySystemEventsSubscriber {
	private boolean playerTurnDone;
	private Turns currentTurn;
	private boolean enemyTurnDone;

	public TurnsSystem(SystemsCommonData systemsCommonData,
					   SoundPlayer soundPlayer,
					   GameAssetsManager assetsManager,
					   GameLifeCycleHandler lifeCycleHandler) {
		super(systemsCommonData, soundPlayer, assetsManager, lifeCycleHandler);
		currentTurn = Turns.PLAYER;
	}

	@Override
	public void onEnemyFinishedTurn( ) {
		enemyTurnDone = true;
	}

	private void resetTurnFlags( ) {
		playerTurnDone = false;
		enemyTurnDone = false;
	}

	private void invokePlayerTurnDone( ) {
		resetTurnFlags();
		SystemsCommonData systemsCommonData = getSystemsCommonData();
		systemsCommonData.setCurrentTurnId(systemsCommonData.getCurrentTurnId() + 1);
		if (!DefaultGameSettings.PARALYZED_ENEMIES) {
			currentTurn = Turns.ENEMY;
			for (TurnsSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onEnemyTurn(systemsCommonData.getCurrentTurnId());
			}
		}
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		if (currentTurn == Turns.PLAYER && playerTurnDone) {
			invokePlayerTurnDone();
		} else if (currentTurn == Turns.ENEMY && enemyTurnDone) {
			invokeEnemyTurnDone();
		}
	}

	private void invokeEnemyTurnDone( ) {
		resetTurnFlags();
		SystemsCommonData systemsCommonData = getSystemsCommonData();
		systemsCommonData.setCurrentTurnId(systemsCommonData.getCurrentTurnId() + 1);
		currentTurn = Turns.PLAYER;
		for (TurnsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onPlayerTurn(getSystemsCommonData().getCurrentTurnId());
		}
	}

	@Override
	public Class<TurnsSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return TurnsSystemEventsSubscriber.class;
	}

	@Override
	public void onPlayerFinishedTurn( ) {
		playerTurnDone = true;
	}

	@Override
	public void initializeData( ) {

	}

	@Override
	public void dispose( ) {

	}

}
