package com.gadarts.necronemes.systems.camera;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.GeneralUtils;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.input.InputSystemEventsSubscriber;

import static com.gadarts.necronemes.DefaultGameSettings.DEBUG_INPUT;
import static com.gadarts.necronemes.DefaultGameSettings.FULL_SCREEN;
import static com.gadarts.necronemes.Necronemes.*;

public class CameraSystem extends GameSystem<CameraSystemEventsSubscriber> implements InputSystemEventsSubscriber {
	public static final int CAMERA_HEIGHT = 15;
	private static final float INITIAL_CAMERA_ANGLE_AROUND_Y = 80;
	private static final float FAR = 100f;
	private static final float NEAR = 0.01f;
	private static final float START_OFFSET = 7;
	private static final float EXTRA_LEVEL_PADDING = 16;
	private static final Vector3 auxVector3_1 = new Vector3();
	private static final Vector3 auxVector3_2 = new Vector3();
	private static final Vector3 auxVector3_3 = new Vector3();
	private final Vector2 lastMousePosition = new Vector2();
	private final Vector2 lastRightPressMousePosition = new Vector2();

	public CameraSystem(SystemsCommonData systemsCommonData, SoundPlayer soundPlayer, GameAssetsManager assetsManager) {
		super(systemsCommonData, soundPlayer, assetsManager);
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
		if (!DEBUG_INPUT && !getSystemsCommonData().getUiStage().hasOpenWindows() && !getSystemsCommonData().isCameraIsRotating()) {
			handleCameraFollow();
		}
		getSystemsCommonData().getCamera().update();
	}

	private void handleCameraFollow( ) {
		Entity player = getSystemsCommonData().getPlayer();
		Vector3 playerPos = ComponentsMapper.characterDecal.get(player).getDecal().getPosition();
		Camera camera = getSystemsCommonData().getCamera();
		Vector3 rotationPoint = GeneralUtils.defineRotationPoint(auxVector3_1, camera);
		Vector3 diff = auxVector3_2.set(playerPos).sub(rotationPoint);
		Vector3 cameraPosDest = auxVector3_3.set(camera.position).add(diff.x, 0, diff.z);
		camera.position.interpolate(cameraPosDest, 0.1F, Interpolation.bounce);
	}

	@Override
	public void touchDragged(final int screenX, final int screenY) {
		if (getSystemsCommonData().isCameraIsRotating()) {
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
			getSystemsCommonData().setCameraIsRotating(true);
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
			getSystemsCommonData().setCameraIsRotating(false);
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
