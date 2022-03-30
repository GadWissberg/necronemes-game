package com.gadarts.necronemes.map;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.gadarts.necromine.model.map.MapNodesTypes;
import com.gadarts.necronemes.utils.GeneralUtils;
import lombok.Getter;

import java.awt.*;

public class MapGraph implements IndexedGraph<MapGraphNode> {

	private static final Vector3 auxVector3 = new Vector3();
	private final Dimension mapSize;
	@Getter
	private final Array<MapGraphNode> nodes;

	public MapGraph(float ambient, Dimension mapSize, PooledEngine engine) {
		this.mapSize = mapSize;
		this.nodes = new Array<>(mapSize.width * mapSize.height);
		for (int row = 0; row < mapSize.height; row++) {
			for (int col = 0; col < mapSize.width; col++) {
				nodes.add(new MapGraphNode(col, row, MapNodesTypes.values()[MapNodesTypes.PASSABLE_NODE.ordinal()], 8));
			}
		}
	}

	public MapGraphNode getRayNode(final int screenX, final int screenY, final Camera camera) {
		Vector3 output = GeneralUtils.calculateGridPositionFromMouse(camera, screenX, screenY, auxVector3);
		output.set(Math.max(output.x, 0), Math.max(output.y, 0), Math.max(output.z, 0));
		return getNode(output);
	}

	public MapGraphNode getNode(final Vector3 position) {
		return getNode((int) position.x, (int) position.z);
	}

	public MapGraphNode getNode(final int col, final int row) {
		int index = Math.max(Math.min(row, mapSize.height) * mapSize.width + Math.min(col, mapSize.width), 0);
		MapGraphNode result = null;
		if (index < getWidth() * getDepth()) {
			result = nodes.get(index);
		}
		return result;
	}

	public int getDepth( ) {
		return mapSize.height;
	}

	public int getWidth( ) {
		return mapSize.width;
	}

	public MapGraphNode getNode(final Vector2 position) {
		return getNode((int) position.x, (int) position.y);
	}

	@Override
	public int getIndex(MapGraphNode node) {
		return 0;
	}

	@Override
	public int getNodeCount( ) {
		return 0;
	}

	@Override
	public Array<Connection<MapGraphNode>> getConnections(MapGraphNode fromNode) {
		return null;
	}
}

