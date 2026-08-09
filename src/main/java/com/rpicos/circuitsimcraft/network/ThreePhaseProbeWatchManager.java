package com.rpicos.circuitsimcraft.network;

import com.rpicos.circuitsimcraft.blockentity.ThreePhaseProbeable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** The bundle analog of {@link ProbeWatchManager}: tracks up to {@value #MAX_CHANNELS} bundled
 *  components each player has pinned with the 3-phase probe, streaming every pinned component's
 *  three-phase live readout to that player each tick. A fully separate pipeline from the mono
 *  probe, not a shared one - same reasoning as the existing AC/Bode family. */
public final class ThreePhaseProbeWatchManager {
	public static final int MAX_CHANNELS = 3;

	private static final Map<UUID, List<BlockPos>> PINS = new ConcurrentHashMap<>();

	private ThreePhaseProbeWatchManager() {
	}

	public static void pin(ServerPlayer player, BlockPos pos) {
		List<BlockPos> pins = PINS.computeIfAbsent(player.getUUID(), id -> new ArrayList<>());
		BlockPos key = pos.immutable();
		pins.remove(key);
		pins.add(key);
		while (pins.size() > MAX_CHANNELS) {
			pins.remove(0);
		}
	}

	public static void unpin(ServerPlayer player, BlockPos pos) {
		List<BlockPos> pins = PINS.get(player.getUUID());
		if (pins != null) {
			pins.remove(pos);
		}
	}

	public static void clear(UUID playerId) {
		PINS.remove(playerId);
	}

	public static void tick(ServerLevel level) {
		for (ServerPlayer player : level.players()) {
			List<BlockPos> pins = PINS.get(player.getUUID());
			if (pins == null || pins.isEmpty()) {
				continue;
			}
			boolean atCapacity = pins.size() >= MAX_CHANNELS;
			BlockPos oldest = atCapacity ? pins.get(0) : null;
			for (BlockPos pos : List.copyOf(pins)) {
				if (level.getBlockEntity(pos) instanceof ThreePhaseProbeable component) {
					double[] v = component.probeBundleVoltage();
					double[] i = component.probeBundleCurrent();
					List<List<Float>> history = component.historySnapshot();
					ThreePhaseProbeDataPayload payload = new ThreePhaseProbeDataPayload(
							pos, component.probeSummary(),
							(float) v[0], (float) v[1], (float) v[2],
							(float) i[0], (float) i[1], (float) i[2],
							history.get(0), history.get(1), history.get(2),
							pos.equals(oldest));
					ServerPlayNetworking.send(player, payload);
				}
			}
		}
	}
}
