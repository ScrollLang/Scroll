package com.scrolllang.scroll.context;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * An event that can be cancelled.
 */
public interface CancellableContext extends TriggerContext {

	/**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in either the client or on the server.
     *
     * @return true if this event is cancelled
     */
    public abstract boolean isCancelled();

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in either the client or on the server.
     *
     * @param cancel true if you wish to cancel this event
     */
    public abstract void setCancelled(boolean cancel);

}
