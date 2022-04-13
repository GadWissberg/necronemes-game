package com.gadarts.necronemes.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class ShadowlessLightComponent implements GameComponent {

	@Getter(AccessLevel.NONE)
	private final Color color = new Color(Color.WHITE);
	@Getter(AccessLevel.NONE)
	private final Vector3 position = new Vector3();
	private float duration;
	private long beginTime;
	private Entity parent;
	private float intensity;
	private float radius;

	public Color getColor(Color output) {
		return output.set(color);
	}

	@Override
	public void reset( ) {

	}

	public void applyColor(final Color color) {
		this.color.set(color);
	}

	public void applyDuration(final float inSeconds) {
		if (inSeconds <= 0) return;
		this.duration = inSeconds;
		this.beginTime = TimeUtils.millis();
	}

	public void init(Vector3 position, float intensity, float radius, Entity parent) {
		this.position.set(position);
		this.intensity = intensity;
		this.radius = radius;
		this.parent = parent;
		color.set(Color.WHITE);
		duration = -1L;
	}

	public Vector3 getPosition(Vector3 output) {
		return output.set(position);
	}
}
