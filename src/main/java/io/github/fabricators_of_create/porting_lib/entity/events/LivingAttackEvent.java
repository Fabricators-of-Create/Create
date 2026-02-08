package io.github.fabricators_of_create.porting_lib.entity.events;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class LivingAttackEvent {
	private final LivingEntity entity;
	private final DamageSource source;
	private boolean canceled;

	public LivingAttackEvent(LivingEntity entity, DamageSource source) {
		this.entity = entity;
		this.source = source;
	}

	public LivingEntity getEntity() {
		return entity;
	}

	public DamageSource getSource() {
		return source;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public boolean isCanceled() {
		return canceled;
	}
}
