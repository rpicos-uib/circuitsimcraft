package com.rpicos.circuitsimcraft.block;

import com.rpicos.circuitsimcraft.blockentity.VccsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class VccsBlock extends GroundedComponentBlock {
	public VccsBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VccsBlockEntity(pos, state);
	}
}
