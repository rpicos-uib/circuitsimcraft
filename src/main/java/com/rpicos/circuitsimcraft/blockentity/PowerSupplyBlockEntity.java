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

public class PowerSupplyBlockEntity extends ComponentBlockEntity implements ValueEditable, AcStampable {

	private static final double[] PRESETS_VOLTS = {1.5, 5, 9, 12, 24};

	private int presetIndex = 1;
	private double voltageVolts = PRESETS_VOLTS[presetIndex];
	private VoltageSource live;
	private boolean redstonePowered = false;

	public PowerSupplyBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.POWER_SUPPLY, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS_VOLTS.length;
		voltageVolts = PRESETS_VOLTS[presetIndex];
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Voltage", "V",
				PRESETS_VOLTS[0], PRESETS_VOLTS[PRESETS_VOLTS.length - 1], voltageVolts));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		voltageVolts = Math.clamp(values.get(0), PRESETS_VOLTS[0], PRESETS_VOLTS[PRESETS_VOLTS.length - 1]);
	}

	/** Called by {@link com.rpicos.circuitsimcraft.block.PowerSupplyBlock#neighborChanged} whenever
	 *  a redstone neighbor changes. Only rebuilds the circuit if the powered state actually flips,
	 *  so idle redstone dust nearby doesn't force a rebuild every tick. */
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
			// Left un-stamped: an inactive supply behaves as an open circuit rather than a 0V
			// source, so a still-being-wired power supply can't short itself into a singular
			// matrix before the builder is ready to switch it on.
			live = null;
			return;
		}
		live = new VoltageSource(nodeA, nodeB, Waveform.dc(voltageVolts));
		circuit.add(live);
	}

	@Override
	public void addToAcCircuit(AcCircuit circuit, int nodeA, int nodeB) {
		// The usual small-signal convention: every independent voltage source other than the one
		// designated AC source is stamped at exactly 0V (a short) during an AC sweep, regardless
		// of its own DC value or (deliberately, as a simplification) its redstone on/off state.
		circuit.add(AcVoltageSource.zero(nodeA, nodeB));
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		String base = "Power Supply " + voltageVolts + " V DC";
		return redstonePowered ? base : base + " (off - needs redstone)";
	}
}
