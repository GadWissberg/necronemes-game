package com.gadarts.necronemes.map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalculatePathRequest {
	private MapGraphNode sourceNode;
	private MapGraphNode destNode;
	private boolean avoidCharactersInCalculations;
	private MapGraphConnectionCosts maxCostInclusive;
	private MapGraphPath outputPath;


}
