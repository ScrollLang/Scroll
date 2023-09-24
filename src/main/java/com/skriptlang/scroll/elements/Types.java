package com.skriptlang.scroll.elements;

import com.skriptlang.scroll.Scroll;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Types {

	public static void register() {
		SkriptRegistration registration = Scroll.getRegistration();
		registration.newType(Entity.class, "entity", "entit@ies")
				.toStringFunction(entity -> entity.getName().toString())
				.defaultChanger(DefaultChangers.ENTITY)
				.register();

		registration.newType(LivingEntity.class, "livingentity", "livingentit@ies")
				.toStringFunction(entity -> entity.getName().getString())
				.defaultChanger(DefaultChangers.ENTITY)
				.register();

		registration.newType(PlayerEntity.class, "player", "player@s")
				.toStringFunction(player -> player.getDisplayName().toString())
				.defaultChanger(DefaultChangers.PLAYER)
				.register();

		registration.newType(World.class, "world", "world@s")
				.toStringFunction(world -> world.getServer().getWorld(world.getRegistryKey()).asString())
				.register();

		registration.newType(Text.class, "text", "text@s")
				.toStringFunction(text -> text.getString())
				.register();

		registration.newType(Vec3d.class, "vec3d", "vec3d@s")
				.toStringFunction(vector -> vector.getX() + "," + vector.getY() + "," + vector.getZ())
				.register();
	}

}
