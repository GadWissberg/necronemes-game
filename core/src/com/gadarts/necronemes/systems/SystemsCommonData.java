package com.gadarts.necronemes.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.gadarts.necronemes.systems.player.PlayerStorage;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.systems.character.CharacterCommand;
import com.gadarts.necronemes.systems.ui.GameStage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SystemsCommonData {

	@Setter(AccessLevel.NONE)
	private final String versionName;
	private final int versionNumber;
	private final PlayerStorage storage = new PlayerStorage();
	private ParticleSystem particleSystem;
	private Entity cursor;
	private Camera camera;
	private MapGraph map;
	private Entity player;
	private int numberOfVisible;
	private GameStage uiStage;
	private boolean cameraIsRotating;
	@Getter
	private CharacterCommand currentCommand;
	private Entity currentHighLightedPickup;
	private Entity itemToPickup;
	private long currentTurnId;

	public SystemsCommonData(String versionName, int versionNumber) {
		this.versionName = versionName;
		this.versionNumber = versionNumber;
	}
}
