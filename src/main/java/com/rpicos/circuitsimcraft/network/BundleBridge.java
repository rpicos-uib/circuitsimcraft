package com.rpicos.circuitsimcraft.network;

import net.minecraft.core.Direction;

/** A block position that deliberately bridges the ordinary graph and the bundle graph at the
 *  *same* position - a Bundler or Unbundler. Everywhere else, a union only ever happens across
 *  an adjacency (two neighbouring positions); this is the one place two structurally different
 *  key types are merged at a single position instead, aliasing phase 0/1/2's separate mono lead
 *  directly onto the bundle's corresponding phase sub-node with zero added impedance - the same
 *  way a plain wire itself introduces no circuit element, just merges nodes. */
public interface BundleBridge {
	/** The single 3-wide bundled lead. */
	Direction bundleFace();

	/** Phase 0/1/2 (A/B/C)'s separate, ordinary single-conductor lead. */
	Direction monoFace(int phase);
}
