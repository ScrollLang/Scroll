package org.scrolllang.scroll.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.scrolllang.scroll.Scroll;

public class FileUtils {

	/**
	 * Returns or creates the directories if they don't exist.
	 * This has proper exception catching and language reference.
	 * 
	 * @param path The path to find and create.
	 * @return The resolved path.
	 */
	public static Path getOrCreateDir(Path path) {
		if (!Files.exists(path)) {
			try {
				return Files.createDirectories(path);
			} catch (IOException exception) {
				throw Scroll.printException(exception, Scroll.languageFormat("files.create.directory", path.getFileName()));
			}
		}
		return path;
	}

}
