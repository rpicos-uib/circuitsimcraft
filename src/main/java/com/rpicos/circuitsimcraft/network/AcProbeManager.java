package com.rpicos.circuitsimcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks, per player, which AC Source block the AC probe currently has pinned as the sweep's
 *  excitation - unlike every other probe in this mod, this one is a genuine two-step action
 *  rather than a continuously-updated pin: the first right-click (on an AC Source) just records
 *  which source to use and asks the client to show a hint; only the second right-click (on
 *  whatever point the player wants a Bode plot of, relative to that source) actually triggers the
 *  (comparatively expensive - a full complex linear solve at every swept frequency) sweep
 *  computation. The source stays pinned afterward, so a player can probe several different
 *  points against the same source without re-clicking it each time; only a shift-right-click, or
 *  clicking a different AC Source block, changes it. */
public final class AcProbeManager {
	private static final Map<UUID, BlockPos> PENDING_SOURCE = new ConcurrentHashMap<>();

	private AcProbeManager() {
	}

	public static void pinSource(ServerPlayer player, BlockPos pos) {
		PENDING_SOURCE.put(player.getUUID(), pos.immutable());
	}

	public static BlockPos pendingSource(ServerPlayer player) {
		return PENDING_SOURCE.get(player.getUUID());
	}

	public static void clearPending(ServerPlayer player) {
		PENDING_SOURCE.remove(player.getUUID());
	}

	public static void clear(UUID playerId) {
		PENDING_SOURCE.remove(playerId);
	}
}
