package com.rpicos.circuitsimcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseAmmeterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ThreePhaseAmmeterBlock extends ComponentBlock {
	public static final MapCodec<ThreePhaseAmmeterBlock> CODEC = simpleCodec(ThreePhaseAmmeterBlock::new);

	public ThreePhaseAmmeterBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<ThreePhaseAmmeterBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ThreePhaseAmmeterBlockEntity(pos, state);
	}
}
