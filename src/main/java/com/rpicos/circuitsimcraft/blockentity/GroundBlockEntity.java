package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import com.rpicos.circuitsimcraft.network.BundleParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Anchors whatever network it touches to the solver's real node 0 (see
 *  {@link com.rpicos.circuitsimcraft.network.CircuitNetworkManager}), giving that network a
 *  meaningful 0V reference point - so a Wire's "voltage at this node" reading actually means
 *  something, instead of being relative to an arbitrary internal reference. Bundle-conductive on
 *  all six faces too, exactly like its ordinary conductivity - a real ground reference doesn't
 *  care whether the wire reaching it is mono or one of three bundled phases, so a 3-phase wire
 *  run can end directly at a Ground block with no Bundler/Unbundler needed, same as a mono run
 *  already could. Ground itself never stamps anything into either {@code Circuit} - it's a pure
 *  topological anchor in both graphs, not an element. */
public class GroundBlockEntity extends SingleNodeBlockEntity implements BundleParticipant {

	public GroundBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.GROUND, pos, state);
	}

	@Override
	public boolean isBundleConductiveTowards(Direction direction) {
		return true;
	}

	@Override
	public String probeSummary() {
		return "Ground (0V reference)";
	}
}
