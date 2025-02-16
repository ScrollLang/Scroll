package org.scrolllang.scroll;

import org.scrolllang.scroll.elements.ServerTypes;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences;

public class ScrollServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Scroll.ADVENTURE = MinecraftClientAudiences.of();
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
