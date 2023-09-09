package com.skriptlang.scroll.elements;

import com.skriptlang.scroll.Scroll;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.minecraft.entity.player.PlayerEntity;

public class Types {

	static {
		SkriptRegistration registration = Scroll.getRegistration();
		registration.newType(PlayerEntity.class, "player", "player@s")
				.toStringFunction(player -> player.getName().getString())
				.defaultChanger(DefaultChangers.PLAYER)
				.register();
	}

}
