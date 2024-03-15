package org.scrolllang.scroll.elements.expressions;

import java.util.Optional;

import org.scrolllang.scroll.Scroll;
import org.scrolllang.scroll.documentation.annotations.Description;
import org.scrolllang.scroll.documentation.annotations.Examples;
import org.scrolllang.scroll.documentation.annotations.Name;
import org.scrolllang.scroll.documentation.annotations.Since;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Name("ItemStack")
@Description("Collects items as an itemstack representing a stack or multiple of items.")
@Examples("set {_itemstack} to 5 of diamonds")
@Since("1.0.0")
public class ExprItemStack implements Expression<ItemStack> {

	static {
		Scroll.getRegistration().addExpression(ExprItemStack.class, ItemStack.class, false, "%number% [of] %items%");
	}

	private Expression<Number> count;
	private Expression<Item> items;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		count = (Expression<Number>) expressions[0];
		items = (Expression<Item>) expressions[1];
		return true;
	}

	@Override
	public ItemStack[] getValues(TriggerContext context) {
		Optional<Integer> count = this.count.getSingle(context).map(Number::intValue);
		if (!count.isPresent())
			return new ItemStack[0];
		Item[] items = this.items.getArray(context);
		ItemStack[] itemstacks = new ItemStack[items.length];
		for (int i = 0; i < items.length; i++)
			itemstacks[i] = new ItemStack(items[i], count.get());
		return itemstacks;
	}

	@Override
	public boolean isSingle() {
		return items.isSingle();
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		return count.toString(context, debug) + " " + Scroll.language("language.of") + " " + items.toString(context, debug);
	}

}
