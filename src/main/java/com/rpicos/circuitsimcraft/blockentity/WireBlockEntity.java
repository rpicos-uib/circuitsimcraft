package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class WireBlockEntity extends SingleNodeBlockEntity {

	public WireBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WIRE, pos, state);
	}

	@Override
	public Component probeSummary() {
		return CircuitText.probe("wire", "%.2f".formatted(probeVoltage()));
	}
}
