package com.gadarts.necronemes.systems;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.gadarts.necronemes.SoundPlayer;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
public abstract class GameSystem<T extends SystemEventsSubscriber> extends EntitySystem implements
		Disposable,
		EventsNotifier<T> {
	protected final List<T> subscribers = new ArrayList<>();
	private final SystemsCommonData systemsCommonData;
	private final SoundPlayer soundPlayer;

	public GameSystem(SystemsCommonData systemsCommonData, SoundPlayer soundPlayer) {
		this.systemsCommonData = systemsCommonData;
		this.soundPlayer = soundPlayer;
	}

	public abstract Class<T> getEventsSubscriberClass( );

	@Override
	public void subscribeForEvents(final T sub) {
		if (subscribers.contains(sub)) return;
		subscribers.add(sub);
	}

	public abstract void initializeData( );
}
