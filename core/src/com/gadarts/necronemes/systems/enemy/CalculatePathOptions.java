package com.gadarts.necronemes.systems.enemy;

import com.gadarts.necronemes.map.MapGraphConnectionCosts;
import lombok.Getter;

@Getter
public class CalculatePathOptions {
	private boolean avoidCharactersInCalculations;
	private MapGraphConnectionCosts maxCostInclusive;

	public CalculatePathOptions init(boolean avoidCharactersInCalculations) {
		return init(avoidCharactersInCalculations, MapGraphConnectionCosts.CLEAN);
	}

	public CalculatePathOptions init(boolean avoidCharactersInCalculations, MapGraphConnectionCosts maxCostInclusive) {
		this.avoidCharactersInCalculations = avoidCharactersInCalculations;
		this.maxCostInclusive = maxCostInclusive;
		return this;
	}
}
