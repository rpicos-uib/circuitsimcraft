package com.rpicos.circuitsimcraft.blockentity;

import java.util.List;

/** The bundle equivalent of {@link Probeable}: three independent values (phase A/B/C) instead of
 *  one, for anything a future dedicated 3-phase probe/HUD can pin. Deliberately separate from
 *  {@link Probeable} rather than forcing a bundle through its single-scalar shape (which would
 *  mean picking one arbitrary phase or an average, losing exactly the "120 degrees apart"
 *  information a 3-phase HUD exists to show) - the same reasoning already applied to the AC/Bode
 *  family (`AcProbeItem`/`AcProbeManager`/... are a fully separate pipeline from the mono
 *  `Probe*` classes, not a shared interface). */
public interface ThreePhaseProbeable {
	/** Phase 0/1/2 (A/B/C) voltage. */
	double[] probeBundleVoltage();

	/** Phase 0/1/2 (A/B/C) current. */
	double[] probeBundleCurrent();

	String probeSummary();

	/** Phase 0/1/2 (A/B/C) history, oldest-to-newest each. */
	List<List<Float>> historySnapshot();

	/** Called once per tick (from the network manager, after the circuit solve) to append a scope sample. */
	void recordSample();
}
