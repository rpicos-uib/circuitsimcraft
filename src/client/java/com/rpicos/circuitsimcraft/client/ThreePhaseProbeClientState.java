package com.rpicos.circuitsimcraft.client;

import com.rpicos.circuitsimcraft.network.ThreePhaseProbeDataPayload;
import net.minecraft.core.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** The bundle analog of {@link ProbeClientState}: latest three-phase oscilloscope data received
 *  from the server, keyed by channel position. */
final class ThreePhaseProbeClientState {
	private static final long STALE_AFTER_MILLIS = 1000;

	private record Entry(ThreePhaseProbeDataPayload payload, long receivedAtMillis) {
	}

	private static final Map<BlockPos, Entry> CHANNELS = new ConcurrentHashMap<>();

	private ThreePhaseProbeClientState() {
	}

	static void update(ThreePhaseProbeDataPayload payload) {
		CHANNELS.put(payload.pos(), new Entry(payload, System.currentTimeMillis()));
	}

	static List<ThreePhaseProbeDataPayload> currentChannels() {
		long now = System.currentTimeMillis();
		CHANNELS.values().removeIf(entry -> now - entry.receivedAtMillis() > STALE_AFTER_MILLIS);
		return CHANNELS.values().stream()
				.map(Entry::payload)
				.sorted(Comparator.<ThreePhaseProbeDataPayload>comparingInt(data -> data.pos().getX())
						.thenComparingInt(data -> data.pos().getY())
						.thenComparingInt(data -> data.pos().getZ()))
				.toList();
	}
}
