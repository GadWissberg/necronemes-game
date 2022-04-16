package com.gadarts.necronemes.components;

import com.badlogic.gdx.math.Vector3;
import com.gadarts.necronemes.systems.render.GameFrameBufferCubeMap;
import lombok.Getter;
import lombok.Setter;

@Getter
public class StaticLightComponent extends LightComponent {

	@Setter
	private GameFrameBufferCubeMap shadowFrameBuffer;

	@Override
	public void init(Vector3 position, float intensity, float radius) {
		super.init(position, intensity, radius);
	}

	@Override
	public void reset( ) {

	}
}
