package org.scrolllang.scroll;

import org.jetbrains.annotations.Nullable;
import org.scrolllang.scroll.elements.ClientTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

@Environment(EnvType.CLIENT)
public class ScrollClient implements ClientModInitializer {

	private static MinecraftClientAudiences ADVENTURE;
	private static MinecraftClient CLIENT;

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			CLIENT = client;
		});
		ADVENTURE = MinecraftClientAudiences.of();
		Scroll.setAdventure(ADVENTURE);

		ClientTypes.register(Scroll.getRegistration());
		Scroll.register();
	}

	/**
	 * @return {@link MinecraftClientAudiences} reference being the client.
	 */
	public static MinecraftClientAudiences getClientAdventure() {
		return ADVENTURE;
	}

	/**
	 * @return {@link ClientPlayerEntity} that represents the client.
	 * Returns null if the client is not in-game.
	 */
	@Nullable
	public static ClientPlayerEntity getClientPlayer() {
		return CLIENT.player;
	}

	/**
	 * @return {@link MinecraftClient} client itself.
	 */
	public static MinecraftClient getClient() {
		return CLIENT;
	}

}
