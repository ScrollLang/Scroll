package com.skriptlang.scroll.elements;

import java.util.Optional;

import com.skriptlang.scroll.Scroll;

import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import net.minecraft.text.Text;

public class DefaultConverters {

	static {
		SkriptRegistration registration = Scroll.getRegistration();
		registration.addConverter(Text.class, String.class, text -> Optional.of(text.toString()));
	}

}
