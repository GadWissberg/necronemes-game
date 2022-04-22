package com.gadarts.necronemes.console.commands;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsoleCommandResult {
	private String message;
	private boolean result;

	public void clear() {
		message = null;
	}

}
