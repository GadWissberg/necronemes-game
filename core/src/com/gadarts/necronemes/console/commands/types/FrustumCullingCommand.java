package com.gadarts.necronemes.console.commands.types;

import com.gadarts.necronemes.console.commands.ConsoleCommand;
import com.gadarts.necronemes.console.commands.ConsoleCommandsList;

public class FrustumCullingCommand extends ConsoleCommand {
	@Override
	protected ConsoleCommandsList getCommandEnumValue( ) {
		return ConsoleCommandsList.FRUSTUM_CULLING;
	}
}
