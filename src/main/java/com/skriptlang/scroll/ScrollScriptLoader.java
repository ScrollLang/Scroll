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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Sets;
import com.skriptlang.scroll.script.Script;

import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.parsing.ScriptLoader;

/**
 * Main class for handling loading to skript-parser.
 * TODO make this a valid API
 */
public class ScrollScriptLoader {

	private static final Collection<String> RESERVED_NAMES = Sets.newHashSet("configuration.scroll", "config.scroll", "languages.scroll");
	private static final List<Script> LOADED_SCRIPTS = new ArrayList<>();

	/**
	 * Collects all the .scroll scripts at the defined {@link Path} directory.
	 * 
	 * @param directory The directory path to search for .scroll files.
	 * @return A collection of all the found and constructed {@link Script} within the defined path directory.
	 */
	@NotNull
	private static Stream<Path> collectScriptsAt(Path directory) {
		if (!Files.isDirectory(directory)) {
			Scroll.LOGGER.error(Scroll.languageFormat("files.not.directory", directory));
			return Stream.empty();
		}
		try {
			return Files.walk(directory)
					.filter(Objects::nonNull)
					.filter(path -> path.getFileName().toString().endsWith(".scroll"))
					.filter(path -> {
						String fileName = path.getFileName().toString();
						if (RESERVED_NAMES.stream().anyMatch(fileName::equalsIgnoreCase)) {
							Scroll.LOGGER.error(Scroll.languageFormat("scripts.name.reserved", fileName));
							return false;
						}
						return true;
					})
					.filter(path -> !path.getFileName().toString().startsWith("-"));
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
	 */
	@NotNull
	static List<Script> loadScripts(Path scriptsPath) {
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
					List<LogEntry> entries = ScriptLoader.loadScript(path, false);
					Parser.printLogs(entries, Calendar.getInstance(), true);
					return new Script(path);
				})
				.collect(Collectors.toList());
		if (scripts.isEmpty()) {
			Scroll.LOGGER.warn(Scroll.language("scroll.no.scripts"));
			return scripts;
		}
		LOADED_SCRIPTS.addAll(scripts);
		Scroll.LOGGER.info(Scroll.languageFormat("scroll.scripts.loaded", scripts.size(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " milliseconds"));
		return scripts;
	}

	/**
	 * @return all the currently loaded scripts. Can be empty, but not null.
	 */
	@NotNull
	public static Collection<Script> getLoadedScripts() {
		return Collections.unmodifiableCollection(LOADED_SCRIPTS);
	}

}
