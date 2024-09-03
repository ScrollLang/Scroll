package org.scrolllang.scroll;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.UnknownFormatConversionException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.scrolllang.scroll.elements.Types;
import org.scrolllang.scroll.exceptions.EmptyStacktraceException;
import org.scrolllang.scroll.language.ScrollEvent;
import org.scrolllang.scroll.log.ExceptionPrinter;
import org.scrolllang.scroll.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.log.ErrorType;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration.EventRegistrar;
import io.github.syst3ms.skriptparser.types.TypeManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

/**
 * Main class for Scroll for all environments.
 */
public class Scroll extends SkriptAddon implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("Scroll");
	public static Configuration CONFIGURATION;
	public static ModContainer MOD_CONTAINER;
	public static Language LANGUAGE;

	private static SkriptRegistration REGISTRATION;
	private static EnvType ENVIRONMENT;
	private static Path GAME_DIRECTORY;
	private static Path ADDONS_FOLDER;
	private static Path SCROLL_FOLDER;
	private static Scroll INSTANCE;

	static final List<ScrollAddon> ADDONS = new ArrayList<>();
	static FabricAudiences ADVENTURE;
	static MinecraftServer SERVER;
	static ScrollAddon SELF;

	@Override
	public void onInitialize() {
		INSTANCE = this;
		FabricLoader loader = FabricLoader.getInstance();
		GAME_DIRECTORY = loader.getGameDir();
		ENVIRONMENT = loader.getEnvironmentType();
		MOD_CONTAINER = loader.getModContainer("scroll").orElseThrow();
		SCROLL_FOLDER = FileUtils.getOrCreateDir(GAME_DIRECTORY.resolve("scroll"));
		ADDONS_FOLDER = FileUtils.getOrCreateDir(SCROLL_FOLDER.resolve("addons"));

		try {
			CONFIGURATION = new Configuration(this);
			LANGUAGE = new Language(this);
		} catch (IOException exception) {
			printException(exception, "Could not load the configuration/properties files!");
			return;
		}

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			ADVENTURE = FabricServerAudiences.of(server);
			SERVER = server;
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			ADVENTURE = null;
			SERVER = null;
		});

		SkriptLogger registrationLogger = new SkriptLogger(CONFIGURATION.isDebug());
		REGISTRATION = new SkriptRegistration(this, registrationLogger);
		SELF = new ScrollAddon("Scroll") {
			@Override
			public void startRegistration(ScrollRegistration registration) {

			}
		};
		ADDONS.add(SELF);
	}

	/**
	 * Separated for client and server specific types to be registered before syntaxes.
	 */
	static void register() {
		Parser.init(new String[0], new String[0], new String[0], true);

		// Types must be before syntaxes.
		Types.register(REGISTRATION);
		TypeManager.register(REGISTRATION);

		// Note that the class scanner in the skript-parser init method above will not work as it assumes classes
		// are in a JAR. So because of that, we have to statically initalize the classes ourselves.
		new Reflections(
				"org.scrolllang.scroll.commands",
				"org.scrolllang.scroll.elements",
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
		Parser.printLogs(REGISTRATION.register(), Calendar.getInstance(), true);
		ADDONS.stream()
			.sorted(Comparator.comparingInt(ScrollAddon::getPriority).reversed())
			.forEach(addon -> {
				ScrollRegistration registration = new ScrollRegistration(addon);
				addon.startRegistration(registration);
				Parser.printLogs(registration.register(), Calendar.getInstance(), true);
			});

		ScrollScriptLoader.loadScriptsDirectory(FileUtils.getOrCreateDir(SCROLL_FOLDER.resolve("scripts")));
		// TODO Deal with triggers not getting cleared after a reload.
	}

	/**
	 * Returns the {@link MinecraftServer} of the dedicated server. Will be client server if single player.
	 */
	@Nullable
	public static MinecraftServer getMinecraftServer() {
		return SERVER;
	}

	static <A extends FabricAudiences> void setAdventure(A adventure) {
		Scroll.ADVENTURE = adventure;
	}

	/**
	 * Returns the {@link FabricAudiences} for Adventure API.
	 * Will be {@link net.kyori.adventure.platform.fabric.FabricClientAudiences} for the client and {@link FabricServerAudiences} for the server.
	 * Cast appropriately depending on {@link #isServerEnvironment()}.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public static <A extends FabricAudiences> A getAdventure() {
		return (@Nullable A) ADVENTURE;
	}

	/**
	 * @return If Scroll is loaded as a {@link EnvType#CLIENT} environment or a {@link EnvType#SERVER} environment.
	 * <p>
	 * Returns false if called before initalization.
	 */
	public static boolean isServerEnvironment() {
		if (ENVIRONMENT == null)
			return false;
		return ENVIRONMENT == EnvType.SERVER;
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

	public static Text adventure(String node, Object... arguments) {
		MiniMessage miniMessage = MiniMessage.miniMessage();
		FabricAudiences adventure = Scroll.getAdventure();
		String string = languageFormat(node, arguments);
		if (string == null)
			return Text.literal(node);
		return adventure.toNative(miniMessage.deserialize(string));
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
	 * Allows to insert logging information during parse time of Scroll scripts.
	 * Use this method for script parsing related warnings.
	 * See {@link #LOGGER} for other errors and info messages.
	 * 
	 * @param message The message to print.
	 */
	public static void warning(String message) {
		if (ScrollScriptLoader.CURRENT_LOGGER != null) {
			ScrollScriptLoader.CURRENT_LOGGER.warn(message);
			return;
		}
		LOGGER.warn(message);
	}

	/**
	 * Allows to insert logging information during parse time of Scroll scripts.
	 * Use this method for script parsing related warnings.
	 * See {@link #LOGGER} for other errors and info messages.
	 * 
	 * @param message The message to print.
	 * @param tip A hint to provide to the Scroll user for the error.
	 */
	public static void warning(String message, @Nullable String tip) {
		if (ScrollScriptLoader.CURRENT_LOGGER != null) {
			ScrollScriptLoader.CURRENT_LOGGER.warn(message, tip);
			return;
		}
		LOGGER.warn(message);
	}

	public static Scroll getInstance() {
		if (INSTANCE == null)
			throw new IllegalStateException();
		return INSTANCE;
	}

	/**
	 * Should only be used by Scroll internally. Use {@link ScrollAddon#startRegistration(ScrollRegistration)}
	 * 
	 * @return The main registration for Scroll syntaxes and addon syntaxes. Use this method over {@link Parser#getMainRegistration()}.
	 */
	public static SkriptRegistration getRegistration() {
		return REGISTRATION;
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
	 * @return Returns the path to the addons folder.
	 */
	public Path getAddonsFolder() {
		return ADDONS_FOLDER;
	}

	/**
	 * Non static method for getting the Logger.
	 * 
	 * @return The {@link Logger} of Scroll.
	 */
	public Logger getLogger() {
		return LOGGER;
	}

	public static List<ScrollAddon> getScrollAddons() {
		return ADDONS;
	}

	/**
	 * This is an override.
	 */
	public static List<SkriptAddon> getAddons() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void handleTrigger(Trigger trigger) {
		SkriptEvent event = trigger.getEvent();
		if (!canHandleEvent(event) || !(event instanceof ScrollEvent))
			return;

		ScrollEvent scrollEvent = (ScrollEvent) event;
		scrollEvent.getTriggers().addTriggers(trigger);
	}

	/**
	 * Returns an EventRegistrar for a {@link ScrollEvent}
	 * 
	 * @param event the ScrollEvent class
	 * @param context the {@link TriggerContext} this ScrollEvent will handle.
	 * @param patterns the ScrollEvent patterns.
	 * @return EventRegistrar
	 */
	public static EventRegistrar<? extends ScrollEvent> newEvent(String name, Class<? extends ScrollEvent> event, Class<? extends TriggerContext> context, String... patterns) {
		return (EventRegistrar<? extends ScrollEvent>) REGISTRATION.newEvent(event, patterns).setHandledContexts(context).addData("scroll-information", new ScrollEvent.Information(name));
	}

	/**
	 * Registers a {@link ScrollEvent}
	 * 
	 * @param event the ScrollEvent class
	 * @param context the {@link TriggerContext} this ScrollEvent will handle.
	 * @param patterns the ScrollEvent patterns.
	 */
	public static void addEvent(String name, Class<? extends ScrollEvent> event, Class<? extends TriggerContext> context, String... patterns) {
		newEvent(name, event, context, patterns).register();
	}

	/**
	 * Safely prints an exception and extra information to the console.
	 * 
	 * @param throwable The {@link Throwable} that has caused an error.
	 * @param messages Optionally any messages to print along with the exception for descriptions.
	 * @return an EmptyStacktraceException to throw if code execution should terminate.
	 */
	public static EmptyStacktraceException printException(Throwable throwable, String... messages) {
		return printException(SELF, throwable, messages);
	}

	/**
	 * Safely prints an exception and extra information to the console.
	 * 
	 * @param throwable The {@link Throwable} that has caused an error.
	 * @param messages Optionally any messages to print along with the exception for descriptions.
	 * @return an EmptyStacktraceException to throw if code execution should terminate.
	 */
	public static EmptyStacktraceException printException(ScrollAddon addon, Throwable throwable, String... messages) {
		return new ExceptionPrinter(addon, throwable, messages).print(LOGGER);
	}

}
