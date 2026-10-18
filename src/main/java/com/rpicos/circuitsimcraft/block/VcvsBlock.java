package com.rpicos.circuitsimcraft.block;

import com.rpicos.circuitsimcraft.blockentity.VcvsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Reuses {@link GroundedComponentBlock}'s pinned-FACING/must-sit-on-Ground handling: a VCVS's
 *  north (control) and up (output) faces are both referenced against that same required Ground
 *  block, exactly like {@code R2VConverterBlock}. */
public class VcvsBlock extends GroundedComponentBlock {
	public VcvsBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VcvsBlockEntity(pos, state);
	}
}
