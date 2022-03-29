package com.gadarts.necronemes.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public abstract class GameSystem extends EntitySystem implements Disposable {
	private final SystemsCommonData systemsCommonData;

	public GameSystem(SystemsCommonData systemsCommonData) {
		this.systemsCommonData = systemsCommonData;
	}

	public abstract void initializeData( );
}
