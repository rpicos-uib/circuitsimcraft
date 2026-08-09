package com.rpicos.circuitsimcraft.item;

import com.rpicos.circuitsimcraft.blockentity.ThreePhaseProbeable;
import com.rpicos.circuitsimcraft.network.ThreePhaseProbeWatchManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * The bundle analog of {@link ProbeItem}: right-click a bundled component to pin it as one of up
 * to {@value ThreePhaseProbeWatchManager#MAX_CHANNELS} channels, each showing all three phases at
 * once; shift+right-click unpins it.
 */
public class ThreePhaseProbeItem extends Item {
	public ThreePhaseProbeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockPos pos = context.getClickedPos();
		if (level.getBlockEntity(pos) instanceof ThreePhaseProbeable component
				&& context.getPlayer() instanceof ServerPlayer player) {
			double[] v = component.probeBundleVoltage();
			double[] i = component.probeBundleCurrent();
			String readout = String.format("%s | A: %.2fV %.4fA  B: %.2fV %.4fA  C: %.2fV %.4fA",
					component.probeSummary(), v[0], i[0], v[1], i[1], v[2], i[2]);
			if (player.isShiftKeyDown()) {
				ThreePhaseProbeWatchManager.unpin(player, pos);
				player.sendOverlayMessage(Component.literal("Unpinned: " + readout));
			} else {
				ThreePhaseProbeWatchManager.pin(player, pos);
				player.sendOverlayMessage(Component.literal("Pinned: " + readout));
			}
			return InteractionResult.SUCCESS_SERVER;
		}

		return InteractionResult.PASS;
	}
}
