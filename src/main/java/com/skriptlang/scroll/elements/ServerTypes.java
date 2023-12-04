package com.skriptlang.scroll.elements;

import java.util.UUID;

import com.google.common.collect.Streams;
import com.skriptlang.scroll.Scroll;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class ServerTypes {

	public static void register(SkriptRegistration registration) {
		registration.newType(ServerPlayerEntity.class, "server player", "player@s")
				.toStringFunction(player -> player.getDisplayName().toString())
				.literalParser(input -> {
					try {
						UUID uuid = UUID.fromString(input);
						return Scroll.getMinecraftServer().getPlayerManager().getPlayer(uuid);
					} catch (Exception ignored) {
						return Scroll.getMinecraftServer().getPlayerManager().getPlayer(input);
					}
				})
				.defaultChanger(DefaultChangers.PLAYER)
				.register();

		registration.newType(World.class, "world", "world@s")
				.literalParser(input -> Streams.stream(Scroll.getMinecraftServer().getWorlds())
						.filter(world -> world.asString().equalsIgnoreCase(input))
						.findFirst()
						.orElse(null)
				)
				.toStringFunction(World::asString)
				.register();
	}

}
