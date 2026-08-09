package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import com.rpicos.circuitsimcraft.sim.Capacitor;
import com.rpicos.circuitsimcraft.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Three ordinary {@link Capacitor}s in parallel, one per phase, sharing one editable capacitance. */
public class ThreePhaseCapacitorBlockEntity extends ThreePhaseComponentBlockEntity implements ValueEditable {

	private static final double[] PRESETS_FARADS = {1e-6, 10e-6, 100e-6, 1_000e-6};
	private static final double MIN_MICROFARADS = PRESETS_FARADS[0] * 1e6;
	private static final double MAX_MICROFARADS = PRESETS_FARADS[PRESETS_FARADS.length - 1] * 1e6;

	private double capacitanceFarads = PRESETS_FARADS[1];
	private final Capacitor[] live = new Capacitor[3];

	public ThreePhaseCapacitorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.THREE_PHASE_CAPACITOR, pos, state);
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Capacitance", "uF",
				MIN_MICROFARADS, MAX_MICROFARADS, capacitanceFarads * 1e6));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		double microfarads = Math.clamp(values.get(0), MIN_MICROFARADS, MAX_MICROFARADS);
		capacitanceFarads = microfarads / 1e6;
	}

	@Override
	public void addToCircuit(Circuit circuit, int[] nodesA, int[] nodesB) {
		for (int phase = 0; phase < 3; phase++) {
			live[phase] = new Capacitor(nodesA[phase], nodesB[phase], capacitanceFarads);
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
	public String probeSummary() {
		return "3-Phase Capacitor " + (capacitanceFarads * 1e6) + " uF (x3)";
	}
}
