package com.gadarts.necronemes.map;

import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.gadarts.necronemes.utils.CalculatePathRequest;

public class GamePathFinder extends IndexedAStarPathFinder<MapGraphNode> {
	private static final CalculatePathRequest CALCULATE_PATH_REQUEST = new CalculatePathRequest();
	private final MapGraph map;

	public GamePathFinder(final MapGraph graph) {
		super(graph);
		this.map = graph;
	}

	public boolean searchNodePathBeforeCommand(final GameHeuristic heuristic,
											   final CalculatePathRequest req) {
		MapGraphNode oldDest = map.getCurrentDestination();
		map.setIncludeEnemiesInGetConnections(req.isAvoidCharactersInCalculations());
		map.setCurrentDestination(req.getDestNode());
		map.setMaxConnectionCostInSearch(req.getMaxCostInclusive());
		boolean result = searchNodePath(req.getSourceNode(), req.getDestNode(), heuristic, req.getOutputPath());
		map.setMaxConnectionCostInSearch(MapGraphConnectionCosts.CLEAN);
		map.setCurrentDestination(oldDest);
		map.setIncludeEnemiesInGetConnections(true);
		return result;
	}
}
