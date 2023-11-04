package com.skriptlang.scroll.commands.arguments;

import org.jetbrains.annotations.Nullable;

/**
 * CommandArgument represents the runtime value of a {@link CommandParameter}
 */
public class CommandArgument<T> {

	private final CommandParameter<T> parameter;
	private final T value;

	public CommandArgument(CommandParameter<T> parameter, @Nullable T value) {
		this.parameter = parameter;
		this.value = value;
	}

	/**
	 * @return The {@link CommandParameter} relating to this argument.
	 */
	public CommandParameter<T> getParameterInfo() {
		return parameter;
	}

	public Class<T> getTypeClass() {
		return parameter.getPatternType().getType().getTypeClass();
	}

	public boolean isPresent() {
		return value != null;
	}

	@Nullable
	public T get() {
		return value;
	}

}
