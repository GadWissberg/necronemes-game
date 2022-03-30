package com.gadarts.necronemes.systems.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.SystemsCommonData;
import lombok.Getter;

import static com.gadarts.necronemes.components.ComponentsMapper.modelInstance;

@Getter
public class CursorHandler implements Disposable {
	public static final Color CURSOR_REGULAR = Color.YELLOW;
	public static final Color CURSOR_UNAVAILABLE = Color.DARK_GRAY;
	public static final Color CURSOR_ATTACK = Color.RED;
	private static final float CURSOR_FLICKER_STEP = 1.5f;
	private static final Vector3 auxVector3_1 = new Vector3();
	public static final String POSITION_LABEL_FORMAT = "Row: %s , Col: %s";
	public static final Color POSITION_LABEL_COLOR = Color.WHITE;
	public static final float POSITION_LABEL_Y = 10F;
	private final SystemsCommonData systemsCommonData;
	private ModelInstance cursorModelInstance;
	private float cursorFlickerChange = CURSOR_FLICKER_STEP;

	public CursorHandler(SystemsCommonData systemsCommonData) {
		this.systemsCommonData = systemsCommonData;
	}

	@SuppressWarnings("SameParameterValue")
	private void setCursorColor(final Color color) {
		Material material = cursorModelInstance.materials.get(0);
		ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
		colorAttribute.color.set(color);
	}

	public void handleCursorFlicker(final float deltaTime) {
		Material mat = modelInstance.get(systemsCommonData.getCursor()).getModelInstance().materials.get(0);
		if (((ColorAttribute) mat.get(ColorAttribute.Diffuse)).color.equals(CURSOR_ATTACK)) {
			BlendingAttribute blend = (BlendingAttribute) mat.get(BlendingAttribute.Type);
			if (blend.opacity > 0.9) {
				cursorFlickerChange = -CURSOR_FLICKER_STEP;
			} else if (blend.opacity < 0.1) {
				cursorFlickerChange = CURSOR_FLICKER_STEP;
			}
			setCursorOpacity(blend.opacity + cursorFlickerChange * deltaTime);
		}
	}

	void colorizeCursor( ) {
		setCursorOpacity(1F);
		setCursorColor(CURSOR_REGULAR);
	}

	private void setCursorOpacity(final float opacity) {
		Material material = cursorModelInstance.materials.get(0);
		BlendingAttribute blend = (BlendingAttribute) material.get(BlendingAttribute.Type);
		blend.opacity = opacity;
		material.set(blend);
	}

	public void init( ) {
		cursorModelInstance = modelInstance.get(systemsCommonData.getCursor()).getModelInstance();
	}

	@Override
	public void dispose( ) {
	}

	public void onMouseEnteredNewNode(final MapGraphNode newNode) {
		int col = newNode.getCol();
		int row = newNode.getRow();
		getCursorModelInstance().transform.setTranslation(col + 0.5f, newNode.getHeight(), row + 0.5f);
		colorizeCursor();
	}
}
