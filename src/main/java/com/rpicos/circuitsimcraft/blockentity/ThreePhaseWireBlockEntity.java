package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import com.rpicos.circuitsimcraft.network.BundleParticipant;
import com.rpicos.circuitsimcraft.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** A wire carrying all three phases bundled together, conductive on all six faces exactly like
 *  a plain {@link WireBlockEntity} - except each such "node" is actually 3 independent scalar
 *  nodes (phase A/B/C) that always travel and merge together as a unit. Opts out of the ordinary
 *  single-conductor graph entirely ({@link #isConductiveTowards} always false), so a plain wire
 *  or component touching it face-to-face simply doesn't connect - crossing between the two
 *  requires a Bundler/Unbundler. */
public class ThreePhaseWireBlockEntity extends NetworkBlockEntity implements BundleParticipant, ThreePhaseProbeable {

	private final ProbeHistory[] history = {new ProbeHistory(), new ProbeHistory(), new ProbeHistory()};

	private Circuit circuit;
	private final int[] nodeIds = {-1, -1, -1};

	public ThreePhaseWireBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.THREE_PHASE_WIRE, pos, state);
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return false;
	}

	@Override
	public boolean isBundleConductiveTowards(Direction direction) {
		return true;
	}

	/** Called once per rebuild with this block's resolved node ids (phase A/B/C, in that order). */
	public void bindBundleNode(Circuit circuit, int[] nodeIds) {
		this.circuit = circuit;
		System.arraycopy(nodeIds, 0, this.nodeIds, 0, 3);
	}

	/** Phase 0/1/2 (A/B/C) node voltage - for diagnostics/probing. */
	public double phaseVoltage(int phase) {
		return circuit == null ? 0 : circuit.getVoltage(nodeIds[phase]);
	}

	@Override
	public double[] probeBundleVoltage() {
		return new double[] {phaseVoltage(0), phaseVoltage(1), phaseVoltage(2)};
	}

	@Override
	public double[] probeBundleCurrent() {
		// current isn't a single well-defined scalar at an arbitrary node, same as plain Wire.
		return new double[3];
	}

	@Override
	public void recordSample() {
		double[] v = probeBundleVoltage();
		for (int phase = 0; phase < 3; phase++) {
			history[phase].record(v[phase]);
		}
	}

	@Override
	public List<List<Float>> historySnapshot() {
		return List.of(history[0].snapshot(), history[1].snapshot(), history[2].snapshot());
	}

	@Override
	public String probeSummary() {
		return String.format("3-Phase Wire node: A=%.2f B=%.2f C=%.2f V",
				phaseVoltage(0), phaseVoltage(1), phaseVoltage(2));
	}
}
