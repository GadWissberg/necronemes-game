package com.gadarts.necronemes.components.character;

import com.gadarts.necromine.model.characters.attributes.Accuracy;
import com.gadarts.necromine.model.characters.attributes.Agility;
import com.gadarts.necromine.model.characters.attributes.Strength;
import lombok.Getter;

@Getter
public class CharacterSkills {

	private final CharacterHealthData healthData = new CharacterHealthData();
	private Agility agility;
	private Strength strength;
	private Accuracy accuracy;

	public void applyParameters(final CharacterSkillsParameters skills) {
		this.healthData.init(skills.getHealth());
		this.agility = skills.getAgility();
		this.strength = skills.getStrength();
		this.accuracy = skills.getAccuracy();
	}
}
