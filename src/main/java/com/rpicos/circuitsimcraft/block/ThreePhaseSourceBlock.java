package com.rpicos.circuitsimcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/** Player-rotatable like {@link PowerSupplyBlock} - FACING is the bundled 3-phase output face,
 *  the opposite face is the ordinary single neutral lead. Its block entity extends
 *  {@code NetworkBlockEntity} directly (not {@code ComponentBlockEntity}, whose two-lead
 *  {@code addToCircuit(Circuit, int, int)} shape doesn't fit a bundle+mono split), so it's
 *  resolved by its own dedicated branch in {@code CircuitNetworkManager.rebuild()} rather than
 *  the generic {@code ComponentBlockEntity} one. */
public class ThreePhaseSourceBlock extends ComponentBlock {
	public static final MapCodec<ThreePhaseSourceBlock> CODEC = simpleCodec(ThreePhaseSourceBlock::new);

	public ThreePhaseSourceBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<ThreePhaseSourceBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ThreePhaseSourceBlockEntity(pos, state);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
			Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ThreePhaseSourceBlockEntity source) {
			source.setRedstonePowered(level.hasNeighborSignal(pos));
		}
	}
}
