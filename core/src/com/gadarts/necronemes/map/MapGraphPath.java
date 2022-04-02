package com.gadarts.necronemes.map;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;

public class MapGraphPath extends DefaultGraphPath<MapGraphNode> {

	public MapGraphNode getNextOf(final MapGraphNode destinationNode) {
		MapGraphNode result = null;
		int index = nodes.indexOf(destinationNode, false);
		if (index < nodes.size - 1) {
			result = nodes.get(index + 1);
		}
		return result;

	}

	public void set(MapGraphPath path) {
		nodes.clear();
		nodes.addAll(path.nodes);
	}
}
