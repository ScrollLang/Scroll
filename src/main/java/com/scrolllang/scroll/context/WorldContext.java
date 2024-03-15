package com.scrolllang.scroll.context;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import net.minecraft.world.World;

/**
 * Represents an event which contains a {@link World}.
 */
public interface WorldContext extends TriggerContext {

	public World getWorld();

}
