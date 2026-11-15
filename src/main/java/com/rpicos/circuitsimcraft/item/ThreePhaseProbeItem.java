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
			Component readout = Component.translatable("message.circuitsimcraft.three_phase_probe.readout", component.probeSummary(),
					"%.2f".formatted(v[0]), "%.4f".formatted(i[0]), "%.2f".formatted(v[1]), "%.4f".formatted(i[1]),
					"%.2f".formatted(v[2]), "%.4f".formatted(i[2]));
			if (player.isShiftKeyDown()) {
				ThreePhaseProbeWatchManager.unpin(player, pos);
				player.sendOverlayMessage(Component.translatable("message.circuitsimcraft.probe.unpinned", readout));
			} else {
				ThreePhaseProbeWatchManager.pin(player, pos);
				player.sendOverlayMessage(Component.translatable("message.circuitsimcraft.probe.pinned", readout));
			}
			return InteractionResult.SUCCESS_SERVER;
		}

		return InteractionResult.PASS;
	}
}
