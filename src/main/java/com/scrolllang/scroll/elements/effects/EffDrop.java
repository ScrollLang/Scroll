package com.scrolllang.scroll.elements.effects;

import com.scrolllang.scroll.Scroll;
import com.scrolllang.scroll.documentation.annotations.Description;
import com.scrolllang.scroll.documentation.annotations.Examples;
import com.scrolllang.scroll.documentation.annotations.Name;
import com.scrolllang.scroll.documentation.annotations.Since;
import com.scrolllang.scroll.objects.Location;

import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.World;

@Name("Drop")
@Description("Drops one or more items.")
@Examples({
	"on death of creeper:",
		"\tdrop 1 TNT"
})
@Since("1.0.0")
public class EffDrop extends Effect {

	static {
		if (Scroll.isServerEnvironment())
			Scroll.getRegistration().addEffect(EffDrop.class, "drop %itemstacks% [[at] %locations%] [:without velocity]"); //TODO replace [at] with %direction%
	}

	private Expression<Location> locations;
	private Expression<ItemStack> items;
	private boolean velocity;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		items = (Expression<ItemStack>) expressions[0];
		locations = (Expression<Location>) expressions[1];
		velocity = !parseContext.hasMark("without");
		return true;
	}

	@Override
	protected void execute(TriggerContext context) {
		for (Location location : locations.getArray(context)) {
			World world = location.getWorld();
			for (ItemStack itemstack : items.getArray(context)) {
				if (velocity) {
					ItemScatterer.spawn(world, location.getX(), location.getY(), location.getZ(), itemstack);
				} else {
					ItemEntity entity = new ItemEntity(world, location.getX(), location.getY(), location.getZ(), itemstack);
					world.spawnEntity(entity);
				}
			}
		}
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		return "drop " + items.toString(context, debug) + " at " + locations.toString(context, debug);
	}

}
