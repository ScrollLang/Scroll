package com.skriptlang.scroll.elements;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerTypes {

	public static void register(SkriptRegistration registration) {
		registration.newType(ServerPlayerEntity.class, "server player", "server[ ]player@s")
				.toStringFunction(player -> player.getDisplayName().toString())
				.defaultChanger(DefaultChangers.PLAYER)
				.register();
	}

}
