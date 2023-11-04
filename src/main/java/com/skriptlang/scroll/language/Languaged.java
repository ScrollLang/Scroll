package com.skriptlang.scroll.language;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.skriptlang.scroll.Scroll;

import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptEventInfo;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.text.Text;

/**
 * Utility class to short cut and help out interating with the Language file.
 */
public interface Languaged {

	record LangNode(String node, Object... arguments) {}

	default LangNode node(String node, Object... arguments) {
		return new LangNode(node, arguments);
	}

	default String languageFormat(LangNode... langs) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < langs.length; i++) {
			LangNode lang = langs[i];
			builder.append(languageFormat(lang.node(), lang.arguments()));
			if (i + 1 < langs.length)
				builder.append(" ");
		}
		return builder.toString();
	}

	/**
	 * Quick shortcut for access the language properties file.
	 * This method also allows for usage of {@link String#format(String, Object...)}.
	 * 
	 * @param key The key from the language properties file.
	 * @param arguments The arguments to be casted through the {@link String#format(String, Object...)}.
	 * @return The found String value. Otherwise will default to English.
	 */
	@Nullable
	default String languageFormat(String key, Object... arguments) {
		return Scroll.languageFormat(key, arguments);
	}

	default Text adventure(LangNode node) {
		MiniMessage miniMessage = MiniMessage.miniMessage();
		FabricAudiences adventure = Scroll.getAdventure();
		String string = languageFormat(node);
		if (string == null)
			return Text.literal(node.node());
		return adventure.toNative(miniMessage.deserialize(languageFormat(node)));
	}

	/**
	 * Quick shortcut for access the language properties file.
	 * 
	 * @param key The key from the language properties file.
	 * @return The found String value. Otherwise will default to English.
	 */
	@NotNull
	default String language(String key) {
		return Scroll.language(key);
	}

	default void error(ParseContext context, LangNode... langs) {
		error(context, languageFormat(langs));
	}

	/**
	 * Allows to insert logging errors during parse time of Scroll scripts.
	 * Use this method for script parsing related errors.
	 * See {@link #LOGGER} for other errors and info messages.
	 * The ErrorType will be set to {@link ErrorType.SEMANTIC_ERROR}
	 * 
	 * @param context The {@link ParseContext} of the init method where this error is happening.
	 * @param message The message to print.
	 */
	default void error(ParseContext context, String message) {
		error(context, message, ErrorType.SEMANTIC_ERROR, null);
	}

	/**
	 * Allows to insert logging errors during parse time of Scroll scripts.
	 * Use this method for script parsing related errors.
	 * See {@link #LOGGER} for other errors and info messages.
	 * The ErrorType will be set to {@link ErrorType.SEMANTIC_ERROR}
	 * 
	 * @param context The {@link ParseContext} of the init method where this error is happening.
	 * @param message The message to print.
	 * @param tip A hint to provide to the Scroll user for the error.
	 */
	default void error(ParseContext context, String message, @Nullable String tip) {
		error(context, message, ErrorType.SEMANTIC_ERROR, tip);
	}

	/**
	 * Allows to insert logging errors during parse time of Scroll scripts.
	 * Use this method for script parsing related errors.
	 * See {@link #LOGGER} for other errors and info messages.
	 * 
	 * @param context The {@link ParseContext} of the init method where this error is happening.
	 * @param message The message to print.
	 * @param type The error type this will provide to the SkriptLogger.
	 * @param tip A hint to provide to the Scroll user for the error.
	 */
	default void error(ParseContext context, String message, ErrorType type, @Nullable String tip) {
		// May need ParseContext in the future. It's forced to future proof this node.
		Scroll.error(message, type, tip);
	}

	default void warning(ParseContext context, LangNode... langs) {
		warning(context, languageFormat(langs), null);
	}

	default void warning(ParseContext context, String message, @Nullable String tip) {
		context.getLogger().warn(message, tip);
	}

	default void info(LangNode... langs) {
		info(languageFormat(langs));
	}

	/**
	 * Allows to insert logging information during parse time of Scroll scripts.
	 * Use this method for script parsing related errors.
	 * See {@link #LOGGER} for other errors and info messages.
	 * 
	 * @param message The message to print.
	 */
	default void info(String message) {
		Scroll.info(message);
	}

	/**
	 * Compares all events against the present event to collect the name of the trigger.
	 * 
	 * @param parseContext The context from an init method to be used to collect the name.
	 * @return The registered name of the trigger, otherwise null if not found.
	 */
	@Nullable
	default String getEventName(ParseContext parseContext) {
		for (CodeSection section : parseContext.getParserState().getCurrentSections()) {
			if (!(section instanceof Trigger))
				continue;
			Trigger trigger = (Trigger) section;
			for (SkriptEventInfo<?> eventInfo : Scroll.getRegistration().getEvents()) {
				if (!eventInfo.getSyntaxClass().equals(trigger.getEvent().getClass()))
					continue;
				ScrollEvent.Information information = eventInfo.getData("scroll-information", ScrollEvent.Information.class);
				if (information != null)
					return information.name();
			}
		}
		// Fallback to check the statement if the API changed.
		for (Statement statement : parseContext.getParserState().getCurrentStatements()) {
			if (!(statement instanceof Trigger))
				continue;
			Trigger trigger = (Trigger) statement;
			for (SkriptEventInfo<?> eventInfo : Scroll.getRegistration().getEvents()) {
				if (!eventInfo.getSyntaxClass().equals(trigger.getEvent().getClass()))
					continue;
				ScrollEvent.Information information = eventInfo.getData("scroll-information", ScrollEvent.Information.class);
				if (information != null)
					return information.name();
			}
		}
		return null;
	}

}
