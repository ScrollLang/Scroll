package com.skriptlang.scroll.elements;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.context.PlayerContext;
import com.skriptlang.scroll.context.WorldContext;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.context.ContextValue.State;
import io.github.syst3ms.skriptparser.registration.context.ContextValue.Usage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class DefaultContextValues {

	static {
		SkriptRegistration registration = Scroll.getRegistration();
//		registration.addContextValue(PlayerContext.class, PlayerEntity.class, true, "[the] [[event]( |-)]player", event -> new PlayerEntity[] {event.getPlayer()});
//		registration.addContextValue(WorldContext.class, World.class, true, "[the] [[event]( |-)]world", event -> new World[] {event.getWorld()});
		registration.addContextType(PlayerContext.class, PlayerEntity.class, event -> event.getPlayer(), State.PRESENT, Usage.EXPRESSION_ONLY);
		registration.addContextType(WorldContext.class, World.class, event -> event.getWorld(), State.PRESENT, Usage.EXPRESSION_ONLY);
	}

}
