package io.github.fabricators_of_create.porting_lib.entity.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;

public class EntityEvents {
	public static class Size {
		private final Entity entity;
		private EntityDimensions newSize;

		public Size(Entity entity, EntityDimensions newSize) {
			this.entity = entity;
			this.newSize = newSize;
		}

		public Entity getEntity() {
			return entity;
		}

		public EntityDimensions getNewSize() {
			return newSize;
		}

		public void setNewSize(EntityDimensions newSize) {
			this.newSize = newSize;
		}
	}
}
