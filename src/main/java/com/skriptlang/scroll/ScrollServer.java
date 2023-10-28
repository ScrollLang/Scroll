package com.skriptlang.scroll;

import com.skriptlang.scroll.elements.ServerTypes;

import net.fabricmc.api.DedicatedServerModInitializer;

public class ScrollServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {
		ServerTypes.register(Scroll.getRegistration());
		Scroll.register();
	}

}
