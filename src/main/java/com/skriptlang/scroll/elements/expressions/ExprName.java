package com.skriptlang.scroll.elements.expressions;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.documentation.annotations.Description;
import com.skriptlang.scroll.documentation.annotations.Examples;
import com.skriptlang.scroll.documentation.annotations.Name;
import com.skriptlang.scroll.documentation.annotations.Since;
import com.skriptlang.scroll.language.Languaged;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

@Name("Name")
@Description({
	"Collects the names of players.",
	"Can be custom, display or normal name based from the player data.",
	"Note if you join in development mode, the name will not be set."
})
@Examples("print the player's name to the console")
@Since("1.0.0")
public class ExprName extends PropertyExpression<Object, Text> implements Languaged {

	static {
		Scroll.getRegistration().addPropertyExpression(ExprName.class, Text.class, "[(:display|:custom|:user|:entity)] name[s]", "entities");
	}

	private enum PatternType {
		DISPLAY(),
		CUSTOM(),
		ENTITY(),
		USER(Entity.class),
		NAME();

		private final Class<?>[] notSupportedClasses;

		PatternType(Class<?>... notSupportedClasses) {
			this.notSupportedClasses = notSupportedClasses;
		}

		public boolean supportsClassType(Class<?>... classes) {
			if (notSupportedClasses == null)
				return true;
			for (Class<?> clazz1 : this.notSupportedClasses) {
				for (Class<?> clazz2 : classes) {
					if (clazz1.equals(clazz2))
						return false;
				}
			}
			return true;
		}
	}

	private PatternType type = PatternType.NAME;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		List<String> marks = parseContext.getMarks();
		if (!marks.isEmpty())
			type = PatternType.valueOf(marks.get(0).toUpperCase(Locale.ENGLISH));

		Class<?> returnType = expressions[0].getReturnType();
		if (!type.supportsClassType(returnType)) {
			Type<?> type = TypeManager.getByClass(returnType).orElseThrow();
			String typeString = type.toString().toLowerCase(Locale.ENGLISH);
			error(parseContext, node("syntaxes.exprname.cannot", type.getBaseName(), typeString), node("syntaxes.exprname.cannot.tip", typeString));
			return false;
		}
		return super.init(expressions, matchedPattern, parseContext);
	}

	@Override
	public Text getProperty(Object owner) {
		if (owner instanceof PlayerEntity player && type == PatternType.USER)
			return Text.literal(player.getGameProfile().getName());
		if (owner instanceof Entity entity) {
			switch (type) {
				case CUSTOM:
					return entity.getCustomName();
				case DISPLAY:
					return entity.getDisplayName();
				case ENTITY:
					return Text.literal(entity.getEntityName());
				case NAME:
					return entity.getName();
				default:
					break;
			}
		}
		return null;
	}

	public Optional<Class<?>[]> acceptsChange(ChangeMode mode) {
		switch (mode) {
			case DELETE:
			case SET:
				if (type == PatternType.CUSTOM)
					return Optional.of(new Class<?>[] {Text.class});
				return Optional.empty();
			case ADD:
			case REMOVE:
			case REMOVE_ALL:
			case RESET:
			default:
				return Optional.empty();
		}
    }

	public void change(TriggerContext context, ChangeMode changeMode, Object[] objects) {
		Text text = (Text) objects[0];
		for (Object object : getOwner().getArray(context)) {
			if (object instanceof PlayerEntity player && type == PatternType.USER) {
				assert type == PatternType.CUSTOM;
				player.setCustomName(text);
			}
		}
	}

}
