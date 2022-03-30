package com.gadarts.necronemes.systems.camera;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;

import static com.gadarts.necronemes.DefaultGameSettings.FULL_SCREEN;
import static com.gadarts.necronemes.Necronemes.*;

public class CameraSystem extends GameSystem<CameraSystemEventsSubscriber> {
	public static final int CAMERA_HEIGHT = 15;
	private static final float INITIAL_CAMERA_ANGLE_AROUND_Y = 80;
	private static final float FAR = 100f;
	private static final float NEAR = 0.01f;
	private static final float START_OFFSET = 7;

	public CameraSystem(SystemsCommonData systemsCommonData) {
		super(systemsCommonData);
	}

	@Override
	public Class<CameraSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return CameraSystemEventsSubscriber.class;
	}

	@Override
	public void initializeData( ) {
		int viewportWidth = (FULL_SCREEN ? FULL_SCREEN_RESOLUTION_WIDTH : WINDOWED_RESOLUTION_WIDTH) / 75;
		int viewportHeight = (FULL_SCREEN ? FULL_SCREEN_RESOLUTION_HEIGHT : WINDOWED_RESOLUTION_HEIGHT) / 75;
		OrthographicCamera cam = new OrthographicCamera(viewportWidth, viewportHeight);
		cam.near = NEAR;
		cam.far = FAR;
		cam.update();
		getSystemsCommonData().setCamera(cam);
		initCamera(cam);
	}

	private void initCamera(OrthographicCamera cam) {
//		Entity player = getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).get(0);
//		Vector2 nodePosition = ComponentsMapper.characterDecal.get(player).getNodePosition(auxVector2_1);
		cam.position.set(5 + START_OFFSET, CAMERA_HEIGHT, 0 + START_OFFSET);
		cam.direction.rotate(Vector3.X, -45);
		cam.direction.rotate(Vector3.Y, INITIAL_CAMERA_ANGLE_AROUND_Y);
		cam.update();
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
	}

	@Override
	public void dispose( ) {

	}
}
