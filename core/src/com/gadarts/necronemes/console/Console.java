package com.gadarts.necronemes.console;

import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.console.commands.ConsoleCommandParameter;
import com.gadarts.necronemes.console.commands.ConsoleCommandResult;
import com.gadarts.necronemes.console.commands.ConsoleCommands;
import com.gadarts.necronemes.systems.EventsNotifier;
import com.gadarts.necronemes.systems.SystemsCommonData;

public interface Console extends EventsNotifier<ConsoleEventsSubscriber> {
	String TEXT_VIEW_NAME = "text";

	String ERROR_COLOR = "[RED]";
	String INPUT_FIELD_NAME = "input";
	String OUTPUT_COLOR = "[LIGHT_GRAY]";
	String INPUT_COLOR = "[YELLOW]";

	void insertNewLog(String text, boolean logTime);

	void insertNewLog(String text, boolean logTime, String color);

	ConsoleCommandResult notifyCommandExecution(ConsoleCommands command);

	ConsoleCommandResult notifyCommandExecution(ConsoleCommands command, ConsoleCommandParameter parameter);

	void activate( );

	void deactivate( );

	boolean isActive( );

	void init(GameAssetsManager assetsManager, SystemsCommonData systemsCommonData);

	void dispose( );
}
