package org.scrolllang.scroll.elements.events;

import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
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

@Name("Attack/Left Click Entity")
@Description({
        "Called when a player is left-clicking (\"attacking\") an entity.",
        "The event is hooked in before the spectator check, so make sure",
        "to check for the player's game mode as well!"
})
@Since("INSERT VERSION")
public class EvtAttackEntity extends ScrollEvent {

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

    public static class AttackEntityContext extends RequiredReturnContext<ActionResult> implements PlayerContext, WorldContext, CancellableContext {

        private ActionResult result = ActionResult.PASS;

        private final PlayerEntity player;
        private final EntityHitResult hitResult;
        private final Entity entity;
        private final World world;
        private final Hand hand;

        public AttackEntityContext(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
            super("attack entity");
            this.hitResult = hitResult;
            this.entity = entity;
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

        public Entity getEntity() {
            return entity;
        }

        @Override
        public PlayerEntity getPlayer() {
            return player;
        }

        public EntityHitResult getHitResult() {
            return hitResult;
        }

        public World getWorld() {
            return world;
        }

        public Hand getHand() {
            return hand;
        }

    }

    static {
        Scroll.addEvent("left click on/attack entity", EvtAttackEntity.class, AttackEntityContext.class, "[player] (attack[ing]|left( |-)[mouse( |-)]click[ing] [on]) [a[n]] entity");
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            AttackEntityContext context = new AttackEntityContext(player, world, hand, entity, hitResult);
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
