package org.scrolllang.scroll.elements;

import java.util.Locale;

import org.scrolllang.scroll.Scroll;
import org.scrolllang.scroll.objects.Location;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

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
				.toStringFunction(Text::getString)
				.register();

		registration.newType(Vec3d.class, "vec3d", "vec3d@s")
				.toStringFunction(vector -> vector.getX() + "," + vector.getY() + "," + vector.getZ())
				.register();

		registration.newType(PlayerEntity.class, "player entity", "player entit@y@ies")
				.toStringFunction(player -> player.getDisplayName().toString())
				.register();

		registration.newType(ItemStack.class, "itemstack", "itemStack@s")
				.toStringFunction(itemstack -> itemstack.getCount() + " " + Scroll.language("language.of") + " " + Registries.ITEM.getId(itemstack.getItem()).getNamespace())
				.register();

		registration.newType(Location.class, "location", "location@s")
				.toStringFunction(location -> location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getWorld().asString())
				.register();

		registration.newType(CommandSource.class, "commandsource", "commandSource@s").register();
		registration.newType(ServerCommandSource.class, "servercommandsource", "serverCommandSource@s")
				.toStringFunction(source -> {
					if (source.getPlayer() != null)
						return source.getPlayer().getName().toString();
					if (source.getServer() != null)
						return Scroll.language("general.console");
					if (source.getEntity() != null)
						return source.getEntity().getName().toString();
					return source.toString();
				})
				.register();

		registration.newType(Item.class, "item", "item@s")
				.literalParser(input -> {
					input = input.replaceAll("\s", "_").toUpperCase(Locale.ENGLISH);
					Registry<Item> itemRegistry = Registries.ITEM;
					Identifier identifier = new Identifier(input);
					if (!itemRegistry.containsId(identifier))
						return null;
					return itemRegistry.get(identifier);
				})
				.toStringFunction(item -> Registries.ITEM.getId(item).getNamespace())
				.register();
	}

}
