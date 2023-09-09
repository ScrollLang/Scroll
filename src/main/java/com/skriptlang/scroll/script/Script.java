package com.skriptlang.scroll.script;

import java.io.File;
import java.nio.file.Path;

/**
 * Represents a parsed and loaded Script.
 */
public class Script {

	private final Path path;

	public Script(Path path) {
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

}
