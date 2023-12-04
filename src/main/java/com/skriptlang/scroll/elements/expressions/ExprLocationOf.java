package com.skriptlang.scroll.elements.expressions;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.documentation.annotations.Description;
import com.skriptlang.scroll.documentation.annotations.Name;
import com.skriptlang.scroll.documentation.annotations.Since;
import com.skriptlang.scroll.objects.Location;

import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import net.minecraft.entity.Entity;

@Name("Location Of")
@Description("The location of things in a world")
@Since("1.0.0")
public class ExprLocationOf extends PropertyExpression<Entity, Location> {

	static {
		Scroll.getRegistration().addPropertyExpression(ExprLocationOf.class, Location.class, "location[s]", "entities");
	}

	@Override
	public Location getProperty(Entity entity) {
		return new Location(entity.getPos(), entity.getWorld());
	}

}
