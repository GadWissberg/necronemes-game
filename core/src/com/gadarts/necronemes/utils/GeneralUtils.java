package com.gadarts.necronemes.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.google.gson.JsonObject;

public class GeneralUtils {
	private static final Plane floorPlane = new Plane(new Vector3(0, 1, 0), 0);

	/**
	 * Calculates the node's position based on screen mouse position.
	 *
	 * @param camera  The rendering camera.
	 * @param screenX MouseX
	 * @param screenY MouseY
	 * @param output  The result
	 * @return output argument for chaining.
	 */
	public static Vector3 calculateGridPositionFromMouse(final Camera camera,
														 final float screenX,
														 final float screenY,
														 final Vector3 output) {
		Ray ray = camera.getPickRay(screenX, screenY);
		Intersector.intersectRayPlane(ray, floorPlane, output);
		return alignPositionToGrid(output);
	}

	/**
	 * Floors x and z.
	 *
	 * @param position Given position
	 * @return position argument for chaining.
	 */
	public static Vector3 alignPositionToGrid(final Vector3 position) {
		position.x = MathUtils.floor(position.x);
		position.y = 0;
		position.z = MathUtils.floor(position.z);
		return position;
	}

	public static float getFloatFromJsonOrDefault(final JsonObject jsonObject,
												  final String key,
												  final float defaultValue) {
		float result = defaultValue;
		if (jsonObject.has(key)) {
			result = jsonObject.get(key).getAsFloat();
		}
		return result;
	}
}
