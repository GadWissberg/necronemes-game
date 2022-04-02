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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.List;

public class MapGraph implements IndexedGraph<MapGraphNode> {
	private static final Vector3 auxVector3 = new Vector3();
	private static final Array<Connection<MapGraphNode>> auxConnectionsList = new Array<>();
	private static final float PASSABLE_MAX_HEIGHT_DIFF = 0.3f;
	private final Dimension mapSize;
	@Getter
	private final Array<MapGraphNode> nodes;
	@Setter(AccessLevel.PACKAGE)
	@Getter(AccessLevel.PACKAGE)
	MapGraphNode currentDestination;
	@Setter
	private MapGraphConnectionCosts maxConnectionCostInSearch;
	@Setter
	private boolean includeEnemiesInGetConnections = true;

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
		return node.getIndex(mapSize);
	}

	@Override
	public int getNodeCount( ) {
		return nodes.size;
	}

	@Override
	public Array<Connection<MapGraphNode>> getConnections(MapGraphNode fromNode) {
		auxConnectionsList.clear();
		Array<MapGraphConnection> connections = fromNode.getConnections();
		for (Connection<MapGraphNode> connection : connections) {
			checkIfConnectionIsAvailable(connection);
		}
		return auxConnectionsList;
	}

	private void checkIfConnectionIsAvailable(final Connection<MapGraphNode> connection) {
		boolean validCost = connection.getCost() <= maxConnectionCostInSearch.getCostValue();
		if (validCost && checkIfConnectionPassable(connection)) {
			auxConnectionsList.add(connection);
		}
	}

	public MapGraphConnection findConnection(MapGraphNode node1, MapGraphNode node2) {
		if (node1 == null || node2 == null) return null;
		MapGraphConnection result = findConnectionBetweenTwoNodes(node1, node2);
		if (result == null) {
			result = findConnectionBetweenTwoNodes(node2, node1);
		}
		return result;
	}

	private MapGraphConnection findConnectionBetweenTwoNodes(MapGraphNode src, MapGraphNode dst) {
		Array<MapGraphConnection> connections = src.getConnections();
		for (MapGraphConnection connection : connections) {
			if (connection.getToNode() == dst) {
				return connection;
			}
		}
		return null;
	}

	private boolean checkIfConnectionPassable(final Connection<MapGraphNode> con) {
		MapGraphNode fromNode = con.getFromNode();
		MapGraphNode toNode = con.getToNode();
		boolean result = fromNode.getType() == MapNodesTypes.PASSABLE_NODE && toNode.getType() == MapNodesTypes.PASSABLE_NODE;
		result &= Math.abs(fromNode.getCol() - toNode.getCol()) < 2 && Math.abs(fromNode.getRow() - toNode.getRow()) < 2;
		if ((fromNode.getCol() != toNode.getCol()) && (fromNode.getRow() != toNode.getRow())) {
			result &= getNode(fromNode.getCol(), toNode.getRow()).getType() != MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN;
			result &= getNode(toNode.getCol(), fromNode.getRow()).getType() != MapNodesTypes.OBSTACLE_KEY_DIAGONAL_FORBIDDEN;
		}
		return result;
	}

	private void getThreeBehind(final MapGraphNode node, final List<MapGraphNode> output) {
		int x = node.getCol();
		int y = node.getRow();
		if (y > 0) {
			if (x > 0) {
				output.add(getNode(x - 1, y - 1));
			}
			output.add(getNode(x, y - 1));
			if (x < mapSize.width - 1) {
				output.add(getNode(x + 1, y - 1));
			}
		}
	}

	public List<MapGraphNode> getNodesAround(final MapGraphNode node, final List<MapGraphNode> output) {
		output.clear();
		getThreeBehind(node, output);
		getThreeInFront(node, output);
		if (node.getCol() > 0) {
			output.add(getNode(node.getCol() - 1, node.getRow()));
		}
		if (node.getCol() < mapSize.width - 1) {
			output.add(getNode(node.getCol() + 1, node.getRow()));
		}
		return output;
	}

	private void getThreeInFront(final MapGraphNode node, final List<MapGraphNode> output) {
		int x = node.getCol();
		int y = node.getRow();
		if (y < mapSize.height - 1) {
			if (x > 0) {
				output.add(getNode(x - 1, y + 1));
			}
			output.add(getNode(x, y + 1));
			if (x < mapSize.width - 1) {
				output.add(getNode(x + 1, y + 1));
			}
		}
	}

	private boolean isDiagonalBlockedWithEastOrWest(final MapGraphNode source, final int col) {
		float east = getNode(col, source.getRow()).getHeight();
		return Math.abs(source.getHeight() - east) > PASSABLE_MAX_HEIGHT_DIFF;
	}

	private boolean isDiagonalBlockedWithNorthAndSouth(final MapGraphNode target,
													   final int srcX,
													   final int srcY,
													   final float srcHeight) {
		if (srcY < target.getRow()) {
			float bottom = getNode(srcX, srcY + 1).getHeight();
			return Math.abs(srcHeight - bottom) > PASSABLE_MAX_HEIGHT_DIFF;
		} else {
			float top = getNode(srcX, srcY - 1).getHeight();
			return Math.abs(srcHeight - top) > PASSABLE_MAX_HEIGHT_DIFF;
		}
	}

	private boolean isDiagonalPossible(final MapGraphNode source, final MapGraphNode target) {
		if (source.getCol() == target.getCol() || source.getRow() == target.getRow()) return true;
		if (source.getCol() < target.getCol()) {
			if (isDiagonalBlockedWithEastOrWest(source, source.getCol() + 1)) {
				return false;
			}
		} else if (isDiagonalBlockedWithEastOrWest(source, source.getCol() - 1)) {
			return false;
		}
		return !isDiagonalBlockedWithNorthAndSouth(target, source.getCol(), source.getRow(), source.getHeight());
	}

	private void addConnection(final MapGraphNode source, final int xOffset, final int yOffset) {
		MapGraphNode target = getNode(source.getCol() + xOffset, source.getRow() + yOffset);
		if (target.getType() == MapNodesTypes.PASSABLE_NODE && isDiagonalPossible(source, target)) {
			MapGraphConnection connection;
			if (Math.abs(source.getHeight() - target.getHeight()) <= PASSABLE_MAX_HEIGHT_DIFF) {
				connection = new MapGraphConnection(source, target, MapGraphConnectionCosts.CLEAN);
			} else {
				connection = new MapGraphConnection(source, target, MapGraphConnectionCosts.HEIGHT_DIFF);
			}
			source.getConnections().add(connection);
		}
	}

	void applyConnections( ) {
		for (int row = 0; row < mapSize.height; row++) {
			int rows = row * mapSize.width;
			for (int col = 0; col < mapSize.width; col++) {
				MapGraphNode n = nodes.get(rows + col);
				if (col > 0) addConnection(n, -1, 0);
				if (col > 0 && row < mapSize.height - 1) addConnection(n, -1, 1);
				if (col > 0 && row > 0) addConnection(n, -1, -1);
				if (row > 0) addConnection(n, 0, -1);
				if (row > 0 && col < mapSize.width - 1) addConnection(n, 1, -1);
				if (col < mapSize.width - 1) addConnection(n, 1, 0);
				if (col < mapSize.width - 1 && row < mapSize.height - 1) addConnection(n, 1, 1);
				if (row < mapSize.height - 1) addConnection(n, 0, 1);
			}
		}
	}

	public void init( ) {
		applyConnections();
	}
}

