package com.skriptlang.scroll.language;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.skriptlang.scroll.Scroll;

import io.github.syst3ms.skriptparser.log.ErrorType;

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

	default void error(LangNode... langs) {
		error(languageFormat(langs));
	}

	/**
	 * Allows to insert logging errors during parse time of Scroll scripts.
	 * Use this method for script parsing related errors.
	 * See {@link #LOGGER} for other errors and info messages.
	 * The ErrorType will be set to {@link ErrorType.SEMANTIC_ERROR}
	 * 
	 * @param message The message to print.
	 */
	default void error(String message) {
		Scroll.error(message, ErrorType.SEMANTIC_ERROR, null);
	}

	/**
	 * Allows to insert logging errors during parse time of Scroll scripts.
	 * Use this method for script parsing related errors.
	 * See {@link #LOGGER} for other errors and info messages.
	 * The ErrorType will be set to {@link ErrorType.SEMANTIC_ERROR}
	 * 
	 * @param message The message to print.
	 * @param tip A hint to provide to the Scroll user for the error.
	 */
	default void error(String message, @Nullable String tip) {
		Scroll.error(message, ErrorType.SEMANTIC_ERROR, tip);
	}

	/**
	 * Allows to insert logging errors during parse time of Scroll scripts.
	 * Use this method for script parsing related errors.
	 * See {@link #LOGGER} for other errors and info messages.
	 * 
	 * @param message The message to print.
	 * @param type The error type this will provide to the SkriptLogger.
	 * @param tip A hint to provide to the Scroll user for the error.
	 */
	default void error(String message, ErrorType type, @Nullable String tip) {
		Scroll.error(message, type, tip);
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

}
