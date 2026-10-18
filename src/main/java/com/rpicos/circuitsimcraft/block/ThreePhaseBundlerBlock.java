package com.rpicos.circuitsimcraft.block;

import com.rpicos.circuitsimcraft.blockentity.ThreePhaseBundlerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** No {@code FACING} property - every face already has a fixed, permanent role (see
 *  {@link ThreePhaseBundlerBlockEntity}), so there's nothing for a player to rotate. */
public class ThreePhaseBundlerBlock extends Block implements EntityBlock {
	public ThreePhaseBundlerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ThreePhaseBundlerBlockEntity(pos, state);
	}
}
