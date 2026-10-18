package com.rpicos.circuitsimcraft.block;

import com.rpicos.circuitsimcraft.blockentity.DiodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class DiodeBlock extends ComponentBlock {
	public DiodeBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DiodeBlockEntity(pos, state);
	}
}
