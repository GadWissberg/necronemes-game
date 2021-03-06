package com.gadarts.necronemes.systems.render;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.gadarts.necronemes.systems.SystemEventsSubscriber;

public interface RenderSystemEventsSubscriber extends SystemEventsSubscriber {
	default void onFrameChanged(Entity entity, float deltaTime, TextureAtlas.AtlasRegion newFrame){}
}
