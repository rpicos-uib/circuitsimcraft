package com.rpicos.circuitsimcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseResistorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ThreePhaseResistorBlock extends ComponentBlock {
	public static final MapCodec<ThreePhaseResistorBlock> CODEC = simpleCodec(ThreePhaseResistorBlock::new);

	public ThreePhaseResistorBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<ThreePhaseResistorBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ThreePhaseResistorBlockEntity(pos, state);
	}
}
