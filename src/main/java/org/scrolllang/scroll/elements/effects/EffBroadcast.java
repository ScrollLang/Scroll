package org.scrolllang.scroll.elements.effects;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.scrolllang.scroll.Scroll;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

@Name("Broadcast")
@Description("Broadcasts a message to the console and players.")
@Examples({
	"broadcast \"Welcome %player% to the server!\"",
	"broadcast \"Woah! It's a message!\""
})
@Since("1.0.0")
public class EffBroadcast extends Effect implements Languaged {

	static {
		if (Scroll.isServerEnvironment())
			Scroll.getRegistration().addEffect(EffBroadcast.class, "broadcast %objects% [(to|in) %-worlds%]");
	}

	@Nullable
	private Expression<ServerWorld> worlds;
	private Expression<?> objects;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		objects = expressions[0];
		if (expressions.length > 1)
			worlds = (Expression<ServerWorld>) expressions[1];
		return true;
	}

	@Override
	protected void execute(TriggerContext context) {
		if (objects == null)
			return;
		List<ServerCommandSource> receivers = new ArrayList<>();
		if (worlds == null) {
			MinecraftServer server = Scroll.getMinecraftServer();
			server.getPlayerManager().getPlayerList().forEach(player -> receivers.add(player.getCommandSource()));
			receivers.add(server.getCommandSource());
		} else {
			for (ServerWorld world : worlds.getArray(context))
				world.getPlayers().forEach(player -> receivers.add(player.getCommandSource()));
		}

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
			receivers.forEach(receiver -> receiver.sendMessage(text));
		}
	}

	@Override
	public String toString(TriggerContext context, boolean debug) {
		return "broadcast " + objects.toString(context, debug) + (worlds == null ? "" : " to " + worlds.toString(context, debug));
	}

}
