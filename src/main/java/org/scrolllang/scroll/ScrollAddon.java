package org.scrolllang.scroll;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.syst3ms.skriptparser.util.FileUtils;

/**
 * Class used to register an addon for Scroll.
 */
public abstract class ScrollAddon {

	private final Logger logger;
	private final int priority;
	private final String name;

	/**
	 * Registers a new ScrollAddon with a priority of 100.
	 * The lowest priotiy to 0 will be loaded first.
	 * 
	 * @param name The name of the addon being registered.
	 */
	protected ScrollAddon(String name) {
		this(name, 100);
	}

	/**
	 * Registers a new ScrollAddon with a priority of 100.
	 * The lowest priotiy to 0 will be loaded first.
	 * 
	 * @param name The name of the addon being registered.
	 * @param priority The priority of the addon.
	 */
	protected ScrollAddon(String name, int priority) {
		this.logger = LoggerFactory.getLogger(name);
		this.priority = priority < 0 ? 0 : priority;
		this.name = name;
		if (Scroll.ADDONS.stream().anyMatch(addon -> addon.getName().equals(name)))
			throw new IllegalStateException("Addon name '" + name + "'' is already registered.");

		Scroll.ADDONS.add(this);
	}

	/**
	 * Utility method to statically load syntaxes classes in packages.
	 * Setting mainPackage to me.example.addon and the subPackage to elements
	 * Will statically initalize all classes in me.example.addon.elements.
	 * 
	 * @param mainPackage The main package to start from.
	 * @param subPackages The sub packages within the main package to initalize.
	 */
	protected void loadClasses(String mainPackage, String... subPackages) {
		try {
			FileUtils.loadClasses(FileUtils.getJarFile(getClass()), mainPackage, subPackages);
		} catch (IOException | URISyntaxException exception) {
			Scroll.printException(exception, "Failed to initalize classes from addon " + name + "");
		}
	}

	/**
	 * Called when you can safely start registering syntaxes.
	 * Should only be done on mod initalization. Not later.
	 * 
	 * @param registration The registration to use to add syntaxes.
	 */
	protected abstract void startRegistration(ScrollRegistration registration);

	/**
	 * The data folder where you addon should store configurations.
	 * Directory may not exist. You need to make directories.
	 * 
	 * @return Path to the data folder of this addon.
	 */
	public Path getDataFolder() {
		return Scroll.getInstance().getAddonsFolder().resolve(name);
	}

	public void info(String message) {
		logger.info(message);
	}

	public void warn(String message) {
		logger.warn(message);
	}

	public void error(String message) {
		logger.error(message);
	}

	/**
	 * @return The logger for this ScrollAddon.
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @return The name of this ScrollAddon.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The load priority of this addon.
	 * The lowest priotiy to 0 will be loaded first.
	 * 
	 * @return The priority of this addon.
	 */
	public int getPriority() {
		return priority;
	}

}
