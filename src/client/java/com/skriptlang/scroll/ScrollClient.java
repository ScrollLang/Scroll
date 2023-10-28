package com.skriptlang.scroll;

import org.jetbrains.annotations.Nullable;

import com.skriptlang.scroll.elements.ClientTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.kyori.adventure.platform.fabric.FabricClientAudiences;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

@Environment(EnvType.CLIENT)
public class ScrollClient implements ClientModInitializer {

	private static FabricClientAudiences ADVENTURE;
	private static MinecraftClient CLIENT;

	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			CLIENT = client;
		});
		ADVENTURE = FabricClientAudiences.of();
		Scroll.setAdventure(ADVENTURE);

		ClientTypes.register(Scroll.getRegistration());
		Scroll.register();
	}

	/**
	 * @return {@link FabricClientAudiences} reference being the client.
	 */
	public static FabricClientAudiences getClientAdventure() {
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
