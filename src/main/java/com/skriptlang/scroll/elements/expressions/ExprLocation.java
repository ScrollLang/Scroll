package com.skriptlang.scroll.elements.expressions;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.documentation.annotations.Description;
import com.skriptlang.scroll.documentation.annotations.Name;
import com.skriptlang.scroll.documentation.annotations.Since;
import com.skriptlang.scroll.language.Languaged;

import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Name("Location")
@Description("The location of things in a world")
@Since("1.0.0")
public class ExprLocation extends PropertyExpression<Entity, Vec3d> implements Languaged {

	static {
		Scroll.getRegistration().addPropertyExpression(ExprLocation.class, Vec3d.class, "location[s]", "entities");
	}

	@Override
	public Vec3d getProperty(Entity entity) {
		return entity.getPos();
	}

}
