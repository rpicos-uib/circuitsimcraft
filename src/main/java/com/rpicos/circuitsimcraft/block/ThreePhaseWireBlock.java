package com.rpicos.circuitsimcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseWireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** A bundle wire: connects to any neighbouring 3-phase wire or bundle-lead terminal on all six
 *  faces, carrying all three phases together. See {@link WireBlock} for the ordinary,
 *  single-conductor equivalent this mirrors. */
public class ThreePhaseWireBlock extends Block implements EntityBlock {
	public static final MapCodec<ThreePhaseWireBlock> CODEC = simpleCodec(ThreePhaseWireBlock::new);

	public ThreePhaseWireBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<ThreePhaseWireBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ThreePhaseWireBlockEntity(pos, state);
	}
}
