package org.scrolllang.scroll.script;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.Validate;

/**
 * Represents a parsed and loaded Script.
 */
public class Script {

	private final Path path;

	public Script(Path path) {
		Validate.isTrue(!Files.isDirectory(path), "The path of the script was a directory. Must be a single file.");
		this.path = path;
	}

	public String getSimpleName() {
		String fileName = getFileName();
		return fileName.substring(0, fileName.lastIndexOf(".scroll"));
	}

	public String getFileName() {
		return path.getFileName().toString();
	}

	public File getFile() {
		return path.toFile();
	}

	public Path getPath() {
		return path;
	}

}
