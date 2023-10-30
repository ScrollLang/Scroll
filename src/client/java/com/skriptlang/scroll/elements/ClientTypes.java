package com.skriptlang.scroll.elements;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;

@Environment(EnvType.CLIENT)
public class ClientTypes {

	public static void register(SkriptRegistration registration) {
		registration.newType(ClientPlayerEntity.class, "client player", "client[[ ]player]@s")
				.toStringFunction(player -> player.getDisplayName().toString())
				.defaultChanger(DefaultChangers.PLAYER)
				.register();
		registration.newType(FabricClientCommandSource.class, "client command source", "client[ ]source[ ]command@s")
				.toStringFunction(source -> source.getPlayer().getDisplayName().toString())
				.register();
	}

}
