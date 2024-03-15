package com.scrolllang.scroll.elements;

import java.util.Optional;

import com.scrolllang.scroll.Scroll;
import com.scrolllang.scroll.objects.Location;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class DefaultConverters {

	static {
		SkriptRegistration registration = Scroll.getRegistration();
		registration.addConverter(Location.class, Vec3d.class, location -> Optional.of(location.getVector()));
		registration.addConverter(Item.class, ItemStack.class, item -> Optional.of(new ItemStack(item)));
		registration.addConverter(String.class, Text.class, string -> Optional.of(Text.literal(string)));
		registration.addConverter(Text.class, String.class, text -> Optional.of(text.toString()));
	}

}
