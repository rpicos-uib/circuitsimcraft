package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** A PMOS transistor - same geometry and {@link com.rpicos.circuitsimcraft.sim.Mosfet} model as
 *  {@link NmosBlockEntity}, mirrored via {@code Mosfet}'s own {@code polarity} parameter. */
public class PmosBlockEntity extends NmosBlockEntity {

	public PmosBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PMOS, pos, state);
	}

	@Override
	protected int polarity() {
		return -1;
	}

	@Override
	protected String deviceName() {
		return "PMOS";
	}
}
