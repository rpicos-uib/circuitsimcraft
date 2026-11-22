package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import com.rpicos.circuitsimcraft.sim.AcCircuit;
import com.rpicos.circuitsimcraft.sim.Circuit;
import com.rpicos.circuitsimcraft.sim.IdealTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/** A single-phase ideal transformer with an editable turns ratio N:M (see {@link
 *  IdealTransformer} for the model). Four electrical terminals, so like the op-amp it extends
 *  {@link NetworkBlockEntity} directly rather than the two-lead {@link ComponentBlockEntity}.
 *
 *  <p>FACING is always horizontal (see {@code TransformerBlock}) and is the front face, the one
 *  that shows the transformer symbol. Seen from the front, the primary (input) is the left (+)
 *  and right (-) faces, and the secondary (output) is the top (+) and bottom (-) faces. The back
 *  face is not a terminal. */
public class TransformerBlockEntity extends NetworkBlockEntity implements Probeable, ValueEditable {

	private static final double TURNS_MIN = 1, TURNS_MAX = 1000;
	/** Empty-hand presets, as {N, M} pairs: 1:1, step-up 1:2 and 1:10, step-down 2:1 and 10:1. */
	private static final double[][] PRESETS = {{1, 1}, {1, 2}, {2, 1}, {1, 10}, {10, 1}};

	private final ProbeHistory history = new ProbeHistory();
	private int presetIndex = 0;
	private double primaryTurns = PRESETS[0][0];
	private double secondaryTurns = PRESETS[0][1];
	private Circuit circuit;
	private int nodeInPlus = -1, nodeInMinus = -1, nodeOutPlus = -1, nodeOutMinus = -1;
	private IdealTransformer live;

	public TransformerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.TRANSFORMER, pos, state);
	}

	public Direction getFacing() {
		return getBlockState().getValue(BlockStateProperties.FACING);
	}

	/** The left face, seen by a player looking at the front face. */
	public Direction inPlusFace() {
		return horizontalFacing().getClockWise();
	}

	/** The right face, seen by a player looking at the front face. */
	public Direction inMinusFace() {
		return horizontalFacing().getCounterClockWise();
	}

	public Direction outPlusFace() {
		return Direction.UP;
	}

	public Direction outMinusFace() {
		return Direction.DOWN;
	}

	/** FACING is only ever horizontal for this block; guard anyway so a vertical state (e.g. from
	 *  a command) can't make getClockWise() throw. */
	private Direction horizontalFacing() {
		Direction facing = getFacing();
		return facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return direction == inPlusFace() || direction == inMinusFace()
				|| direction == outPlusFace() || direction == outMinusFace();
	}

	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS.length;
		primaryTurns = PRESETS[presetIndex][0];
		secondaryTurns = PRESETS[presetIndex][1];
	}

	private double ratio() {
		return primaryTurns / secondaryTurns;
	}

	public void addToCircuit(Circuit circuit, int nodeInPlus, int nodeInMinus, int nodeOutPlus, int nodeOutMinus) {
		this.circuit = circuit;
		this.nodeInPlus = nodeInPlus;
		this.nodeInMinus = nodeInMinus;
		this.nodeOutPlus = nodeOutPlus;
		this.nodeOutMinus = nodeOutMinus;
		live = new IdealTransformer(nodeInPlus, nodeInMinus, nodeOutPlus, nodeOutMinus, ratio());
		circuit.add(live);
	}

	public void addToAcCircuit(AcCircuit circuit, int nodeInPlus, int nodeInMinus, int nodeOutPlus, int nodeOutMinus) {
		circuit.add(new IdealTransformer(nodeInPlus, nodeInMinus, nodeOutPlus, nodeOutMinus, ratio()));
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(
				new EditableField("field.circuitsimcraft.primary_turns", "", TURNS_MIN, TURNS_MAX, primaryTurns),
				new EditableField("field.circuitsimcraft.secondary_turns", "", TURNS_MIN, TURNS_MAX, secondaryTurns));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		primaryTurns = Math.clamp(values.get(0), TURNS_MIN, TURNS_MAX);
		secondaryTurns = Math.clamp(values.get(1), TURNS_MIN, TURNS_MAX);
	}

	private double inputVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeInPlus) - circuit.getVoltage(nodeInMinus);
	}

	/** The secondary (output) voltage, top minus bottom. */
	@Override
	public double probeVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeOutPlus) - circuit.getVoltage(nodeOutMinus);
	}

	/** The secondary (output) current, delivered from the top terminal into the load. */
	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.secondaryCurrent();
	}

	@Override
	public Component probeSummary() {
		return CircuitText.probe("transformer", turns(primaryTurns), turns(secondaryTurns),
				"%.3f".formatted(inputVoltage()), "%.3f".formatted(probeVoltage()));
	}

	/** Whole turn counts without a trailing ".0"; anything typed with decimals keeps them. */
	private static String turns(double value) {
		return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
	}

	@Override
	public void recordSample() {
		history.record(probeVoltage());
	}

	@Override
	public List<Float> historySnapshot() {
		return history.snapshot();
	}
}
