package org.scrolllang.scroll.elements.events;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

@Name("Use/Right Click Entity")
@Description({
        "Called when a player is right-clicking (\"using\") an entity.",
        "The event is hooked in before the spectator check, so make sure",
        "to check for the player's game mode as well!"
})
@Since("INSERT VERSION")
public class EvtUseEntity extends ScrollEvent {

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

    public static class UseEntityContext extends RequiredReturnContext<ActionResult> implements PlayerContext, WorldContext, CancellableContext {

        private ActionResult result = ActionResult.PASS;

        private final PlayerEntity player;
        private final World world;
        private final Hand hand;
        @Nullable private final EntityHitResult hitResult;
        private final Entity entity;

        public UseEntityContext(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
            super("use entity");
            this.player = player;
            this.world = world;
            this.hand = hand;
            this.hitResult = hitResult;
            this.entity = entity;
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

        public Entity getEntity() {
            return entity;
        }

        @Nullable
        public EntityHitResult getHitResult() {
            return hitResult;
        }

    }

    static {
        Scroll.addEvent("right click on/use entity", EvtUseEntity.class, UseEntityContext.class, "[player] (us(e|ing)|left( |-)[mouse( |-)]click[ing] [on]) [a[n]] entity");
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            UseEntityContext context = new UseEntityContext(player, world, hand, entity, hitResult);
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
