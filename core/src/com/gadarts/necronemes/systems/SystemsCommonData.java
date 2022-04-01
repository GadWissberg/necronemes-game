package com.gadarts.necronemes.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Camera;
import com.gadarts.necronemes.map.MapGraph;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SystemsCommonData {
	private Entity cursor;
	private Camera camera;
	private MapGraph map;
	private Entity player;
}
