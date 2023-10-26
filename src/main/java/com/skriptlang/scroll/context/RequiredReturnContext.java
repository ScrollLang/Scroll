package com.skriptlang.scroll.context;

import org.jetbrains.annotations.NotNull;

/**
 * An event that is required to return a result.
 */
public abstract class RequiredReturnContext<T> extends FabricContext {

	public RequiredReturnContext(String name) {
		super(name);
	}

	/**
	 * Return the required result of this event context.
	 * 
	 * @return the required result of this event context.
	 */
	@NotNull
    public abstract T getResult();

}
