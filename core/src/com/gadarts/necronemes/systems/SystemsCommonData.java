package com.gadarts.necronemes.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.systems.character.CharacterCommand;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SystemsCommonData {

	@Setter(AccessLevel.NONE)
	private final String versionName;
	private final int versionNumber;
	private Entity cursor;
	private Camera camera;
	private MapGraph map;
	private Entity player;
	private int numberOfVisible;
	private Stage uiStage;
	private boolean cameraIsRotating;
	@Getter
	private CharacterCommand currentCommand;
	private Entity currentHighLightedPickup;
	private Entity itemToPickup;

	public SystemsCommonData(String versionName, int versionNumber) {
		this.versionName = versionName;
		this.versionNumber = versionNumber;
	}
}
