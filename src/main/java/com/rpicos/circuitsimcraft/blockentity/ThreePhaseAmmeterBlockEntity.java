package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import com.rpicos.circuitsimcraft.sim.Circuit;
import com.rpicos.circuitsimcraft.sim.VoltageSource;
import com.rpicos.circuitsimcraft.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Three independent 0V voltage sources in series, one per phase - the bundle equivalent of
 *  {@link AmmeterBlockEntity}. Both leads (FACING and its opposite) are bundled; there is no
 *  ordinary single-conductor lead at all. */
public class ThreePhaseAmmeterBlockEntity extends ThreePhaseComponentBlockEntity {

	private final VoltageSource[] live = new VoltageSource[3];

	public ThreePhaseAmmeterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.THREE_PHASE_AMMETER, pos, state);
	}

	@Override
	public void addToCircuit(Circuit circuit, int[] nodesA, int[] nodesB) {
		for (int phase = 0; phase < 3; phase++) {
			live[phase] = new VoltageSource(nodesA[phase], nodesB[phase], Waveform.dc(0));
			circuit.add(live[phase]);
		}
		bindNodes(circuit, nodesA, nodesB);
	}

	@Override
	public double[] probeBundleCurrent() {
		double[] i = new double[3];
		for (int phase = 0; phase < 3; phase++) {
			i[phase] = live[phase] == null ? 0 : live[phase].current();
		}
		return i;
	}

	@Override
	protected double[] sampleValues() {
		// the oscilloscope trace plots current here, not voltage (which is always ~0 across an
		// ideal ammeter and wouldn't be a useful signal to look at) - mirrors AmmeterBlockEntity.
		return probeBundleCurrent();
	}

	@Override
	public String probeSummary() {
		double[] i = probeBundleCurrent();
		return String.format("3-Phase Ammeter A=%.4f B=%.4f C=%.4f A", i[0], i[1], i[2]);
	}
}
