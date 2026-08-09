package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import com.rpicos.circuitsimcraft.sim.Circuit;
import com.rpicos.circuitsimcraft.sim.Resistor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Three ordinary {@link Resistor}s in parallel, one per phase, sharing one editable resistance -
 *  a balanced 3-phase resistive load/bank. */
public class ThreePhaseResistorBlockEntity extends ThreePhaseComponentBlockEntity implements ValueEditable {

	private static final double[] PRESETS_OHMS = {10, 100, 1_000, 10_000};

	private double resistanceOhms = PRESETS_OHMS[1];
	private final Resistor[] live = new Resistor[3];

	public ThreePhaseResistorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.THREE_PHASE_RESISTOR, pos, state);
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Resistance", "ohm",
				PRESETS_OHMS[0], PRESETS_OHMS[PRESETS_OHMS.length - 1], resistanceOhms));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		resistanceOhms = Math.clamp(values.get(0), PRESETS_OHMS[0], PRESETS_OHMS[PRESETS_OHMS.length - 1]);
	}

	@Override
	public void addToCircuit(Circuit circuit, int[] nodesA, int[] nodesB) {
		for (int phase = 0; phase < 3; phase++) {
			live[phase] = new Resistor(nodesA[phase], nodesB[phase], resistanceOhms);
			circuit.add(live[phase]);
		}
		bindNodes(circuit, nodesA, nodesB);
	}

	@Override
	public double[] probeBundleCurrent() {
		double[] v = probeBundleVoltage();
		double[] i = new double[3];
		for (int phase = 0; phase < 3; phase++) {
			i[phase] = live[phase] == null ? 0 : v[phase] / resistanceOhms;
		}
		return i;
	}

	@Override
	public String probeSummary() {
		return "3-Phase Resistor " + resistanceOhms + " ohm (x3)";
	}
}
