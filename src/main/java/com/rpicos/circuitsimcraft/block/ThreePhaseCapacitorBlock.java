package com.rpicos.circuitsimcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseCapacitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ThreePhaseCapacitorBlock extends ComponentBlock {
	public static final MapCodec<ThreePhaseCapacitorBlock> CODEC = simpleCodec(ThreePhaseCapacitorBlock::new);

	public ThreePhaseCapacitorBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<ThreePhaseCapacitorBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ThreePhaseCapacitorBlockEntity(pos, state);
	}
}
