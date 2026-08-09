package com.rpicos.circuitsimcraft.network;

import net.minecraft.core.Direction;

/** A block position that carries a bundle of 3 independent phase conductors (A/B/C) through a
 *  single face, alongside (or instead of) the ordinary single-conductor graph
 *  {@link NetworkParticipant} already models. A {@link com.rpicos.circuitsimcraft.blockentity.ThreePhaseWireBlockEntity}
 *  is bundle-conductive on all six faces, the same way a plain wire is conductive on all six
 *  faces in the ordinary graph; a 3-phase component is bundle-conductive only on its bundled
 *  lead face(s). The two graphs never merge just by two blocks touching - an ordinary wire or
 *  component face is never bundle-conductive, and a bundle face is never ordinary-conductive -
 *  only a Bundler/Unbundler deliberately bridges them, at a single shared position rather than
 *  across an adjacency. */
public interface BundleParticipant {
	boolean isBundleConductiveTowards(Direction direction);
}
