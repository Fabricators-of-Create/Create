package com.simibubi.create.foundation.mixin.client;

import java.lang.ref.Reference;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.fabricators_of_create.porting_lib.block.CustomRunningEffectsBlock;

import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionCollider;
import com.simibubi.create.content.contraptions.ContraptionHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityContraptionInteractionMixin {
	@Unique
	private Stream<AbstractContraptionEntity> create$getIntersectionContraptionsStream() {
		Entity self = (Entity) (Object) this;
		return ContraptionHandler.loadedContraptions.get(self.level())
			.values()
			.stream()
			.map(Reference::get)
			.filter(cEntity -> cEntity != null && cEntity.collidingEntities.containsKey(self));
	}

	@Unique
	private Set<AbstractContraptionEntity> create$getIntersectingContraptions() {
		Entity self = (Entity) (Object) this;
		Set<AbstractContraptionEntity> contraptions = create$getIntersectionContraptionsStream().collect(Collectors.toSet());

		contraptions.addAll(self.level().getEntitiesOfClass(AbstractContraptionEntity.class, self.getBoundingBox()
			.inflate(1f)));
		return contraptions;
	}

	@Unique
	private void create$forCollision(Vec3 worldPos, TriConsumer<Contraption, BlockState, BlockPos> action) {
		create$getIntersectingContraptions().forEach(cEntity -> {
			Vec3 localPos = ContraptionCollider.worldToLocalPos(worldPos, cEntity);

			BlockPos blockPos = BlockPos.containing(localPos);
			Contraption contraption = cEntity.getContraption();
			StructureTemplate.StructureBlockInfo info = contraption.getBlocks()
				.get(blockPos);

			if (info != null) {
				BlockState blockstate = info.state();
				action.accept(contraption, blockstate, blockPos);
			}
		});
	}

	// involves client-side view bobbing animation on contraptions
	@Inject(method = "move", at = @At(value = "TAIL"))
	private void create$onMove(MoverType mover, Vec3 movement, CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		if (!self.level().isClientSide)
			return;
		if (self.onGround())
			return;
		if (self.isPassenger())
			return;

		Vec3 worldPos = self.position().add(0, -0.2, 0);
		boolean onAtLeastOneContraption = create$getIntersectionContraptionsStream().anyMatch(cEntity -> {
			Vec3 localPos = ContraptionCollider.worldToLocalPos(worldPos, cEntity);

			BlockPos blockPos = BlockPos.containing(localPos);
			Contraption contraption = cEntity.getContraption();
			StructureTemplate.StructureBlockInfo info = contraption.getBlocks()
				.get(blockPos);

			if (info == null)
				return false;

			cEntity.registerColliding(self);
			return true;
		});

		if (!onAtLeastOneContraption)
			return;

		self.setOnGround(true);
		self.getCustomData()
			.putBoolean("ContraptionGrounded", true);
	}

	@Inject(method = "spawnSprintParticle", at = @At(value = "TAIL"))
	private void create$onSpawnSprintParticle(CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		Vec3 worldPos = self.position().add(0, -0.2, 0);
		BlockPos particlePos = BlockPos.containing(worldPos); // pos where particles are spawned
		EntityDimensions dimensions = self.getDimensions(self.getPose());
		RandomSource random = self.level().getRandom();
		Level level = self.level();

		create$forCollision(worldPos, (contraption, state, pos) -> {
			boolean particles = state.getRenderShape() != RenderShape.INVISIBLE;
			if (state.getBlock() instanceof CustomRunningEffectsBlock custom &&
					custom.addRunningEffects(state, self.level(), pos, self)) {
				particles = false;
			}
			if (particles) {
				Vec3 speed = self.getDeltaMovement();
				level.addParticle(
					new BlockParticleOption(ParticleTypes.BLOCK, state).setSourcePos(particlePos),
					self.getX() + ((double) random.nextFloat() - 0.5D) * (double) dimensions.width(),
					self.getY() + 0.1D,
					self.getZ() + ((double) random.nextFloat() - 0.5D) * (double) dimensions.height(),
					speed.x * -4.0D, 1.5D, speed.z * -4.0D
				);
			}
		});
	}
}
