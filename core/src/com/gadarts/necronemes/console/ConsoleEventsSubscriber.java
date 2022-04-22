package com.gadarts.necronemes.console;

import com.gadarts.necronemes.EventsSubscriber;
import com.gadarts.necronemes.console.commands.ConsoleCommandParameter;
import com.gadarts.necronemes.console.commands.ConsoleCommandResult;
import com.gadarts.necronemes.console.commands.ConsoleCommands;

public interface ConsoleEventsSubscriber extends EventsSubscriber {
	default void onConsoleActivated( ) {

	}

	default boolean onCommandRun(ConsoleCommands command, ConsoleCommandResult consoleCommandResult) {
		return false;
	}

	default boolean onCommandRun(ConsoleCommands command,
								 ConsoleCommandResult consoleCommandResult,
								 ConsoleCommandParameter parameter) {
		return false;
	}

	default void onConsoleDeactivated( ) {

	}

	default void onConsoleInitialized(Console console){

	}
}
