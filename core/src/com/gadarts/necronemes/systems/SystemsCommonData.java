package com.gadarts.necronemes.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.necronemes.systems.player.PlayerStorage;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.systems.character.CharacterCommand;
import com.gadarts.necronemes.systems.render.DrawFlags;
import com.gadarts.necronemes.systems.ui.GameStage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SystemsCommonData implements Disposable {
	public static final int CAMERA_LIGHT_FAR = 10;
	public static final String TABLE_NAME_HUD = "hud";

	@Setter(AccessLevel.NONE)
	private final String versionName;
	private final int versionNumber;
	private final PlayerStorage storage = new PlayerStorage();
	private DrawFlags drawFlags;
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
	private Table menuTable;

	public SystemsCommonData(String versionName, int versionNumber) {
		this.versionName = versionName;
		this.versionNumber = versionNumber;
	}

	@Override
	public void dispose( ) {
		uiStage.clear();
		uiStage.dispose();
	}
}
