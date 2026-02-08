package net.neoforged.neoforge.event.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BlockEvent {
	public static class BreakEvent {
		private final Player player;

		public BreakEvent(Player player) {
			this.player = player;
		}

		public Player getPlayer() {
			return player;
		}
	}

	public static class EntityPlaceEvent {
		private final Entity entity;

		public EntityPlaceEvent(Entity entity) {
			this.entity = entity;
		}

		public Entity getEntity() {
			return entity;
		}

		public Level getLevel() {
			return entity.level();
		}
	}
}
