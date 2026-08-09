package com.rpicos.circuitsimcraft.client;

import com.rpicos.circuitsimcraft.network.AcBodePayload;
import net.minecraft.core.BlockPos;

/** Unlike every other probe's client state in this mod, the AC probe's result isn't
 *  continuously re-sent every tick - it's a one-shot sweep, computed once per second click - so
 *  there's no staleness timeout here at all: whichever of the two payload kinds arrived most
 *  recently (a hint after the first click, or a full Bode result after the second) is what stays
 *  displayed until the next one replaces it. */
final class AcProbeClientState {
	sealed interface Display {
	}

	record Hint(BlockPos sourcePos) implements Display {
	}

	record Result(AcBodePayload payload) implements Display {
	}

	private static volatile Display current;

	private AcProbeClientState() {
	}

	static void updateHint(BlockPos sourcePos) {
		current = new Hint(sourcePos);
	}

	static void updateResult(AcBodePayload payload) {
		current = new Result(payload);
	}

	/** Null if the player has never used the AC probe this session. */
	static Display current() {
		return current;
	}
}
