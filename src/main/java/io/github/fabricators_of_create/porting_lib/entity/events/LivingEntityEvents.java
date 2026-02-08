package io.github.fabricators_of_create.porting_lib.entity.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class LivingEntityEvents {
	public static class ChangeTarget {
		public static class ChangeTargetEvent {
			private final Entity entity;
			private final LivingEntity originalTarget;
			private boolean canceled;

			public ChangeTargetEvent(Entity entity, LivingEntity originalTarget) {
				this.entity = entity;
				this.originalTarget = originalTarget;
			}

			public Entity getEntity() {
				return entity;
			}

			public LivingEntity getOriginalTarget() {
				return originalTarget;
			}

			public void setCanceled(boolean canceled) {
				this.canceled = canceled;
			}

			public boolean isCanceled() {
				return canceled;
			}
		}
	}
}
