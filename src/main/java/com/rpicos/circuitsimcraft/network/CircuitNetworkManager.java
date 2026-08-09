package com.rpicos.circuitsimcraft.network;

import com.rpicos.circuitsimcraft.CircuitSimCraft;
import com.rpicos.circuitsimcraft.blockentity.AcSourceBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.AcStampable;
import com.rpicos.circuitsimcraft.blockentity.AmmeterBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.CccsBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.CcvsBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ComponentBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.GroundBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.NetworkBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.NmosBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.NpnBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.OpAmpBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.Probeable;
import com.rpicos.circuitsimcraft.blockentity.SingleNodeBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseComponentBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseProbeable;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseSourceBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseWireBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.V2RConverterBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.VccsBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.VcvsBlockEntity;
import com.rpicos.circuitsimcraft.sim.AcCircuit;
import com.rpicos.circuitsimcraft.sim.Circuit;
import com.rpicos.circuitsimcraft.sim.Complex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * One instance per loaded ServerLevel. Tracks every placed wire/component position and, once per
 * tick, rebuilds the circuit topology (only when something changed) and steps the simulation.
 *
 * <p>Topology is a union-find over "conductive" adjacency: two neighbouring positions merge into
 * the same electrical node if each presents a conductive face toward the other. A
 * {@link SingleNodeBlockEntity} (wire, ground) has a single graph identity per block - its whole
 * body is one electrical point, conductive on all six faces. Anything else (a two-terminal
 * component, or a three-terminal op-amp) instead gets one separate graph identity per lead,
 * keyed by {@code (pos, side)} rather than by its bare position: keying every lead by the same
 * block position would let the block's own body union-find its way into a single node,
 * short-circuiting the very element it's supposed to be wired across. This generalizes to any
 * number of terminals for free - {@link #keyFor} only needs to know whether an entity is a
 * single shared point or not, since the specific conductive {@code direction} that triggered a
 * given union already identifies exactly which lead is involved.
 *
 * <p>The same topology (see {@link #computeNodeAssignment}) is reused, unchanged, by the AC
 * (Bode-plot) sweep in {@link #computeAcSweep}: that method just builds a separate,
 * complex-valued {@link AcCircuit} on top of the identical node numbering rather than the
 * transient {@link Circuit} the regular per-tick simulation uses.
 *
 * <p>A second, structurally distinct union-find graph - the "bundle" graph, for three-phase
 * wiring - lives in the same {@code parent} map alongside the graph described above, gated
 * behind {@link BundleParticipant} instead of {@link NetworkParticipant}. {@link PhaseNodeKey}/
 * {@link BundleBodyKey} can never {@code .equals()} a {@link NodeKey} or a bare {@link BlockPos}
 * (different record types), so the two graphs coexist with no risk of cross-talk despite sharing
 * one union-find structure; an ordinary wire/component face is never bundle-conductive and a
 * bundle face is never ordinary-conductive, so the two graphs never merge just by two blocks
 * touching - only a Bundler/Unbundler (not yet implemented) deliberately bridges them, via an
 * explicit same-position union rather than an adjacency one.
 */
public class CircuitNetworkManager {
	private static final Map<ServerLevel, CircuitNetworkManager> INSTANCES = new WeakHashMap<>();
	private static final double TICK_SECONDS = 1.0 / 20.0;
	private static final int AC_SWEEP_POINTS = 60;

	public static CircuitNetworkManager forLevel(ServerLevel level) {
		return INSTANCES.computeIfAbsent(level, l -> new CircuitNetworkManager());
	}

	private final Map<BlockPos, NetworkBlockEntity> participants = new HashMap<>();
	private boolean dirty = true;
	private Circuit circuit;
	// Set when the last step() threw (e.g. a voltage source shorted across itself); stepping is
	// skipped until the topology changes again, so one bad wiring doesn't spam the log every tick
	// or take down the server thread.
	private boolean faulted;
	// Per-component (nodeA, nodeB) from the most recent rebuild(), kept only so a solver fault can
	// be logged with enough detail (which block, which facing, which nodes) to diagnose from the
	// server log alone - no need to reconstruct the wiring from a screenshot.
	private final Map<BlockPos, int[]> lastComponentNodes = new HashMap<>();

	public void register(BlockPos pos, NetworkBlockEntity entity) {
		participants.put(pos.immutable(), entity);
		dirty = true;
	}

	public void unregister(BlockPos pos) {
		if (participants.remove(pos) != null) {
			dirty = true;
		}
	}

	public void markDirty() {
		dirty = true;
	}

	public void tick(ServerLevel level) {
		// Self-heals entries left behind when a chunk unloads without setRemoved() firing.
		participants.keySet().removeIf(pos -> !level.isLoaded(pos));

		if (dirty) {
			rebuild();
			faulted = false;
		}
		if (circuit != null && !faulted) {
			try {
				circuit.step(TICK_SECONDS);
			} catch (ArithmeticException e) {
				faulted = true;
				CircuitSimCraft.LOGGER.warn(
						"Circuit network paused after solver error (will resume once its wiring changes): {}",
						e.getMessage());
				logComponentNodesForDiagnosis();
				return;
			}
			for (NetworkBlockEntity entity : participants.values()) {
				if (entity instanceof Probeable probeable) {
					probeable.recordSample();
				}
				if (entity instanceof ThreePhaseProbeable threePhaseProbeable) {
					threePhaseProbeable.recordSample();
				}
				if (entity instanceof V2RConverterBlockEntity v2r) {
					v2r.refreshRedstoneOutput();
				}
			}
		}
	}

	private void logComponentNodesForDiagnosis() {
		for (Map.Entry<BlockPos, int[]> entry : lastComponentNodes.entrySet()) {
			BlockPos pos = entry.getKey();
			int[] nodes = entry.getValue();
			NetworkBlockEntity entity = participants.get(pos);
			String facing = entity instanceof ComponentBlockEntity component
					? component.getFacing().toString()
					: "?";
			CircuitSimCraft.LOGGER.warn("  {} at {} facing={}: node A={}, node B={}{}",
					entity == null ? "?" : entity.getClass().getSimpleName(), pos, facing,
					nodes[0], nodes[1], nodes[0] == nodes[1] ? "  <-- both terminals on the same node (shorted)" : "");
		}
	}

	/** A wire's graph identity is just its position (one electrical point for the whole block).
	 *  A component's graph identity is one of these per lead, so its two terminals can never be
	 *  merged into each other via the component's own body. */
	private record NodeKey(BlockPos pos, Direction side) {
	}

	/** Per-lead bundle key - a component/ammeter/source's bundled face, one per phase (0=A,
	 *  1=B, 2=C). The bundle analog of {@link NodeKey}. */
	private record PhaseNodeKey(BlockPos pos, Direction side, int phase) {
	}

	/** Whole-body bundle key - a {@link ThreePhaseWireBlockEntity}'s single electrical identity
	 *  per phase, conductive on all six faces. The bundle analog of a bare {@link BlockPos}. */
	private record BundleBodyKey(BlockPos pos, int phase) {
	}

	/** The union-find's resolved node-id assignment, decoupled from any particular {@link Circuit}
	 *  or {@link AcCircuit} instance so both solvers can share exactly the same topology and node
	 *  numbering rather than each recomputing (and potentially disagreeing on) it independently. */
	private record NodeAssignment(Map<Object, Integer> nodeIdByKey, int nodeCount) {
	}

	private static Object keyFor(BlockPos pos, NetworkBlockEntity entity, Direction side) {
		return entity instanceof SingleNodeBlockEntity ? pos : new NodeKey(pos, side);
	}

	private static Object bundleKeyFor(BlockPos pos, BundleParticipant entity, Direction side, int phase) {
		return entity instanceof ThreePhaseWireBlockEntity ? new BundleBodyKey(pos, phase) : new PhaseNodeKey(pos, side, phase);
	}

	private NodeAssignment computeNodeAssignment() {
		Map<Object, Object> parent = new HashMap<>();
		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			NetworkBlockEntity entity = entry.getValue();
			for (Direction direction : Direction.values()) {
				if (!entity.isConductiveTowards(direction)) continue;
				Object key = keyFor(pos, entity, direction);
				parent.putIfAbsent(key, key);
			}
		}

		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			NetworkBlockEntity entity = entry.getValue();
			for (Direction direction : Direction.values()) {
				if (!entity.isConductiveTowards(direction)) continue;
				BlockPos neighborPos = pos.relative(direction);
				NetworkBlockEntity neighbor = participants.get(neighborPos);
				if (neighbor != null && neighbor.isConductiveTowards(direction.getOpposite())) {
					Object myKey = keyFor(pos, entity, direction);
					Object neighborKey = keyFor(neighborPos, neighbor, direction.getOpposite());
					union(parent, myKey, neighborKey);
				}
			}
		}

		// Bundle graph (three-phase wiring) - same shape as the two loops above, gated behind
		// BundleParticipant instead of NetworkParticipant, looped once per phase. See this class's
		// own doc comment for why this can safely share the same `parent` map.
		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			if (!(entry.getValue() instanceof BundleParticipant bundle)) continue;
			for (Direction direction : Direction.values()) {
				if (!bundle.isBundleConductiveTowards(direction)) continue;
				for (int phase = 0; phase < 3; phase++) {
					Object key = bundleKeyFor(pos, bundle, direction, phase);
					parent.putIfAbsent(key, key);
				}
			}
		}
		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			if (!(entry.getValue() instanceof BundleParticipant bundle)) continue;
			for (Direction direction : Direction.values()) {
				if (!bundle.isBundleConductiveTowards(direction)) continue;
				BlockPos neighborPos = pos.relative(direction);
				NetworkBlockEntity neighbor = participants.get(neighborPos);
				if (neighbor instanceof BundleParticipant neighborBundle
						&& neighborBundle.isBundleConductiveTowards(direction.getOpposite())) {
					for (int phase = 0; phase < 3; phase++) {
						Object myKey = bundleKeyFor(pos, bundle, direction, phase);
						Object neighborKey = bundleKeyFor(neighborPos, neighborBundle, direction.getOpposite(), phase);
						union(parent, myKey, neighborKey);
					}
				}
			}
		}

		// Bundler/Unbundler: the one place a union happens at the SAME position rather than across
		// an adjacency - phase 0/1/2's mono lead is deliberately aliased directly onto the bundle's
		// corresponding phase sub-node. Both keys are guaranteed already present in `parent` by
		// this point (the mono and bundle init loops above already added them, since a
		// BundleBridge is simultaneously a real NetworkParticipant on its mono faces and a real
		// BundleParticipant on its bundle face).
		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			if (!(entry.getValue() instanceof BundleBridge bridge)) continue;
			BundleParticipant bundle = (BundleParticipant) bridge;
			for (int phase = 0; phase < 3; phase++) {
				Object monoKey = keyFor(pos, entry.getValue(), bridge.monoFace(phase));
				Object bundleKey = bundleKeyFor(pos, bundle, bridge.bundleFace(), phase);
				union(parent, monoKey, bundleKey);
			}
		}

		Map<Object, Integer> nodeIdByRoot = new HashMap<>();
		// Any network a Ground block touches gets anchored to node 0 (always exactly 0V) instead
		// of an arbitrary freshly-allocated node - see rebuild()'s original comment for why.
		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			if (entry.getValue() instanceof GroundBlockEntity) {
				nodeIdByRoot.put(find(parent, entry.getKey()), 0);
			}
		}

		Map<Object, Integer> nodeIdByKey = new HashMap<>();
		int[] nextId = {1}; // node 0 = ground
		for (Object key : parent.keySet()) {
			Object root = find(parent, key);
			int nodeId = nodeIdByRoot.computeIfAbsent(root, r -> nextId[0]++);
			nodeIdByKey.put(key, nodeId);
		}

		return new NodeAssignment(nodeIdByKey, nextId[0]);
	}

	private void rebuild() {
		dirty = false;
		circuit = new Circuit();
		lastComponentNodes.clear();

		NodeAssignment assignment = computeNodeAssignment();
		for (int i = 1; i < assignment.nodeCount(); i++) {
			circuit.addNode();
		}

		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			NetworkBlockEntity entity = entry.getValue();
			if (entity instanceof ComponentBlockEntity component) {
				int nodeA = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing()));
				int nodeB = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing().getOpposite()));
				component.addToCircuit(circuit, nodeA, nodeB);
				lastComponentNodes.put(pos, new int[] {nodeA, nodeB});
			} else if (entity instanceof OpAmpBlockEntity opAmp) {
				int nodeOut = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.outputFace()));
				int nodeMinus = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.minusFace()));
				int nodePlus = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.plusFace()));
				opAmp.addToCircuit(circuit, nodeOut, nodeMinus, nodePlus);
			} else if (entity instanceof NpnBlockEntity bjt) {
				// Also matches PnpBlockEntity, which extends this class - see its own doc comment.
				int nodeCollector = assignment.nodeIdByKey().get(new NodeKey(pos, bjt.collectorFace()));
				int nodeEmitter = assignment.nodeIdByKey().get(new NodeKey(pos, bjt.emitterFace()));
				int nodeBase = assignment.nodeIdByKey().get(new NodeKey(pos, bjt.baseFace()));
				bjt.addToCircuit(circuit, nodeBase, nodeCollector, nodeEmitter);
			} else if (entity instanceof NmosBlockEntity mosfet) {
				// Also matches PmosBlockEntity.
				int nodeDrain = assignment.nodeIdByKey().get(new NodeKey(pos, mosfet.drainFace()));
				int nodeSource = assignment.nodeIdByKey().get(new NodeKey(pos, mosfet.sourceFace()));
				int nodeGate = assignment.nodeIdByKey().get(new NodeKey(pos, mosfet.gateFace()));
				mosfet.addToCircuit(circuit, nodeGate, nodeDrain, nodeSource);
			} else if (entity instanceof VcvsBlockEntity vcvs) {
				// Fixed absolute faces, not resolved via getFacing() - GroundedComponentBlock
				// pins FACING to UP permanently, so north/up are always these literal directions.
				int nodeControl = assignment.nodeIdByKey().get(new NodeKey(pos, Direction.NORTH));
				int nodeOutput = assignment.nodeIdByKey().get(new NodeKey(pos, Direction.UP));
				vcvs.addToCircuit(circuit, nodeControl, nodeOutput);
			} else if (entity instanceof VccsBlockEntity vccs) {
				int nodeControl = assignment.nodeIdByKey().get(new NodeKey(pos, Direction.NORTH));
				int nodeOutput = assignment.nodeIdByKey().get(new NodeKey(pos, Direction.UP));
				vccs.addToCircuit(circuit, nodeControl, nodeOutput);
			} else if (entity instanceof CccsBlockEntity cccs) {
				int nodeControlIn = assignment.nodeIdByKey().get(new NodeKey(pos, Direction.NORTH));
				int nodeControlOut = assignment.nodeIdByKey().get(new NodeKey(pos, Direction.SOUTH));
				int nodeOutput = assignment.nodeIdByKey().get(new NodeKey(pos, Direction.UP));
				cccs.addToCircuit(circuit, nodeControlIn, nodeControlOut, nodeOutput);
			} else if (entity instanceof CcvsBlockEntity ccvs) {
				int nodeControlIn = assignment.nodeIdByKey().get(new NodeKey(pos, Direction.NORTH));
				int nodeControlOut = assignment.nodeIdByKey().get(new NodeKey(pos, Direction.SOUTH));
				int nodeOutput = assignment.nodeIdByKey().get(new NodeKey(pos, Direction.UP));
				ccvs.addToCircuit(circuit, nodeControlIn, nodeControlOut, nodeOutput);
			} else if (entity instanceof ThreePhaseSourceBlockEntity source) {
				int nodeNeutral = assignment.nodeIdByKey().get(new NodeKey(pos, source.getFacing().getOpposite()));
				int[] nodesPhase = new int[3];
				for (int phase = 0; phase < 3; phase++) {
					nodesPhase[phase] = assignment.nodeIdByKey().get(new PhaseNodeKey(pos, source.getFacing(), phase));
				}
				source.addToCircuit(circuit, nodeNeutral, nodesPhase);
			} else if (entity instanceof ThreePhaseComponentBlockEntity threePhaseComponent) {
				// Covers the 3-phase Ammeter/Resistor/Inductor/Capacitor uniformly - all four share
				// this base's "two bundled leads along FACING" shape, the bundle analog of how the
				// generic ComponentBlockEntity branch above covers every mono 2-lead part at once.
				int[] nodesA = new int[3];
				int[] nodesB = new int[3];
				for (int phase = 0; phase < 3; phase++) {
					nodesA[phase] = assignment.nodeIdByKey().get(new PhaseNodeKey(pos, threePhaseComponent.getFacing(), phase));
					nodesB[phase] = assignment.nodeIdByKey().get(new PhaseNodeKey(pos, threePhaseComponent.getFacing().getOpposite(), phase));
				}
				threePhaseComponent.addToCircuit(circuit, nodesA, nodesB);
			} else if (entity instanceof ThreePhaseWireBlockEntity wire) {
				int[] nodeIds = new int[3];
				for (int phase = 0; phase < 3; phase++) {
					nodeIds[phase] = assignment.nodeIdByKey().get(new BundleBodyKey(pos, phase));
				}
				wire.bindBundleNode(circuit, nodeIds);
			} else if (entity instanceof SingleNodeBlockEntity singleNode) {
				singleNode.bindNode(circuit, assignment.nodeIdByKey().get(pos));
			}
		}
	}

	/** Result of one AC (Bode-plot) sweep: log-spaced frequencies across the AC source's
	 *  configured range, each with the signal/source complex ratio's magnitude (dB) and phase
	 *  (degrees). {@code warning} is non-null, and the three lists empty, if the sweep could not
	 *  be performed at all (e.g. the pinned position wasn't actually an AC Source). */
	public record AcSweepResult(List<Double> freqsHz, List<Double> magnitudesDb, List<Double> phasesDeg, String warning) {
		public static AcSweepResult error(String warning) {
			return new AcSweepResult(List.of(), List.of(), List.of(), warning);
		}
	}

	public AcSweepResult computeAcSweep(BlockPos sourcePos, BlockPos signalPos) {
		if (!(participants.get(sourcePos) instanceof AcSourceBlockEntity acSource)) {
			return AcSweepResult.error("Pinned position is not an AC Source block.");
		}
		if (!acSource.isRedstonePowered()) {
			return AcSweepResult.error("AC Source has no redstone power.");
		}
		NetworkBlockEntity signalEntity = participants.get(signalPos);
		if (signalEntity == null) {
			return AcSweepResult.error("No wired component at the signal position.");
		}

		NodeAssignment assignment = computeNodeAssignment();
		double logMin = Math.log10(acSource.minFrequencyHz());
		double logMax = Math.log10(acSource.maxFrequencyHz());
		Complex sourceAmplitude = Complex.real(acSource.amplitudeVolts());

		List<Double> freqs = new ArrayList<>();
		List<Double> mags = new ArrayList<>();
		List<Double> phases = new ArrayList<>();

		for (int i = 0; i < AC_SWEEP_POINTS; i++) {
			double t = (double) i / (AC_SWEEP_POINTS - 1);
			double freqHz = Math.pow(10, logMin + t * (logMax - logMin));
			double omega = 2 * Math.PI * freqHz;

			AcCircuit acCircuit = new AcCircuit();
			for (int n = 1; n < assignment.nodeCount(); n++) {
				acCircuit.addNode();
			}

			for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
				BlockPos pos = entry.getKey();
				NetworkBlockEntity entity = entry.getValue();
				if (entity instanceof OpAmpBlockEntity opAmp) {
					int nodeOut = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.outputFace()));
					int nodeMinus = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.minusFace()));
					int nodePlus = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.plusFace()));
					opAmp.addToAcCircuit(acCircuit, nodeOut, nodeMinus, nodePlus);
				} else if (entity instanceof ComponentBlockEntity component && entity instanceof AcStampable stampable) {
					int nodeA = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing()));
					int nodeB = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing().getOpposite()));
					stampable.addToAcCircuit(acCircuit, nodeA, nodeB);
				}
				// A plain SingleNodeBlockEntity (wire/ground) contributes no element - it's just a
				// node, exactly as in the transient circuit.
			}

			try {
				acCircuit.solve(omega);
			} catch (ArithmeticException e) {
				return AcSweepResult.error("AC solver error: " + e.getMessage());
			}

			Complex signalVoltage = readAcVoltage(signalPos, signalEntity, assignment, acCircuit);
			Complex h = signalVoltage.divide(sourceAmplitude);

			freqs.add(freqHz);
			mags.add(20 * Math.log10(Math.max(h.magnitude(), 1e-12)));
			phases.add(Math.toDegrees(h.angle()));
		}

		return new AcSweepResult(freqs, mags, phases, null);
	}

	/** Returns whatever quantity an AC-probed signal point should be judged against the source
	 *  amplitude: normally that's the small-signal voltage across the pinned element, but an ideal
	 *  ammeter is electrically a wire, so the voltage across it is identically zero at every
	 *  frequency by construction - probing one for voltage would always show a flat, meaningless
	 *  trace. Its branch current (see {@link AmmeterBlockEntity#acCurrent}) is used instead, which
	 *  makes the resulting ratio a transadmittance rather than a unitless gain, exactly as pinning
	 *  the regular (transient) probe on an ammeter already reads current instead of voltage. */
	private static Complex readAcVoltage(BlockPos pos, NetworkBlockEntity entity, NodeAssignment assignment, AcCircuit acCircuit) {
		if (entity instanceof AmmeterBlockEntity ammeter) {
			return ammeter.acCurrent();
		}
		if (entity instanceof OpAmpBlockEntity opAmp) {
			int nodeOut = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.outputFace()));
			return acCircuit.getVoltage(nodeOut);
		}
		if (entity instanceof ComponentBlockEntity component) {
			int nodeA = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing()));
			int nodeB = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing().getOpposite()));
			return acCircuit.getVoltage(nodeA).subtract(acCircuit.getVoltage(nodeB));
		}
		if (entity instanceof SingleNodeBlockEntity) {
			return acCircuit.getVoltage(assignment.nodeIdByKey().get(pos));
		}
		return Complex.ZERO;
	}

	private static Object find(Map<Object, Object> parent, Object key) {
		Object root = key;
		while (!parent.get(root).equals(root)) {
			root = parent.get(root);
		}
		Object cur = key;
		while (!cur.equals(root)) {
			Object next = parent.get(cur);
			parent.put(cur, root);
			cur = next;
		}
		return root;
	}

	private static void union(Map<Object, Object> parent, Object a, Object b) {
		Object rootA = find(parent, a);
		Object rootB = find(parent, b);
		if (!rootA.equals(rootB)) {
			parent.put(rootA, rootB);
		}
	}
}
