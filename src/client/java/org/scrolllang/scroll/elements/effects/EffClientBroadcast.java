package org.scrolllang.scroll.elements.effects;

import org.scrolllang.scroll.Scroll;
import org.scrolllang.scroll.ScrollClient;
import org.scrolllang.scroll.documentation.annotations.Description;
import org.scrolllang.scroll.documentation.annotations.Examples;
import org.scrolllang.scroll.documentation.annotations.Name;
import org.scrolllang.scroll.documentation.annotations.Since;
import org.scrolllang.scroll.language.Languaged;
import org.scrolllang.scroll.utils.collections.CollectionUtils;

import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.TypeManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

@Name("Client Broadcast")
@Description("Broadcasts a message to the console and players.")
@Examples({
	"broadcast \"Welcome %client player% to single player!\"",
	"broadcast \"Woah! It's a message!\""
})
@Environment(EnvType.CLIENT)
@Since("1.0.0")
public class EffClientBroadcast extends Effect implements Languaged {

	static {
		Scroll.getRegistration().addEffect(EffClientBroadcast.class, "broadcast %objects%");
	}

	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		objects = expressions[0];
		return true;
	}

	@Override
	protected void execute(TriggerContext context) {
		ClientPlayerEntity player = ScrollClient.getClientPlayer();
		if (player == null)
			return;
		for (Object object : objects.getArray(context)) {
			Text text;
			if (object instanceof Text) {
				text = (Text) object;
			} else if (object instanceof Component) {
				text = Scroll.getAdventure().toNative((TextComponent) object);
			} else {
				String message = object instanceof String ? (String) object : TypeManager.toString(CollectionUtils.array(object));
				text = Text.literal(message);
			}
			if (text == null)
				continue;
			player.sendMessage(text);
		}
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		return "broadcast " + objects.toString(context, debug);
	}

}
