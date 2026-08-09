package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import com.rpicos.circuitsimcraft.sim.AcCircuit;
import com.rpicos.circuitsimcraft.sim.AcVoltageSource;
import com.rpicos.circuitsimcraft.sim.Circuit;
import com.rpicos.circuitsimcraft.sim.VoltageSource;
import com.rpicos.circuitsimcraft.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FunctionGeneratorBlockEntity extends ComponentBlockEntity implements AcStampable, ValueEditable {

	private enum Kind {SINE, SQUARE, TRIANGLE}

	private static final Kind[] KINDS = Kind.values();
	// Package-visible: ModuleBlockEntity uses these as the "nobody has set this yet" baseline for
	// whichever channel a given module doesn't own.
	static final double DEFAULT_AMPLITUDE_VOLTS = 5;
	static final double DEFAULT_FREQUENCY_HZ = 1;
	private static final double MIN_PHASE_DEGREES = 0;
	private static final double MAX_PHASE_DEGREES = 360;

	private int kindIndex = 0;
	// Overridden by an adjacent Voltage/Frequency module (see ModuleBlockEntity); these are plain
	// defaults so the generator still produces a signal with no modules attached at all.
	private double amplitudeVolts = DEFAULT_AMPLITUDE_VOLTS;
	private double frequencyHz = DEFAULT_FREQUENCY_HZ;
	// Unlike amplitude/frequency, phase has no module to relay it in from - set directly through
	// this block's own shift-right-click value editor instead. Applies to all three waveform
	// kinds uniformly, not just SINE.
	private double phaseDegrees = 0;
	private VoltageSource live;
	private boolean redstonePowered = false;

	public FunctionGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FUNCTION_GENERATOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		kindIndex = (kindIndex + 1) % KINDS.length;
		markNetworkDirty();
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Phase", "deg", MIN_PHASE_DEGREES, MAX_PHASE_DEGREES, phaseDegrees));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		phaseDegrees = Math.clamp(values.get(0), MIN_PHASE_DEGREES, MAX_PHASE_DEGREES);
	}

	/** Called by an adjacent {@link VoltageModuleBlockEntity} to set the output amplitude. */
	public void setAmplitude(double volts) {
		if (amplitudeVolts != volts) {
			amplitudeVolts = volts;
			markNetworkDirty();
		}
	}

	/** Called by an adjacent {@link FrequencyModuleBlockEntity} to set the output frequency. */
	public void setFrequency(double hz) {
		if (frequencyHz != hz) {
			frequencyHz = hz;
			markNetworkDirty();
		}
	}

	/** Called by {@link com.rpicos.circuitsimcraft.block.FunctionGeneratorBlock#neighborChanged}
	 *  whenever a redstone neighbor changes; see {@link PowerSupplyBlockEntity#setRedstonePowered}
	 *  for why an inactive source is left un-stamped rather than driven at 0V. */
	public void setRedstonePowered(boolean powered) {
		if (redstonePowered != powered) {
			redstonePowered = powered;
			markNetworkDirty();
		}
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		bindNodes(circuit, nodeA, nodeB);
		if (!redstonePowered) {
			live = null;
			return;
		}
		double phaseRad = Math.toRadians(phaseDegrees);
		Waveform waveform = switch (KINDS[kindIndex]) {
			case SINE -> Waveform.sine(amplitudeVolts, frequencyHz, phaseRad, 0);
			case SQUARE -> Waveform.square(amplitudeVolts, frequencyHz, phaseRad, 0);
			case TRIANGLE -> Waveform.triangle(amplitudeVolts, frequencyHz, phaseRad, 0);
		};
		live = new VoltageSource(nodeA, nodeB, waveform);
		circuit.add(live);
	}

	@Override
	public void addToAcCircuit(AcCircuit circuit, int nodeA, int nodeB) {
		// Same "voltage sources go to zero" convention as PowerSupplyBlockEntity.
		circuit.add(AcVoltageSource.zero(nodeA, nodeB));
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		String base = String.format("Function Generator: %s %.1fV %.2fHz %.0fdeg",
				KINDS[kindIndex].toString().toLowerCase(), amplitudeVolts, frequencyHz, phaseDegrees);
		return redstonePowered ? base : base + " (off - needs redstone)";
	}
}
