package com.gadarts.necronemes.systems;

public interface EventsNotifier<T> {
	void subscribeForEvents(T sub);
}
