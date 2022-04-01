package com.gadarts.necronemes.components.character;

import com.gadarts.necromine.model.characters.attributes.Accuracy;
import com.gadarts.necromine.model.characters.attributes.Agility;
import com.gadarts.necromine.model.characters.attributes.Strength;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CharacterSkillsParameters {
	private final int health;
	private final Agility agility;
	private final Strength strength;
	private final Accuracy accuracy;

}
