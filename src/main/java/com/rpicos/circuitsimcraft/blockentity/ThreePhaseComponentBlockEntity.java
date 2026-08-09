package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.network.BundleParticipant;
import com.rpicos.circuitsimcraft.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/** A two-terminal circuit component wired along its FACING axis, exactly like {@link
 *  ComponentBlockEntity} - except both leads are 3-wide bundles instead of a single node. The
 *  bundle analog of {@link ComponentBlockEntity}: subclasses stamp three parallel per-phase
 *  {@code sim/} elements (one per phase pair {@code nodesA[p]}/{@code nodesB[p]}) rather than
 *  one, reusing the existing mono element classes unchanged - a "3-phase resistor" really is just
 *  three ordinary {@code Resistor}s sharing one editable value. Right-click-without-item cycling
 *  (the mono convention) isn't reachable here since {@code ComponentBlock.useWithoutItem()} only
 *  checks {@code instanceof ComponentBlockEntity} - subclasses that need player-editable values
 *  use {@link ValueEditable}'s shift-right-click screen instead, which already has a generic path
 *  independent of {@code ComponentBlockEntity} (the same one the Op-Amp/transistors use). */
public abstract class ThreePhaseComponentBlockEntity extends NetworkBlockEntity implements BundleParticipant, ThreePhaseProbeable {

	private final ProbeHistory[] history = {new ProbeHistory(), new ProbeHistory(), new ProbeHistory()};

	private Circuit circuit;
	private final int[] nodesA = {-1, -1, -1};
	private final int[] nodesB = {-1, -1, -1};

	protected ThreePhaseComponentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public Direction getFacing() {
		return getBlockState().getValue(BlockStateProperties.FACING);
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return false;
	}

	@Override
	public boolean isBundleConductiveTowards(Direction direction) {
		return direction == getFacing() || direction == getFacing().getOpposite();
	}

	/** Called once per rebuild to add this component's three parallel per-phase elements/sources
	 *  to the live circuit. */
	public abstract void addToCircuit(Circuit circuit, int[] nodesA, int[] nodesB);

	protected void bindNodes(Circuit circuit, int[] nodesA, int[] nodesB) {
		this.circuit = circuit;
		System.arraycopy(nodesA, 0, this.nodesA, 0, 3);
		System.arraycopy(nodesB, 0, this.nodesB, 0, 3);
	}

	@Override
	public double[] probeBundleVoltage() {
		double[] v = new double[3];
		if (circuit != null) {
			for (int phase = 0; phase < 3; phase++) {
				v[phase] = circuit.getVoltage(nodesA[phase]) - circuit.getVoltage(nodesB[phase]);
			}
		}
		return v;
	}

	@Override
	public abstract double[] probeBundleCurrent();

	@Override
	public abstract String probeSummary();

	/** What the oscilloscope trace plots for this component - voltage for every normal component,
	 *  mirroring {@link ComponentBlockEntity#sampleValue()}. */
	protected double[] sampleValues() {
		return probeBundleVoltage();
	}

	@Override
	public void recordSample() {
		double[] values = sampleValues();
		for (int phase = 0; phase < 3; phase++) {
			history[phase].record(values[phase]);
		}
	}

	@Override
	public List<List<Float>> historySnapshot() {
		return List.of(history[0].snapshot(), history[1].snapshot(), history[2].snapshot());
	}
}
