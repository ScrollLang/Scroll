package org.scrolllang.scroll.elements.effects;

import org.jetbrains.annotations.Nullable;
import org.scrolllang.scroll.Scroll;
import org.scrolllang.scroll.documentation.annotations.Description;
import org.scrolllang.scroll.documentation.annotations.Examples;
import org.scrolllang.scroll.documentation.annotations.Name;
import org.scrolllang.scroll.documentation.annotations.Since;

import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Name("Equip")
@Description("Equips or unequips an entity with some given armor. This will replace any armor that the entity is wearing.")
@Examples({
	"equip player with diamond helmet",
	"equip player with all diamond armor",
	"unequip diamond chestplate from player",
	"unequip all armor from player",
	"unequip player's armor"
})
@Since("1.0.0")
public class EffEquip extends Effect {

	static {
//		if (Scroll.isServerEnvironment())
//			Scroll.getRegistration().addEffect(EffEquip.class, 
//					"equip [%livingentities%] with %itemstacks%",
//					"make %livingentities% wear %itemstacks%",
//					"unequip %itemstacks% [from %livingentities%]",
//					"unequip %livingentities%'[s] (armor|equipment)"
//			);
	}

	private Expression<LivingEntity> entities;

	@Nullable
	private Expression<ItemStack> itemstacks;
	private boolean equip = true;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		if (matchedPattern == 0 || matchedPattern == 1) {
			entities = (Expression<LivingEntity>) expressions[0];
			itemstacks = (Expression<ItemStack>) expressions[1];
		} else if (matchedPattern == 2) {
			itemstacks = (Expression<ItemStack>) expressions[0];
			entities = (Expression<LivingEntity>) expressions[1];
			equip = false;
		} else if (matchedPattern == 3) {
			entities = (Expression<LivingEntity>) expressions[0];
			equip = false;
		}
		return true;
	}

	@Override
	protected void execute(TriggerContext context) {
		for (LivingEntity entity : entities.getArray(context)) {
			
		}
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		if (equip) {
			return "equip " + entities.toString(context, debug) + " with " + itemstacks.toString(context, debug);
		} else if (itemstacks != null) {
			return "unequip " + itemstacks.toString(context, debug) + " from " + entities.toString(context, debug);
		} else {
			return "unequip " + entities.toString(context, debug) + "'s equipment";
		}
	}

}
