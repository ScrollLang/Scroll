package com.skriptlang.scroll.commands.arguments;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.exceptions.ScrollAPIException;

import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.TypeManager;

public class CommandParameter<T> {

	private final PatternType<T> type;
	private final String identifier;
	private final boolean optional;
	private int index;

	protected CommandParameter(PatternType<T> type, @Nullable String identifier, boolean optional) {
		this.identifier = identifier;
		this.optional = optional;
		this.type = type;
	}

	/**
	 * Parses user input as a Parameter.
	 * 
	 * @param input The user input used in a command strucutre.
	 * @return The parsed Parameter based on the user input.
	 * @throws ScrollAPIException if there is no type for the user input. Used for catching in the command structure.
	 */
	public static CommandParameter<?> parse(String input) throws ScrollAPIException {
		boolean optional = false;
		if (input.startsWith("[") && input.endsWith("]")) {
			input = input.replaceFirst("[", "").replace("]", "");
			optional = true;
		}
		String typeInput = input;
		String identifier = null;
		if (input.contains(":")) {
			String[] split = input.split(":", 2);
			identifier = split[0];
			typeInput = split[1];
		}
		Optional<PatternType<?>> type = TypeManager.getPatternType(typeInput);
		if (!type.isPresent())
			throw new ScrollAPIException(Scroll.languageFormat("scripts.commands.register.parameters.no.type", typeInput));
		if (!type.get().getType().getLiteralParser().isPresent())
			throw new ScrollAPIException(Scroll.languageFormat("scripts.commands.register.parameters.no.parser", typeInput));
		return new CommandParameter<>(type.get(), identifier, optional);
	}

	/**
	 * @return The parsed PatternType for this parameter. Use {@link PatternType#isSingle()} for plurality.
	 */
	public PatternType<T> getPatternType() {
		return type;
	}

	/**
	 * @return The identifier used to collect this parameter.
	 */
	public String getIdentifier() {
		return Optional.ofNullable(identifier)
				.orElse(type.toString() + (index != -1 ? "-" + index : ""));
	}

	/**
	 * @return If this parameter is optional.
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @return the type index of this command parameter if there is no identifier.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Sets the type index of this parameter. Used if there are multiple types and no identifier.
	 * 
	 * @param index The type index of this parameter.
	 */
	public void setIndex(int index) {
		if (index != -1)
			throw new IllegalStateException("The command parameter index has already been set.");
		this.index = index;
	}

}
