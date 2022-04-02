package com.gadarts.necronemes.systems.character;

import com.badlogic.ashley.core.Entity;
import com.gadarts.necronemes.map.MapGraphPath;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CharacterCommand {
	private CharacterCommands type;
	private Entity character;
	private Object additionalData;

	@Setter
	private boolean started;

	private final MapGraphPath path = new MapGraphPath();

	public CharacterCommand init(final CharacterCommands type,
								 final MapGraphPath path,
								 final Entity character) {
		return init(type, path, character, null);
	}

	public CharacterCommand init(final CharacterCommands type,
								 final MapGraphPath path,
								 final Entity character,
								 final Object additionalData) {
		this.type = type;
		this.path.set(path);
		this.character = character;
		this.additionalData = additionalData;
		return this;
	}
}
