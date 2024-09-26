package org.scrolllang.scroll.elements.events;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.scrolllang.scroll.Scroll;
import org.scrolllang.scroll.context.CancellableContext;
import org.scrolllang.scroll.context.PlayerContext;
import org.scrolllang.scroll.context.RequiredReturnContext;
import org.scrolllang.scroll.context.WorldContext;
import org.scrolllang.scroll.documentation.annotations.Description;
import org.scrolllang.scroll.documentation.annotations.Name;
import org.scrolllang.scroll.documentation.annotations.Since;
import org.scrolllang.scroll.language.ScrollEvent;
import org.scrolllang.scroll.language.ScrollTriggerList;

import java.util.List;

@Name("Use/Right Click Block")
@Description({
        "Called when a player is right-clicking (\"using\") a block.",
        "The event is hooked in before the spectator check, so make sure",
        "to check for the player's game mode as well!"
})
@Since("1.21.1+dev.2")
public class EvtUseBlock extends ScrollEvent {

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

    public static class UseBlockContext extends RequiredReturnContext<ActionResult> implements PlayerContext, WorldContext, CancellableContext {

        private ActionResult result = ActionResult.PASS;

        private final PlayerEntity player;
        private final World world;
        private final Hand hand;
        private final BlockHitResult hitResult;

        public UseBlockContext(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
            super("use block");
            this.player = player;
            this.world = world;
            this.hand = hand;
            this.hitResult = hitResult;
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

        @Override
        public PlayerEntity getPlayer() {
            return player;
        }

        public World getWorld() {
            return world;
        }

        public Hand getHand() {
            return hand;
        }

        public BlockHitResult getHitResult() {
            return hitResult;
        }

    }

    static {
        Scroll.addEvent("right click on/use block", EvtUseBlock.class, UseBlockContext.class, "[player] (us(e|ing)|left( |-)[mouse( |-)]click[ing] [on]) [a] block");
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            UseBlockContext context = new UseBlockContext(player, world, hand, hitResult);
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
