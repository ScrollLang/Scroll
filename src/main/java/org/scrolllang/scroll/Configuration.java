package org.scrolllang.scroll;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.scrolllang.scroll.language.Reloadable;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * Main class for handling configurations.
 */
public class Configuration implements Reloadable {

	private final ModContainer modContainer;

	private TomlTable toml;

	Configuration(Scroll instance) throws FileNotFoundException, IOException {
		this.modContainer = instance.getModContainer();
		this.toml = this.load();
	}

	/**
	 * Method that loads the TOML files.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException if the file could not be found.
	 */
	TomlTable load() throws FileNotFoundException, IOException {
		Path configurationPath = FabricLoader.getInstance().getGameDir().resolve("scroll/configuration.toml");
		if (!Files.exists(configurationPath)) {
			Path path = modContainer.findPath("configuration.toml").orElseThrow();
			Files.copy(path, configurationPath);
		}
		TomlParseResult result = Toml.parse(configurationPath);
		result.errors().forEach(error -> Scroll.LOGGER.error(error.toString()));
		return result;
	}

	@Override
	public boolean reload() {
		try {
			this.toml = this.load();
			return true;
		} catch (IOException exception) {
			Scroll.printException(exception, Scroll.languageFormat("scroll.reload.failed", "scroll/configuration.toml"));
			return false;
		}
	}

	/**
	 * @return All language configurations.
	 */
	public TomlTable getLanguageSection() {
		return toml.getTable("languages");
	}

	/**
	 * @return All command configurations.
	 */
	public TomlTable getCommandSection() {
		return toml.getTable("scroll.commands");
	}

	/**
	 * @return The configurations relating to Scroll itself.
	 */
	public TomlTable getScrollSection() {
		return toml.getTable("scroll");
	}

	/**
	 * @return true if scroll is in debug mode.
	 */
	public boolean isDebug() {
		return getScrollSection().getBoolean("scroll.debug", () -> false);
	}

}
