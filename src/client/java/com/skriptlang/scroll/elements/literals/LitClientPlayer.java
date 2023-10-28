package com.skriptlang.scroll.elements.literals;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.ScrollClient;
import com.skriptlang.scroll.documentation.annotations.Description;
import com.skriptlang.scroll.documentation.annotations.Name;
import com.skriptlang.scroll.documentation.annotations.Since;

import io.github.syst3ms.skriptparser.lang.SimpleLiteral;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;

@Name("Client Player")
@Description("Return the actual ClientPlayerEntity that represents the player of the client.")
@Environment(EnvType.CLIENT)
@Since("1.0.0")
public class LitClientPlayer extends SimpleLiteral<ClientPlayerEntity> {

	static {
		if (!Scroll.isServerEnvironment())
			Scroll.getRegistration().addExpression(LitClientPlayer.class, ClientPlayerEntity.class, true, "[the] client player [entity]", "[the] player of [the] client");
	}

	public LitClientPlayer() {
		super(ClientPlayerEntity.class, ScrollClient.getClientPlayer());
	}

}
