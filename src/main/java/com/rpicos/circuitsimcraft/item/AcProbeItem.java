package com.rpicos.circuitsimcraft.item;

import com.rpicos.circuitsimcraft.blockentity.AcSourceBlockEntity;
import com.rpicos.circuitsimcraft.network.AcBodePayload;
import com.rpicos.circuitsimcraft.network.AcHintPayload;
import com.rpicos.circuitsimcraft.network.AcProbeManager;
import com.rpicos.circuitsimcraft.network.CircuitNetworkManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * The AC oscilloscope probe: unlike every other probe in this mod, a genuine two-step action
 * rather than a single pin. Right-click an AC Source block first - this only registers which
 * source to sweep and shows a hint, it does not compute anything yet. Right-click a second,
 * different position afterward - any wired component, wire, or ground - to actually run the
 * sweep and show a Bode plot (magnitude in dB and phase in degrees, both against log-frequency)
 * of that position's voltage relative to the pinned source. The source stays pinned afterward,
 * so further right-clicks probe new signal points against the same source; shift+right-click
 * unpins it.
 */
public class AcProbeItem extends Item {
	public AcProbeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!(context.getPlayer() instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.PASS;
		}

		BlockPos pos = context.getClickedPos();

		if (player.isShiftKeyDown()) {
			AcProbeManager.clearPending(player);
			player.sendOverlayMessage(Component.literal("AC probe unpinned"));
			return InteractionResult.SUCCESS_SERVER;
		}

		// Clicking an AC Source (re-)pins it as the sweep's excitation, whether or not one was
		// already pinned - this lets a player switch to a different source without needing to
		// shift-click first.
		if (level.getBlockEntity(pos) instanceof AcSourceBlockEntity) {
			AcProbeManager.pinSource(player, pos);
			ServerPlayNetworking.send(player, new AcHintPayload(pos.immutable()));
			player.sendOverlayMessage(Component.literal("AC source pinned - now click a signal point"));
			return InteractionResult.SUCCESS_SERVER;
		}

		BlockPos sourcePos = AcProbeManager.pendingSource(player);
		if (sourcePos == null) {
			player.sendOverlayMessage(Component.literal("Right-click an AC Source block first"));
			return InteractionResult.SUCCESS_SERVER;
		}

		CircuitNetworkManager.AcSweepResult result =
				CircuitNetworkManager.forLevel(serverLevel).computeAcSweep(sourcePos, pos);
		if (result.warning() != null) {
			player.sendOverlayMessage(Component.literal("AC probe: " + result.warning()));
			return InteractionResult.SUCCESS_SERVER;
		}

		List<Float> freqs = toFloatList(result.freqsHz());
		List<Float> mags = toFloatList(result.magnitudesDb());
		List<Float> phases = toFloatList(result.phasesDeg());
		ServerPlayNetworking.send(player,
				new AcBodePayload(sourcePos.immutable(), pos.immutable(), freqs, mags, phases, ""));
		player.sendOverlayMessage(Component.literal("AC sweep complete"));
		return InteractionResult.SUCCESS_SERVER;
	}

	private static List<Float> toFloatList(List<Double> values) {
		List<Float> out = new ArrayList<>(values.size());
		for (double v : values) {
			out.add((float) v);
		}
		return out;
	}
}
