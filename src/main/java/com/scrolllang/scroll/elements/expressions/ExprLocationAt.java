package com.scrolllang.scroll.elements.expressions;

import java.util.Optional;

import com.scrolllang.scroll.Scroll;
import com.scrolllang.scroll.documentation.annotations.Description;
import com.scrolllang.scroll.documentation.annotations.Name;
import com.scrolllang.scroll.documentation.annotations.Since;
import com.scrolllang.scroll.objects.Location;
import com.scrolllang.scroll.utils.collections.CollectionUtils;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Name("Location At")
@Description("The location in a world.")
@Since("1.0.0")
public class ExprLocationAt implements Expression<Location> {

	static {
		Scroll.getRegistration().addExpression(ExprLocationAt.class, Location.class, true, "[the] (location|position) [at] [\\(][x[ ][=[ ]]]%number%, [y[ ][=[ ]]]%number%, [and] [z[ ][=[ ]]]%number%[\\)] [[(in|of) [[the] world]] %world%]");
	}

	private Expression<Number> x, y, z;
	private Expression<World> world;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		x = (Expression<Number>) expressions[0];
		y = (Expression<Number>) expressions[1];
		z = (Expression<Number>) expressions[2];
		world = (Expression<World>) expressions[3];
		return true;
	}

	@Override
	public Location[] getValues(TriggerContext context) {
		Optional<Double> x = this.x.getSingle(context).map(Number::doubleValue);
		Optional<Double> y = this.y.getSingle(context).map(Number::doubleValue);
		Optional<Double> z = this.z.getSingle(context).map(Number::doubleValue);
		Optional<? extends World> world = this.world.getSingle(context);
		if (!x.isPresent() || !y.isPresent() || !z.isPresent() || !world.isPresent())
			return new Location[0];
		Vec3d vector = new Vec3d(x.get(), y.get(), z.get());
		return CollectionUtils.array(new Location(vector, world.get()));
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		return "location " + x.toString(context, debug) + ", " + x.toString(context, debug) + ", " + x.toString(context, debug) + " in " + world.toString(context, debug);
	}

}
