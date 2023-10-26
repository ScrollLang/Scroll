package com.skriptlang.scroll.context;

import io.github.syst3ms.skriptparser.lang.TriggerContext;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Represents an event which contains a {@link PlayerEntity}.
 */
public interface PlayerContext extends TriggerContext {

	public PlayerEntity getPlayer();

}
