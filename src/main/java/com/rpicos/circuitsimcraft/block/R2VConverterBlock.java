package com.rpicos.circuitsimcraft.block;

import com.rpicos.circuitsimcraft.blockentity.R2VConverterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/** Redstone-to-Voltage converter. Reads two 0-15 redstone signal strengths - North = A, South =
 *  B - and outputs V = A*16 + B (0-255V) on its up lead, referenced against the Ground block it
 *  must be placed directly on (its down lead, enforced by {@link GroundedComponentBlock}). */
public class R2VConverterBlock extends GroundedComponentBlock {
	public R2VConverterBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new R2VConverterBlockEntity(pos, state);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
			Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof R2VConverterBlockEntity r2v) {
			r2v.setRedstoneInputs(level.getSignal(pos.north(), Direction.NORTH), level.getSignal(pos.south(), Direction.SOUTH));
		}
	}
}
