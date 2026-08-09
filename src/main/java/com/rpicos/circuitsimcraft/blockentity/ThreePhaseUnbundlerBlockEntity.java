package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Electrically identical to {@link ThreePhaseBundlerBlockEntity} (see its doc comment) - a
 *  separate block purely so a player can place "the one that goes from a bundle to three wires"
 *  distinctly from "the one that goes from three wires to a bundle", even though the underlying
 *  topology bridge doesn't actually care which direction current flows. */
public class ThreePhaseUnbundlerBlockEntity extends ThreePhaseBundlerBlockEntity {

	public ThreePhaseUnbundlerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.THREE_PHASE_UNBUNDLER, pos, state);
	}

	@Override
	protected String deviceName() {
		return "Unbundler";
	}
}
