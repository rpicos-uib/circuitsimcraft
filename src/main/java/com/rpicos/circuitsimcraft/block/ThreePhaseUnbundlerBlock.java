package com.rpicos.circuitsimcraft.block;

import com.rpicos.circuitsimcraft.blockentity.ThreePhaseUnbundlerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ThreePhaseUnbundlerBlock extends Block implements EntityBlock {
	public ThreePhaseUnbundlerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ThreePhaseUnbundlerBlockEntity(pos, state);
	}
}
