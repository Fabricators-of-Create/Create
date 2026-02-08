package com.simibubi.create.content.fluids.particle;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;

import net.minecraft.core.particles.ColorParticleOption;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.fluids.potion.PotionFluid;

import net.createmod.catnip.theme.Color;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;

import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;

public class FluidStackParticle extends TextureSheetParticle {
	private final float uo;
	private final float vo;
	private final FluidStack fluid;
//	private final IClientFluidTypeExtensions clientFluid; // fabric: replaced with FluidVariantRendering

	public static FluidStackParticle create(ParticleType<FluidParticleData> type, ClientLevel world, FluidStack fluid,
		double x, double y, double z, double vx, double vy, double vz) {
		if (type == AllParticleTypes.BASIN_FLUID.get())
			return new BasinFluidParticle(world, fluid, x, y, z, vx, vy, vz);
		return new FluidStackParticle(world, fluid, x, y, z, vx, vy, vz);
	}

	@SuppressWarnings("UnstableApiUsage")
	public FluidStackParticle(ClientLevel world, FluidStack fluid, double x, double y, double z, double vx, double vy,
							  double vz) {
		super(world, x, y, z, vx, vy, vz);

		this.fluid = fluid;
		FluidVariantRenderHandler handler = FluidVariantRendering.getHandlerOrDefault(fluid.getFluid());
		this.setSprite(handler.getSprites(fluid.getVariant())[0]);

		this.gravity = 1.0F;
		this.rCol = 0.8F;
		this.gCol = 0.8F;
		this.bCol = 0.8F;
		int color = handler.getColor(fluid.getVariant(), world, BlockPos.containing(x, y, z));
		multiplyColor(color);

		this.xd = vx;
		this.yd = vy;
		this.zd = vz;

		this.quadSize /= 2.0F;
		this.uo = this.random.nextFloat() * 3.0F;
		this.vo = this.random.nextFloat() * 3.0F;
	}

	@Override
	protected int getLightColor(float p_189214_1_) {
		int brightnessForRender = super.getLightColor(p_189214_1_);
		int skyLight = brightnessForRender >> 20;
		int blockLight = (brightnessForRender >> 4) & 0xf;
		blockLight = Math.max(blockLight, FluidVariantAttributes.getLuminance(fluid.getVariant()));
		return (skyLight << 20) | (blockLight << 4);
	}

	protected void multiplyColor(int color) {
		this.rCol *= (float) (color >> 16 & 255) / 255.0F;
		this.gCol *= (float) (color >> 8 & 255) / 255.0F;
		this.bCol *= (float) (color & 255) / 255.0F;
	}

	protected float getU0() {
		return this.sprite.getU((this.uo + 1.0F) / 4.0F);
	}

	protected float getU1() {
		return this.sprite.getU(this.uo / 4.0F);
	}

	protected float getV0() {
		return this.sprite.getV(this.vo / 4.0F);
	}

	protected float getV1() {
		return this.sprite.getV((this.vo + 1.0F) / 4.0F);
	}

	@Override
	public void tick() {
		super.tick();
		if (!canEvaporate())
			return;
		if (onGround)
			remove();
		if (!removed)
			return;
		if (!onGround && level.random.nextFloat() < 1 / 8f)
			return;

			FluidVariantRenderHandler handler = FluidVariantRendering.getHandlerOrDefault(fluid.getFluid());
			Color color = new Color(handler.getColor(fluid.getVariant(), level, BlockPos.containing(x, y, z)));
			level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat()), x, y, z, 0, 0, 0);
		}

	protected boolean canEvaporate() {
		return fluid.getFluid() instanceof PotionFluid;
	}

	@Override
	public @NotNull ParticleRenderType getRenderType() {
		return ParticleRenderType.TERRAIN_SHEET;
	}

}
