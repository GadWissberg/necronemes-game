package com.gadarts.necronemes.systems.camera;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.input.InputSystemEventsSubscriber;

import static com.gadarts.necronemes.DefaultGameSettings.FULL_SCREEN;
import static com.gadarts.necronemes.Necronemes.*;

public class CameraSystem extends GameSystem<CameraSystemEventsSubscriber> implements InputSystemEventsSubscriber {
	public static final int CAMERA_HEIGHT = 15;
	private static final float INITIAL_CAMERA_ANGLE_AROUND_Y = 80;
	private static final float FAR = 100f;
	private static final float NEAR = 0.01f;
	private static final float START_OFFSET = 7;
	private static final float EXTRA_LEVEL_PADDING = 16;
	private final Vector2 lastMousePosition = new Vector2();
	private final Vector2 lastRightPressMousePosition = new Vector2();
	private boolean rotateCamera;

	public CameraSystem(SystemsCommonData systemsCommonData) {
		super(systemsCommonData);
	}

	@Override
	public void mouseMoved(int screenX, int screenY) {
		lastMousePosition.set(screenX, screenY);
	}

	private void clampCameraPosition(final Vector3 pos) {
		MapGraph map = getSystemsCommonData().getMap();
		pos.x = MathUtils.clamp(pos.x, -EXTRA_LEVEL_PADDING, map.getWidth() + EXTRA_LEVEL_PADDING);
		pos.z = MathUtils.clamp(pos.z, -EXTRA_LEVEL_PADDING, map.getDepth() + EXTRA_LEVEL_PADDING);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		getSystemsCommonData().getCamera().update();
	}

	@Override
	public void touchDragged(final int screenX, final int screenY) {
		if (rotateCamera) {
			Entity player = getSystemsCommonData().getPlayer();
			Vector3 rotationPoint = ComponentsMapper.characterDecal.get(player).getDecal().getPosition();
			Camera camera = getSystemsCommonData().getCamera();
			camera.rotateAround(rotationPoint, Vector3.Y, (lastRightPressMousePosition.x - screenX) / 2f);
			clampCameraPosition(camera.position);
			lastRightPressMousePosition.set(screenX, screenY);
		}
	}

	@Override
	public void touchDown(final int screenX, final int screenY, final int button) {
		if (button == Input.Buttons.RIGHT) {
			rotateCamera = true;
			lastRightPressMousePosition.set(screenX, screenY);
		}
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

	@Override
	public void touchUp(final int screenX, final int screenY, final int button) {
		if (button == Input.Buttons.RIGHT) {
			rotateCamera = false;
		}
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
