package com.gadarts.necronemes.systems.turns;

import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.player.PlayerSystemEventsSubscriber;

public class TurnsSystem extends GameSystem<TurnsSystemEventsSubscriber> implements PlayerSystemEventsSubscriber {
	private boolean playerTurnDone;
	private Turns currentTurn;
	private boolean enemyTurnDone;
	private long currentTurnId;

	public TurnsSystem(SystemsCommonData systemsCommonData, SoundPlayer soundPlayer, GameAssetsManager assetsManager) {
		super(systemsCommonData, soundPlayer, assetsManager);
	}

	private void resetTurnFlags() {
		playerTurnDone = false;
		enemyTurnDone = false;
	}

	private void invokePlayerTurnDone() {
		resetTurnFlags();
		currentTurnId++;
		if (!DefaultGameSettings.PARALYZED_ENEMIES) {
			currentTurn = Turns.ENEMY;
			for (TurnsSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onEnemyTurn(currentTurnId);
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

	private void invokeEnemyTurnDone() {
		resetTurnFlags();
		currentTurnId++;
		currentTurn = Turns.PLAYER;
		for (TurnsSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onPlayerTurn(currentTurnId);
		}
	}

	@Override
	public Class<TurnsSystemEventsSubscriber> getEventsSubscriberClass() {
		return TurnsSystemEventsSubscriber.class;
	}

	@Override
	public void onPlayerFinishedTurn() {
		playerTurnDone = true;
	}

	@Override
	public void initializeData() {

	}

	@Override
	public void dispose() {

	}

}
