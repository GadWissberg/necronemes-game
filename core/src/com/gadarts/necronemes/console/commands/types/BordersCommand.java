package com.gadarts.necronemes.console.commands.types;

import com.gadarts.necronemes.console.commands.ConsoleCommand;
import com.gadarts.necronemes.console.commands.ConsoleCommandsList;

public class BordersCommand extends ConsoleCommand {
	@Override
	protected ConsoleCommandsList getCommandEnumValue() {
		return ConsoleCommandsList.BORDERS;
	}
}
