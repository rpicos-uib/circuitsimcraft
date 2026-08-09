package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import com.rpicos.circuitsimcraft.sim.Circuit;
import com.rpicos.circuitsimcraft.sim.Inductor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Three ordinary {@link Inductor}s in parallel, one per phase, sharing one editable inductance. */
public class ThreePhaseInductorBlockEntity extends ThreePhaseComponentBlockEntity implements ValueEditable {

	// Same tick-timescale-tuned values as the mono InductorBlockEntity, not real-world ratings.
	private static final double[] PRESETS_HENRIES = {0.01, 0.1, 1.0, 5.0};

	private double inductanceHenries = PRESETS_HENRIES[1];
	private final Inductor[] live = new Inductor[3];

	public ThreePhaseInductorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.THREE_PHASE_INDUCTOR, pos, state);
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Inductance", "H",
				PRESETS_HENRIES[0], PRESETS_HENRIES[PRESETS_HENRIES.length - 1], inductanceHenries));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		inductanceHenries = Math.clamp(values.get(0), PRESETS_HENRIES[0], PRESETS_HENRIES[PRESETS_HENRIES.length - 1]);
	}

	@Override
	public void addToCircuit(Circuit circuit, int[] nodesA, int[] nodesB) {
		for (int phase = 0; phase < 3; phase++) {
			live[phase] = new Inductor(nodesA[phase], nodesB[phase], inductanceHenries);
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
		return "3-Phase Inductor " + inductanceHenries + " H (x3)";
	}
}
