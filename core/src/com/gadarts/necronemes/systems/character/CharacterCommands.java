package com.gadarts.necronemes.systems.character;

import com.gadarts.necronemes.systems.character.actions.ToDoAfterDestinationReached;
import lombok.Getter;

public enum CharacterCommands {
	GO_TO;

	private final ToDoAfterDestinationReached toDoAfterDestinationReached;

	@Getter
	private final boolean requiresMovement;

	CharacterCommands( ) {
		this(null);
	}

	CharacterCommands(final ToDoAfterDestinationReached toDoAfterDestinationReached) {
		this(toDoAfterDestinationReached, true);
	}

	CharacterCommands(final ToDoAfterDestinationReached toDoAfterDestinationReached, final boolean requiresMovement) {
		this.toDoAfterDestinationReached = toDoAfterDestinationReached;
		this.requiresMovement = requiresMovement;
	}

	public ToDoAfterDestinationReached getToDoAfterDestinationReached( ) {
		return toDoAfterDestinationReached;
	}
}
