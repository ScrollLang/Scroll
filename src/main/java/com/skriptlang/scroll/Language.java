package com.skriptlang.scroll;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.tomlj.TomlTable;

import com.skriptlang.scroll.language.Reloadable;
import com.skriptlang.scroll.utils.FileUtils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * Main class for loading languages from the properties files.
 */
public class Language implements Reloadable {

	private static final Path languagesFolder = FileUtils.getOrCreateDir(FabricLoader.getInstance().getGameDir().resolve("scroll/languages"));
	private static final Pattern OTHER_NODE_PATTERN = Pattern.compile("\\{(.*?)\\}");
	private final ModContainer modContainer;

	private Properties properties;
	private Properties english;

	private String language;

	Language(Scroll instance) throws FileNotFoundException, IOException {
		TomlTable languageConfigurations = instance.getConfiguration().getLanguageSection();
		this.language = languageConfigurations.getString("languages.language", () -> "english");
		this.modContainer = instance.getModContainer();
		this.properties = this.loadLanguage();
		this.english = this.loadEnglish();
	}

	/**
	 * Method that loads the language properties.
	 * 
	 * @return The parsed Properties file.
	 * @throws IOException 
	 * @throws FileNotFoundException if the file could not be found.
	 */
	Properties loadLanguage() throws FileNotFoundException, IOException {
		Files.list(modContainer.findPath("languages").orElseThrow())
				.forEach(path -> {
					Path language = languagesFolder.resolve(path.getFileName().toString());
					if (!Files.exists(language)) {
						try {
							Files.copy(path, language);
						} catch (IOException exception) {
							Scroll.printException(exception, Scroll.languageFormat("language.failed.copy", path.getFileName()));
						}
					}
				});
		Path path;
		try {
			path = languagesFolder.resolve(language + ".properties");
		} catch (InvalidPathException exception) {
			path = languagesFolder.resolve("english.properties");
			Scroll.LOGGER.error(Scroll.languageFormat("language.not.found", language + ".properties"));
		}
		Properties properties = new Properties();
		properties.load(new FileInputStream(path.toFile()));
		return properties;
	}

	/**
	 * Loads the english.properites language file for usage in the {@link #getOrDefaultEnglish(String)} method.
	 * 
	 * @return The parsed Properties file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	Properties loadEnglish() throws FileNotFoundException, IOException, InvalidPathException {
		Path path = languagesFolder.resolve("english.properties");
		Properties properties = new Properties();
		properties.load(new FileInputStream(path.toFile()));
		return properties;
	}

	/**
	 * Reloads the currently selected language file.
	 * Will read the main configuration node before to see if the language has changed.
	 */
	@Override
	public boolean reload() {
		TomlTable languageConfigurations = Scroll.CONFIGURATION.getLanguageSection();
		this.language = languageConfigurations.getString("languages.language", () -> "english");
		try {
			this.properties = this.loadLanguage();
			this.english = this.loadEnglish();
			return true;
		} catch (InvalidPathException | IOException exception) {
			Scroll.printException(exception, Scroll.languageFormat("language.reload.fail", "scroll/configuration.toml"));
			return false;
		}
	}

	/**
	 * @return Get the language set in the configuration.
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * The English properties file is always to be updated.
	 * So if a language file does not contain the correct key, this method
	 * will default the value to the English value. Typically because outdated file.
	 * 
	 * @param key The key to search the properties file for.
	 * @return The String value from the defined language file otherwise from English language file.
	 */
	public String getOrDefaultEnglish(String key) {
		String value = Optional.ofNullable(properties.getProperty(key)).orElse(english.getProperty(key));
		if (value == null)
			return key;
		Matcher matcher = OTHER_NODE_PATTERN.matcher(value);
		value = matcher.replaceAll(match -> {
			String group = match.group(1);
			if (group.equalsIgnoreCase(key)) // Avoid recursion.
				return group;
			return getOrDefaultEnglish(group);
		});
		return value;
	}

	/**
	 * Returns a key from the language file, null if not found.
	 * 
	 * @param key The key to lookup in the language file.
	 * @return The value of the key if found, otherwise will return null.
	 */
	@Nullable
	public String get(String key) {
		return properties.getProperty(key);
	}

	/**
	 * Returns a key from the language file, otherwise uses the parameter provided for default value.
	 * 
	 * @param key The key to lookup in the language file.
	 * @param defaultValue The default value that will be returned if no key was present in the language file.
	 * @return The value of the key if found, otherwise the default value parameter.
	 */
	public String get(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

}
