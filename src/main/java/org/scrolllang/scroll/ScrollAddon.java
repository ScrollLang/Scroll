package org.scrolllang.scroll;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import io.github.syst3ms.skriptparser.util.FileUtils;

/**
 * Class used to register an addon for Scroll.
 */
public abstract class ScrollAddon {

	private final String name;

	protected ScrollAddon(String name) {
		this.name = name;
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

	/**
	 * @return The name of this ScrollAddon.
	 */
	public String getName() {
		return name;
	}

}
