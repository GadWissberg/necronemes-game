package com.gadarts.necronemes.components;

import com.badlogic.ashley.core.ComponentMapper;
import com.gadarts.necronemes.components.mi.ModelInstanceComponent;

/**
 * Easy to access component mappers.
 */
public class ComponentsMapper {
	public static final ComponentMapper<ModelInstanceComponent> modelInstance = ComponentMapper.getFor(ModelInstanceComponent.class);
	public static final ComponentMapper<FloorComponent> floor = ComponentMapper.getFor(FloorComponent.class);
}

