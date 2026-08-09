package com.rpicos.circuitsimcraft.network;

import net.minecraft.core.Direction;

/** A block position that carries a bundle of 3 independent phase conductors (A/B/C) through a
 *  single face, alongside (or instead of) the ordinary single-conductor graph
 *  {@link NetworkParticipant} already models. A {@link com.rpicos.circuitsimcraft.blockentity.ThreePhaseWireBlockEntity}
 *  is bundle-conductive on all six faces, the same way a plain wire is conductive on all six
 *  faces in the ordinary graph; a 3-phase component is bundle-conductive only on its bundled
 *  lead face(s). The two graphs never merge just by two blocks touching - an ordinary wire or
 *  component face is never bundle-conductive, and a bundle face is never ordinary-conductive.
 *  Two things deliberately bridge them anyway: a Bundler/Unbundler, at a single shared position
 *  rather than across an adjacency; and {@link com.rpicos.circuitsimcraft.blockentity.GroundBlockEntity},
 *  which is bundle-conductive on all six faces exactly like a mono Ground is ordinary-conductive
 *  on all six - a real ground reference doesn't care which graph is returning to it, so a bundle
 *  wire run can end directly at a Ground block with no Unbundler needed, the same way a mono wire
 *  run already can. */
public interface BundleParticipant {
	boolean isBundleConductiveTowards(Direction direction);
}
