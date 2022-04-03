package com.gadarts.necronemes.components.mi;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.gadarts.necromine.assets.Assets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ModelBoundingBox extends BoundingBox {
	private final Assets.Models modelDefinition;
}
