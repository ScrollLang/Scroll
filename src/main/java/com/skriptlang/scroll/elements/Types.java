package com.skriptlang.scroll.elements;

import com.skriptlang.scroll.Scroll;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Types {

	public static void register(SkriptRegistration registration) {
		registration.newType(Entity.class, "entity", "entit@y@ies")
				.toStringFunction(entity -> entity.getName().toString())
				.defaultChanger(DefaultChangers.ENTITY)
				.register();

		registration.newType(LivingEntity.class, "livingentity", "livingEntit@y@ies")
				.toStringFunction(entity -> entity.getName().getString())
				.defaultChanger(DefaultChangers.ENTITY)
				.register();

		registration.newType(Text.class, "text", "text@s")
				.literalParser(input -> {
					MiniMessage miniMessage = MiniMessage.miniMessage();
					FabricAudiences adventure = Scroll.getAdventure();
					return adventure.toNative(miniMessage.deserialize(input));
				})
				.toStringFunction(text -> text.getString())
				.register();

		registration.newType(Vec3d.class, "vec3d", "vec3d@s")
				.toStringFunction(vector -> vector.getX() + "," + vector.getY() + "," + vector.getZ())
				.register();

		registration.newType(CommandSource.class, "commandsource", "commandSource@s").register();
		registration.newType(ServerCommandSource.class, "servercommandsource", "serverCommandSource@s")
				.toStringFunction(source -> {
					if (source.getPlayer() != null)
						return source.getPlayer().getName().toString();
					if (source.getServer() != null)
						return "console";
					if (source.getEntity() != null)
						return source.getEntity().getName().toString();
					return source.toString();
				})
				.register();

		registration.newType(World.class, "world", "world@s")
				.toStringFunction(world -> {
					if (!Scroll.isServerEnvironment())
						return world.toString();
					return world.getServer().getWorld(world.getRegistryKey()).asString();
				})
				.register();
	}

}
