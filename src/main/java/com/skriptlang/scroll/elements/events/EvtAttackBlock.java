package com.skriptlang.scroll.elements.events;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.skriptlang.scroll.Scroll;
import com.skriptlang.scroll.context.CancellableContext;
import com.skriptlang.scroll.context.PlayerContext;
import com.skriptlang.scroll.context.RequiredReturnContext;
import com.skriptlang.scroll.context.WorldContext;
import com.skriptlang.scroll.documentation.annotations.Description;
import com.skriptlang.scroll.documentation.annotations.Name;
import com.skriptlang.scroll.documentation.annotations.Since;
import com.skriptlang.scroll.language.ScrollEvent;
import com.skriptlang.scroll.language.ScrollTriggerList;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Name("Attack/Left Click Block")
@Description({
	"Called when a player is left-clicking (\"attacking\") a block.",
	"The event is hooked in before the spectator check, so make sure",
	"to check for the player's game mode as well!",
// TODO add this description below to the action result expression.
//	"",
//	"On the logical client, the action result values have the following meaning:",
//		"\tSUCCESS cancels further processing, causes a hand swing, and sends a packet to the server.\r\n"
//	+ "    CONSUME cancels further processing, and sends a packet to the server. It does NOT cause a hand swing.\r\n"
//	+ "    PASS falls back to further processing.\r\n"
//	+ "    FAIL cancels further processing and does not send a packet to the server.\r\n"
//	+ "\r\n"
//	+ "On the logical server, the return values have the following meaning:\r\n"
//	+ "\r\n"
//	+ "    PASS falls back to further processing.\r\n"
//	+ "    Any other value cancels further processing.\r\n"
//	+ ""
})
@Since("1.0.0")
public class EvtAttackBlock extends ScrollEvent {

	// Required Context triggers methods. Start.
	private final static ScrollTriggerList triggers = new ScrollTriggerList();

	public static List<Trigger> getTriggersList() {
		return triggers.getTriggers();
	}

	@Override
	public @NotNull ScrollTriggerList getTriggers() {
		return triggers;
	}
	// Required Context triggers methods. End.

	public static class AttackBlockContext extends RequiredReturnContext<ActionResult> implements PlayerContext, WorldContext, CancellableContext {

		private ActionResult result = ActionResult.PASS;

		private final PlayerEntity player;
		private final Direction direction;
		private final BlockPos position;
		private final World world;
		private final Hand hand;

		public AttackBlockContext(PlayerEntity player, World world, Hand hand, BlockPos position, Direction direction) {
			super("attack block");
			this.direction = direction;
			this.position = position;
			this.player = player;
			this.world = world;
			this.hand = hand;
		}

		@Override
		public void setCancelled(boolean cancel) {
			result = cancel ? ActionResult.FAIL : ActionResult.PASS;
		}

		@Override
		public boolean isCancelled() {
			return result == ActionResult.FAIL;
		}

		public void setResult(@NotNull ActionResult result) {
			this.result = result;
		}

		@Override
		public @NotNull ActionResult getResult() {
			return result;
		}

		public Direction getDirection() {
			return direction;
		}

		@Override
		public PlayerEntity getPlayer() {
			return player;
		}

		public BlockPos getPosition() {
			return position;
		}

		public World getWorld() {
			return world;
		}

		public Hand getHand() {
			return hand;
		}

	}

	static {
		// TODO add support for itemtypes.
		Scroll.addEvent("left click on/attack block", EvtAttackBlock.class, AttackBlockContext.class, "[player] (attack[ing]|left( |-)[mouse( |-)]click[ing] [on]) [a] block");
		AttackBlockCallback.EVENT.register((player, world, hand, position, direction) -> {
			AttackBlockContext context = new AttackBlockContext(player, world, hand, position, direction);
			runTriggers(triggers, context);
			return context.getResult();
		});
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
		return true;
	}

	@Override
	public boolean check(TriggerContext context) {
		return true;
	}

}
