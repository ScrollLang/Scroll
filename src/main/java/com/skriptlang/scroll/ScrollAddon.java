package com.skriptlang.scroll;

import java.io.IOException;
import java.net.URISyntaxException;

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
	 * Called when your addon gets initalized.
	 * This is called before Scroll has registered syntaxes.
	 */
	protected abstract void initAddon();

	/**
	 * Called when you can safely start registering syntaxes.
	 */
	protected abstract void startRegistration();

	/**
	 * @return The name of this ScrollAddon.
	 */
	public String getName() {
		return name;
	}

}
