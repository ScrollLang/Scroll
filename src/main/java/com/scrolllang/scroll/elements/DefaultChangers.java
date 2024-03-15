package com.scrolllang.scroll.elements;

import org.jetbrains.annotations.Nullable;

import com.scrolllang.scroll.utils.collections.CollectionUtils;

import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import io.github.syst3ms.skriptparser.types.changers.Changer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;

public class DefaultChangers {

	public final static Changer<Entity> ENTITY = new Changer<Entity>() {

		@Override
		public Class<?>[] acceptsChange(ChangeMode mode) {
			switch (mode) {
// TODO
//				case ADD:
//					return CollectionUtils.array(ItemType[].class, Inventory.class, Experience[].class);
//				case DELETE:
//					return CollectionUtils.array();
//				case REMOVE:
//					return CollectionUtils.array(PotionEffectType[].class, ItemType[].class, Inventory.class);
//				case REMOVE_ALL:
//					return CollectionUtils.array(PotionEffectType[].class, ItemType[].class);
//				case SET:
//				case RESET: // REMIND reset entity? (unshear, remove held item, reset weapon/armour, ...)
				default:
					return null;
			}
		}

		@Override
		public void change(Entity[] entities, Object[] delta, ChangeMode mode) {
			// TODO
		}

	};

	public final static Changer<PlayerEntity> PLAYER = new Changer<PlayerEntity>() {

		@Override
		@Nullable
		public Class<? extends Object>[] acceptsChange(ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return null;
			return ENTITY.acceptsChange(mode);
		}
	
		@Override
		public void change(PlayerEntity[] players, @Nullable Object[] delta, ChangeMode mode) {
			ENTITY.change(players, delta, mode);
		}

	};

	public final static Changer<Entity> NON_LIVING_ENTITY = new Changer<Entity>() {
		@Override
		@Nullable
		public Class<Object>[] acceptsChange(ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return CollectionUtils.array();
			return null;
		}

		@Override
		public void change(Entity[] entities, @Nullable Object[] delta, ChangeMode mode) {
			assert mode == ChangeMode.DELETE;
			for (Entity entity : entities) {
				if (entity instanceof PlayerEntity)
					continue;
				entity.remove(RemovalReason.DISCARDED);
			}
		}
	};

}
