package com.gadarts.necronemes;

import com.gadarts.necromine.model.pickups.WeaponsDefinitions;

import static com.badlogic.gdx.Application.LOG_DEBUG;

public final class DefaultGameSettings {
	public static final boolean SHOW_GL_PROFILING = true;
	public static final boolean DISPLAY_HUD_OUTLINES = false;
	public static final boolean MELODY_ENABLED = false;
	public static final boolean SFX_ENABLED = false;
	public static final boolean FULL_SCREEN = false;
	public static final boolean DEBUG_INPUT = false;
	public static final boolean DISABLE_FRUSTUM_CULLING = false;
	public static final int LOG_LEVEL = LOG_DEBUG;
	public static final WeaponsDefinitions STARTING_WEAPON = WeaponsDefinitions.KNIFE;
	public static final boolean PARALYZED_ENEMIES = false;

}
