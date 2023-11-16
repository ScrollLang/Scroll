package com.skriptlang.scroll;

import com.skriptlang.scroll.elements.ServerTypes;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;

public class ScrollServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			Scroll.ADVENTURE = FabricServerAudiences.of(server);
			Scroll.SERVER = server;
			ServerTypes.register(Scroll.getRegistration());
			Scroll.register();
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			Scroll.ADVENTURE = null;
			Scroll.SERVER = null;
		});
	}

}
