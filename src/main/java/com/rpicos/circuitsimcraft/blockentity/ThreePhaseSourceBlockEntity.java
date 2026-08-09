package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import com.rpicos.circuitsimcraft.network.BundleParticipant;
import com.rpicos.circuitsimcraft.sim.Circuit;
import com.rpicos.circuitsimcraft.sim.VoltageSource;
import com.rpicos.circuitsimcraft.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/** A three-phase voltage source: the FACING face is a bundled 3-phase output (three internal
 *  sinusoidal sources at the same frequency/amplitude, 120 degrees apart), the opposite face is
 *  an ordinary single neutral node - wired to a regular {@code circuitsimcraft:ground} block,
 *  exactly the same convention every existing 2-terminal source already uses for its "back" lead.
 *  Redstone-gated the same way as {@link PowerSupplyBlockEntity}. */
public class ThreePhaseSourceBlockEntity extends NetworkBlockEntity implements BundleParticipant, ThreePhaseProbeable, ValueEditable {

	private static final double[] PRESETS_VOLTS = {12, 48, 120, 230, 400};
	private static final double[] PRESETS_HZ = {0.5, 1, 2, 5};
	// Standard phase rotation: A at 0 deg, B at -120 deg, C at -240 deg.
	private static final double[] PHASE_OFFSETS_RAD = {0, -2 * Math.PI / 3, -4 * Math.PI / 3};

	private final ProbeHistory[] history = {new ProbeHistory(), new ProbeHistory(), new ProbeHistory()};

	private double amplitudeVolts = PRESETS_VOLTS[3];
	private double frequencyHz = PRESETS_HZ[1];
	private boolean redstonePowered = false;
	private Circuit circuit;
	private int nodeNeutral = -1;
	private final int[] nodesPhase = {-1, -1, -1};
	private final VoltageSource[] live = new VoltageSource[3];

	public ThreePhaseSourceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.THREE_PHASE_SOURCE, pos, state);
	}

	public Direction getFacing() {
		return getBlockState().getValue(BlockStateProperties.FACING);
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return direction == getFacing().getOpposite();
	}

	@Override
	public boolean isBundleConductiveTowards(Direction direction) {
		return direction == getFacing();
	}

	/** Called by {@link com.rpicos.circuitsimcraft.block.ThreePhaseSourceBlock#neighborChanged}
	 *  whenever a redstone neighbor changes - see {@link PowerSupplyBlockEntity#setRedstonePowered}
	 *  for why an inactive source is left un-stamped rather than driven at 0V. */
	public void setRedstonePowered(boolean powered) {
		if (redstonePowered != powered) {
			redstonePowered = powered;
			markNetworkDirty();
		}
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(
				new EditableField("Amplitude", "V", PRESETS_VOLTS[0], PRESETS_VOLTS[PRESETS_VOLTS.length - 1], amplitudeVolts),
				new EditableField("Frequency", "Hz", PRESETS_HZ[0], PRESETS_HZ[PRESETS_HZ.length - 1], frequencyHz));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		amplitudeVolts = Math.clamp(values.get(0), PRESETS_VOLTS[0], PRESETS_VOLTS[PRESETS_VOLTS.length - 1]);
		frequencyHz = Math.clamp(values.get(1), PRESETS_HZ[0], PRESETS_HZ[PRESETS_HZ.length - 1]);
	}

	/** Called once per rebuild: {@code nodeNeutral} is the resolved back-face node, {@code
	 *  nodesPhase} the three resolved bundle-face nodes (A/B/C, in that order). */
	public void addToCircuit(Circuit circuit, int nodeNeutral, int[] nodesPhase) {
		this.circuit = circuit;
		this.nodeNeutral = nodeNeutral;
		System.arraycopy(nodesPhase, 0, this.nodesPhase, 0, 3);
		if (!redstonePowered) {
			live[0] = live[1] = live[2] = null;
			return;
		}
		for (int phase = 0; phase < 3; phase++) {
			Waveform waveform = Waveform.sine(amplitudeVolts, frequencyHz, PHASE_OFFSETS_RAD[phase], 0);
			live[phase] = new VoltageSource(nodesPhase[phase], nodeNeutral, waveform);
			circuit.add(live[phase]);
		}
	}

	@Override
	public double[] probeBundleVoltage() {
		double[] v = new double[3];
		if (circuit != null) {
			for (int phase = 0; phase < 3; phase++) {
				v[phase] = circuit.getVoltage(nodesPhase[phase]) - circuit.getVoltage(nodeNeutral);
			}
		}
		return v;
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
		String base = String.format("3-Phase Source %.0fV %.2fHz", amplitudeVolts, frequencyHz);
		return redstonePowered ? base : base + " (off - needs redstone)";
	}
}
