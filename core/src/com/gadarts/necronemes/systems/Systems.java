package com.gadarts.necronemes.systems;

import com.gadarts.necronemes.systems.camera.CameraSystem;
import com.gadarts.necronemes.systems.character.CharacterSystem;
import com.gadarts.necronemes.systems.enemy.EnemySystem;
import com.gadarts.necronemes.systems.input.InputSystem;
import com.gadarts.necronemes.systems.player.PlayerSystem;
import com.gadarts.necronemes.systems.projectiles.ProjectilesSystem;
import com.gadarts.necronemes.systems.render.RenderSystem;
import com.gadarts.necronemes.systems.turns.TurnsSystem;
import com.gadarts.necronemes.systems.ui.UserInterfaceSystem;
import lombok.Getter;

@Getter
public enum Systems {
	CAMERA(CameraSystem.class),
	INPUT(InputSystem.class),
	PLAYER(PlayerSystem.class),
	RENDER(RenderSystem.class),
	USER_INTERFACE(UserInterfaceSystem.class),
	PROFILING(ProfilingSystem.class),
	PICKUP(PickupSystem.class),
	TURNS(TurnsSystem.class),
	ENEMY(EnemySystem.class),
	PROJECTILE(ProjectilesSystem.class),
	CHARACTER(CharacterSystem.class);

	private final Class<? extends GameSystem<? extends SystemEventsSubscriber>> systemClass;

	Systems(Class<? extends GameSystem<? extends SystemEventsSubscriber>> systemClass) {
		this.systemClass = systemClass;
	}
}
