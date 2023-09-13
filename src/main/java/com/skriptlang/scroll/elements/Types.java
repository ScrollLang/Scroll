package com.skriptlang.scroll.elements;

import com.skriptlang.scroll.Scroll;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class Types {

	public static void register() {
		SkriptRegistration registration = Scroll.getRegistration();
		registration.newType(PlayerEntity.class, "player", "player@s")
				.toStringFunction(player -> player.getName().getString())
				.defaultChanger(DefaultChangers.PLAYER)
				.register();

		registration.newType(Text.class, "text", "text@s")
				.toStringFunction(text -> text.getString())
				.register();
	}

}
