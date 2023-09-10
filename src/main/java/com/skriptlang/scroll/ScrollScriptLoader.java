package com.skriptlang.scroll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Sets;
import com.skriptlang.scroll.script.Script;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

/**
 * Main class for handling loading to skript-parser.
 */
public class ScrollScriptLoader {

	private static final Collection<String> RESERVED_NAMES = Sets.newHashSet("configuration.scroll", "config.scroll", "languages.scroll");
	private static final List<Script> LOADED_SCRIPTS = new ArrayList<>();
	private static final boolean DEBUG = Scroll.CONFIGURATION.isDebug();

	/**
	 * Validates that the provided path is a Scroll script.
	 * 
	 * @param script The path to the script to validate.
	 * @return true if the scroll script file is valid.
	 * @throws IllegalArgumentException if the provided path was a directory.
	 */
	public static boolean validateScriptAt(@NotNull Path script) {
		Validate.isTrue(!Files.isDirectory(script), Scroll.languageFormat("scripts.load.error.directory", script.toString()));
		String fileName = script.getFileName().toString();
		if (!fileName.endsWith(".scroll"))
			return false;
		if (fileName.startsWith("-"))
			return false;
		if (RESERVED_NAMES.stream().anyMatch(fileName::equalsIgnoreCase)) {
			Scroll.LOGGER.error(Scroll.languageFormat("scripts.name.reserved", fileName));
			return false;
		}
		return true;
	}

	/**
	 * Collects all the .scroll scripts at the defined {@link Path} directory.
	 * 
	 * @param directory The directory path to search for .scroll files.
	 * @return A collection of all the found and constructed {@link Script} within the defined path directory.
	 * @throws IllegalArgumentException if the provided path was not a directory.
	 */
	@NotNull
	public static Stream<Path> collectScriptsAt(Path directory) {
		Validate.isTrue(Files.isDirectory(directory), Scroll.languageFormat("scripts.load.error.not.directory", directory.toString()));
		try {
			return Files.list(directory)
					.filter(Objects::nonNull)
					.flatMap(path -> {
						if (Files.isDirectory(path))
							try {
								return Files.list(path);
							} catch (IOException exception) {
								Scroll.printException(exception, Scroll.languageFormat("files.read.directory", directory));
								return Stream.empty();
							}
						return Stream.of(path);
					})
					.filter(ScrollScriptLoader::validateScriptAt);
		} catch (IOException exception) {
			Scroll.printException(exception, Scroll.languageFormat("files.read.directory", directory));
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
		Validate.isTrue(Files.isDirectory(scriptsPath), Scroll.language("scripts.load.internal.scripts"));
		long start = System.nanoTime();
		LOADED_SCRIPTS.clear(); // TODO proper unloading.
		if (!Files.exists(scriptsPath)) {
			try {
				Files.createDirectories(scriptsPath);
			} catch (IOException exception) {
				Scroll.printException(exception, Scroll.languageFormat("files.create.directory.select", "/scripts", "/scroll"));
				return new ArrayList<>();
			}
		}
		List<Script> scripts = collectScriptsAt(scriptsPath)
				.parallel()
				.map(path -> {
					List<LogEntry> entries = ScriptLoader.loadScript(path, DEBUG);
					Parser.printLogs(entries, Calendar.getInstance(), true);
					return new Script(path);
				})
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
		Validate.isTrue(!Files.isDirectory(path), Scroll.languageFormat("scripts.load.error.directory", path.toString()));
		if (!validateScriptAt(path))
			return Optional.empty();
		LOADED_SCRIPTS.removeIf(script -> script.getPath().equals(path));
		List<LogEntry> entries = ScriptLoader.loadScript(path, DEBUG);
		Parser.printLogs(entries, Calendar.getInstance(), true);
		Script script = new Script(path);
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
		Validate.isTrue(Files.isDirectory(directory), Scroll.languageFormat("scripts.load.error.not.directory", directory.toString()));
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
			if (fileName.substring(0, fileName.lastIndexOf(".scroll")).equalsIgnoreCase(name))
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
