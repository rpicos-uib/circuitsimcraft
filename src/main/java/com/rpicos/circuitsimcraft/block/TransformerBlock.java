package com.rpicos.circuitsimcraft.block;

import com.rpicos.circuitsimcraft.blockentity.TransformerBlockEntity;
import com.rpicos.circuitsimcraft.network.ValueEditorOpener;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** The single-phase transformer. Reuses ComponentBlock's FACING property, rotation and
 *  item-in-hand handling, but is only ever placed facing horizontally, front toward the player:
 *  its secondary uses the top and bottom faces, so a vertical FACING would leave no room for
 *  the primary. See {@link TransformerBlockEntity} for which face is which terminal. */
public class TransformerBlock extends ComponentBlock {
	public TransformerBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		// Same interaction as the two-lead components: shift-right-click edits N and M, a plain
		// empty-hand right-click cycles the preset ratios.
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof TransformerBlockEntity transformer) {
			if (ValueEditorOpener.tryOpen(blockEntity, pos, player)) {
				return InteractionResult.SUCCESS_SERVER;
			}
			transformer.cyclePreset();
			transformer.markNetworkDirty();
			player.sendOverlayMessage(transformer.probeSummary());
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TransformerBlockEntity(pos, state);
	}
}
