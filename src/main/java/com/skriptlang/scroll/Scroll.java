package com.skriptlang.scroll;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UnknownFormatConversionException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skriptlang.scroll.exceptions.EmptyStacktraceException;
import com.skriptlang.scroll.language.ScrollEvent;
import com.skriptlang.scroll.log.ExceptionPrinter;
import com.skriptlang.scroll.utils.FileUtils;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * Main class for Scroll for all environments.
 */
public class Scroll extends SkriptAddon implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("Scroll");
	public static Configuration CONFIGURATION;
	public static ModContainer MOD_CONTAINER;
	public static Language LANGUAGE;

	private static SkriptRegistration registration;
	private static Path SCROLL_FOLDER;

	@Override
	public void onInitialize() {
		MOD_CONTAINER = FabricLoader.getInstance().getModContainer("scroll").orElseThrow();
		SCROLL_FOLDER = FileUtils.getOrCreateDir(FabricLoader.getInstance().getGameDir().resolve("scroll"));
		try {
			CONFIGURATION = new Configuration(this);
			LANGUAGE = new Language(this);
		} catch (IOException exception) {
			printException(exception, "Could not load the configuration/properties files!");
			return;
		}

		registration = new SkriptRegistration(this);
		Parser.init(new String[0], new String[0], new String[0], true);
		// Note that the class scanner in the skript-parser init method above will not work as it assumes classes
		// are in a JAR. So because of that, we have to statically initalize the classes ourselves.
		new Reflections(
				"com.skriptlang.scroll.elements",
				"io.github.syst3ms.skriptparser.expressions",
				"io.github.syst3ms.skriptparser.effects",
				"io.github.syst3ms.skriptparser.event",
				"io.github.syst3ms.skriptparser.lang",
				"io.github.syst3ms.skriptparser.sections",
				"io.github.syst3ms.skriptparser.tags",
				Scanners.SubTypes.filterResultsBy(s -> true)
		).getSubTypesOf(Object.class).forEach(clazz -> {
			try {
				Class.forName(clazz.getName(), true, Scroll.class.getClassLoader());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		});
		registration.register();

		ScrollScriptLoader.loadScriptsDirectory(FileUtils.getOrCreateDir(SCROLL_FOLDER.resolve("scripts")));
		// TODO Deal with triggers not getting cleared after a reload.
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
	public static String languageFormat(String key, Object... arguments) {
		try {
			return String.format(LANGUAGE.getOrDefaultEnglish(key), arguments);
		} catch (UnknownFormatConversionException exception) {
			printException(exception, "Potentially incorrect format in the language properties file.");
			return null;
		}
	}

	/**
	 * Quick shortcut for access the language properties file.
	 * 
	 * @param key The key from the language properties file.
	 * @return The found String value. Otherwise will default to English.
	 */
	@NotNull
	public static String language(String key) {
		return LANGUAGE.getOrDefaultEnglish(key);
	}

	/**
	 * Allows to insert logging errors during parse time of Scroll scripts.
	 * Use this method for script parsing related errors.
	 * See {@link #LOGGER} for other errors and info messages.
	 * The ErrorType will be set to {@link ErrorType.SEMANTIC_ERROR}
	 * 
	 * @param message The message to print.
	 */
	public static void error(String message) {
		error(message, ErrorType.SEMANTIC_ERROR, null);
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
	public static void error(String message, @Nullable String tip) {
		error(message, ErrorType.SEMANTIC_ERROR, tip);
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
	public static void error(String message, ErrorType type, @Nullable String tip) {
		if (ScrollScriptLoader.CURRENT_LOGGER != null) {
			ScrollScriptLoader.CURRENT_LOGGER.error(message, type, tip);
			return;
		}
		LOGGER.error(message);
	}

	/**
	 * Allows to insert logging information during parse time of Scroll scripts.
	 * Use this method for script parsing related errors.
	 * See {@link #LOGGER} for other errors and info messages.
	 * 
	 * @param message The message to print.
	 */
	public static void info(String message) {
		if (ScrollScriptLoader.CURRENT_LOGGER != null) {
			ScrollScriptLoader.CURRENT_LOGGER.info(message);
			return;
		}
		LOGGER.info(message);
	}

	/**
	 * @return The main registration for Scroll syntaxes and addon syntaxes. Use this method over {@link Parser#getMainRegistration()}.
	 */
	public static SkriptRegistration getRegistration() {
		return registration;
	}

	/**
	 * @return The main {@link Configuration} of Scroll.
	 */
	public Configuration getConfiguration() {
		return CONFIGURATION;
	}

	/**
	 * Non static method for getting the {@link ModContainer}
	 * 
	 * @return Returns the {@link ModContainer} of Scroll.
	 */
	public ModContainer getModContainer() {
		return MOD_CONTAINER;
	}

	/**
	 * Non static method for getting the Logger.
	 * 
	 * @return The {@link Logger} of Scroll.
	 */
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	public void handleTrigger(Trigger trigger) {
		SkriptEvent event = trigger.getEvent();
		if (!canHandleEvent(event) || !(event instanceof ScrollEvent))
			return;

		ScrollEvent scrollEvent = (ScrollEvent) event;
		scrollEvent.getTriggers().addTriggers(trigger);
	}

	@Override
    public void finishedLoading() {}

	/**
	 * Registers a {@link ScrollEvent}
	 * 
	 * @param event the ScrollEvent class
	 * @param context the {@link TriggerContext} this ScrollEvent will handle.
	 * @param patterns the ScrollEvent patterns.
	 */
	public static void addEvent(Class<? extends ScrollEvent> event, Class<? extends TriggerContext> context, String... patterns) {
		registration.newEvent(event, patterns).setHandledContexts(context).register();
	}

	/**
	 * Safely prints an exception and extra information to the console.
	 * 
	 * @param throwable The {@link Throwable} that has caused an error.
	 * @param messages Optionally any messages to print along with the exception for descriptions.
	 * @return an EmptyStacktraceException to throw if code execution should terminate.
	 */
	public static EmptyStacktraceException printException(Throwable throwable, String... messages) {
		return new ExceptionPrinter(throwable, messages).print(LOGGER);
	}

}
