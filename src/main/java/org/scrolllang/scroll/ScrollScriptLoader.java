package org.scrolllang.scroll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.scrolllang.scroll.commands.CommandManager;
import org.scrolllang.scroll.language.ScrollEvent;
import org.scrolllang.scroll.script.Script;
import org.scrolllang.scroll.utils.FileUtils;

import com.google.common.collect.Sets;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Main class for handling loading to skript-parser.
 */
public class ScrollScriptLoader {

	private static Path SCRIPTS_FOLDER = FileUtils.getOrCreateDir(FabricLoader.getInstance().getGameDir().resolve("scroll/scripts"));

	private static final Collection<String> RESERVED_NAMES = Sets.newHashSet(
		"configuration.scroll",
		"languages.scroll",
		"language.scroll",
		"settings.scroll",
		"config.scroll",
		"lang.scroll"
	);

	private static final List<Script> LOADED_SCRIPTS = new ArrayList<>();
	private static final boolean DEBUG = Scroll.CONFIGURATION.isDebug();

	public static final String DISABLED_PREFIX = "-";
	public static final String EXTENSION = ".scroll";

	@Nullable
	public static SkriptLogger CURRENT_LOGGER;

	@Nullable
	private static Script CURRENT_SCRIPT;

	public static Script getCurrentlyLoadingScript() {
		return CURRENT_SCRIPT;
	}

	/**
	 * @return The main folder containing all the scripts.
	 */
	public static Path getScriptsFolder() {
		if (SCRIPTS_FOLDER != null)
			return SCRIPTS_FOLDER;
		SCRIPTS_FOLDER = FileUtils.getOrCreateDir(FabricLoader.getInstance().getGameDir().resolve("scroll/scripts"));
		return SCRIPTS_FOLDER;
	}

	/**
	 * Validates that the provided path is a Scroll script.
	 * 
	 * @param script The path to the script to validate.
	 * @return true if the scroll script file is valid.
	 * @throws IllegalArgumentException if the provided path was a directory.
	 */
	public static boolean validateScriptAt(@NotNull Path script) {
		if (Files.isDirectory(script)) {
			Scroll.LOGGER.error(Scroll.languageFormat("scripts.load.error.directory", script.toString()));
			return false;
		}
		String fileName = script.getFileName().toString();
		if (!fileName.endsWith(EXTENSION))
			return false;
		if (fileName.startsWith(DISABLED_PREFIX))
			return false;
		if (RESERVED_NAMES.stream().anyMatch(fileName::equalsIgnoreCase)) {
			Scroll.LOGGER.error(Scroll.languageFormat("scripts.name.reserved", fileName));
			return false;
		}
		return true;
	}

	/**
	 * Collects all the .scroll scripts at the defined {@link Path} directory.
	 * Using the default validation method to collect .scroll files.
	 * 
	 * @param directory The directory path to search for .scroll files.
	 * @return A collection of all the found and constructed {@link Script} within the defined path directory.
	 * @throws IllegalArgumentException if the provided path was not a directory.
	 */
	@NotNull
	public static Stream<Path> collectScriptsAt(Path directory) {
		return collectScriptsAt(directory, ScrollScriptLoader::validateScriptAt);
	}

	/**
	 * Collects all the scripts at the defined {@link Path} directory.
	 * Use the filter to apply additional checks to the paths.
	 * 
	 * @param directory The directory path to search for .scroll files.
	 * @param filter The filter to apply to the paths.
	 * @return A collection of all the found and constructed {@link Script} within the defined path directory.
	 * @throws IllegalArgumentException if the provided path was not a directory.
	 */
	@NotNull
	public static Stream<Path> collectScriptsAt(Path directory, Predicate<Path> filter) {
		if (!Files.isDirectory(directory)) {
			Scroll.LOGGER.error(Scroll.languageFormat("scripts.load.error.not.directory", directory.toString()));
			return Stream.empty();
		}
		try {
			return Files.list(directory)
					.filter(Objects::nonNull)
					.flatMap(path -> {
						if (Files.isDirectory(path))
							try {
								return Files.list(path);
							} catch (IOException exception) {
								Scroll.getInstance().printException(exception, Scroll.languageFormat("files.read.directory", directory));
								return Stream.empty();
							}
						return Stream.of(path);
					})
					.filter(filter);
		} catch (IOException exception) {
			Scroll.getInstance().printException(exception, Scroll.languageFormat("files.read.directory", directory));
			return Stream.empty();
		}
	}

	/**
	 * Loads the scripts from the provided path of the scripts folder.
	 * 
	 * @param scriptsPath The directory /scroll/scripts inside the game directory.
	 * @return The Scripts that were loaded from the parse. Can be empty, but not null.
	 * @throws IllegalArgumentException if the provided Path argument was not the /scroll/scripts folder.
	 */
	@NotNull
	@Internal
	static List<Script> loadScriptsDirectory(Path scriptsPath) {
		if (!Files.isDirectory(scriptsPath)) {
			Scroll.LOGGER.error(Scroll.language("scripts.load.internal.scripts"));
			return new ArrayList<>();
		};
		long start = System.nanoTime();
		LOADED_SCRIPTS.clear(); // TODO proper unloading.
		SCRIPTS_FOLDER = scriptsPath;
		List<Script> scripts = collectScriptsAt(scriptsPath)
				.parallel()
				.map(ScrollScriptLoader::loadScriptAt)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
		if (scripts.isEmpty()) {
			Scroll.LOGGER.warn(Scroll.language("scroll.no.scripts"));
			return scripts;
		}
		LOADED_SCRIPTS.addAll(scripts);
		Scroll.LOGGER.info(Scroll.languageFormat("scroll.scripts.loaded", scripts.size(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)));
		return scripts;
	}

	/**
	 * Loads a script at the provided path.
	 * 
	 * @param path The path to the script file.
	 * @return The Script instance of the script at the provided path. Optional if the provided path is a valid Scroll script.
	 * @throws IllegalArgumentException if the provided path was a directory.
	 */
	public static Optional<Script> loadScriptAt(Path path) {
		if (Files.isDirectory(path)) {
			Scroll.LOGGER.error(Scroll.languageFormat("scripts.load.error.directory", path.toString()));
			return Optional.empty();
		}
		if (!validateScriptAt(path))
			return Optional.empty();
		Script script = LOADED_SCRIPTS.stream()
				.filter(existing -> existing.getPath().equals(path))
				.map(existing -> {
					CommandManager.unregisterAll(existing);
					return existing;
				})
				.findFirst()
				.orElse(new Script(path));
		LOADED_SCRIPTS.remove(script);
		CURRENT_SCRIPT = script;
		ScriptLoader.getTriggerMap().entrySet().stream()
				.filter(entry -> {
					String fileName = path.getFileName().toString().replaceAll("(.+)\\..+", "$1");
					return entry.getKey().equalsIgnoreCase(fileName);
				})
				.map(Entry::getValue)
				.flatMap(List::stream)
				.forEach(trigger -> {
					SkriptEvent event = trigger.getEvent();
					if (!(event instanceof ScrollEvent))
						return;

					ScrollEvent scrollEvent = (ScrollEvent) event;
					scrollEvent.getTriggers().clear();
				});
		List<LogEntry> entries;
		try {
			CURRENT_LOGGER = new SkriptLogger(DEBUG);
			entries = CompletableFuture.supplyAsync(() -> ScriptLoader.loadScript(path, CURRENT_LOGGER, DEBUG)).get(5, TimeUnit.MINUTES);
//			for (LogEntry log : entries) {
//				ConsoleColors color = ConsoleColors.WHITE;
//				if (log.getType() == LogType.WARNING) {
//					color = ConsoleColors.YELLOW;
//				} else if (log.getType() == LogType.ERROR) {
//					color = ConsoleColors.RED;
//				} else if (log.getType() == LogType.INFO) {
//					color = ConsoleColors.BLUE;
//				} else if (log.getType() == LogType.DEBUG) {
//					color = ConsoleColors.PURPLE;
//				}
//				String CONSOLE_FORMAT = "[%tT] %s: %s%n";
//				Calendar time = Calendar.getInstance();
//				Scroll.LOGGER.info(String.format(color + CONSOLE_FORMAT + ConsoleColors.RESET, time, log.getType().name(), log.getMessage()));
//				boolean tipsEnabled = true;
//				if (tipsEnabled && log.getTip().isPresent())
//					Scroll.LOGGER.info(String.format(ConsoleColors.BLUE_BRIGHT + CONSOLE_FORMAT + ConsoleColors.RESET, time, "TIP", log.getTip().get()));
//	        }
			CURRENT_LOGGER.finalizeLogs();
			entries.addAll(CURRENT_LOGGER.close());
			// TODO print to command sender if done via command.
			Parser.printLogs(entries, Calendar.getInstance(), true);
			CURRENT_LOGGER = null;
		} catch (InterruptedException | TimeoutException exception) {
			Scroll.LOGGER.error(Scroll.languageFormat("scripts.loading.timeout", path.getFileName()));
			return Optional.empty();
		} catch (ExecutionException exception) {
			Scroll.getInstance().printException(exception, Scroll.languageFormat("scripts.parse.exception", path.getFileName()));
			return Optional.empty();
		}
		CURRENT_SCRIPT = null;
		LOADED_SCRIPTS.add(script);
		return Optional.of(script);
	}

	/**
	 * Loads scripts at the provided path.
	 * 
	 * @param directory The path to the script files.
	 * @return The Script instances of the script at the provided path. List may be empty if none of the files in the directory are valid Scroll scripts.
	 * @throws IllegalArgumentException if the provided path was not a directory.
	 */
	public static List<Script> loadScriptsAt(Path directory) {
		if (!Files.isDirectory(directory)) {
			Scroll.LOGGER.error(Scroll.languageFormat("scripts.load.error.not.directory", directory.toString()));
			return new ArrayList<>();
		}
		return collectScriptsAt(directory)
				.map(ScrollScriptLoader::loadScriptAt)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	/**
	 * Reloads the provided script.
	 * 
	 * @param script The {@link Script} to reload.
	 * @return The {@link Script}s that loaded successfully. Optional If a script path location was changed, it will not be present.
	 */
	public static Optional<Script> reloadScript(Script script) {
		return loadScriptAt(script.getPath());
	}

	/**
	 * Reloads all the provided scripts.
	 * 
	 * @param scripts Collection of scripts to reload.
	 * @return All the {@link Script}s that loaded successfully. If a script path location was changed, it will not be present in the collection.
	 */
	public static List<Script> reloadScripts(Collection<Script> scripts) {
		return scripts.stream()
				.map(ScrollScriptLoader::reloadScript)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	/**
	 * Disables the provided script.
	 * 
	 * @param script The {@link Script} to disable.
	 */
	public static void disableScript(Script script) {
		Path path = script.getPath();
		String fileName = script.getFileName();
		LOADED_SCRIPTS.remove(script);
		if (!Files.exists(path)) // Was renamed
			return;
		try {
			Files.move(path, SCRIPTS_FOLDER.resolve(DISABLED_PREFIX + path.getFileName()));
		} catch (IOException exception) {
			Scroll.LOGGER.error(Scroll.languageFormat("scripts.disable.failed", fileName));
		}
	}

	/**
	 * Disables all the provided scripts.
	 * 
	 * @param scripts Collection of scripts to disable.
	 */
	public static void disableScripts(Collection<Script> scripts) {
		scripts.stream().forEach(ScrollScriptLoader::disableScript);
	}

	/**
	 * Enables the provided script.
	 * 
	 * @param path The {@link Path} to the script to enable. Must have the disabled script prefix.
	 * @return The {@link Script}s that loaded successfully after an enable. If the script failed to be loaded, the Optional will be empty.
	 */
	public static Optional<Script> enableScriptAt(Path path) {
		if (!Files.exists(path))
			return Optional.empty();
		String fileName = path.getFileName().toString();
		if (!fileName.startsWith(DISABLED_PREFIX))
			return Optional.empty();
		Path futurePath = SCRIPTS_FOLDER.resolve(fileName.substring(DISABLED_PREFIX.length()));
		if (LOADED_SCRIPTS.stream().anyMatch(script -> script.getPath().equals(futurePath))) {
			Scroll.LOGGER.error(Scroll.languageFormat("scripts.enable.failed", fileName));
			return Optional.empty();
		}
		try {
			return loadScriptAt(Files.move(path, futurePath));
		} catch (IOException exception) {
			Scroll.LOGGER.error(Scroll.languageFormat("scripts.enable.failed", fileName));
			return Optional.empty();
		}
	}

	/**
	 * Enables all the provided scripts.
	 * 
	 * @param paths Collection of paths to the scripts to enable. All scripts must have the disabled script prefix.
	 */
	public static void enableScriptsAt(Collection<Path> paths) {
		paths.stream().forEach(ScrollScriptLoader::enableScriptAt);
	}

	/**
	 * Returns an optional if the Script is currently loaded. Ignoring case.
	 * 
	 * @param name The name to test against. Can contain .scroll extension or not.
	 * @return Optional will be present if the Script does exists.
	 */
	public static Optional<Script> getScriptByName(String name) {
		return LOADED_SCRIPTS.stream().filter(script -> {
			String fileName = script.getFileName();
			if (fileName.equalsIgnoreCase(name))
				return true;
			if (fileName.substring(0, fileName.lastIndexOf(EXTENSION)).equalsIgnoreCase(name))
				return true;
			return false;
		}).findFirst();
	}

	/**
	 * @return all the currently loaded scripts. Can be empty, but not null.
	 */
	@NotNull
	public static Collection<Script> getLoadedScripts() {
		return Collections.unmodifiableCollection(LOADED_SCRIPTS);
	}

}
