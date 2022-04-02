package com.gadarts.necronemes.utils;

import com.gadarts.necronemes.map.MapGraphConnectionCosts;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.map.MapGraphPath;
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
